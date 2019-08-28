package com.ssword.imserver.server.helper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.SpringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImConfig;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.cache.redis.RedisCache;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.listener.ImBindListener;
import org.jim.common.message.AbstractMessageHelper;
import org.jim.common.packets.*;
import org.jim.common.utils.ChatKit;
import org.jim.common.utils.JsonKit;
import org.jim.server.helper.redis.RedisImBindListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 本类主要都是复制的RedisMessageHelper，主要是为了在依赖redis的同时，使部分接口依赖mysql
 */
public class ImRedisMessageHelper extends AbstractMessageHelper {
    private RedisCache groupCache = null;// 群组信息
    private RedisCache pushCache = null; // 离线消息
    private RedisCache storeCache = null;// 聊天记录
    private RedisCache userCache = null; // 用户信息

    public static final String SUBFIX = ":";
    public static final Integer OUTTIME = 86400;// 缓存最大时间 24小时
    private Logger log = LoggerFactory.getLogger(ImRedisMessageHelper.class);
    private UserInfoService userInfoService = SpringUtil.getBean(UserInfoService.class);

    static {
        RedisCacheManager.register(USER, OUTTIME, OUTTIME);
        RedisCacheManager.register(GROUP, OUTTIME, OUTTIME);
        RedisCacheManager.register(STORE, OUTTIME, OUTTIME);
        RedisCacheManager.register(PUSH, OUTTIME, OUTTIME);
    }

    public ImRedisMessageHelper() {
        this(null);
    }

    public ImRedisMessageHelper(ImConfig imConfig) {
        this.groupCache = RedisCacheManager.getCache(GROUP);
        this.pushCache = RedisCacheManager.getCache(PUSH);
        this.storeCache = RedisCacheManager.getCache(STORE);
        this.userCache = RedisCacheManager.getCache(USER);
        this.imConfig = imConfig;
    }

    @Override
    public ImBindListener getBindListener() {
        return new RedisImBindListener(imConfig);
    }

    @Override
    public boolean isOnline(String userid) {
        try {
            Set<String> keys = JedisTemplate.me().keys(USER + SUBFIX + userid + SUBFIX + TERMINAL);
            if (keys != null && keys.size() > 0) {
                Iterator<String> keyitr = keys.iterator();
                while (keyitr.hasNext()) {
                    String key = keyitr.next();
                    key = key.substring(key.indexOf(userid));
                    String isOnline = userCache.get(key, String.class);
                    if (ONLINE.equals(isOnline)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return false;
    }

    @Override
    public List<String> getGroupUsers(String group_id) {
        List<User> userList = userInfoService.getGroupUserById(group_id);
        List<String> list = Lists.newArrayList();
        for (User user : userList) {
            list.add(user.getId());
        }
        return list;
    }

    /**
     * 这里可能会被调用两次，
     * 第一次是消息发送的时候，
     * 第二次是发送的对象不再线时，如果在线是不会调用的
     *
     * @param timelineTable
     * @param timelineId
     * @param chatBody
     */
    @Override
    public void writeMessage(String timelineTable, String timelineId, ChatBody chatBody) {
        double score = chatBody.getCreateTime();
        if (STORE.equals(timelineTable)) {// 离线消息会重发一次，类型为PUSH，我这边就不重复的记录到数据库了
            userInfoService.saveMsg(chatBody);// 持久化到mysql
        }
        RedisCacheManager.getCache(timelineTable).sortSetPush(timelineId, score, chatBody);
    }

    @Override
    public void addGroupUser(String userid, String group_id) {
        List<String> users = groupCache.listGetAll(group_id);
        if (!users.contains(userid)) {
            groupCache.listPushTail(group_id, userid);
        }
    }

    @Override
    public void removeGroupUser(String userid, String group_id) {
        groupCache.listRemove(group_id, userid);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String userid, String from_userid) {
        String key = USER + SUBFIX + userid + SUBFIX + from_userid;
        List<String> messageList = pushCache.sortSetGetAll(key);
        List<ChatBody> datas = JsonKit.toArray(messageList, ChatBody.class);
        pushCache.remove(key);
        return putFriendsMessage(new UserMessageData(userid), datas);
    }

    @Override
    public UserMessageData getFriendsOfflineMessage(String userid) {
        try {
            Set<String> keys = JedisTemplate.me().keys(PUSH + SUBFIX + USER + SUBFIX + userid);
            UserMessageData messageData = new UserMessageData(userid);
            if (keys != null && keys.size() > 0) {
                List<ChatBody> results = new ArrayList<ChatBody>();
                Iterator<String> keyitr = keys.iterator();
                //获取好友离线消息;
                while (keyitr.hasNext()) {
                    String key = keyitr.next();
                    key = key.substring(key.indexOf(USER + SUBFIX));
                    List<String> messages = pushCache.sortSetGetAll(key);
                    pushCache.remove(key);
                    results.addAll(JsonKit.toArray(messages, ChatBody.class));
                }
                putFriendsMessage(messageData, results);
            }
            List<String> groups = userCache.listGetAll(userid + SUBFIX + GROUP);
            //获取群组离线消息;
            if (groups != null) {
                for (String groupid : groups) {
                    UserMessageData groupMessageData = getGroupOfflineMessage(userid, groupid);
                    if (groupMessageData != null) {
                        putGroupMessage(messageData, groupMessageData.getGroups().get(groupid));
                    }
                }
            }
            return messageData;
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return null;
    }

    @Override
    public UserMessageData getGroupOfflineMessage(String userid, String groupid) {
        String key = GROUP + SUBFIX + groupid + SUBFIX + userid;
        List<String> messages = pushCache.sortSetGetAll(key);
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        UserMessageData messageData = new UserMessageData(userid);
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        pushCache.remove(key);
        return messageData;
    }

    @Override
    public UserMessageData getFriendHistoryMessage(String userid, String from_userid, Double beginTime, Double endTime, Integer offset, Integer count) {
        String sessionId = ChatKit.sessionId(userid, from_userid);
        List<String> messages = null;
        String key = USER + SUBFIX + sessionId;
        boolean isTimeBetween = (beginTime != null && endTime != null);
        boolean isPage = (offset != null && count != null);
        //消息区间，不分页
        if (isTimeBetween && !isPage) {
            messages = storeCache.sortSetGetAll(key, beginTime, endTime);
            //消息区间，并且分页;
        } else if (isTimeBetween && isPage) {
            messages = storeCache.sortSetGetAll(key, beginTime, endTime, offset, count);
            //所有消息，并且分页;
        } else if (!isTimeBetween && isPage) {
            messages = storeCache.sortSetGetAll(key, 0, Double.MAX_VALUE, offset, count);
            //所有消息，不分页;
        } else {
            messages = storeCache.sortSetGetAll(key);
        }
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        UserMessageData messageData = new UserMessageData(userid);
        putFriendsHistoryMessage(messageData, JsonKit.toArray(messages, ChatBody.class), from_userid);
        return messageData;
    }

    @Override
    public UserMessageData getGroupHistoryMessage(String userid, String groupid, Double beginTime, Double endTime, Integer offset, Integer count) {
        String key = GROUP + SUBFIX + groupid;
        List<String> messages = null;
        boolean isTimeBetween = (beginTime != null && endTime != null);
        boolean isPage = (offset != null && count != null);
        //消息区间，不分页
        if (isTimeBetween && !isPage) {
            messages = storeCache.sortSetGetAll(key, beginTime, endTime);
            //消息区间，并且分页;
        } else if (isTimeBetween && isPage) {
            messages = storeCache.sortSetGetAll(key, beginTime, endTime, offset, count);
            //所有消息，并且分页;
        } else if (!isTimeBetween && isPage) {
            messages = storeCache.sortSetGetAll(key, 0, Double.MAX_VALUE, offset, count);
            //所有消息，不分页;
        } else {
            messages = storeCache.sortSetGetAll(key);
        }
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        UserMessageData messageData = new UserMessageData(userid);
        putGroupMessage(messageData, JsonKit.toArray(messages, ChatBody.class));
        return messageData;
    }

    /**
     * 放入用户群组消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putGroupMessage(UserMessageData userMessage, List<ChatBody> messages) {
        if (userMessage == null || messages == null) {
            return null;
        }
        for (ChatBody chatBody : messages) {
            String group = chatBody.getGroup_id();
            if (StringUtils.isEmpty(group)) {
                continue;
            }
            List<ChatBody> groupMessages = userMessage.getGroups().get(group);
            if (groupMessages == null) {
                groupMessages = new ArrayList<ChatBody>();
                userMessage.getGroups().put(group, groupMessages);
            }
            groupMessages.add(chatBody);
        }
        return userMessage;
    }

    /**
     * 放入用户好友消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putFriendsMessage(UserMessageData userMessage, List<ChatBody> messages) {
        if (userMessage == null || messages == null) {
            return null;
        }
        for (ChatBody chatBody : messages) {
            String fromUserId = chatBody.getFrom();
            if (StringUtils.isEmpty(fromUserId)) {
                continue;
            }
            List<ChatBody> friendMessages = userMessage.getFriends().get(fromUserId);
            if (friendMessages == null) {
                friendMessages = new ArrayList<ChatBody>();
                userMessage.getFriends().put(fromUserId, friendMessages);
            }
            friendMessages.add(chatBody);
        }
        return userMessage;
    }

    /**
     * 放入用户好友历史消息;
     *
     * @param userMessage
     * @param messages
     */
    public UserMessageData putFriendsHistoryMessage(UserMessageData userMessage, List<ChatBody> messages, String friendId) {
        if (userMessage == null || messages == null) {
            return null;
        }
        for (ChatBody chatBody : messages) {
            String fromUserId = chatBody.getFrom();
            if (StringUtils.isEmpty(fromUserId)) {
                continue;
            }
            List<ChatBody> friendMessages = userMessage.getFriends().get(friendId);
            if (friendMessages == null) {
                friendMessages = new ArrayList<ChatBody>();
                userMessage.getFriends().put(friendId, friendMessages);
            }
            friendMessages.add(chatBody);
        }
        return userMessage;
    }

    /**
     * 获取群组所有成员信息-----------------改过----------------
     *
     * @param group_id
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public Group getGroupUsers(String group_id, Integer type) {
        type = 2;
        if (group_id == null || type == null) {
            return null;
        }
        Group group = userInfoService.getGroupById(group_id);
        if (group == null) {
            return null;
        }
        List<String> userIds = this.getGroupUsers(group_id);
        if (CollectionUtils.isEmpty(userIds)) {
            return null;
        }
        List<User> users = new ArrayList<User>();
        for (String userId : userIds) {
            User user = getUserByType(userId, type);
            if (user != null) {
                String status = user.getStatus();
                if (type == 0 && ONLINE.equals(status)) {
                    users.add(user);
                } else if (type == 1 && OFFLINE.equals(status)) {
                    users.add(user);
                } else if (type == 2) {
                    users.add(user);
                }
            }
        }
        group.setUsers(users);
        return group;
    }

    /**
     * 根据在线类型获取用户信息;
     *
     * @param userid
     * @param type
     * @return
     */
    @Override
    public User getUserByType(String userid, Integer type) {
        User user = userInfoService.getUserById(userid);
        if (user == null) {
            return null;
        } else {
            userCache.put(userid + SUBFIX + INFO, user);// 对搜索到的数据进行缓存
        }
        boolean isOnline = this.isOnline(userid);
        String status = isOnline ? ONLINE : OFFLINE;
        if (type == 0 || type == 1) {
            if (type == 0 && isOnline) {
                user.setStatus(status);
            } else if (type == 1 && !isOnline) {
                user.setStatus(status);
            }
        } else if (type == 2) {
            user.setStatus(status);
        }
        return user;
    }

    /**
     * 获取好友分组所有成员信息
     *
     * @param user_id
     * @param friend_group_id
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */

    @Override
    public Group getFriendUsers(String user_id, String friend_group_id, Integer type) {
        type = 2;
        if (user_id == null || friend_group_id == null || type == null) {
            return null;
        }
        List<Group> friends = userInfoService.listPeople(user_id);
        if (friends == null || friends.isEmpty()) {
            return null;
        }
        for (Group group : friends) {
            if (friend_group_id.equals(group.getGroup_id())) {
                List<User> users = group.getUsers();
                if (CollectionUtils.isEmpty(users)) {
                    return null;
                }
                List<User> userResults = new ArrayList<User>();
                for (User user : users) {
                    initUserStatus(user);
                    String status = user.getStatus();
                    if (type == 0 && ONLINE.equals(status)) {
                        userResults.add(user);
                    } else if (type == 1 && OFFLINE.equals(status)) {
                        userResults.add(user);
                    } else {
                        userResults.add(user);
                    }
                }
                group.setUsers(userResults);
                return group;
            }
        }
        return null;
    }

    /**
     * 初始化用户在线状态;
     *
     * @param user
     */
    public void initUserStatus(User user) {
        if (user == null) {
            return;
        }
        String userId = user.getId();
        boolean isOnline = this.isOnline(userId);
        if (isOnline) {
            user.setStatus(ONLINE);
        } else {
            user.setStatus(OFFLINE);
        }
    }

    /**
     * 获取好友分组所有成员信息
     *
     * @param user_id
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public List<Group> getAllFriendUsers(String user_id, Integer type) {
        type = 2;
        if (user_id == null) {
            return null;
        }
        List<JSONObject> friendJsonArray = Lists.newArrayList();
        List<Group> groupList = userInfoService.listPeople(user_id);
        for (Group group : groupList) {
            friendJsonArray.add(JSONObject.parseObject(JSONObject.toJSONString(group)));
        }
        if (CollectionUtils.isEmpty(friendJsonArray)) {
            return null;
        }
        List<Group> friends = new ArrayList<Group>();
        for (JSONObject groupJson : friendJsonArray) {
            Group group = JSONObject.toJavaObject(groupJson, Group.class);
            List<User> users = group.getUsers();
            if (CollectionUtils.isEmpty(users)) {
                continue;
            }
            List<User> userResults = new ArrayList<User>();
            for (User user : users) {
                initUserStatus(user);
                String status = user.getStatus();
                if (type == 0 && ONLINE.equals(status)) {
                    userResults.add(user);
                } else if (type == 1 && OFFLINE.equals(status)) {
                    userResults.add(user);
                } else if (type == 2) {
                    userResults.add(user);
                }
            }
            group.setUsers(userResults);
            friends.add(group);
        }
        return friends;
    }

    /**
     * 获取群组所有成员信息（在线+离线)
     *
     * @param user_id
     * @param type(0:所有在线用户,1:所有离线用户,2:所有用户[在线+离线])
     * @return
     */
    @Override
    public List<Group> getAllGroupUsers(String user_id, Integer type) {
        if (user_id == null) {
            return null;
        }
        List<String> group_ids = getGroups(user_id);
        if (CollectionUtils.isEmpty(group_ids)) {
            return null;
        }
        List<Group> groups = new ArrayList<Group>();
        for (String group_id : group_ids) {
            Group group = getGroupUsers(group_id, type);
            if (group != null) {
                groups.add(group);
            }
        }
        return groups;
    }

    /**
     * 获取用户拥有的群组;
     *
     * @param user_id
     * @return
     */
    @Override
    public List<String> getGroups(String user_id) {
        return userInfoService.listGroupIds(user_id);
    }
}

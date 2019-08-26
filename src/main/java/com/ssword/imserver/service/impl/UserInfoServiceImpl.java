package com.ssword.imserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.entity.MsgInfo;
import com.ssword.imserver.entity.Dialogue;
import com.ssword.imserver.entity.NewFriend;
import com.ssword.imserver.entity.UserIIM;
import com.ssword.imserver.model.MsgInfoVo;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.CmdUtils;
import com.ssword.imserver.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.jim.common.cache.redis.RedisCache;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.ssword.imserver.server.helper.ImRedisMessageHelper.SUBFIX;
import static org.jim.common.ImConst.GROUP;

// TODO 所有的添加之前需要先验证本条数据是否已存在
@Service
@Transactional
public class UserInfoServiceImpl implements UserInfoService {
    private static Logger logger = LoggerFactory.getLogger(UserInfoServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 获取用户好友
     */
    @Override
    public List<Group> listPeople(String userid) {
        Integer c = jdbcTemplate.queryForObject("SELECT count(1) FROM t_iim_group WHERE type=1 AND userid = ?", new Object[]{userid}, Integer.class);
        if (c < 1) {// 用户若没有默认的好友群组，则为其新建一个
            this.saveGroup(userid, "默认好友组", 1);
            Group group = new Group();
            group.setUsers(Lists.newArrayList());
            List<Group> l = Lists.newArrayList();
            l.add(group);
            return l;
        } else {
            List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT b.* FROM t_iim_group a LEFT JOIN t_iim_group_user b ON a.id=b.groupid WHERE type=1 AND a.userid = ?", userid);
            return groupUserToGroup(list, 1);
        }
    }

    /**
     * 获取用户群组
     */
    @Override
    public List<Group> listGroup(String userid) {
        List<Map<String, Object>> list2 = jdbcTemplate.queryForList("SELECT * FROM t_iim_group_user WHERE groupid IN (SELECT a.id FROM t_iim_group a LEFT JOIN t_iim_group_user b ON a.id=b.groupid WHERE a.type=2 AND b.userid = ?) ORDER BY cdate ASC", userid);
        List<Group> groupList = groupUserToGroup(list2, 2);
        return groupList;
    }

    @Override
    public List<String> listGroupIds(String userid) {
        List<String> chacheList = Utils.userG(userid);
        if (chacheList.size() < 1) {
            chacheList = jdbcTemplate.queryForList("SELECT groupid FROM t_iim_group_user WHERE groupid IN (SELECT a.id FROM t_iim_group a LEFT JOIN t_iim_group_user b ON a.id=b.groupid WHERE a.type=2 AND b.userid = ?) GROUP BY groupid", String.class, userid);
            for (String item : chacheList) {
                Utils.userAddG(item, userid);
            }
        }
        return chacheList;
    }

    /**
     * 将组与用户的对应关系数据组合成 组对象
     *
     * @param list
     * @return
     */
    private List<Group> groupUserToGroup(List<Map<String, Object>> list, Integer type) {
        Map<String, List<String>> concurrentMap = Maps.newConcurrentMap();
        for (Map<String, Object> map : list) {
            String uid = (String) map.get("userid");
            String gpid = (String) map.get("groupid");
            if (uid == null || gpid == null) {
                continue;
            }
            List<String> userList = Lists.newArrayList();
            if (concurrentMap.containsKey(gpid)) {
                userList = concurrentMap.get(gpid);
            }
            userList.add(uid);
            concurrentMap.put(gpid, userList);
        }
        List<Group> groupList = Lists.newArrayList();
        for (String groupId : concurrentMap.keySet()) {
            Group g = getSqlGroup(groupId);
            if (g == null) continue;
            List<String> usl = concurrentMap.get(groupId);
            List<User> lu = Lists.newArrayList();
            for (String uid : usl) {
                User u = getSqlUser(uid);
                if (u != null) {
                    lu.add(u);
                }
            }
            if (type == 1) {
                Map<String, User> uu = gethelpp();
                lu.add(uu.get(ImConst.USER_FRIEND_HELP_INFO));
                lu.add(uu.get(ImConst.USER_GROUP_HELP_INFO));
            }
            g.setUsers(lu);
            groupList.add(g);
        }
        return groupList;
    }

    /**
     * 修改用户信息
     */
    @Override
    public User UpUserInfo(String userid) {
        return null;
    }

    @Override
    public User getUserInfoById(String userid) {
        User user = getSqlUser(userid);
        if (user == null) return null;
        user.setGroups(listGroup(userid));
        user.setFriends(listPeople(userid));
        return userAssistant(user);
    }

    @Override
    public User getUserById(String userid) {
        User user = getSqlUser(userid);
        if (user == null) return null;
        return userAssistant(user);
    }

    @Override
    public User getUserInfoById(String loginname, String password) {
        User user = getSqlUser(loginname, password);
        if (user == null) return null;
        user.setGroups(listGroup(user.getId()));
        user.setFriends(listPeople(user.getId()));
        return userAssistant(user);
    }

    // 为用户添加群助手和好友助手的好友信息
    private static User userAssistant(User user) {
        List<Group> list = user.getFriends();
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.size() < 1) {
            Group g = new Group();
            g.setUsers(Lists.newArrayList());
            list.add(g);
        }
        Group group = list.get(0);
        List<User> users = group.getUsers();

        Map<String, User> uu = gethelpp();
        users.add(uu.get(ImConst.USER_FRIEND_HELP_INFO));
        users.add(uu.get(ImConst.USER_GROUP_HELP_INFO));
        List<Group> listnnn = Lists.newArrayList();
        group.setUsers(users);
        listnnn.add(group);
        user.setFriends(listnnn);
        return user;
    }

    private static Map<String, User> gethelpp() {
        Map<String, User> map = Maps.newConcurrentMap();
        User user1 = new User();
        user1.setId(ImConst.USER_FRIEND_HELP_INFO);
        user1.setNick("验证消息助手");
        user1.setAvatar("http://101.200.151.183:8080/img/default-user.png");
        user1.setStatus("online");
        user1.setSign("");

        User user2 = new User();
        user2.setId(ImConst.USER_GROUP_HELP_INFO);
        user2.setNick("群组消息助手");
        user2.setAvatar("http://101.200.151.183:8080/img/default-user.png");
        user2.setStatus("online");
        user2.setSign("");
        map.put(user1.getId(), user1);
        map.put(user2.getId(), user2);
        return map;
    }

    @Override
    public Group getGroupById(String groupid) {
        Group group = getSqlGroup(groupid);
        if (group == null) return null;
        group.setUsers(getGroupUserById(groupid));
        return group;
    }

    @Override
    public Group getGById(String groupid) {
        return getSqlGroup(groupid);
    }

    @Override
    public List<User> getGroupUserById(String groupid) {
        List<String> userids = Utils.groupU(groupid);
        if (userids.size() < 1) {
            userids = jdbcTemplate.queryForList("SELECT userid FROM t_iim_group_user WHERE groupid = ? ORDER BY cdate ASC", new Object[]{groupid}, String.class);
        }
        List<User> list = Lists.newArrayList();
        for (String uid : userids) {
            User user = getSqlUser(uid);
            if (user != null) {
                list.add(user);
                Utils.groupAddU(groupid, user.getId());
            }
        }
        return list;
    }

    @Override
    public List<Dialogue> getDio(String userId) {
        List<Map<String, Object>> dialogues = jdbcTemplate.queryForList("SELECT * FROM t_iim_dialogue WHERE userid = ?", userId);
        List<Dialogue> list = Lists.newArrayList();
        for (Map<String, Object> item : dialogues) {
            Dialogue dialogue = mapToDialogue(item);
            if (dialogue != null) {
                list.add(dialogue);
            }
        }
        return list;
    }

    @Override
    public Integer saveDio(String userId, String obid, Integer type) {
        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM t_iim_dialogue WHERE userid=? and objectid=? and type=?", new Object[]{userId, obid, type}, Integer.class);
        if (count > 0) {// 有就不用再加了
            return 0;
        } else {
            return jdbcTemplate.update("INSERT INTO t_iim_dialogue (id, cdate, userid, objectid, type) VALUES (?, ?, ?, ?, ?)", Utils.getuuid(), Utils.now(), userId, obid, type);
        }
    }

    @Override
    public void saveMsg(ChatBody chatBody) {
        MsgInfo msgInfo = this.chatToMsg(chatBody);
        String saveMsgSql = "INSERT INTO t_iim_record (id, cdate, jfrom, jto, jcmd, jcreatetime, jmsgtype, jchattype, jgroupid, jcontent, jextras) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Integer count = jdbcTemplate.update(saveMsgSql, Utils.getuuid(), Utils.now(), msgInfo.getJfrom(), msgInfo.getJto(), msgInfo.getJcmd(), msgInfo.getJcreatetime(), msgInfo.getJmsgtype(), msgInfo.getJchattype(), msgInfo.getJgroupid(), msgInfo.getJcontent(), msgInfo.getJextras());
        if (count < 1) {
            logger.warn("msg save warning:{}", JSONObject.toJSONString(msgInfo));
        }
    }

    @Override
    public Integer saveGroup(String userid, String name, Integer type) {
        return saveGroup("", userid, name, type);
    }

    @Override
    public Integer saveGroup(String groupid, String userid, String name, Integer type) {
        if (StringUtils.isBlank(groupid)) {
            groupid = Utils.getuuid();
        }
        Integer count1 = jdbcTemplate.update("INSERT INTO t_iim_group (id, userid, name, avatar, type) VALUES (?, ?, ?, ?, ?)", groupid, userid, name, "", type);
        // 新建群组之后直接将其本人填入本群组
        jdbcTemplate.update("INSERT INTO t_iim_group_user (id, userid, groupid, cdate) VALUES (?, ?, ?, ?)", Utils.getuuid(), userid, groupid, Utils.now());
        if (count1 < 1) {
            logger.warn("saveGroup warning:{},{},{}", userid, name, type);
        }
        // 添加用户的群组信息缓存
        Utils.userAddG(groupid, userid);
        return count1;
    }

    @Override
    public Integer delGroup(String ids, String uid) {
        String[] idAttr = ids.split(",");
        int count = 0;
        for (String id : idAttr) {
            // 删除对话
            jdbcTemplate.update("DELETE FROM t_iim_dialogue WHERE objectid=? AND type=2", id);
            List<String> list = jdbcTemplate.queryForList("SELECT userid FROM t_iim_group_user WHERE groupid=?", String.class, id);
            for (String userid : list) {
                Utils.unbindGroup(id, userid);
                // 创建对话
                this.saveDio(userid, ImConst.USER_GROUP_HELP_INFO, 1);
                // 发送消息提醒
                CmdUtils.sendMsg(ImConst.USER_GROUP_HELP_INFO, userid, 0, 2, null, "群聊已解散");
                // 清除用户的群组信息缓存
                Utils.userDelG(id, userid);
                // 刷新用户的好友和群组信息
                CmdUtils.sendUserCommand(userid, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
                Utils.delUser(userid);
            }
            // 清除用户的群组信息缓存
            Utils.userDelG(id, uid);
            Utils.delUser(uid);
            // 删除与群组与用户对应表信息
            jdbcTemplate.update("DELETE FROM t_iim_group_user WHERE groupid=?", id);
            // 最后删除群组表
            count += jdbcTemplate.update("DELETE FROM t_iim_group WHERE id=?", id);
        }
        if (count < 1) {
            logger.warn("delGroup warning:{}", ids);
        }
        return count;
    }

    @Override
    public Integer upGroupName(String groupid, String name) {
        RedisCache groupCache = RedisCacheManager.getCache(GROUP);
        String key = groupid + SUBFIX + org.jim.common.ImConst.INFO;
        Group group = groupCache.get(key, Group.class);
        group.setName(name);
        groupCache.put(key, group);
        Utils.setGroup(group);
        return jdbcTemplate.update("UPDATE t_iim_group SET name=? WHERE id=?", name, groupid);
    }

    @Override
    public Integer delGroupUser(String groupid, String ids) {
        String[] idAttr = ids.split(",");
        int count = 0;
        for (String id : idAttr) {
            // 删除对话信息
            jdbcTemplate.update("DELETE FROM t_iim_dialogue WHERE objectid=? AND userid=? AND type=2", groupid, id);
            // 删除群组与用户关系
            count += jdbcTemplate.update("DELETE FROM t_iim_group_user WHERE groupid=? AND userid=?", groupid, id);
            // 创建对话
            this.saveDio(id, ImConst.USER_GROUP_HELP_INFO, 1);
            Utils.unb(groupid, id);
        }
        List<String> list = jdbcTemplate.queryForList("SELECT userid FROM t_iim_group_user WHERE groupid=?", String.class, groupid);
        for (String uid : list) {// 一旦有人退出，刷新群中所有人的列表
            // 刷新用户的好友和群组信息
            CmdUtils.sendUserCommand(uid, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
        }
        if (count < 1) {
            logger.warn("delGroupUser warning:{}", ids);
        }
        return count;
    }

    @Override
    public Integer saveGroupUser(String groupid, String ids) {
        String[] idAttr = ids.split(",");
        int count = 0;
        for (String id : idAttr) {
            Integer num = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM t_iim_group_user WHERE userid=? AND groupid=?", Integer.class, id, groupid);
            if (num < 1) {
                // 添加群组与用户关系
                count += jdbcTemplate.update("INSERT INTO t_iim_group_user (id, userid, groupid, cdate) VALUES (?, ?, ?, ?)", Utils.getuuid(), id, groupid, Utils.now());
            }
            // 创建对话
            this.saveDio(id, ImConst.USER_GROUP_HELP_INFO, 1);
            Utils.sab(groupid, id);
        }
        if (count < 1) {
            logger.warn("saveGroupUser warning:{}", ids);
        }
        return count;
    }

    @Override
    public List<UserIIM> listUserByCell(String cell, String userid) {
        List<UserIIM> userList = Lists.newArrayList();
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("SELECT * FROM t_iim_userinfo WHERE cellnumber=? AND id!=?", cell, userid);
        for (Map<String, Object> map : mapList) {
            UserIIM user = mapToUserIIM(map);
            userList.add(user);
        }
        return userList;
    }

    @Override
    public void saveFriendRequest(String useridA, String useridB, String msg) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM t_iim_newfriend WHERE userida=? AND useridb=? AND sta=0", Integer.class, useridA, useridB);
        if (count < 1) {
            jdbcTemplate.update("INSERT INTO t_iim_newfriend (id, ctdt, userida, useridb, msg, sta) VALUES (?,?,?,?,?,?)", Utils.getuuid(), Utils.now(), useridA, useridB, msg, 2);
        }
    }

    @Override
    public int saveFriendRequestYES(String id) {
        this.newFriendToFriend(id);
        Map<String, Object> map = jdbcTemplate.queryForMap("select * from t_iim_newfriend where id=?", id);
        String useridaa = (String) map.get("userida");// 申请者
        String useridbb = (String) map.get("useridb");
        CmdUtils.sendUserCommand(useridaa, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
        CmdUtils.sendUserCommand(useridbb, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
        return jdbcTemplate.update("UPDATE t_iim_newfriend SET sta=1 WHERE id=?", id);
    }

    @Override
    public int saveFriendRequestNO(String id) {
        return jdbcTemplate.update("UPDATE t_iim_newfriend SET sta=0 WHERE id=?", id);
    }

    @Override
    public List<NewFriend> listNewFriendByUseridB(String userid) {
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("SELECT * FROM t_iim_newfriend WHERE useridb=? AND sta=2", userid);
        List<NewFriend> list = Lists.newArrayList();
        for (Map<String, Object> map : mapList) {
            NewFriend newFriend = mapToNewFriend(map);
            String userida = (String) map.get("userida");
            User user = getUserById(userida);
            newFriend.setAvatar(user.getAvatar());
            newFriend.setUsername(user.getNick());
            list.add(newFriend);
        }
        return list;
    }

    @Override
    public Integer delUserFirend(String userid, String deluserid) {
        String gid1 = jdbcTemplate.queryForObject("SELECT id FROM t_iim_group WHERE userid=? AND type=1", String.class, userid);
        String gid2 = jdbcTemplate.queryForObject("SELECT id FROM t_iim_group WHERE userid=? AND type=1", String.class, deluserid);
        // 删除对话(互删)
        jdbcTemplate.update("DELETE FROM t_iim_dialogue WHERE userid=? AND objectid=? AND type=1", userid, deluserid);
        jdbcTemplate.update("DELETE FROM t_iim_dialogue WHERE userid=? AND objectid=? AND type=1", deluserid, userid);
        // 删除好友组里面的信息(互删)
        jdbcTemplate.update("DELETE FROM t_iim_group_user WHERE userid=? AND groupid=?", userid, gid2);
        int num = jdbcTemplate.update("DELETE FROM t_iim_group_user WHERE userid=? AND groupid=?", deluserid, gid1);
        CmdUtils.sendUserCommand(deluserid, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));// 刷新被删用户的好友列表
        return num;
    }

    @Override
    public Integer delUserDio(String userid, String delobid, Integer type) {
        return jdbcTemplate.update("DELETE FROM t_iim_dialogue WHERE userid=? AND objectid=? AND type=?", userid, delobid, type);
    }

    @Override
    public Map listMsgInfoVo(String aid, String bid, Integer page, Integer num) {
        Integer lim = (page - 1) * num;
        List<MsgInfoVo> infoVoList = Lists.newArrayList();
        List<Map<String, Object>> mapList = jdbcTemplate.queryForList("SELECT * FROM t_iim_record WHERE (jfrom=? AND jto=? AND jchattype=2) OR (jfrom=? AND jto=? AND jchattype=2) OR (jgroupid=? AND jchattype=1) ORDER BY jcreatetime DESC LIMIT ?,?", aid, bid, bid, aid, bid, lim, num);
        Integer type = -1;
        for (Map<String, Object> map : mapList) {
            MsgInfoVo msgInfoVo = new MsgInfoVo();
            msgInfoVo.setId((String) map.get("id"));
            msgInfoVo.setCdate((String) map.get("cdate"));
            String jf = (String) map.get("jfrom");
            msgInfoVo.setJfrom(jf);
            User user = this.getUserById(jf);
            msgInfoVo.setJfromname(user.getNick());
            msgInfoVo.setJcontent((String) map.get("jcontent"));
            Integer jtype = (Integer) map.get("jchattype");
            if (type == -1) {
                type = jtype == 1 ? 2 : 1;
            }
            if (type == 1) {
                String jt = (String) map.get("jto");
                msgInfoVo.setJto(jt);
                User userjt = this.getUserById(jt);
                msgInfoVo.setJtoname(userjt.getNick());
            } else {
                String jt = (String) map.get("jgroupid");
                msgInfoVo.setJto(jt);
                Group group = this.getGroupById(jt);
                msgInfoVo.setJtoname(group.getName());
            }
            infoVoList.add(msgInfoVo);
        }
        Map map = Maps.newConcurrentMap();
        map.put("type", type);
        map.put("list", infoVoList);
        return map;
    }

    /**
     * 将好友申请转化为好友关系
     * 1.查询被申请人的默认好友组
     * 2.将申请人的信息加入被申请人的默认好友组
     * 3.查询申请人的默认好友组
     * 4.将被申请人的信息加入申请人的默认好友组
     */
    private void newFriendToFriend(String id) {
        Map<String, Object> map = jdbcTemplate.queryForMap("SELECT * FROM t_iim_newfriend WHERE id=?", id);
        NewFriend newFriend = mapToNewFriend(map);
        Map<String, Object> mapGb = jdbcTemplate.queryForMap("SELECT * FROM t_iim_group WHERE type=1 AND userid=?", newFriend.getUseridb());
        Map<String, Object> mapGa = jdbcTemplate.queryForMap("SELECT * FROM t_iim_group WHERE type=1 AND userid=?", newFriend.getUserida());
        String groupidb = (String) mapGb.get("id");
        String groupida = (String) mapGa.get("id");
        Integer count1 = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM t_iim_group_user WHERE userid=? and groupid=?", Integer.class, newFriend.getUserida(), groupidb);
        Integer count2 = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM t_iim_group_user WHERE userid=? and groupid=?", Integer.class, newFriend.getUseridb(), groupida);
        if (count1 < 1) {
            jdbcTemplate.update("INSERT INTO t_iim_group_user (id, userid, groupid, cdate) VALUES (?, ?, ?, ?)", Utils.getuuid(), newFriend.getUserida(), groupidb, Utils.now());
        }
        if (count2 < 1) {
            jdbcTemplate.update("INSERT INTO t_iim_group_user (id, userid, groupid, cdate) VALUES (?, ?, ?, ?)", Utils.getuuid(), newFriend.getUseridb(), groupida, Utils.now());
        }
    }

    private NewFriend mapToNewFriend(Map<String, Object> map) {
        NewFriend newFriend = new NewFriend();
        newFriend.setId((String) map.get("id"));
        newFriend.setCtdt((String) map.get("ctdt"));
        newFriend.setUserida((String) map.get("userida"));
        newFriend.setUseridb((String) map.get("useridb"));
        newFriend.setMsg((String) map.get("msg"));
        newFriend.setSta((Integer) map.get("sta"));
        return newFriend;
    }

    /**
     * 将消息ChatBody转为数据库存储对象MsgInfo
     *
     * @param chatBody
     * @return
     */
    private MsgInfo chatToMsg(ChatBody chatBody) {
        MsgInfo msgInfo = new MsgInfo();
        msgInfo.setJfrom(chatBody.getFrom());
        msgInfo.setJto(chatBody.getTo());
        msgInfo.setJcmd(chatBody.getCmd());
        msgInfo.setJcreatetime(chatBody.getCreateTime());
        msgInfo.setJmsgtype(chatBody.getMsgType());
        msgInfo.setJchattype(chatBody.getChatType());
        msgInfo.setJgroupid(chatBody.getGroup_id());
        msgInfo.setJcontent(chatBody.getContent());
        JSONObject j = chatBody.getExtras();
        if (j == null) {
            msgInfo.setJextras("");
        } else {
            msgInfo.setJextras(j.toJSONString());
        }
        return msgInfo;
    }

    /**
     * 将查询出来的对话map转为Dialogue对象
     *
     * @param map
     * @return
     */
    private Dialogue mapToDialogue(Map<String, Object> map) {
        Dialogue dialogue = new Dialogue();
        dialogue.setId((String) map.get("id"));
        dialogue.setCdate((String) map.get("cdate"));
        Integer type = (Integer) map.get("type");
        dialogue.setType(type);
        dialogue.setUserid((String) map.get("userid"));
        String objectid = (String) map.get("objectid");
        dialogue.setObjectid(objectid);
        String name = "???";
        String url = "???";
        try {
            if (type == 1) {
                User user = getSqlUser(objectid);
                name = user.getNick();
                url = user.getAvatar();
            } else if (type == 2) {
                Group group = getGroupById(objectid);
                name = group.getName();
                url = group.getAvatar();
            }
        } catch (Exception e) {
            logger.warn("Dialogue Data Error!");// 对话数据未正确删除就会报错
            return null;
        }
        dialogue.setName(name);
        dialogue.setAvatar(url);
        return dialogue;
    }

    /**
     * 获取用户信息
     *
     * @param userid
     * @return
     */
    private User getSqlUser(String userid) {
        User user = Utils.getU(userid);
        if (user != null) {
            return user;
        } else {
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM t_iim_userinfo WHERE id = ?", userid);
            if (maps == null || maps.size() < 1) {
                logger.error("getSqlUser return null===>{}", userid);
                return null;
            }
            User u = mapToUser(maps.get(0));
            Utils.setUser(u);
            return u;
        }
    }

    /**
     * 获取用户信息
     *
     * @param loginname
     * @param password
     * @return
     */
    private User getSqlUser(String loginname, String password) {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM t_iim_userinfo WHERE loginname = ? AND password = ?", loginname, password);
        if (maps == null || maps.size() < 1) {
            logger.error("getSqlUser return null===>{},{}", loginname, password);
            return null;
        }
        return mapToUser(maps.get(0));
    }

    /**
     * 将数据库中查出来用户转化为用户对象，抛弃对应不上的属性
     *
     * @param map
     * @return
     */
    private User mapToUser(Map<String, Object> map) {
        User user = new User();
        user.setId((String) map.get("id"));
        user.setAvatar((String) map.get("avatar"));
        user.setNick((String) map.get("name"));
        return user;
    }

    private UserIIM mapToUserIIM(Map<String, Object> map) {
        UserIIM user = new UserIIM();
        user.setId((String) map.get("id"));
        user.setAvatar((String) map.get("avatar"));
        user.setNick((String) map.get("name"));
        user.setCellnumber((String) map.get("cellnumber"));
        return user;
    }

    /**
     * 获取群组信息
     *
     * @param groupid
     * @return
     */
    private Group getSqlGroup(String groupid) {
        Group cacheGroup = Utils.getG(groupid);
        if (cacheGroup != null) {
            return cacheGroup;
        } else {
            List<Map<String, Object>> maps = jdbcTemplate.queryForList("SELECT * FROM t_iim_group WHERE id = ?", groupid);
            if (maps == null || maps.size() < 1) {
                logger.error("getSqlGroup return null===>{},{}", groupid);
                return null;
            }
            Map<String, Object> map = maps.get(0);
            Group group = new Group();
            group.setGroup_id((String) map.get("id"));
            group.setName((String) map.get("name"));
            group.setAvatar((String) map.get("avatar"));
            Utils.setGroup(group);
            return group;
        }
    }

}

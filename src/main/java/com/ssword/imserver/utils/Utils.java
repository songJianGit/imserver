package com.ssword.imserver.utils;


import com.google.common.collect.Lists;
import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.helper.ImRedisMessageHelper;
import com.ssword.imserver.service.impl.UserInfoServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImAio;
import org.jim.common.ImConfig;
import org.jim.common.ImPacket;
import org.jim.common.cache.redis.JedisTemplate;
import org.jim.common.cache.redis.RedisCache;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.packets.Command;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;
import org.jim.server.command.CommandManager;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.web.multipart.MultipartFile;
import org.tio.core.ChannelContext;
import org.tio.utils.lock.SetWithLock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.ssword.imserver.server.service.LoginServiceProcessor.TOKENMAPKEY;
import static org.jim.common.ImConst.*;

public class Utils {

    public static final String KEYP_REFIX = "xxx:";// 后期自己添加的缓存前缀
    public static final String SJ_GROUP_USERS = KEYP_REFIX + "sj:group:users:";// 群组里面有哪些用户
    public static final String SJ_USER_GROUPS = KEYP_REFIX + "sj:user:groups:";// 用户拥有哪些群组
    public static final String SJ_GROUP_INFO = KEYP_REFIX + "sj:group:info:";// 群组
    public static final String SJ_USER_INFO = KEYP_REFIX + "sj:user:info:";// 用户

    public static final DateTimeFormatter sdf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static String getuuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String now() {
        return new DateTime().toString(sdf);
    }

    // 获取群组缓存对象
    public static RedisCache getGroupCache() {
        return RedisCacheManager.getCache(GROUP);
    }

    // 获取用户缓存对象
    public static RedisCache getUserCache() {
        return RedisCacheManager.getCache(USER);
    }

    // 往用户里面添加群组id
    public static void userAddG(String groupid, String userid) {
        String key = SJ_USER_GROUPS + userid;
        RedisCache groupCache = getGroupCache();
        List<String> users = groupCache.listGetAll(key);
        if (users == null) {
            users = Lists.newArrayList();
        }
        if (!users.contains(groupid)) {
            groupCache.listPushTail(key, groupid);
        }
    }

    // 从用户里面删除群组id
    public static void userDelG(String groupid, String userid) {
        String key = SJ_USER_GROUPS + userid;
        RedisCache groupCache = getGroupCache();
        groupCache.listRemove(key, groupid);
    }

    // 用户里面有哪些群组
    public static List<String> userG(String userid) {
        String key = SJ_USER_GROUPS + userid;
        RedisCache groupCache = getGroupCache();
        List<String> groups = groupCache.listGetAll(key);
        if (groups == null) {
            groups = Lists.newArrayList();
        }
        return groups;
    }

    // 往群组里面添加用户id
    public static void groupAddU(String groupid, String userid) {
        String key = SJ_GROUP_USERS + groupid;
        RedisCache groupCache = getGroupCache();
        List<String> users = groupCache.listGetAll(key);
        if (users == null) {
            users = Lists.newArrayList();
        }
        if (!users.contains(userid)) {
            groupCache.listPushTail(key, userid);
        }
    }

    // 从群组里面删除用户id
    public static void groupDelU(String groupid, String userid) {
        String key = SJ_GROUP_USERS + groupid;
        RedisCache groupCache = getGroupCache();
        groupCache.listRemove(key, userid);
    }

    // 群组里面有哪些用户
    public static List<String> groupU(String groupid) {
        String key = SJ_GROUP_USERS + groupid;
        RedisCache groupCache = getGroupCache();
        List<String> users = groupCache.listGetAll(key);
        if (users == null) {
            users = Lists.newArrayList();
        }
        return users;
    }

    // 缓存群组
    public static void setGroup(Group group) {
        String key = SJ_GROUP_INFO + group.getGroup_id();
        RedisCache groupCache = getGroupCache();
        groupCache.put(key, group);
    }

    public static Group getG(String groupid) {
        String key = SJ_GROUP_INFO + groupid;
        RedisCache groupCache = getGroupCache();
        return groupCache.get(key, Group.class);
    }

    // 删除群组缓存
    public static void delGroup(Group group) {
        delGroup(group.getGroup_id());
    }

    // 删除群组缓存
    public static void delGroup(String groupid) {
        String key = SJ_GROUP_INFO + groupid;
        RedisCache groupCache = getGroupCache();
        groupCache.remove(key);
    }

    // 缓存用户
    public static void setUser(User user) {
        String key = SJ_USER_INFO + user.getId();
        RedisCache userCache = getUserCache();
        userCache.put(key, user);
    }

    public static User getU(String userid) {
        String key = SJ_USER_INFO + userid;
        RedisCache userCache = getUserCache();
        return userCache.get(key, User.class);
    }

    // 删除用户缓存
    private static void clearUserById(String userid) {
        if (StringUtils.isNotBlank(userid)) {
            RedisCache tokenMap = RedisCacheManager.getCache(TOKENMAPKEY);
            String token = tokenMap.get(ImConst.USER_TOKEN_INFO + userid, String.class);
            if (StringUtils.isNotBlank(token)) {
                tokenMap.remove(token);
            }
        }
    }

    // 删除用户缓存
    public static void delUser(User user) {
        clearUserById(user.getId());
        String key = SJ_USER_INFO + user.getId();
        RedisCache userCache = getUserCache();
        userCache.remove(key);
    }

    // 删除用户缓存
    public static void delUser(String userid) {
        clearUserById(userid);
        String key = SJ_USER_INFO + userid;
        RedisCache userCache = getUserCache();
        userCache.remove(key);
    }


//    // 清除群组中的用户
//    public static void clearGroupUser(String groupid, String userid) {
//        String group_user_key = groupid + ImRedisMessageHelper.SUBFIX + USER;
//        RedisCache groupCache = RedisCacheManager.getCache(GROUP);
//        groupCache.listRemove(group_user_key, userid);
//    }
//
//    // 添加群组中的用户
//    public static void addGroupUser(String groupid, String userid) {
//        String group_user_key = groupid + ImRedisMessageHelper.SUBFIX + USER;
//        RedisCache groupCache = RedisCacheManager.getCache(GROUP);
//        List<String> users = groupCache.listGetAll(group_user_key);
//        if (!users.contains(userid)) {
//            groupCache.listPushTail(group_user_key, userid);
//        }
//    }
//
//    // 清除用户的群组信息
//    public static void clearUserGroup(String groupid, String userid) {
//        String user_group_key = userid + ImRedisMessageHelper.SUBFIX + GROUP;
//        RedisCache userCache = RedisCacheManager.getCache(USER);
//        userCache.listRemove(user_group_key, groupid);
//    }

//    // 添加用户的群组信息
//    public static void addUserGroup(String groupid, String userid) {
//        String user_group_key = userid + ImRedisMessageHelper.SUBFIX + GROUP;
//        RedisCache userCache = RedisCacheManager.getCache(USER);
//        List<String> groups = userCache.listGetAll(user_group_key);
//        if (!groups.contains(groupid)) {
//            userCache.listPushTail(user_group_key, groupid);
//        }
//    }


    /**
     * 绑定群组;
     */
    public static void bindGroup(String groupid, String userid) {
        if (StringUtils.isNotBlank(groupid) && StringUtils.isNotBlank(userid)) {
            SetWithLock<ChannelContext> setWithLock = ImAio.getChannelContextsByUserId(userid);
            if (setWithLock != null && setWithLock.size() > 0) {
                for (ChannelContext channel : setWithLock.getObj()) {
                    ImAio.bindGroup(channel, groupid);
                }
            }
        }
    }

    // 当用户加入群组
    public static void sab(String groupid, String userid) {
        // 用户与群组绑定
        Utils.bindGroup(groupid, userid);
        // 添加群组中的用户信息
        Utils.groupAddU(groupid, userid);
        // 发送消息提醒
        CmdUtils.sendMsg(ImConst.USER_GROUP_HELP_INFO, userid, 0, 2, null, "您已加入群聊");
        // 添加用户的群组信息缓存
        Utils.userAddG(groupid, userid);
        // 刷新用户的好友和群组信息
        CmdUtils.sendUserCommand(userid, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
        clearUserById(userid);
    }

    // 当用户退出群组
    public static void unb(String groupid, String userid) {
        // 用户与群组解绑
        Utils.unbindGroup(groupid, userid);
        // 清除群组中的用户信息
        Utils.groupDelU(groupid, userid);
        // 发送消息提醒
        CmdUtils.sendMsg(ImConst.USER_GROUP_HELP_INFO, userid, 0, 2, null, "您已不在群聊中");
        // 清除用户的群组信息缓存
        Utils.userDelG(groupid, userid);
        // 刷新用户的好友和群组信息---这里要刷，因为下面那个就查不到这里删除的用户了，，，，
        CmdUtils.sendUserCommand(userid, Command.valueOf(ImConst.COMMAND_RELOAD_USERGROUP_RESP));
        clearUserById(userid);
    }

    /**
     * 解绑群组;
     */
    public static void unbindGroup(String groupid, String userid) {
        ImAio.unbindGroup(userid, groupid);
    }

    public static boolean isOnline(String userid) {
        ImConfig imConfig = CommandManager.getImConfig();
        return imConfig.getMessageHelper().isOnline(userid);
    }

    /**
     * 这里返回的支付没头部斜杠，也没有结尾斜杠。
     *
     * @param md5
     * @return
     */
    public static String getPathByMD5(String md5) {
        return md5.substring(0, 2) + "/" + md5.substring(2, 4) + "/" + md5.substring(4, 6) + md5.substring(6);
    }

    public static String getMD5ByFile(MultipartFile multipartFile) {
        try {
            InputStream fileInputStream = multipartFile.getInputStream();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int length = -1;
            while ((length = fileInputStream.read(buffer, 0, 8192)) != -1) {
                messageDigest.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, messageDigest.digest());
            return bigInt.toString(16).toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 检查文件夹是否存在，不存在则创建
    public static void hasfolder(String folder) {
        File file = new File(folder);
        if (!file.exists() && !file.isDirectory()) {
            file.mkdirs();
        }
    }

}

/**
 *
 */
package com.ssword.imserver.server.command;

import com.alibaba.fastjson.JSONObject;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.SpringUtil;
import com.ssword.imserver.utils.Utils;
import org.jim.common.ImPacket;
import org.jim.common.cache.redis.RedisCache;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.packets.Group;
import org.jim.common.ws.WsRequestPacket;
import org.jim.server.command.handler.processor.CmdProcessor;
import org.tio.core.ChannelContext;

import static com.ssword.imserver.server.helper.ImRedisMessageHelper.SUBFIX;
import static org.jim.common.ImConst.GROUP;


/**
 *
 * @author songJ
 *
 */
public class GroupServiceProcessor implements CmdProcessor {

    private UserInfoService userInfoService = SpringUtil.getBean(UserInfoService.class);

    @Override
    public boolean isProtocol(ChannelContext channelContext) {
        return true;// 往上干就完事
    }

    @Override
    public String name() {
        return "default";// 无名
    }

    /**
     * 新建群组
     */
    public Integer saveGroup(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        String name = json.getString("name");
        Integer type = json.getInteger("type");
        String groupid = Utils.getuuid();
        Integer count = userInfoService.saveGroup(groupid, userid, name, type);
        // 群组信息的缓存
        RedisCache groupCache = RedisCacheManager.getCache(GROUP);
        String key = groupid + SUBFIX + org.jim.common.ImConst.INFO;
        Group group = userInfoService.getGroupById(groupid);
        groupCache.put(key, group);
        // 用户与群组绑定
        Utils.bindGroup(groupid, userid);
        // 添加群组中的用户信息
        Utils.groupAddU(groupid, userid);
        return count;
    }

    /**
     * 删除群组
     */
    public Integer delGroup(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String ids = json.getString("ids");
        String userid = json.getString("userid");
        return userInfoService.delGroup(ids, userid);
    }

    /**
     * 群组改名
     */
    public Integer upGroupName(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String groupid = json.getString("groupid");
        String name = json.getString("name");
        String userid = json.getString("userid");
//        Utils.clearUserById(userid);// 清除用户缓存
        return userInfoService.upGroupName(groupid, name);
    }

    /**
     * 群组添加成员
     */
    public Integer saveGroupUser(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String groupid = json.getString("groupid");
        String ids = json.getString("ids");
        String userid = json.getString("userid");
        return userInfoService.saveGroupUser(groupid, ids);
    }

    /**
     * 群组删除成员
     */
    public Integer delGroupUser(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String groupid = json.getString("groupid");
        String ids = json.getString("ids");
        String userid = json.getString("userid");
        return userInfoService.delGroupUser(groupid, ids);
    }
}

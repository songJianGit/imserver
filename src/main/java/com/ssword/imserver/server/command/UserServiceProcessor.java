/**
 *
 */
package com.ssword.imserver.server.command;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ssword.imserver.model.MsgInfoVo;
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

import java.util.List;
import java.util.Map;

import static com.ssword.imserver.server.helper.ImRedisMessageHelper.SUBFIX;
import static org.jim.common.ImConst.GROUP;


/**
 *
 * @author songJ
 *
 */
public class UserServiceProcessor implements CmdProcessor {

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
     * 删除好友
     */
    public Integer delUserFirend(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        String deluserid = json.getString("deluserid");
        Integer count = userInfoService.delUserFirend(userid, deluserid);
        return count;
    }

    /**
     * 聊天记录
     */
    public Map listMsgInfoVo(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String aid = json.getString("aid");
        String bid = json.getString("bid");
        Integer page = json.getInteger("page");
        Map map = Maps.newConcurrentMap();
        Map map1 = userInfoService.listMsgInfoVo(aid, bid, page, 50);
        map.put("list", map1.get("list"));
        map.put("aid", aid);
        map.put("bid", bid);
        map.put("type", map1.get("type"));
        return map;
    }
}

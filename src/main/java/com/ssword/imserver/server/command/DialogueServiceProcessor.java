/**
 *
 */
package com.ssword.imserver.server.command;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ssword.imserver.entity.Dialogue;
import com.ssword.imserver.model.InfoVo;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.SpringUtil;
import org.jim.common.ImPacket;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;
import org.jim.common.ws.WsRequestPacket;
import org.jim.server.command.handler.processor.CmdProcessor;
import org.tio.core.ChannelContext;

import java.util.List;
import java.util.Map;

/**
 *
 * @author songJ
 *
 */
public class DialogueServiceProcessor implements CmdProcessor {

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
     * 获取用户对话
     * @param packet
     * @param channelContext
     * @return
     */
    public List<Dialogue> getUserDialogue(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        return userInfoService.getDio(userid);
    }

    /**
     * 新建用户对话信息
     */
    public Map<String, Object> saveUserDialogue(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        String obid = json.getString("obid");
        Integer type = json.getInteger("type");
        Integer upcount = userInfoService.saveDio(userid, obid, type);
        Map<String, Object> map = Maps.newConcurrentMap();
        map.put("userid", userid);
        map.put("objectid", obid);
        map.put("type", type);
        map.put("upcount", upcount);
        if (type == 1) {
            User user = userInfoService.getUserById(obid);
            map.put("name", user.getNick());
            map.put("avatar", user.getAvatar());
        } else {
            Group group = userInfoService.getGById(obid);
            map.put("name", group.getName());
            map.put("avatar", group.getAvatar());
        }
        return map;
    }

    /**
     * 删除对话
     */
    public Integer delUserDio(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        String delobid = json.getString("delobid");
        Integer type = json.getInteger("type");
        Integer count = userInfoService.delUserDio(userid, delobid, type);
        return count;
    }

    /**
     * 获取用户或群组信息
     */
    public InfoVo getInfoVo(ImPacket packet, ChannelContext channelContext) {
        JSONObject json = (JSONObject) JSONObject.parse(((WsRequestPacket) packet).getWsBodyText().getBytes());
        String userid = json.getString("userid");
        String obid = json.getString("obid");
        Integer type = json.getInteger("type");
        if (type == 1) {
            User user = userInfoService.getUserById(obid);
            return new InfoVo(user.getId(), user.getNick(), user.getAvatar(), type);
        } else if (type == 2) {
            Group group = userInfoService.getGById(obid);
            return new InfoVo(group.getId(), group.getName(), group.getAvatar(), type);
        } else {
            return null;
        }
    }
}

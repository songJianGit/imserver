package com.ssword.imserver.utils;

import com.google.common.collect.Maps;
import com.ssword.imserver.constant.ImConst;
import com.sun.org.apache.bcel.internal.generic.PUSH;
import org.jim.common.ImAio;
import org.jim.common.ImPacket;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.packets.*;
import org.joda.time.Instant;

import java.util.Map;

public class CmdUtils {
    /**
     * 发送消息
     * 这里的消息都不会被ImRedisMessageHelper拦截
     *
     * @param fromId   消息发送人
     * @param toId     消息接收人
     * @param msgType  消息类型;(如：0:text、1:image、2:voice、3:vedio、4:music、5:news)
     * @param chatType 聊天类型int类型(0:未知,1:公聊,2:私聊)
     * @param group_id 群组id
     * @param content  内容
     */

    public static void sendMsg(String fromId, String toId, Integer msgType, Integer chatType, String group_id, String content) {
        ChatBody chatBody = ChatBody.newBuilder()
                .setFrom(fromId)
                .setTo(toId)
                .setMsgType(msgType)
                .setChatType(chatType)
                .setGroup_id(group_id)
                .setContent(content)
                .setCreateTime(Instant.now().getMillis()).build();
        if (Utils.isOnline(toId)) {
            ImPacket chatPacket = new ImPacket(Command.COMMAND_CHAT_REQ, new RespBody(Command.COMMAND_CHAT_REQ, chatBody).toByte());
            ImAio.sendToUser(toId, chatPacket);
        } else {
            sendPush(chatBody, fromId, toId);
        }
    }

    /**
     * 离线消息
     */
    public static void sendPush(ChatBody chatBody, String useridA, String useridB) {
        double score = chatBody.getCreateTime();
        RedisCacheManager.getCache(org.jim.common.ImConst.PUSH).sortSetPush(org.jim.common.ImConst.USER + ":" + useridB + ":" + useridA, score, chatBody);
    }

    public static void sendUserCommand(String userid, Command command) {
        ImPacket chatPacket = new ImPacket(command, new RespBody(command, ChatBody.newBuilder().build()).toByte());
        ImAio.sendToUser(userid, chatPacket);
    }

    public static void newFictitiousDio_group(String userid, Group group) {
        Map<String, Object> map = Maps.newConcurrentMap();
        map.put("userid", userid);
        map.put("name", group.getName());
        map.put("avatar", group.getAvatar());
        map.put("objectid", group.getGroup_id());
        map.put("type", 2);
        map.put("cdate", Utils.now());
        ImPacket chatPacket = new ImPacket(Command.valueOf(ImConst.COMMAND_NEW_DIALOGUE_RESP), new RespBody(Command.valueOf(ImConst.COMMAND_NEW_DIALOGUE_RESP), map).toByte());
        ImAio.sendToUser(userid, chatPacket);
    }
}

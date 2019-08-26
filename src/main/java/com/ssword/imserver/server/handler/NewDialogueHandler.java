/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.DialogueServiceProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.common.ImPacket;
import org.jim.common.packets.Command;
import org.jim.common.packets.RespBody;
import org.jim.server.command.AbstractCmdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;

import java.util.List;
import java.util.Map;


/**
 * 新建会话 处理
 * @author songJ
 */
public class NewDialogueHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(NewDialogueHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_NEW_DIALOGUE_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<DialogueServiceProcessor> dialogueProcessors = this.getProcessor(channelContext, DialogueServiceProcessor.class);
        if (CollectionUtils.isEmpty(dialogueProcessors)) {
            log.info("新建对话失败,没有新建对话业务处理器!");
            Aio.remove(channelContext, "no NewDialogue serviceHandler processor!");
            return null;
        }
        DialogueServiceProcessor loginServiceHandler = dialogueProcessors.get(0);
        Map<String, Object> map = loginServiceHandler.saveUserDialogue(packet, channelContext);
        Integer upcount = (Integer) map.get("upcount");
        Command command = Command.valueOf(ImConst.COMMAND_NEW_DIALOGUE_RESP_VAL);
        RespBody respBody = new RespBody(command, map);
        respBody.setCode(ImConst.COMMAND_NEW_DIALOGUE_RESP_CODE);
        if (upcount < 1) {
            respBody.setMsg("对话已存在");
        } else {
            respBody.setMsg("新建对话信息成功");
        }
        ImPacket userDialogue = new ImPacket(command, respBody.toByte());
        return userDialogue;
    }
}

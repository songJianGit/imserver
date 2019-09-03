/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.entity.Dialogue;
import com.ssword.imserver.server.command.DialogueServiceProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.jim.common.ImPacket;
import org.jim.common.packets.Command;
import org.jim.common.packets.RespBody;
import org.jim.server.command.AbstractCmdHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

import java.util.List;


/**
 * 获取最近会话处理
 * @author songJ
 */
public class DialogueHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(DialogueHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_GET_DIALOGUE_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<DialogueServiceProcessor> dialogueProcessors = this.getProcessor(channelContext, DialogueServiceProcessor.class);
        if (CollectionUtils.isEmpty(dialogueProcessors)) {
            log.info("获取对话失败,没有对话业务处理器!");
            Tio.remove(channelContext, "no Dialogue serviceHandler processor!");
            return null;
        }
        DialogueServiceProcessor loginServiceHandler = dialogueProcessors.get(0);
        List<Dialogue> dialogueList = loginServiceHandler.getUserDialogue(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_GET_DIALOGUE_RESP_VAL);
        RespBody respBody = new RespBody(command, dialogueList);
        respBody.setCode(ImConst.COMMAND_GET_DIALOGUE_RESP_CODE);
        respBody.setMsg("获取对话信息成功");
        ImPacket userDialogue = new ImPacket(command, respBody.toByte());
        return userDialogue;
    }
}

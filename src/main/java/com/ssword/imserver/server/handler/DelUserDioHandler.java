/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.DialogueServiceProcessor;
import com.ssword.imserver.server.command.UserServiceProcessor;
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


/**
 * 对话删除处理
 * @author songJ
 */
public class DelUserDioHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(DelUserDioHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_DELLDIO_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<DialogueServiceProcessor> dialogueProcessors = this.getProcessor(channelContext, DialogueServiceProcessor.class);
        if (CollectionUtils.isEmpty(dialogueProcessors)) {
            log.info("对话删除业务失败,没有好友删除处理器!");
            Aio.remove(channelContext, "no DialogueServiceProcessor processor!");
            return null;
        }
        DialogueServiceProcessor dialogueServiceProcessor = dialogueProcessors.get(0);
        Integer count = dialogueServiceProcessor.delUserDio(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_DELLDIO_RESP_VAL);
        RespBody respBody = new RespBody(command, count);
        respBody.setCode(ImConst.COMMAND_DELLDIO_RESP_CODE);
        respBody.setMsg("删除对话成功");
        return new ImPacket(command, respBody.toByte());
    }
}

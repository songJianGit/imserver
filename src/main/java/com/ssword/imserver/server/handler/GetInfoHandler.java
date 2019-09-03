/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.model.InfoVo;
import com.ssword.imserver.server.command.DialogueServiceProcessor;
import com.ssword.imserver.server.command.GroupServiceProcessor;
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
 * 获取用户或群组最新消息
 * @author songJ
 */
public class GetInfoHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(GetInfoHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_GETINFO_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<DialogueServiceProcessor> dialogueProcessors = this.getProcessor(channelContext, DialogueServiceProcessor.class);
        if (CollectionUtils.isEmpty(dialogueProcessors)) {
            log.info("对话业务失败,没有对话业务处理器!");
            Tio.remove(channelContext, "no Dialogue serviceHandler processor!");
            return null;
        }
        DialogueServiceProcessor dialogueServiceProcessor = dialogueProcessors.get(0);
        InfoVo infoVo = dialogueServiceProcessor.getInfoVo(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_GETINFO_RESP_VAL);
        RespBody respBody = new RespBody(command, infoVo);
        respBody.setCode(ImConst.COMMAND_GETINFO_RESP_CODE);
        respBody.setMsg("获取最新信息成功");
        return new ImPacket(command, respBody.toByte());
    }
}

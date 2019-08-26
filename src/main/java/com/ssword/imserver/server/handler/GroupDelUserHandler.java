/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.GroupServiceProcessor;
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
 * 群组处理
 * @author songJ
 */
public class GroupDelUserHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(GroupDelUserHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_DELLUSER_GROUP_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<GroupServiceProcessor> dialogueProcessors = this.getProcessor(channelContext, GroupServiceProcessor.class);
        if (CollectionUtils.isEmpty(dialogueProcessors)) {
            log.info("群组业务失败,没有群组业务处理器!");
            Aio.remove(channelContext, "no Group serviceHandler processor!");
            return null;
        }
        GroupServiceProcessor groupServiceProcessor = dialogueProcessors.get(0);
        Integer count = groupServiceProcessor.delGroupUser(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_DELLUSER_GROUP_RESP_VAL);
        RespBody respBody = new RespBody(command, count);
        respBody.setCode(ImConst.COMMAND_DELLUSER_GROUP_RESP_CODE);
        respBody.setMsg("用户退出群组成功");
        return new ImPacket(command, respBody.toByte());
    }
}

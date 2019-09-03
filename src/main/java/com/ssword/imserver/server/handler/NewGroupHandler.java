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
import org.tio.core.ChannelContext;
import org.tio.core.Tio;

import java.util.List;


/**
 * 群组处理
 * @author songJ
 */
public class NewGroupHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(NewGroupHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_NEW_GROUP_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<GroupServiceProcessor> groupServiceProcessors = this.getProcessor(channelContext, GroupServiceProcessor.class);
        if (CollectionUtils.isEmpty(groupServiceProcessors)) {
            log.info("群组业务失败,没有群组业务处理器!");
            Tio.remove(channelContext, "no Group serviceHandler processor!");
            return null;
        }
        GroupServiceProcessor groupServiceProcessor = groupServiceProcessors.get(0);
        Integer count = groupServiceProcessor.saveGroup(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_NEW_GROUP_RESP_VAL);
        RespBody respBody = new RespBody(command, count);
        respBody.setCode(ImConst.COMMAND_NEW_GROUP_RESP_CODE);
        respBody.setMsg("新建群组成功");
        return new ImPacket(command, respBody.toByte());
    }
}

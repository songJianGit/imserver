/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.GroupServiceProcessor;
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
 * 删除好友处理
 * @author songJ
 */
public class DelUserFriendHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(DelUserFriendHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_DELLUSER_FRIEND_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<UserServiceProcessor> userServiceProcessors = this.getProcessor(channelContext, UserServiceProcessor.class);
        if (CollectionUtils.isEmpty(userServiceProcessors)) {
            log.info("好友删除业务失败,没有好友删除处理器!");
            Aio.remove(channelContext, "no UserServiceProcessor processor!");
            return null;
        }
        UserServiceProcessor userServiceProcessor = userServiceProcessors.get(0);
        Integer count = userServiceProcessor.delUserFirend(packet, channelContext);
        Command command = Command.valueOf(ImConst.COMMAND_DELLUSER_FRIEND_RESP_VAL);
        RespBody respBody = new RespBody(command, count);
        respBody.setCode(ImConst.COMMAND_DELLUSER_FRIEND_RESP_CODE);
        respBody.setMsg("删除好友成功");
        return new ImPacket(command, respBody.toByte());
    }
}

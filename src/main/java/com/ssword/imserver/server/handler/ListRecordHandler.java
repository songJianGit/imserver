/**
 *
 */
package com.ssword.imserver.server.handler;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.UserServiceProcessor;
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
import java.util.Map;


/**
 * 聊天记录
 * @author songJ
 */
public class ListRecordHandler extends AbstractCmdHandler {

    private static Logger log = LoggerFactory.getLogger(ListRecordHandler.class);

    @Override
    public Command command() {
        return Command.valueOf(ImConst.COMMAND_RECORD_REQ_VAL);
    }

    @Override
    public ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception {
        List<UserServiceProcessor> userServiceProcessors = this.getProcessor(channelContext, UserServiceProcessor.class);
        if (CollectionUtils.isEmpty(userServiceProcessors)) {
            log.info("聊天记录获取业务失败,没有业务处理器!");
            Tio.remove(channelContext, "no UserServiceProcessor serviceHandler processor!");
            return null;
        }
        UserServiceProcessor userServiceProcessor = userServiceProcessors.get(0);
        Map map = userServiceProcessor.listMsgInfoVo(packet, channelContext);// 有时候可能获取到了，但是页面不显示，因为获取的数据可能页面上都有，不必显示。
        Command command = Command.valueOf(ImConst.COMMAND_RECORD_RESP_VAL);
        RespBody respBody = new RespBody(command, map);
        respBody.setCode(ImConst.COMMAND_RECORD_RESP_CODE);
        respBody.setMsg("聊天记录获取成功");
        return new ImPacket(command, respBody.toByte());
    }
}

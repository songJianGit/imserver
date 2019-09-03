package org.jim.server.command;

import org.jim.common.ImPacket;
import org.jim.common.packets.Command;
import org.tio.core.ChannelContext;

/**
 * 版本: [1.0]
 * 功能说明:
 * 作者: WChao 创建时间: 2017年9月8日 下午4:29:38
 */
public interface CmdHandler {
    /**
     * 功能描述：[命令主键]
     * 创建者：WChao 创建时间: 2017年7月17日 下午2:31:51
     *
     * @return
     */
	Command command();

    /**
     * @param packet
     * @param channelContext
     * @return
     * @throws Exception
     * @date 2016年11月18日 下午1:08:45
     */
	ImPacket handler(ImPacket packet, ChannelContext channelContext) throws Exception;

}

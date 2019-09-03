/**
 *
 */
package org.jim.server.command.handler.processor;

import org.tio.core.ChannelContext;

/**
 * 不同协议CMD命令处理接口
 * @author WChao
 *
 */
public interface CmdProcessor {
    /**
     * 不同协议判断方法
     * @param channelContext
     * @return
     */
	boolean isProtocol(ChannelContext channelContext);

    /**
     * 该proCmd处理器名称(自定义)
     * @return
     */
	String name();

}

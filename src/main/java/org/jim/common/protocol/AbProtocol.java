/**
 *
 */
package org.jim.common.protocol;

import org.tio.core.ChannelContext;

import java.nio.ByteBuffer;

/**
 * @author WChao
 * @date 2018-09-05 23:52:00
 */
public abstract class AbProtocol implements IProtocol {
    /**
     * 协议包转化器;
     */
    private IConvertProtocolPacket converter;

    public AbProtocol() {
        this.converter = converter();
    }

    /**
     * 根据buffer判断是否属于指定协议
     * @param buffer
     * @param channelContext
     * @return
     * @throws Throwable
     */
    public abstract boolean isProtocolByBuffer(ByteBuffer buffer, ChannelContext channelContext) throws Throwable;

    public boolean isProtocol(ByteBuffer buffer, ChannelContext channelContext) throws Throwable {
        ByteBuffer copyByteBuffer = null;
        if (buffer != null && channelContext.get() == null) {
            copyByteBuffer = ByteBuffer.wrap(buffer.array());
        }
        return isProtocolByBuffer(copyByteBuffer, channelContext);
    }

    public IConvertProtocolPacket getConverter() {
        return converter;
    }
}

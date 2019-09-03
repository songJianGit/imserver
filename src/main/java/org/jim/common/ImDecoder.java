/**
 *
 */
package org.jim.common;

import org.tio.core.ChannelContext;
import org.tio.core.exception.AioDecodeException;

import java.nio.ByteBuffer;


/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年7月27日 下午5:25:13
 */
public interface ImDecoder {

    ImPacket decode(ByteBuffer buffer, ChannelContext channelContext) throws AioDecodeException;
}

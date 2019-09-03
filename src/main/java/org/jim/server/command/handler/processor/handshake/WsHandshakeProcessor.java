/**
 *
 */
package org.jim.server.command.handler.processor.handshake;

import org.jim.common.ImPacket;
import org.jim.common.Protocol;
import org.jim.common.packets.Command;
import org.jim.common.ws.WsRequestPacket;
import org.jim.common.ws.WsResponsePacket;
import org.jim.common.ws.WsSessionContext;
import org.tio.core.ChannelContext;

/**
 * 版本: [1.0]
 * 功能说明: 
 * @author : WChao 创建时间: 2017年9月11日 下午4:22:36
 */
public class WsHandshakeProcessor implements HandshakeCmdProcessor {

    /**
     * 对httpResponsePacket参数进行补充并返回，如果返回null表示不想和对方建立连接，框架会断开连接，如果返回非null，框架会把这个对象发送给对方
     * @param packet
     * @param channelContext
     * @return
     * @throws Exception
     * @author: WChao
     */
    @Override
    public ImPacket handshake(ImPacket packet, ChannelContext channelContext) throws Exception {
        WsRequestPacket wsRequestPacket = (WsRequestPacket) packet;
        WsSessionContext wsSessionContext = (WsSessionContext) channelContext.get();
        if (wsRequestPacket.isHandShake()) {
            WsResponsePacket wsResponsePacket = new WsResponsePacket();
            wsResponsePacket.setHandShake(true);
            wsResponsePacket.setCommand(Command.COMMAND_HANDSHAKE_RESP);
            wsSessionContext.setHandshaked(true);
            return wsResponsePacket;
        }
        return null;
    }

    /**
     * 握手成功后
     * @param packet
     * @param channelContext
     * @throws Exception
     * @author Wchao
     */
    @Override
    public void onAfterHandshaked(ImPacket packet, ChannelContext channelContext) throws Exception {

    }

    /**
     * @Author WChao
     * @Description 判断当前连接是否属于WS协议
     * @param [channelContext]
     * @return boolean
     **/
    @Override
    public boolean isProtocol(ChannelContext channelContext) {
        Object sessionContext = channelContext.get();
        if (sessionContext == null) {
            return false;
        } else return sessionContext instanceof WsSessionContext;
    }

    /**
     * @Author WChao
     * @Description 协议名称
     * @param []
     * @return java.lang.String
     **/
    @Override
    public String name() {

        return Protocol.WEBSOCKET;
    }

}

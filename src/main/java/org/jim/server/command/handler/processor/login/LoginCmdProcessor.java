/**
 *
 */
package org.jim.server.command.handler.processor.login;

import org.jim.common.packets.LoginReqBody;
import org.jim.common.packets.LoginRespBody;
import org.jim.server.command.handler.processor.CmdProcessor;
import org.tio.core.ChannelContext;

/**
 *
 * @author WChao
 */
public interface LoginCmdProcessor extends CmdProcessor {
    /**
     * 执行登录操作接口方法
     * @param loginReqBody
     * @param channelContext
     * @return
     */
    LoginRespBody doLogin(LoginReqBody loginReqBody, ChannelContext channelContext);

    /**
     * 登录成功回调方法
     * @param channelContext
     */
    void onSuccess(ChannelContext channelContext);
}

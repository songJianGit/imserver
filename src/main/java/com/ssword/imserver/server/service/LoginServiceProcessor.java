/**
 *
 */
package com.ssword.imserver.server.service;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jim.common.ImPacket;
import org.jim.common.ImSessionContext;
import org.jim.common.ImStatus;
import org.jim.common.cache.redis.RedisCache;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.http.HttpConst;
import org.jim.common.packets.*;
import org.jim.common.utils.JsonKit;
import org.jim.common.utils.Md5;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.JoinGroupReqHandler;
import org.jim.server.command.handler.processor.login.LoginCmdProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;


/**
 * @author WChao
 *
 */
public class LoginServiceProcessor implements LoginCmdProcessor {

    private static Logger logger = LoggerFactory.getLogger(LoginServiceProcessor.class);

    public static final String TOKENMAPKEY = "tokenmap";

    private RedisCache tokenMap = null;

    public LoginServiceProcessor() {
        this.tokenMap = RedisCacheManager.getCache(TOKENMAPKEY);
    }

    private UserInfoService userInfoService = SpringUtil.getBean(UserInfoService.class);

    /**
     * 根据用户名和密码获取用户
     * @param loginname
     * @param password
     * @return
     * @author: WChao
     */
    public User getUser(String loginname, String password) {
        String text = loginname + password;
        String key = ImConst.AUTH_KEY;
        String token = Md5.sign(text, key, HttpConst.CHARSET_NAME);
        User user = getUser(token);
        if (user != null) {
            return user;
        } else {
            User u = userInfoService.getUserInfoById(loginname, password);
            tokenMap.put(token, u);
            tokenMap.put(ImConst.USER_TOKEN_INFO + u.getId(), token);
            return u;
        }
    }

    /**
     * 根据token获取用户信息
     * @param token
     * @return
     * @author: WChao
     */
    public User getUser(String token) {
        User user = tokenMap.get(token, User.class);
        if (user != null) {
            return user;
        } else {
            return null;
        }
    }

    /**
     * doLogin方法注意：J-IM登陆命令是根据user是否为空判断是否登陆成功,
     *
     * 当登陆失败时设置user属性需要为空，相反登陆成功user属性是必须非空的;
     */
    @Override
    public LoginRespBody doLogin(LoginReqBody loginReqBody, ChannelContext channelContext) {
        String loginname = loginReqBody.getLoginname();
        String password = loginReqBody.getPassword();
        ImSessionContext imSessionContext = (ImSessionContext) channelContext.getAttribute();
        String handshakeToken = imSessionContext.getToken();
        User user;
        LoginRespBody loginRespBody;
        if (!StringUtils.isBlank(handshakeToken)) {
            user = this.getUser(handshakeToken);
        } else {
            user = this.getUser(loginname, password);
        }
        if (user == null) {
            loginRespBody = new LoginRespBody(Command.COMMAND_LOGIN_RESP, ImStatus.C10008);
        } else {
            loginRespBody = new LoginRespBody(Command.COMMAND_LOGIN_RESP, ImStatus.C10007, user);
            loginRespBody.setData(user);
        }
        return loginRespBody;
    }

    @Override
    public void onSuccess(ChannelContext channelContext) {
        logger.info("登录成功回调方法");
        ImSessionContext imSessionContext = (ImSessionContext) channelContext.getAttribute();
        User user = imSessionContext.getClient().getUser();
        if (user.getGroups() != null) {
            for (Group group : user.getGroups()) {//发送加入群组通知
                ImPacket groupPacket = new ImPacket(Command.COMMAND_JOIN_GROUP_REQ, JsonKit.toJsonBytes(group));
                try {
                    JoinGroupReqHandler joinGroupReqHandler = CommandManager.getCommand(Command.COMMAND_JOIN_GROUP_REQ, JoinGroupReqHandler.class);
                    joinGroupReqHandler.joinGroupNotify(groupPacket, channelContext);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean isProtocol(ChannelContext channelContext) {
        return true;
    }

    @Override
    public String name() {
        return "default";
    }
}

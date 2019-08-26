package com.ssword.imserver;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.server.command.DemoWsHandshakeProcessor;
import com.ssword.imserver.server.command.DialogueServiceProcessor;
import com.ssword.imserver.server.command.GroupServiceProcessor;
import com.ssword.imserver.server.command.UserServiceProcessor;
import com.ssword.imserver.server.handler.*;
import com.ssword.imserver.server.helper.ImRedisMessageHelper;
import com.ssword.imserver.server.listener.ImDemoGroupListener;
import com.ssword.imserver.server.service.LoginServiceProcessor;
import org.jim.common.ImConfig;
import org.jim.common.cache.redis.RedisCacheManager;
import org.jim.common.config.PropertyImConfigBuilder;
import org.jim.common.packets.Command;
import org.jim.server.ImServerStarter;
import org.jim.server.command.CommandManager;
import org.jim.server.command.handler.HandshakeReqHandler;
import org.jim.server.command.handler.LoginReqHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

import static com.ssword.imserver.server.service.LoginServiceProcessor.TOKENMAPKEY;

@SpringBootApplication
public class ImserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImserverApplication.class, args);
        try {
            initCache();
            initCommand();
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initCache() {
        RedisCacheManager.register(TOKENMAPKEY, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    // 自定义命令
    private static void initCommand() {
        Command.addAndGet(ImConst.COMMAND_GET_DIALOGUE_REQ, ImConst.COMMAND_GET_DIALOGUE_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_GET_DIALOGUE_RESP, ImConst.COMMAND_GET_DIALOGUE_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_NEW_DIALOGUE_REQ, ImConst.COMMAND_NEW_DIALOGUE_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_NEW_DIALOGUE_RESP, ImConst.COMMAND_NEW_DIALOGUE_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_NEW_GROUP_REQ, ImConst.COMMAND_NEW_GROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_NEW_GROUP_RESP, ImConst.COMMAND_NEW_GROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_DEL_GROUP_REQ, ImConst.COMMAND_DEL_GROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_DEL_GROUP_RESP, ImConst.COMMAND_DEL_GROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_UPNAME_GROUP_REQ, ImConst.COMMAND_UPNAME_GROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_UPNAME_GROUP_RESP, ImConst.COMMAND_UPNAME_GROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_ADDUSER_GROUP_REQ, ImConst.COMMAND_ADDUSER_GROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_ADDUSER_GROUP_RESP, ImConst.COMMAND_ADDUSER_GROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLUSER_GROUP_REQ, ImConst.COMMAND_DELLUSER_GROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLUSER_GROUP_RESP, ImConst.COMMAND_DELLUSER_GROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_RELOAD_REQ, ImConst.COMMAND_RELOAD_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_RELOAD_RESP, ImConst.COMMAND_RELOAD_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_RELOAD_USERGROUP_REQ, ImConst.COMMAND_RELOAD_USERGROUP_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_RELOAD_USERGROUP_RESP, ImConst.COMMAND_RELOAD_USERGROUP_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLUSER_FRIEND_REQ, ImConst.COMMAND_DELLUSER_FRIEND_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLUSER_FRIEND_RESP, ImConst.COMMAND_DELLUSER_FRIEND_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLDIO_REQ, ImConst.COMMAND_DELLDIO_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_DELLDIO_RESP, ImConst.COMMAND_DELLDIO_RESP_VAL);
        Command.addAndGet(ImConst.COMMAND_RECORD_REQ, ImConst.COMMAND_RECORD_REQ_VAL);
        Command.addAndGet(ImConst.COMMAND_RECORD_RESP, ImConst.COMMAND_RECORD_RESP_VAL);
        try {
            CommandManager.registerCommand(new DialogueHandler());// 注册 会话获取
            CommandManager.registerCommand(new NewDialogueHandler());// 注册 会话新建
            CommandManager.registerCommand(new NewGroupHandler());// 注册 新建群组
            CommandManager.registerCommand(new DelGroupHandler());// 注册 删除群组
            CommandManager.registerCommand(new UpGroupNameHandler());// 注册 更新群组名
            CommandManager.registerCommand(new GroupAddUserHandler());// 注册 群组添加用户
            CommandManager.registerCommand(new GroupDelUserHandler());// 注册 群组删除用户
            CommandManager.registerCommand(new DelUserFriendHandler());// 注册 用户删除好友
            CommandManager.registerCommand(new DelUserFriendHandler());// 注册 用户删除对话
            CommandManager.registerCommand(new DelUserDioHandler());// 注册 用户删除对话
            CommandManager.registerCommand(new ListRecordHandler());// 注册 获取聊天记录
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void init() throws IOException {
        ImConfig imConfig = new PropertyImConfigBuilder("jim.properties").build();
        imConfig.setMessageHelper(new ImRedisMessageHelper(imConfig));
        //初始化SSL;(开启SSL之前,你要保证你有SSL证书哦...)
//        initSsl(imConfig);
        //设置群组监听器，非必须，根据需要自己选择性实现;
        imConfig.setImGroupListener(new ImDemoGroupListener());
        ImServerStarter imServerStarter = new ImServerStarter(imConfig);
        /*****************start 以下处理器根据业务需要自行添加与扩展，每个Command都可以添加扩展,此处为demo中处理**********************************/
        HandshakeReqHandler handshakeReqHandler = CommandManager.getCommand(Command.COMMAND_HANDSHAKE_REQ, HandshakeReqHandler.class);
        //添加自定义握手处理器;
        handshakeReqHandler.addProcessor(new DemoWsHandshakeProcessor());
        LoginReqHandler loginReqHandler = CommandManager.getCommand(Command.COMMAND_LOGIN_REQ, LoginReqHandler.class);
        //添加登录业务处理器;
        loginReqHandler.addProcessor(new LoginServiceProcessor());
        // 添加对话处理器
        DialogueHandler dialogueHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_GET_DIALOGUE_REQ), DialogueHandler.class);
        dialogueHandler.addProcessor(new DialogueServiceProcessor());
        NewDialogueHandler newDialogueHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_NEW_DIALOGUE_REQ), NewDialogueHandler.class);
        newDialogueHandler.addProcessor(new DialogueServiceProcessor());
        DelUserDioHandler delUserDioHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_DELLDIO_REQ), DelUserDioHandler.class);
        delUserDioHandler.addProcessor(new DialogueServiceProcessor());
        // 添加群组处理器
        NewGroupHandler newGroupHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_NEW_GROUP_REQ), NewGroupHandler.class);
        newGroupHandler.addProcessor(new GroupServiceProcessor());
        DelGroupHandler delGroupHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_DEL_GROUP_REQ), DelGroupHandler.class);
        delGroupHandler.addProcessor(new GroupServiceProcessor());
        UpGroupNameHandler upGroupNameHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_UPNAME_GROUP_REQ), UpGroupNameHandler.class);
        upGroupNameHandler.addProcessor(new GroupServiceProcessor());
        GroupAddUserHandler groupAddUserHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_ADDUSER_GROUP_REQ), GroupAddUserHandler.class);
        groupAddUserHandler.addProcessor(new GroupServiceProcessor());
        GroupDelUserHandler groupDelUserHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_DELLUSER_GROUP_REQ), GroupDelUserHandler.class);
        groupDelUserHandler.addProcessor(new GroupServiceProcessor());
        // 添加好友处理器
        DelUserFriendHandler delUserFriendHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_DELLUSER_FRIEND_REQ), DelUserFriendHandler.class);
        delUserFriendHandler.addProcessor(new UserServiceProcessor());
        // 获取聊天记录
        ListRecordHandler listRecordHandler = CommandManager.getCommand(Command.valueOf(ImConst.COMMAND_RECORD_REQ), ListRecordHandler.class);
        listRecordHandler.addProcessor(new UserServiceProcessor());
        /*****************end *******************************************************************************************/
        imServerStarter.start();
    }


    /**
     * 开启SSL之前，你要保证你有SSL证书哦！
     * @param imConfig
     * @throws Exception
     */
//	private static void initSsl(ImConfig imConfig) throws Exception {
//		//开启SSL
//		if(ImConst.ON.equals(imConfig.getIsSSL())){
//			String keyStorePath = PropUtil.get("jim.key.store.path");
//			String keyStoreFile = keyStorePath;
//			String trustStoreFile = keyStorePath;
//			String keyStorePwd = PropUtil.get("jim.key.store.pwd");
//			if (StringUtils.isNotBlank(keyStoreFile) && StringUtils.isNotBlank(trustStoreFile)) {
//				SslConfig sslConfig = SslConfig.forServer(keyStoreFile, trustStoreFile, keyStorePwd);
//				imConfig.setSslConfig(sslConfig);
//			}
//		}
//	}
}

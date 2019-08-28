package com.ssword.imserver.constant;

public class ImConst {
    public static final String AUTH_KEY = "wo3g";
    public static final String USER_TOKEN_INFO = "usertk:";// 用来保存用户id和用户token的对应关系
    public static final String USER_GROUP_HELP_INFO = "101";// 群消息助手的用户id
    public static final String USER_FRIEND_HELP_INFO = "102";// 好友消息助手的用户id
    // 获取对话请求
    public static final String COMMAND_GET_DIALOGUE_REQ = "COMMAND_GET_DIALOGUE_REQ";
    public static final Integer COMMAND_GET_DIALOGUE_REQ_VAL = 21;
    // 获取对话响应
    public static final String COMMAND_GET_DIALOGUE_RESP = "COMMAND_GET_DIALOGUE_RESP";
    public static final Integer COMMAND_GET_DIALOGUE_RESP_VAL = 22;
    // 获取会话响应code
    public static final Integer COMMAND_GET_DIALOGUE_RESP_CODE = 20000;

    // 新建对话请求
    public static final String COMMAND_NEW_DIALOGUE_REQ = "COMMAND_NEW_DIALOGUE_REQ";
    public static final Integer COMMAND_NEW_DIALOGUE_REQ_VAL = 23;
    // 新建对话响应
    public static final String COMMAND_NEW_DIALOGUE_RESP = "COMMAND_NEW_DIALOGUE_RESP";
    public static final Integer COMMAND_NEW_DIALOGUE_RESP_VAL = 24;
    // 新建会话响应code
    public static final Integer COMMAND_NEW_DIALOGUE_RESP_CODE = 20001;

    // 新建群组请求
    public static final String COMMAND_NEW_GROUP_REQ = "COMMAND_NEW_GROUP_REQ";
    public static final Integer COMMAND_NEW_GROUP_REQ_VAL = 25;
    // 新建群组响应
    public static final String COMMAND_NEW_GROUP_RESP = "COMMAND_NEW_GROUP_RESP";
    public static final Integer COMMAND_NEW_GROUP_RESP_VAL = 26;
    // 新建群组响应code
    public static final Integer COMMAND_NEW_GROUP_RESP_CODE = 20002;

    // 删除群组请求
    public static final String COMMAND_DEL_GROUP_REQ = "COMMAND_DEL_GROUP_REQ";
    public static final Integer COMMAND_DEL_GROUP_REQ_VAL = 27;
    // 删除群组响应
    public static final String COMMAND_DEL_GROUP_RESP = "COMMAND_DEL_GROUP_RESP";
    public static final Integer COMMAND_DEL_GROUP_RESP_VAL = 28;
    // 删除群组响应code
    public static final Integer COMMAND_DEL_GROUP_RESP_CODE = 20003;

    // 群组改名请求
    public static final String COMMAND_UPNAME_GROUP_REQ = "COMMAND_UPNAME_GROUP_REQ";
    public static final Integer COMMAND_UPNAME_GROUP_REQ_VAL = 29;
    // 群组改名响应
    public static final String COMMAND_UPNAME_GROUP_RESP = "COMMAND_UPNAME_GROUP_RESP";
    public static final Integer COMMAND_UPNAME_GROUP_RESP_VAL = 30;
    // 群组改名响应code
    public static final Integer COMMAND_UPNAME_GROUP_RESP_CODE = 20004;

    // 群组添加群员请求
    public static final String COMMAND_ADDUSER_GROUP_REQ = "COMMAND_ADDUSER_GROUP_REQ";
    public static final Integer COMMAND_ADDUSER_GROUP_REQ_VAL = 31;
    // 群组添加群员响应
    public static final String COMMAND_ADDUSER_GROUP_RESP = "COMMAND_ADDUSER_GROUP_RESP";
    public static final Integer COMMAND_ADDUSER_GROUP_RESP_VAL = 32;
    // 群组添加群员响应code
    public static final Integer COMMAND_ADDUSER_GROUP_RESP_CODE = 20005;

    // 群组删除群员请求
    public static final String COMMAND_DELLUSER_GROUP_REQ = "COMMAND_DELLUSER_GROUP_REQ";
    public static final Integer COMMAND_DELLUSER_GROUP_REQ_VAL = 33;
    // 群组删除群员响应
    public static final String COMMAND_DELLUSER_GROUP_RESP = "COMMAND_DELLUSER_GROUP_RESP";
    public static final Integer COMMAND_DELLUSER_GROUP_RESP_VAL = 34;
    // 群组删除群员响应code
    public static final Integer COMMAND_DELLUSER_GROUP_RESP_CODE = 20006;

    // 刷新用户页面
    public static final String COMMAND_RELOAD_REQ = "COMMAND_RELOAD_REQ";
    public static final Integer COMMAND_RELOAD_REQ_VAL = 35;
    // 刷新用户页面响应
    public static final String COMMAND_RELOAD_RESP = "COMMAND_RELOAD_RESP";
    public static final Integer COMMAND_RELOAD_RESP_VAL = 36;
    // 刷新用户页面响应code
    public static final Integer COMMAND_RELOAD_RESP_CODE = 20007;

    // 刷新用户好友和群组信息
    public static final String COMMAND_RELOAD_USERGROUP_REQ = "COMMAND_RELOAD_USERGROUP_REQ";
    public static final Integer COMMAND_RELOAD_USERGROUP_REQ_VAL = 37;
    // 刷新用户好友和群组信息响应
    public static final String COMMAND_RELOAD_USERGROUP_RESP = "COMMAND_RELOAD_USERGROUP_RESP";
    public static final Integer COMMAND_RELOAD_USERGROUP_RESP_VAL = 38;
    // 刷新用户好友和群组信息响应code
    public static final Integer COMMAND_RELOAD_USERGROUP_RESP_CODE = 20008;

    // 删除好友信息请求
    public static final String COMMAND_DELLUSER_FRIEND_REQ = "COMMAND_DELLUSER_FRIEND_REQ";
    public static final Integer COMMAND_DELLUSER_FRIEND_REQ_VAL = 39;
    // 删除好友信息响应
    public static final String COMMAND_DELLUSER_FRIEND_RESP = "COMMAND_DELLUSER_FRIEND_RESP";
    public static final Integer COMMAND_DELLUSER_FRIEND_RESP_VAL = 40;
    // 删除好友信息响应code
    public static final Integer COMMAND_DELLUSER_FRIEND_RESP_CODE = 20009;

    // 删除对话信息请求
    public static final String COMMAND_DELLDIO_REQ = "COMMAND_DELLDIO_REQ";
    public static final Integer COMMAND_DELLDIO_REQ_VAL = 41;
    // 删除对话信息响应
    public static final String COMMAND_DELLDIO_RESP = "COMMAND_DELLDIO_RESP";
    public static final Integer COMMAND_DELLDIO_RESP_VAL = 42;
    // 删除对话信息响应code
    public static final Integer COMMAND_DELLDIO_RESP_CODE = 20010;

    // 获取聊天记录
    public static final String COMMAND_RECORD_REQ = "COMMAND_RECORD_REQ";
    public static final Integer COMMAND_RECORD_REQ_VAL = 43;
    // 获取聊天记录
    public static final String COMMAND_RECORD_RESP = "COMMAND_RECORD_RESP";
    public static final Integer COMMAND_RECORD_RESP_VAL = 44;
    // 获取聊天记录
    public static final Integer COMMAND_RECORD_RESP_CODE = 20011;

    // 获取群组或用户信息 主要用作获取群组或用户的最新名称，头像等信息
    public static final String COMMAND_GETINFO_REQ = "COMMAND_GETINFO_REQ";
    public static final Integer COMMAND_GETINFO_REQ_VAL = 45;
    public static final String COMMAND_GETINFO_RESP = "COMMAND_GETINFO_RESP";
    public static final Integer COMMAND_GETINFO_RESP_VAL = 46;
    public static final Integer COMMAND_GETINFO_RESP_CODE = 20012;
}

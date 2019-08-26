package com.ssword.imserver.service;

import com.ssword.imserver.entity.Dialogue;
import com.ssword.imserver.entity.NewFriend;
import com.ssword.imserver.entity.UserIIM;
import com.ssword.imserver.model.MsgInfoVo;
import org.jim.common.packets.ChatBody;
import org.jim.common.packets.Group;
import org.jim.common.packets.User;

import java.util.List;
import java.util.Map;

public interface UserInfoService {
    /**
     * 获取用户好友
     */
    List<Group> listPeople(String userid);

    /**
     * 获取用户群组
     */
    List<Group> listGroup(String userid);

    /**
     * 获取用户群组ids
     */
    List<String> listGroupIds(String userid);

    /**
     * 修改用户信息
     */
    User UpUserInfo(String userid);

    /**
     * 获取用户信息
     *
     * @return
     */
    User getUserInfoById(String userid);

    /**
     * 获取用户，但是没有群组和好友信息
     *
     * @param userid
     * @return
     */
    User getUserById(String userid);

    /**
     * 获取用户信息
     *
     * @return
     */
    User getUserInfoById(String loginname, String password);

    /**
     * 获取组信息
     *
     * @param groupid
     * @return
     */
    Group getGroupById(String groupid);

    /**
     * 获取组信息，但是没有好友信息
     *
     * @param groupid
     * @return
     */
    Group getGById(String groupid);

    /**
     * 获取组内好友
     */
    List<User> getGroupUserById(String groupid);

    /**
     * 获取对话信息
     */
    List<Dialogue> getDio(String userId);

    /**
     * 新建对话信息
     * 如果无则新建，有则忽略
     */
    Integer saveDio(String userId, String obid, Integer type);

    /**
     * 保存信息到mysql
     */
    void saveMsg(ChatBody chatBody);

    /**
     * 新建群组
     */
    Integer saveGroup(String userid, String name, Integer type);

    /**
     * 新建群组
     */
    Integer saveGroup(String groupid, String userid, String name, Integer type);

    /**
     * 删除群组
     */
    Integer delGroup(String ids, String userid);

    /**
     * 群组改名
     */
    Integer upGroupName(String groupid, String name);

    /**
     * 群组删除用户
     */
    Integer delGroupUser(String groupid, String ids);

    /**
     * 群组添加用户
     */
    Integer saveGroupUser(String groupid, String ids);

    /**
     * 根据手机号码搜索用户信息
     */
    List<UserIIM> listUserByCell(String cell, String userid);

    /**
     * 添加好友请求
     */
    void saveFriendRequest(String useridA, String useridB, String msg);
    /**
     * 同意好友申请
     */
    int saveFriendRequestYES(String id);
    /**
     * 拒绝好友申请
     */
    int saveFriendRequestNO(String id);
    /**
     * 获取申请列表
     */
    List<NewFriend> listNewFriendByUseridB(String userid);
    /**
     * 删除用户的好友
     */
    Integer delUserFirend(String userid, String deluserid);
    /**
     * 删除用户的对话
     */
    Integer delUserDio(String userid, String delobid, Integer type);

    /**
     * 获取聊天记录（获取这两个人的聊天记录）
     * @param aid 发起 获取聊天记录请求 的人
     * @param bid
     * @param page 1开始
     * @param num
     * @return
     */
    Map listMsgInfoVo(String aid, String bid, Integer page, Integer num);
}

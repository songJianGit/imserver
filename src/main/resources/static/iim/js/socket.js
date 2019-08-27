var socket;
var userId;
var userName;
var curUser;
var curUserDio;
var curUserGroupOrFriend = [];// 用户的群组或好友信息
// 服务器配置
var serverIp = '127.0.0.1';// 服务器ip
var serverPort = '8888';// 服务器端口
// 全局信息记录
var shwogpid;// 当前显示的群组用户信息box的id,在点击群组列的时候会进行记录，点击用户列的时候清空
var shwogp_groupid;// 群id
var shwouserid;// 当前显示的用户id
var showtype;// 当前在和群组还是用户聊天
var dateFFF = "yyyy-MM-dd HH:mm:ss";// 聊天窗口打印的日期格式
function connect() {
    if (curUser) {
        alert("当前已登录,请先退出登录!");
        return;
    }
    var indexMask = layer.load(1, {
        shade: [0.1, '#fff'] //0.1透明度的白色背景
    });
    var ip = serverIp;
    var port = serverPort;
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    socket = new WebSocket("ws:" + ip + ":" + port + "?username=" + username + "&password=" + password);
    socket.onopen = function (e) {

    };
    socket.onerror = function (e) {
        console.error('异常:' + e);
    };
    socket.onclose = function (e) {
        curUser = null;
        console.warn('关闭连接...');
        window.location.reload();// 直接刷新
    };
    socket.onmessage = function (e) {
        var data = e.data;
        var dataObj = eval("(" + data + ")");//转换为json对象
        console.info(dataObj);
        if (dataObj.command == 11) {//接收到聊天响应处理;
            COMMAND_CHAT_RESP(dataObj);
        } else if (dataObj.command == 18) {//获取用户信息响应处理;
            COMMAND_GET_USER_RESP(dataObj);
        } else if (10000 == dataObj.code && dataObj.command == 12) {//聊天发送状态;
            COMMAND_CHAT_RESP_SEND_STATUS(data);
        } else if (dataObj.command == 9) {//加入群组的消息通知处理;
            COMMAND_JOIN_GROUP_NOTIFY_RESP(dataObj);
        } else if (dataObj.command == 10) {
            COMMAND_EXIT_GROUP_NOTIFY_RESP(dataObj);
        } else if (dataObj.command == 20 && dataObj.code == 10015) {
            //获取消息失败，未开启持久化处理
            //...
            console.error('获取消息失败，未开启持久化处理');
        } else if (dataObj.command == 20 && dataObj.code == 10016) {//处理离线消息;
            var msgFlag = "离线消息";
            COMMAND_GET_MESSAGE_RESP(dataObj, msgFlag);
        } else if (dataObj.command == 20 && dataObj.code == 10018) {//处理历史消息;
            var msgFlag = "历史消息";
            var msgObj = dataObj.data;
            if (msgObj) {
                COMMAND_GET_MESSAGE_RESP(dataObj, msgFlag);
            } else {//没有历史消息;
                OTHER(data);
            }
        } else if (dataObj.command == 6) {//登陆命令返回状态处理
            layer.close(indexMask);
            COMMAND_LOGIN_RESP(dataObj, data);
        } else if (dataObj.command == 24) {
            COMMAND_NEW_DIALOGUE_RESP(dataObj, data);
        } else if (dataObj.command == 22 && dataObj.code == 20000) {// 获取会话响应消息
            COMMAND_GET_DIALOGUE_RESP(dataObj, data);
        } else if (dataObj.command == 26 && dataObj.code == 20002) {// 新建群组响应
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 32 && dataObj.code == 20005) {// 群组添加群员响应
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 34 && dataObj.code == 20006) {// 群组删除群员响应
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 28 && dataObj.code == 20003) {// 删除群组响应
            COMMAND_DEL_GROUP_RESP(dataObj, data);
        } else if (dataObj.command == 30 && dataObj.code == 20004) {// 群组改名响应
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 36) {
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 38) {
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 40) {
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 42) {
            RELOAD_USER(dataObj, data);
        } else if (dataObj.command == 44) {
            COMMAND_RECORD_RESP(dataObj, data);
        } else {
            OTHER(data);
        }
    };
}

// 获取聊天记录响应
function COMMAND_RECORD_RESP(dataObj, data) {
    dataObj = dataObj.data;
    var aid = dataObj.aid;
    var bid = dataObj.bid;
    var type = dataObj.type;// type为-1时，说明有问题
    if (type == -1) {
        alert("已无更多");
        return false;
    }
    var list = dataObj.list;
    var winbox = $("#" + bid + type + " .info-show-box");
    for (var i = 0; i < list.length; i++) {
        var record = list[i];
        if (record.jfrom == userId) {
            showtowin(record.jfromname, record.jfrom, record.cdate, record.jcontent, winbox, 0);
        } else {
            showtowin_result(record.jfromname, record.cdate, record.jcontent, winbox, 0);
        }
    }
}

// 删除群组响应
function COMMAND_DEL_GROUP_RESP(dataObj, data) {
    $("#" + shwogp_groupid + "2").remove();// 删除群组后删除其聊天框
    $("#gpinfo").html("");// 删除头标
}

/**
 *
 * @param obj
 * @param type 1-好友 2-群组 3-对话
 */
function putCurUserGroupOrFriend(obj, type) {
    var bb = true;
    var id = "";
    var name = "";
    var avatar = "";
    if (type == 1) {
        for (var i = 0; i < curUserGroupOrFriend.length; i++) {
            var item = curUserGroupOrFriend[i];
            if (item.id == obj.id && item.type == type) {
                bb = false;
            }
        }
        id = obj.id;
        name = obj.nick;
        avatar = obj.avatar;
    } else if (type == 2) {
        for (var i = 0; i < curUserGroupOrFriend.length; i++) {
            var item = curUserGroupOrFriend[i];
            if (item.id == obj.group_id && item.type == type) {
                bb = false;
            }
        }
        id = obj.group_id;
        name = obj.name;
        avatar = obj.avatar;
    } else if (type == 3) {
        for (var i = 0; i < curUserGroupOrFriend.length; i++) {
            var item = curUserGroupOrFriend[i];
            if (item.id == obj.objectid && item.type == type) {
                bb = false;
            }
        }
        id = obj.objectid;
        name = obj.name;
        avatar = obj.avatar;
    }
    if (bb) {
        var iii = {
            "id": id,
            "name": name,
            "type": type,
            "avatar": avatar
        };
        curUserGroupOrFriend.push(iii);
    }
    return curUserGroupOrFriend;
}

// 新建对话响应
function COMMAND_NEW_DIALOGUE_RESP(dataObj, data) {
    dataObj = dataObj.data;
    putCurUserGroupOrFriend(dataObj, 3);
    // 在客户端的最近联系界面添加对话信息
    if (dataObj.type == 1) {
        var htm = cOneP(dataObj.objectid, dataObj.name, dataObj.avatar, dataObj.type, 1);
        $("#dioPanel").append(htm);
    } else if (dataObj.type == 2) {
        var htm = cOneP(dataObj.objectid, dataObj.name, dataObj.avatar, dataObj.type, 1);
        $("#dioPanel").append(htm);
    }
}

//登陆通知处理
function COMMAND_LOGIN_RESP(dataObj, data) {
    userId = dataObj.data.id;
    userName = dataObj.data.nick;
    $("#username-left-show").html(userName);
    if (10007 == dataObj.code) {//登陆成功;
        console.info('连接成功...');
        var userCmd = "{\"cmd\":17,\"type\":\"2\",\"userid\":\"" + userId + "\"}";
        var msgCmd = "{\"cmd\":19,\"type\":\"0\",\"userid\":\"" + userId + "\"}";
        var dioCmd = "{\"cmd\":21,\"userid\":\"" + userId + "\"}";
        socket.send(userCmd);//获取登录用户信息;
        socket.send(dioCmd);//获取用户最近对话(好友+群组);
        socket.send(msgCmd);//获取用户离线消息(好友+群组);
    } else if (10008 == dataObj.code) {//登录失败;
        OTHER(data);
    }
}

// 获取会话响应
function COMMAND_GET_DIALOGUE_RESP(dataObj, data) {
    initDioUsers(dataObj);
}

//退出群组通知;
function COMMAND_EXIT_GROUP_NOTIFY_RESP(data) {
    var onlineUserCmd = "{\"cmd\":17,\"type\":\"0\",\"userid\":\"" + userId + "\"}";
    socket.send(onlineUserCmd);//获取在线用户列表(有人退出后刷信息);
}

//加入群组的消息通知处理;
function COMMAND_JOIN_GROUP_NOTIFY_RESP(data) {
    if (userId != undefined) {
        var onlineUserCmd = "{\"cmd\":17,\"type\":\"2\",\"userid\":\"" + userId + "\"}";
        socket.send(onlineUserCmd);//获取在线用户列表;
    }
}

function RELOAD(dataObj, data) {
    disConnect();
    setTimeout(function () {
        connect();
    }, 1000);
}

function RELOAD_USER(dataObj, data) {
    if (userId != undefined) {
        var onlineUserCmd = "{\"cmd\":17,\"type\":\"2\",\"userid\":\"" + userId + "\"}";
        var dioCmd = "{\"cmd\":21,\"userid\":\"" + userId + "\"}";
        socket.send(onlineUserCmd);//获取在线用户列表;
        socket.send(dioCmd);//获取用户最近对话(好友+群组);
    }
}

//发送聊天请求发送状态处理;
function COMMAND_CHAT_RESP_SEND_STATUS(data) {
    //发送成功后的状态处理...
}

//获取用户信息响应处理;
function COMMAND_GET_USER_RESP(data) {
    var curUserOld;
    if (curUser != undefined && curUser != null) {
        curUserOld = curUser;
    }
    var user = data.data;
    curUser = user;
    putCurUserGroupOrFriendByCurUser(curUser);
    initOnlineUsers(curUserOld);// 好友信息
    initOnlineGroup(curUserOld);// 群组信息
}

//接收到聊天响应处理;
function COMMAND_CHAT_RESP(data) {
    var chatObj = data.data;
    var chatType = chatObj.chatType;// 聊天类型int类型(0:未知,1:公聊,2:私聊)
    var msgType = chatObj.msgType;// 消息类型int类型(0:text、1:image、2:voice、3:vedio、4:music、5:news)
    var type = 1;
    var from = chatObj.from;
    var obid = from;
    if (chatType == 1) {
        type = 2;
        obid = chatObj.group_id;
    } else if (chatType == 2) {
        type = 1;
    }
    var createTime = new Date(chatObj.createTime).Format(dateFFF);
    if (from == userId) return;
    var content = chatObj.content;
    openDiologue(type, obid, 1, 0);
    var winbox = $("#" + obid + type + " .info-show-box");
    var iii = getUserOrGroup(obid, type);
    showtowin_result(iii.name, createTime, content, winbox);
}

//处理用户同步+持久化消息
function COMMAND_GET_MESSAGE_RESP(data, msgFlag) {
    var msgObj = data.data;
    friendOfflineMessage(msgObj, msgFlag);
    groupOfflineMessage(msgObj, msgFlag);
}

//好友消息
function friendOfflineMessage(msgObj, msgFlag) {
    var friends = msgObj.friends;
    for (var key in friends) {
        var chatDatas = friends[key];
        for (var index in chatDatas) {
            var user_id = chatDatas[index].from;
            var createTime = new Date(chatDatas[index].createTime).Format(dateFFF);
            openDiologue(1, user_id, 1, 0);
            var winbox = $("#" + user_id + "1 .info-show-box");
            var htm = "<span color='#009ACD' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>" + user_id + "</span><span color='#DC143C' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>(好友" + msgFlag + ")</span>" + "<span color='#009ACD' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>" + createTime + "</span><br>";
            htm += "<span  style='font-size: 10px;color: #000000'>&nbsp;" + chatDatas[index].content + "</span><br>";
            winbox.append(htm);
        }
    }
}

//群组消息
function groupOfflineMessage(msgObj, msgFlag) {
    var groups = msgObj.groups;
    for (var key in groups) {
        var chatDatas = groups[key];
        for (var index in chatDatas) {
            var user_id = chatDatas[index].from;
            var group_id = chatDatas[index].group_id;
            var createTime = new Date(chatDatas[index].createTime).Format(dateFFF);
            openDiologue(2, group_id, 1, 0);
            var winbox = $("#" + group_id + "2 .info-show-box");
            var htm = "<span color='#009ACD' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>" + user_id + "</span><span color='#DC143C' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>(群聊" + msgFlag + ")</span>" + "<span style='font-size: 10px;color: #009ACD' style='font-weight: bold'>" + createTime + "</span><br>";
            htm += "<span  style='font-size: 10px;color: #000000'>&nbsp;" + chatDatas[index].content + "</span><br>";
            winbox.append(htm);
        }
    }
}

//其它信息处理;
function OTHER(data) {
    //处理数据
    console.info(data);
}

// 将用户的好友，群组等信息组合成一个vo，方便后续业务使用
function putCurUserGroupOrFriendByCurUser(curUser) {
    var groups = curUser.groups;
    if (groups != undefined) {
        for (var g = 0; g < groups.length; g++) {
            var group = groups[g];
            putCurUserGroupOrFriend(group, 2);
            var users = group.users;
            for (var u = 0; u < users.length; u++) {
                var user = users[u];
                putCurUserGroupOrFriend(user, 1);
            }
        }
    }
    var friends = curUser.friends;
    if (friends != undefined) {
        for (var g = 0; g < friends.length; g++) {
            var group = friends[g];
            var users = group.users;
            for (var u = 0; u < users.length; u++) {
                var user = users[u];
                putCurUserGroupOrFriend(user, 1);
            }
        }
    }
}

function getUserOrGroup(id, type) {
    for (var i = 0; i < curUserGroupOrFriend.length; i++) {
        var item = curUserGroupOrFriend[i];
        if (item.id == id && item.type == type) {
            return item;
        }
    }
    return "";
}

function initOnlineUsers(curUserOld) {
    var groups = curUser.friends;
    var onlineUserStr = "";
    if (groups != undefined) {
        for (var g = 0; g < groups.length; g++) {
            var group = groups[g];
            var users = group.users;
            if (curUserOld == undefined || curUserOld == null) {
                for (var u = 0; u < users.length; u++) {
                    var user = users[u];
                    onlineUserStr += cOneP(user.id, user.nick, user.avatar, 1, 0, 0);
                }
            } else {
                var u1;// 需要删除的
                var u2;// 需要添加的
                if (curUser.friends && curUserOld.friends) {
                    u1 = a_b(curUserOld.friends[0].users, curUser.friends[0].users);
                } else {
                    u1 = [];
                }
                if (curUser.friends && curUserOld.friends) {
                    u2 = a_b(curUser.friends[0].users, curUserOld.friends[0].users);
                } else {
                    if (curUser.friends) {
                        u2 = curUser.friends[0].users;
                    } else {
                        u2 = [];
                    }
                }
                for (var u = 0; u < u2.length; u++) {
                    var user = u2[u];
                    onlineUserStr += cOneP(user.id, user.nick, user.avatar, 1, 0, 0);
                }
                for (var u = 0; u < u1.length; u++) {
                    var user = u1[u];
                    $(".cOnePflag-" + user.id + "10").remove();
                }
            }
        }
    }
    if (!onlineUserStr && !curUser.friends) {
        $("#onlineUser .uop-list-box").remove();
    }
    $("#onlineUser").append(onlineUserStr);
}

function initDioUsers(dataObj) {
    var groups = dataObj.data;
    if (curUserDio == undefined) {
        curUserDio = [];
    }
    var u1 = a_b(curUserDio, groups);// 需要删除的
    var u2 = a_b(groups, curUserDio);// 需要添加的

    var onlineUserStr = "";
    for (var u = 0; u < u2.length; u++) {
        var user = u2[u];
        onlineUserStr += cOneP(user.objectid, user.name, user.avatar, user.type, 1);
    }
    for (var u = 0; u < u1.length; u++) {
        var user = u1[u];
        $(".cOnePflag-" + user.id + "10").remove();
    }

    if (!onlineUserStr && groups.length < 1) {
        $("#dioPanel .uop-list-box").remove();
    }
    $("#dioPanel").append(onlineUserStr);
    curUserDio = groups;
}

function initOnlineGroup(curUserOld) {
    var groups = curUser.groups;
    var onlineUserStr = "<button type='button' onclick='addGroup();'>新建群组</button>";
    if (groups != undefined) {
        for (var g = 0; g < groups.length; g++) {
            var group = groups[g];
            var users = group.users;
            onlineUserStr += "<div style='margin: 3px;' onclick='groupSenddInit(\"" + group.group_id + "\");'>" + group.name + "</div>";
            var groupuserhtm = "<div style='display: none;' class='init-group-hiden' id='initgroup" + group.group_id + "'>";
            var user_one = users[0];
            groupuserhtm += "<button type='button' onclick='addGroupUser(\"" + group.group_id + "\")'>添加群成员</button>";
            if (user_one.id == userId) {
                groupuserhtm += "<button type='button' onclick='delGroupUser(\"" + group.group_id + "\")'>删除群员</button>";
                groupuserhtm += "<button type='button' onclick='delGroup(\"" + group.group_id + "\")'>删除本群</button>";
            } else {
                groupuserhtm += "<button type='button' onclick='delGroupUser_me(\"" + group.group_id + "\")'>退出群聊</button>";
            }
            for (var u = 0; u < users.length; u++) {
                var user = users[u];
                groupuserhtm += cOneP(user.id, user.nick, user.avatar, 1, 0, 1);
            }
            groupuserhtm += "</div>";
            $("#group-user #initgroup" + group.group_id).remove();
            $("#group-user").append(groupuserhtm);
        }
        if (shwogpid != undefined) {
            $("#" + shwogpid).show();
        }
    }
    document.getElementById("onlinePanel").innerHTML = onlineUserStr;
}

function addGroup() {
    var person = prompt("请输入群组名称", "");
    if (person != null && person != "") {
        var newGroupCmd = "{\"cmd\":25,\"userid\":\"" + userId + "\",\"name\":\"" + person + "\",\"type\":\"2\"}";
        socket.send(newGroupCmd);
    }
}

function addGroupUser(groupid) {
    layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['270px', '400px'], //宽高
        title: '添加群员',
        content: cUserTOGroup(groupid)
    });
}

// 退出群聊
function delGroupUser_me(groupid) {
    var addGroupUserCmd = "{\"cmd\":33,\"userid\":\"" + userId + "\",\"groupid\":\"" + groupid + "\",\"ids\":\"" + userId + "\"}";
    socket.send(addGroupUserCmd);
    init_panel();
    $("#" + groupid + "2").remove();
}

// 删除群组
function delGroup(groupid) {
    var delGroupCmd = "{\"cmd\":27,\"userid\":\"" + userId + "\",\"ids\":\"" + groupid + "\"}";
    socket.send(delGroupCmd);
    init_panel();
}

// 删除群员
function delGroupUser(groupid) {
    layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['270px', '400px'], //宽高
        title: '删除群员',
        content: cUserTOGroup_del(groupid)
    });
}

function cUserTOGroup_del(groupid) {
    var htm = "<div style='overflow-y: scroll;'>";
    var groups = curUser.groups;
    for (var g = 0; g < groups.length; g++) {
        var group = groups[g];
        if (groupid != group.group_id) {
            continue;
        }
        var users = group.users;
        for (var u = 1; u < users.length; u++) {// 这里从第二个开始显示，因为第一个是群主，自己要么删别人，要么解散。
            var user = users[u];
            htm += "<div><input type='checkbox' name='delGroupUserid' data-id='" + user.id + "'/><img  src=\"" + user.avatar + "\" height=\"25px\" width=\"25px;\">&nbsp;<span style='font-size: 12px'>" + user.nick + "(" + user.id + ")</span></div>";
        }
    }
    htm += "</div>";
    htm += "<button type='button' onclick='cUserTOGroupBtn_del(\"" + groupid + "\")'>确定</button>";
    return htm;
}

function cUserTOGroupBtn_del(groupid) {
    var ids = "";
    $("input:checkbox[name=delGroupUserid]:checked").each(function () {
        var id = $(this).data("id");
        ids += id + ",";
    });
    ids = ids.substring(0, ids.length - 1);
    var addGroupUserCmd = "{\"cmd\":33,\"userid\":\"" + userId + "\",\"groupid\":\"" + groupid + "\",\"ids\":\"" + ids + "\"}";
    socket.send(addGroupUserCmd);
    layer.closeAll();
}

function cUserTOGroup(groupid) {
    var htm = "<div style='overflow-y: scroll;'>";
    var friends = curUser.friends;
    var groups = curUser.groups;
    for (var g = 0; g < friends.length; g++) {
        var group = friends[g];
        var users = group.users;
        for (var i = 0; i < groups.length; i++) {
            var ggg = groups[i];
            if (ggg.group_id == groupid) {// 拿到这个组的已有人员信息，用来限制弹出框的显示
                var ggguser = ggg.users;
                for (var u = 0; u < users.length; u++) {
                    var bbbb = true;
                    var user = users[u];
                    for (var j = 0; j < ggguser.length; j++) {
                        if (ggguser[j].id == user.id) {
                            bbbb = false;
                            break;
                        }
                    }
                    if (bbbb) {
                        htm += "<div><input type='checkbox' name='addGroupUserid' data-id='" + user.id + "'/><img  src=\"" + user.avatar + "\" height=\"25px\" width=\"25px;\">&nbsp;<span style='font-size: 12px'>" + user.nick + "(" + user.id + ")</span></div>";
                    }
                }
            }
        }
    }
    htm += "</div>";
    htm += "<button type='button' onclick='cUserTOGroupBtn(\"" + groupid + "\")'>确定</button>";
    return htm;
}

function cUserTOGroupBtn(groupid) {
    var ids = "";
    $("input:checkbox[name=addGroupUserid]:checked").each(function () {
        var id = $(this).data("id");
        ids += id + ",";
    });
    ids = ids.substring(0, ids.length - 1);
    var addGroupUserCmd = "{\"cmd\":31,\"userid\":\"" + userId + "\",\"groupid\":\"" + groupid + "\",\"ids\":\"" + ids + "\"}";
    socket.send(addGroupUserCmd);
    layer.closeAll();
}

/**
 * 创建列信息
 * @param id       用户或群组id
 * @param name     用户或群组名字
 * @param avatar   图片
 * @param type     1-用户 2-群组
 * @param diotype  是否创建列的对话标识，有这个标识的列不会触发添加对话方法。1-标识为对话列，会为其添加标识信息 0-会话标识为普通列
 * @param groupl   1-是群组里面的用户列 0-不是
 * @returns {string}
 */
function cOneP(id, name, avatar, type, diotype, groupl) {
    if (type == 2 && (avatar == '' || avatar == undefined || avatar == null)) {
        avatar = "http://101.200.151.183:8080/img/group-img.png";
    }
    if (groupl == undefined) {
        groupl = 0;
    }
    var htm = "";
    if (diotype == 1) {
        var key = "cOnP" + id + type;
        var box = $("#dioPanel #" + key);
        if (box.length < 1) {
            htm = "<div class='uop-list-box cOnePflag-" + id + type + groupl + "' id=\"" + key + "\" nick=\"" + name + "\" onclick=\"openDiologue('" + type + "','" + id + "','" + diotype + "');\"><img alt=\"" + id + "\" src=\"" + avatar + "\" height=\"25px\" width=\"25px;\" style=\"float:left\">&nbsp;<span style='font-size: 12px'>" + name + "</span><span style='font-size: 12px;float: right;margin-right: 3px;'>[<span class='uninfonum-" + id + type + "'>0</span>]</span></div>";
        }
    } else {
        htm = "<div class='uop-list-box cOnePflag-" + id + type + groupl + "' id=\"" + id + "\" nick=\"" + name + "\" onclick=\"openDiologue('" + type + "','" + id + "','" + diotype + "');\"><img alt=\"" + id + "\" src=\"" + avatar + "\" height=\"25px\" width=\"25px;\" style=\"float:left\">&nbsp;<span style='font-size: 12px'>" + name + "</span><span style='font-size: 12px;float: right;margin-right: 3px;'></span></div>";
    }
    return htm;
}

// 点击群组之后，加载群组消息窗口
function groupSenddInit(groupid) {
    openDiologue(2, groupid);
}

function disConnect() {
    socket.close();
}

function sendUser(userid) {
    if (!fux(userid)) {
        alert("发送失败");
        return false;
    }
    var toId = userid;
    var createTime = new Date().getTime();
    var content = $("#" + userid + "1 .edit-box").html();
    if (content == "")
        return;
    content = content.replace(/(["])/g, "\\\"");// 转义双引号
    var msg = "{\"from\": \"" + userId + "\",\"to\": \"" + toId + "\",\"cmd\":11,\"createTime\":" + createTime + ",\"chatType\":\"2\",\"msgType\": \"0\",\"content\": \"" + content + "\"}";
    if (!toId) {
        alert("请选择要私聊的人!");
        return;
    }
    if (toId == userId) {
        alert("无法给自己发送消息!");
        return;
    }
    socket.send(msg);
    var chatObj = eval("(" + msg + ")");
    var createTime = new Date(chatObj.createTime).Format(dateFFF);
    var winbox = $("#" + userid + "1 .info-show-box");
    showtowin(curUser.nick, curUser.id, createTime, chatObj.content, winbox);
    $("#" + userid + "1 .edit-box").html("");
    $("#" + userid + "1 .edit-box").empty();
}

function sendGroup(groupid) {
    if (!fgx(groupid)) {
        alert("发送失败");
        return false;
    }
    var createTimesss = new Date().getTime();
    var winbox = $("#" + groupid + "2 .info-show-box");
    var content = $("#" + groupid + "2 .edit-box").html();
    if (content == "")
        return;
    content = content.replace(/(["])/g, "\\\"");// 转义双引号
    var msg = "{\"from\": \"" + userId + "\",\"createTime\":" + createTimesss + ",\"cmd\":11,\"group_id\":\"" + groupid + "\",\"chatType\":\"1\",\"msgType\":\"0\",\"content\": \"" + content + "\"}";
    socket.send(msg);
    var chatObj = eval("(" + msg + ")");
    var createTime = new Date(chatObj.createTime).Format(dateFFF);
    showtowin(curUser.nick, curUser.id, createTime, chatObj.content, winbox);
    $("#" + groupid + "2 .edit-box").html("");
    $("#" + groupid + "2 .edit-box").empty();
}

// 当列表中没有这个组的时候，就无法发信息到这个组
function fgx(groupid) {
    return fuxxx(groupid, 2);
}

// 当列表中没有这个用户的时候，就无法发信息到这个组
function fux(userid) {
    return fuxxx(userid, 1);
}

function fuxxx(id, type) {
    var iii = getUserOrGroup(groupid, type);
    if (iii != undefined) {
        return true;
    } else {
        return false;
    }
}

// 将用户发送的消息直接打印到窗口
function showtowin(uname, uid, time, content, winbox, patternT) {
    var key = uname + uid + time + content + winbox.parent().attr('id');
    key = $.md5(key);
    if ($("#" + key) != undefined && $("#" + key).attr('id') == key) {
        return false;
    }
    var htm = "<p style='text-align: right' id='" + key + "'>";
    htm += "<span style='font-size: 10px;color: #228B22' style='font-weight: bold'>" + uname + " " + time + "</span><br/>";
    htm += "<span style='font-size: 10px;color: #000000'>&nbsp;" + content + "</span><br/>";
    htm += "</p>";
    if (patternT == undefined || patternT == 1) {
        winbox.append(htm);
        scrollToBottom();
    } else {
        winbox.prepend(htm);
    }
}

// 将用户接受的消息直接打印到窗口
function showtowin_result(name, createTime, content, winbox, patternT) {
    var key = name + createTime + content + winbox.parent().attr('id');
    key = $.md5(key);
    if ($("#" + key) != undefined && $("#" + key).attr('id') == key) {
        return false;
    }
    var htm = "<p style='text-align: left' id='" + key + "'>";
    htm += "<span color='#009ACD' style='font-size: 10px;color: #009ACD' style='font-weight: bold'>" + name + " " + createTime + "</span><br>";
    //处理数据
    htm += "<span  style='font-size: 10px;color: #000000'>&nbsp;" + content + "</span><br>";
    htm += "</p>";
    if (patternT == undefined || patternT == 1) {
        winbox.append(htm);
        scrollToBottom();
    } else {
        winbox.prepend(htm);
    }
}

// 初始化右侧对话面板
function init_panel() {
    $(".dialogueO").hide();// 关闭所有的对话框
    $("#group-user .init-group-hiden").hide();// 关闭群组联系人
    $("#gpinfo").html('');// 清除title信息
}

// 打开会话窗口(没有则新建)  type 1-用户  2-群组
function openDiologue(type, obid, diotype, opentype) {
    // todo 每次open聊天框时，发起一个请求，获取本对象的最新 名字和头像

    if (!(opentype != undefined && opentype == 0)) {
        init_panel();// 不是聊天响应那边来的就做init_panel
    }
    var win = $("#" + obid + type);
    if (win.length < 1) {
        var htm = '<div class="info-box dialogueO" id="' + obid + type + '">';
        htm += '<div style="font-size: 11px;color: #1E9FFF;margin: 0px auto;width: 50px; cursor: pointer" class="historyrecord" data-page="1" onclick="historyrecord(\'' + obid + '\',\'' + type + '\')">查看更多</div>';
        htm += '<div class="info-show-box"></div>';
        htm += '<div class="info-edit-box">';
        htm += '<div class="div-border-solid"><button type="button" id="edit-box-btn-' + obid + type + '" class="btn btn-sm btn-default">:)</button><button type="button" class="uploadImgtoolbtn">图片</button></div>';
        htm += '<div class="edit-box upload-img-show-box" onkeydown="keyDown(event,\'' + obid + '\',\'' + type + '\')" contenteditable="true" id="edit-box-' + obid + type + '" style="width: 460px;height: 80px"></div>';
        htm += '<button class="edit-box-sendbtn" style="float:right;margin:3px 17px 0px 0px;" type="button" id="edit-box-' + obid + type + '-sendbtn" onclick="sendMsg(\'' + type + '\',\'' + obid + '\');">发送</button>';
        htm += '</div>';
        htm += '</div>';
        $("#info-boxsss").append(htm);
        initEmoji(type, obid);// 加载表情
        initNewDio(type, obid);// 打开新的窗口的时候检查需不需要新建对话信息
        inituploadImg(type, obid);// 初始化图片按钮的点击事件
        // 初始化查看聊天信息按钮
        if (opentype != undefined && opentype == 0) {// opentype=0表示这玩意是聊天响应那边过来的
            $("#" + obid + type).hide();
            uninfohandler(obid, type);
        } else {
            showDioTitle(type, obid, diotype);// 对话title
            showInfoRecord(type, obid);
        }
    } else {
        if (opentype != undefined && opentype == 0) {// opentype=0表示这玩意是聊天响应那边过来的
            if ((showtype == 1 && obid == shwouserid) || (showtype == 2 && obid == shwogp_groupid)) {// 当前聊天对象是不是返回的消息对象
                // 当前聊天对象就是返回的聊天响应对象
                scrollToBottom();
                showInfoRecord(type, obid);
            } else {
                // 当前聊天对象不是返回的聊天响应对象
                // 给win添加未读信息数
                uninfohandler(obid, type);
            }
        } else {
            win.show();
            showDioTitle(type, obid, diotype);
            scrollToBottom();
            showInfoRecord(type, obid);
            reinfohandler(obid, type);
        }
    }
}

// 更新左侧列未读消息数和总的未读消息数(添加)
function uninfohandler(obid, type) {
    var num = $(".uninfonum-" + obid + type).html();
    num = parseInt(num) + 1;
    $(".uninfonum-" + obid + type).html(num);
    $("#info-num").text(parseInt($("#info-num").text()) + 1);
}

// 更新左侧列未读消息数和总的未读消息数(清除)
function reinfohandler(obid, type) {
    var num = $(".uninfonum-" + obid + type).html();
    num = parseInt(num);
    $(".uninfonum-" + obid + type).html(0);
    $("#info-num").text(parseInt($("#info-num").text()) - num);
}

function inituploadImg(type, obid) {
    $("#" + obid + type + " .uploadImgtoolbtn").click(function () {
        $("#files").click();
    });
}

function historyrecord(obid, type) {
    var page = $("#" + obid + type).find(".historyrecord").data("page");
    $("#" + obid + type).find(".historyrecord").data("page", parseInt(page) + 1);
    var listRecordCmd = "{\"cmd\":43,\"aid\":\"" + userId + "\",\"bid\":\"" + obid + "\",\"page\":\"" + page + "\"}";
    socket.send(listRecordCmd);
}

// 对话框的上半部分title信息显示(你能点到，说明你就能拿到这个人的信息)
// diotype 是否创建列的对话标识，有这个标识的列不会触发添加对话方法。1-标识为对话列，会为其添加标识信息 0-会话标识为普通列
function showDioTitle(type, obid, diotype) {
    var iii = getUserOrGroup(obid, type);
    var name = iii.name;
    if (type == 1) {
        var htm = "<span onclick='infos_user(\"" + obid + "\",\"" + name + "\")'>" + name + "</span>";
        if (diotype == 1) {
            htm = "<span onclick='infos_dio(\"" + obid + "\",\"" + name + "\",\"" + type + "\")'>" + name + "</span>";
        }
        $("#gpinfo").html(htm);
    } else {
        var htm = "<span onclick='rename_group(\"" + obid + "\",\"" + name + "\")'>" + name + "</span>";
        if (diotype == 1) {
            htm = "<span onclick='infos_dio(\"" + obid + "\",\"" + name + "\",\"" + type + "\")'>" + name + "</span>";
        }
        $("#gpinfo").html(htm);
    }
}

// 重命名-群组
function rename_group(groupid, name) {
    var person = prompt("群组重命名", name);
    if (person != null && person != "") {
        var addGroupUserCmd = "{\"cmd\":29,\"userid\":\"" + userId + "\",\"groupid\":\"" + groupid + "\",\"name\":\"" + person + "\"}";
        socket.send(addGroupUserCmd);
    }
}

// 个人信息弹窗显示
function infos_user(userid, name) {
    layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['270px', '400px'], //宽高
        title: '用户信息',
        content: "<div><p>" + name + "</p><div><button type='button' onclick='delmyfrienduser(\"" + userid + "\")'>删除好友</button></div></div>"
    });
}

// 个人信息弹窗显示
function infos_dio(obid, name, type) {
    layer.open({
        type: 1,
        skin: 'layui-layer-rim', //加上边框
        area: ['270px', '400px'], //宽高
        title: '对话信息',
        content: "<div><p>" + name + "</p><div><button type='button' onclick='delmyfrienduser_dio(\"" + obid + "\",\"" + type + "\")'>删除对话</button></div></div>"
    });
}

// 删除好友
function delmyfrienduser(userid) {
    var delUserCmd = "{\"cmd\":39,\"userid\":\"" + userId + "\",\"deluserid\":\"" + userid + "\"}";
    socket.send(delUserCmd);
    layer.closeAll();
    init_panel();
    $("#" + userid + "1").remove();
}

// 删除对话
function delmyfrienduser_dio(obid, type) {
    var delDioCmd = "{\"cmd\":41,\"userid\":\"" + userId + "\",\"delobid\":\"" + obid + "\",\"type\":\"" + type + "\"}";
    socket.send(delDioCmd);
    layer.closeAll();
    init_panel();
    $("#" + obid + type).remove();
}

// 记录当前用户的点击的显示信息
function showInfoRecord(type, obid) {
    showtype = type;
    if (type == 2) {
        var groupbox = $("#group-user #initgroup" + obid);
        groupbox.siblings().hide();
        groupbox.show();
        shwogpid = groupbox.attr("id");
        shwogp_groupid = obid;
    } else {
        $("#group-user .init-group-hiden").hide();
        shwogpid = undefined;
        shwouserid = obid;
    }
}

// 先检查最近联系人
// 如果最近联系人中没有这个对话信息，则为他新建一个对话信息（对话信息就是最近联系信息）
// 然后在最近联系列表里面直接添加其信息
function initNewDio(type, obid) {
    var newDioCmd = "{\"cmd\":23,\"userid\":\"" + userId + "\",\"type\":\"" + type + "\",\"obid\":\"" + obid + "\"}";
    socket.send(newDioCmd);//新建会话;
}

function sendMsg(type, obid) {
    if (type == 1) {
        sendUser(obid);
    } else if (type == 2) {
        sendGroup(obid);
    }
}

// 滚动到元素最下面
function scrollToBottom(height) {
    if ($('.info-show-box:visible') != undefined && $('.info-show-box:visible').length > 0) {
        if (height != undefined) {
            $('.info-show-box:visible').scrollTop(height);
        } else {
            $('.info-show-box:visible').scrollTop($('.info-show-box:visible')[0].scrollHeight);
        }
    }
}

function keyDown(e, id, type) {
    var ev = window.event || e;
    //13是键盘上面固定的回车键
    if (ev.keyCode == 13) {
        $("#edit-box-" + id + type + "-sendbtn").focus();
        $("#edit-box-" + id + type + "-sendbtn").click();
        setTimeout(function () {// 延时一会再获取焦点，因为不知道为什么，他有焦点的话，点击entre就会莫名的多一行div，这样也有好处，就是聊天框闪一下，很炫。
            $("#" + id + type + " .edit-box").focus();
        }, 16);
    }
}

$(function () {
    heartbeatCmd();
})

// 心跳
function heartbeatCmd() {
    setInterval(function () {
        if (!curUser) {
            // alert("demo中模拟命令需要先登录，请先登录!");
            return false;
        }
        var heartbeatCmd = "{\"cmd\":13,\"hbbyte\":\"-127\"}";
        socket.send(heartbeatCmd);
    }, 300 * 1000);
}


function initEmoji(type, obid) {
    $("#edit-box-" + obid + type).emoji({
        button: "#edit-box-btn-" + obid + type,
        showTab: false,
        animation: 'none',
        icons: [{
            name: "表情",
            path: "/iim/jQuery-emoji-master/dist/img/qq/",
            // maxNum: 91,
            maxNum: 20,
            // excludeNums: [41, 45, 54],
            file: ".gif"
        }]
    });
}

function addfriendbox() {
    layer.open({
        type: 2,
        area: ['700px', '500px'],
        content: '/newfriend.do?userid=' + userId + '&username=' + userName
    });
}

function checkfriendbox() {
    layer.open({
        type: 2,
        area: ['700px', '500px'],
        content: '/verificationfriend.do?userid=' + userId
    });
}

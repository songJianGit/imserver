package com.ssword.imserver.controller;

import com.ssword.imserver.constant.ImConst;
import com.ssword.imserver.entity.NewFriend;
import com.ssword.imserver.entity.UserIIM;
import com.ssword.imserver.model.AjaxResult;
import com.ssword.imserver.service.UserInfoService;
import com.ssword.imserver.utils.CmdUtils;
import org.apache.commons.lang3.StringUtils;
import org.jim.server.http.api.HttpApiController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class UserController extends HttpApiController {

    @Autowired
    private UserInfoService userInfoService;

    @RequestMapping("index.do")
    public String index(Model model) {
        return "index";
    }

    /**
     * 进入好友搜索页面
     *
     * @return
     */
    @RequestMapping("newfriend.do")
    public String newfriend(String userid, String username, Model model) {
        model.addAttribute("useridA", userid);
        model.addAttribute("username", username);
        return "newfriend";
    }

    /**
     * 好友搜索数据
     *
     * @return
     */
    @RequestMapping("listuserbysearch.json")
    @ResponseBody
    public AjaxResult listuserbysearch(String searchInfo, String userid) {
        if (StringUtils.isBlank(searchInfo)) {
            return AjaxResult.Fail();
        }
        AjaxResult ajaxResult = AjaxResult.OK();
        List<UserIIM> list = userInfoService.listUserByCell(searchInfo, userid);
        if (list.size() < 1) {
            return AjaxResult.Fail();
        }
        ajaxResult.setData(list);
        return ajaxResult;
    }

    /**
     * 保存好友请求
     */
    @RequestMapping("friendrequest.do")
    @ResponseBody
    public AjaxResult friendRequest(String useridA, String useridB, String msg) {
        if (StringUtils.isBlank(useridA) || StringUtils.isBlank(useridB)) {
            return AjaxResult.Fail();
        }
        userInfoService.saveFriendRequest(useridA, useridB, msg);
        CmdUtils.sendMsg(ImConst.USER_FRIEND_HELP_INFO, useridB, 0, 2, null, "新朋友");
        return AjaxResult.OK();
    }

    /**
     * 进入验证页面
     */
    @RequestMapping("verificationfriend.do")
    public String verificationfriend(String userid, Model model) {
        List<NewFriend> list = userInfoService.listNewFriendByUseridB(userid);
        model.addAttribute("list", list);
        return "verificationfriend";
    }

    /**
     * 验证页面数据
     */
//    @RequestMapping("verificationfriend.json")
//    @ResponseBody
//    public AjaxResult verificationfriendData(String userid) {
//        List<NewFriend> list = userInfoService.listNewFriendByUseridB(userid);
//        AjaxResult ajaxResult = AjaxResult.OK();
//        ajaxResult.setData(list);
//        return ajaxResult;
//    }

    /**
     * 同意或拒绝
     */
    @RequestMapping("upnewfriend.do")
    @ResponseBody
    public AjaxResult upnewfriend(String id, Integer flag) {
        if (flag == null || flag == 0) {
            userInfoService.saveFriendRequestNO(id);
        } else {
            userInfoService.saveFriendRequestYES(id);
        }
        return AjaxResult.OK();
    }
}

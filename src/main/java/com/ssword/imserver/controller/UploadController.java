package com.ssword.imserver.controller;

import com.google.common.collect.Lists;
import com.ssword.imserver.utils.UpLoadUtils;
import org.jim.server.http.api.HttpApiController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class UploadController extends HttpApiController {

    @Value("${server.port}")
    private String port;

    @Value("${upload.mode}")
    private String mode;

    // 外网访问资源域名
    @Value("${fileDomainName}")
    private String fileDomainName;

    // 外网访问资源端口
    @Value("${fileDomainNamePort}")
    private String fileDomainNamePort;

    /**
     * 文件上传
     *
     * @param files
     * @param request
     * @return
     */
    @CrossOrigin
    @RequestMapping("/upload.do")
    @ResponseBody
    public List<String> singleFileUpload(@RequestParam("files") MultipartFile[] files, HttpServletRequest request) {
        List<Map> mapList = UpLoadUtils.upload(files, "/chatimgs/");
        List<String> stringList = Lists.newArrayList();
        String path = "";
        if (!mode.equals("1")) {
            path = "http://" + fileDomainName + ":" + fileDomainNamePort;
        }
        for (Map map : mapList) {
            stringList.add(path + map.get("path"));
        }
        return stringList;
    }
}

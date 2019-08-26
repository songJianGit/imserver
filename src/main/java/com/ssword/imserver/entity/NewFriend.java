package com.ssword.imserver.entity;

import java.io.Serializable;

public class NewFriend implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String ctdt;// 创建时间
    private String userida;// 申请人
    private String useridb;// 审批人
    private String msg;// 验证消息
    private Integer sta;// 审批情况 0-拒绝 1-接受 2-初始

    private String username;// 申请人名字（后赋值）
    private String avatar;//  申请人头像（后赋值）

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCtdt() {
        return ctdt;
    }

    public void setCtdt(String ctdt) {
        this.ctdt = ctdt;
    }

    public String getUserida() {
        return userida;
    }

    public void setUserida(String userida) {
        this.userida = userida;
    }

    public String getUseridb() {
        return useridb;
    }

    public void setUseridb(String useridb) {
        this.useridb = useridb;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getSta() {
        return sta;
    }

    public void setSta(Integer sta) {
        this.sta = sta;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

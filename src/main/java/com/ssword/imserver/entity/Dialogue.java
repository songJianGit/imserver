package com.ssword.imserver.entity;

import java.io.Serializable;

/**
 * 对话
 *
 * @author songJ
 */
public class Dialogue implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;// 对话id
    private String name;// 对话名称（用户名或组名）  ,读取的时候会根据id进行获取赋值
    private String avatar;// 对话图片   ,读取的时候会根据id进行获取赋值
    private String userid;// 用户id
    private String cdate;// 对话创建时间
    private String objectid;// 对话id
    private Integer type;// 对话类型(1-好友对话2-群组对话)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }

    public String getObjectid() {
        return objectid;
    }

    public void setObjectid(String objectid) {
        this.objectid = objectid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}

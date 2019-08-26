package com.ssword.imserver.entity;

import java.io.Serializable;

/**
 * mysql持久化消息实体
 */
public class MsgInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;// 主键id
    private String cdate; // 该条数据的创建时间
    private String jfrom;// 来源ID   本条消息是谁创建的
    private String jto;// 目标ID   本条消息要发给谁
    private Integer jcmd;// 命令码(11)int类型
    private Long jcreatetime;// 消息创建时间long类型
    private Integer jmsgtype;// 消息类型int类型(0:text、1:image、2:voice、3:vedio、4:music、5:news)
    private Integer jchattype;// 聊天类型int类型(0:未知,1:公聊,2:私聊)
    private String jgroupid;// 群组id仅在chatType为(1)时需要,String类型
    private String jcontent;// 内容
    private String jextras;// 扩展字段,JSON对象格式如：{'扩展字段名称':'扩展字段value'}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCdate() {
        return cdate;
    }

    public void setCdate(String cdate) {
        this.cdate = cdate;
    }

    public String getJfrom() {
        return jfrom;
    }

    public void setJfrom(String jfrom) {
        this.jfrom = jfrom;
    }

    public String getJto() {
        return jto;
    }

    public void setJto(String jto) {
        this.jto = jto;
    }

    public Integer getJcmd() {
        return jcmd;
    }

    public void setJcmd(Integer jcmd) {
        this.jcmd = jcmd;
    }

    public Long getJcreatetime() {
        return jcreatetime;
    }

    public void setJcreatetime(Long jcreatetime) {
        this.jcreatetime = jcreatetime;
    }

    public Integer getJmsgtype() {
        return jmsgtype;
    }

    public void setJmsgtype(Integer jmsgtype) {
        this.jmsgtype = jmsgtype;
    }

    public Integer getJchattype() {
        return jchattype;
    }

    public void setJchattype(Integer jchattype) {
        this.jchattype = jchattype;
    }

    public String getJgroupid() {
        return jgroupid;
    }

    public void setJgroupid(String jgroupid) {
        this.jgroupid = jgroupid;
    }

    public String getJcontent() {
        return jcontent;
    }

    public void setJcontent(String jcontent) {
        this.jcontent = jcontent;
    }

    public String getJextras() {
        return jextras;
    }

    public void setJextras(String jextras) {
        this.jextras = jextras;
    }
}

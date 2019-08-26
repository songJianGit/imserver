package com.ssword.imserver.model;

import java.io.Serializable;

/**
 * mysql持久化消息实体的显示vo对象
 */
public class MsgInfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;// 主键id
    private String cdate; // 该条数据的创建时间
    private String jfrom;// 来源ID
    private String jto;// 目标ID
    private String jfromname;
    private String jtoname;
    private String jcontent;

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

    public String getJfromname() {
        return jfromname;
    }

    public void setJfromname(String jfromname) {
        this.jfromname = jfromname;
    }

    public String getJtoname() {
        return jtoname;
    }

    public void setJtoname(String jtoname) {
        this.jtoname = jtoname;
    }

    public String getJcontent() {
        return jcontent;
    }

    public void setJcontent(String jcontent) {
        this.jcontent = jcontent;
    }
}

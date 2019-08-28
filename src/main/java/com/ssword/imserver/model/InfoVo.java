package com.ssword.imserver.model;

import java.io.Serializable;

public class InfoVo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String avatar;

    public InfoVo(String id, String name, String avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

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
}

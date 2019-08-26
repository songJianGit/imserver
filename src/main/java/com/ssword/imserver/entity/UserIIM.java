package com.ssword.imserver.entity;

import org.jim.common.packets.User;

public class UserIIM extends User {
    private String cellnumber;// 手机号码

    public String getCellnumber() {
        return cellnumber;
    }

    public void setCellnumber(String cellnumber) {
        this.cellnumber = cellnumber;
    }
}

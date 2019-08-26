package com.ssword.imserver.model;

public class AjaxResult<T> {
    private static final String MSGOK = "操作成功";
    private static final String MSGFAIL = "操作失败";
    /**
     * 请求状态 1-成功 0-失败
     */
    private Integer result;
    /**
     * 描述
     */
    private String msg;
    /**
     * 返回的数据
     */
    private T data;

    public AjaxResult() {
        this.result = 1;
        this.msg = MSGOK;
    }

    public static AjaxResult OK() {
        AjaxResult r = new AjaxResult();
        r.setResult(1);
        return r;
    }

    public static AjaxResult OK(String msg) {
        AjaxResult r = new AjaxResult();
        r.setResult(1);
        r.setMsg(msg);
        return r;
    }

    public static AjaxResult Fail() {
        AjaxResult r = new AjaxResult();
        r.setResult(0);
        r.setMsg(MSGFAIL);
        return r;
    }

    public static AjaxResult Fail(String msg) {
        AjaxResult r = new AjaxResult();
        r.setResult(0);
        r.setMsg(msg);
        return r;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}

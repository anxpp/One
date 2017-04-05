package com.anxpp.one.utils;


/**
 * 通用响应消息构造类
 * Created by anxpp.com on 2017/4/2.
 */
public class Response<T> {

    public final static Boolean SUCCESS = true;
    public final static Boolean FAIL = false;
    private Boolean state = false;
    private String msg;
    private T data;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
package com.anxpp.one.utils;

/**
 * 全局信息
 * Created by anxpp.com on 2017/3/22.
 */
public interface Global {

    //用户ID
    String CURRENT_USER_ID = "visitor";
    //URL
    String URL_BASE = "http://anxpp.com:7654";
    String URL_BASE_DEBUG = "http://10.0.2.2:7654";
    String URL_HOME_ARTICLE_BASE = URL_BASE+"/api/one/home/get/article/";
    String URL_USER_REGISTER = URL_BASE_DEBUG+"/one/user/temp/register";
}

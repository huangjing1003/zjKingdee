package com.kingdee.eas.custom.utils;

/**
 * 常量类
 * @author hj
 */
public class Constants {

    /**
     * 调用企业微信接口超时时间
     */
    public static final int TIME_OUT = 5000;
    /**
     * 调用企业微信接口返回成功code
     */
    public static final int WX_CODE =0;
    /**
     * 调用企业微信接口返回的code
     */
    public static final String ERRCODE ="errcode";
    /**
     * 调用企业微信接口返回的msg
     */
    public static final String ERRMSG = "errmsg";

    /**
     * 获取企业微信access_token的url
     */
    public static final String QYWX_ACCESS_TOKEN_URL =
            "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s";
    /**
     * 获取企业微信打卡记录的url
     */
    public static final String  QYWX_PUNCHCARDRECORD_URL="https://qyapi.weixin.qq.com/cgi-bin/checkin/getcheckindata?access_token=%s";

}

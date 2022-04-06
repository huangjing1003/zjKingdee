package com.kingdee.eas.custom.resp;

/**
 * 调用获取token的返回结果
 * @author ASUS
 */
public class WxTokenResp {

    private  int  errcode;
    private  String  errmsg;
    private  String  access_token;
    private  int  expires_in;


    public int getErrcode() {
        return errcode;
    }

    public void setErrcode(int errcode) {
        this.errcode = errcode;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }
}
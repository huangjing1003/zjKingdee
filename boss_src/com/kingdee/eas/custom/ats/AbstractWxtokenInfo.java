package com.kingdee.eas.custom.ats;

import java.io.Serializable;
import com.kingdee.bos.dao.AbstractObjectValue;
import java.util.Locale;
import com.kingdee.util.TypeConversionUtils;
import com.kingdee.bos.util.BOSObjectType;


public class AbstractWxtokenInfo extends com.kingdee.eas.framework.DataBaseInfo implements Serializable 
{
    public AbstractWxtokenInfo()
    {
        this("id");
    }
    protected AbstractWxtokenInfo(String pkField)
    {
        super(pkField);
    }
    /**
     * Object:微信token记录表's 微信打卡tokenproperty 
     */
    public String getToken()
    {
        return getString("token");
    }
    public void setToken(String item)
    {
        setString("token", item);
    }
    public BOSObjectType getBOSType()
    {
        return new BOSObjectType("22EA8B85");
    }
}
package com.kingdee.eas.custom.ats;

import com.kingdee.bos.framework.ejb.EJBRemoteException;
import com.kingdee.bos.util.BOSObjectType;
import java.rmi.RemoteException;
import com.kingdee.bos.framework.AbstractBizCtrl;
import com.kingdee.bos.orm.template.ORMObject;

import com.kingdee.eas.custom.ats.app.*;
import com.kingdee.bos.Context;
import com.kingdee.bos.BOSException;
import java.lang.String;
import com.kingdee.bos.framework.*;
import com.kingdee.bos.util.*;

public class SynCardFacade extends AbstractBizCtrl implements ISynCardFacade
{
    public SynCardFacade()
    {
        super();
        registerInterface(ISynCardFacade.class, this);
    }
    public SynCardFacade(Context ctx)
    {
        super(ctx);
        registerInterface(ISynCardFacade.class, this);
    }
    public BOSObjectType getType()
    {
        return new BOSObjectType("049A3ABF");
    }
    private SynCardFacadeController getController() throws BOSException
    {
        return (SynCardFacadeController)getBizController();
    }
    /**
     *同步打卡数据-User defined method
     *@param days 前沿天数
     *@param userNumber 用户登录账号
     */
    public void synData(int days, String userNumber) throws BOSException
    {
        try {
            getController().synData(getContext(), days, userNumber);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
}
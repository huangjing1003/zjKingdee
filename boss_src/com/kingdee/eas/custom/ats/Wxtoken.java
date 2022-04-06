package com.kingdee.eas.custom.ats;

import com.kingdee.bos.framework.ejb.EJBRemoteException;
import com.kingdee.bos.util.BOSObjectType;
import java.rmi.RemoteException;
import com.kingdee.bos.framework.AbstractBizCtrl;
import com.kingdee.bos.orm.template.ORMObject;

import com.kingdee.eas.custom.ats.app.*;
import com.kingdee.bos.BOSException;
import com.kingdee.bos.dao.IObjectPK;
import java.lang.String;
import com.kingdee.bos.framework.*;
import com.kingdee.bos.Context;
import com.kingdee.bos.metadata.entity.EntityViewInfo;
import com.kingdee.eas.framework.DataBase;
import com.kingdee.eas.framework.CoreBaseCollection;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.framework.IDataBase;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.bos.util.*;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;

public class Wxtoken extends DataBase implements IWxtoken
{
    public Wxtoken()
    {
        super();
        registerInterface(IWxtoken.class, this);
    }
    public Wxtoken(Context ctx)
    {
        super(ctx);
        registerInterface(IWxtoken.class, this);
    }
    public BOSObjectType getType()
    {
        return new BOSObjectType("22EA8B85");
    }
    private WxtokenController getController() throws BOSException
    {
        return (WxtokenController)getBizController();
    }
    /**
     *取值-System defined method
     *@param pk 取值
     *@return
     */
    public WxtokenInfo getWxtokenInfo(IObjectPK pk) throws BOSException, EASBizException
    {
        try {
            return getController().getWxtokenInfo(getContext(), pk);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
    /**
     *取值-System defined method
     *@param pk 取值
     *@param selector 取值
     *@return
     */
    public WxtokenInfo getWxtokenInfo(IObjectPK pk, SelectorItemCollection selector) throws BOSException, EASBizException
    {
        try {
            return getController().getWxtokenInfo(getContext(), pk, selector);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
    /**
     *取值-System defined method
     *@param oql 取值
     *@return
     */
    public WxtokenInfo getWxtokenInfo(String oql) throws BOSException, EASBizException
    {
        try {
            return getController().getWxtokenInfo(getContext(), oql);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
    /**
     *取集合-System defined method
     *@return
     */
    public WxtokenCollection getWxtokenCollection() throws BOSException
    {
        try {
            return getController().getWxtokenCollection(getContext());
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
    /**
     *取集合-System defined method
     *@param view 取集合
     *@return
     */
    public WxtokenCollection getWxtokenCollection(EntityViewInfo view) throws BOSException
    {
        try {
            return getController().getWxtokenCollection(getContext(), view);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
    /**
     *取集合-System defined method
     *@param oql 取集合
     *@return
     */
    public WxtokenCollection getWxtokenCollection(String oql) throws BOSException
    {
        try {
            return getController().getWxtokenCollection(getContext(), oql);
        }
        catch(RemoteException err) {
            throw new EJBRemoteException(err);
        }
    }
}
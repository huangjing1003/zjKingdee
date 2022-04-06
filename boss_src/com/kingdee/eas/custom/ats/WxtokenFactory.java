package com.kingdee.eas.custom.ats;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.BOSObjectFactory;
import com.kingdee.bos.util.BOSObjectType;
import com.kingdee.bos.Context;

public class WxtokenFactory
{
    private WxtokenFactory()
    {
    }
    public static IWxtoken getRemoteInstance() throws BOSException
    {
        return (IWxtoken)BOSObjectFactory.createRemoteBOSObject(new BOSObjectType("22EA8B85") , IWxtoken.class);
    }
    
    public static IWxtoken getRemoteInstanceWithObjectContext(Context objectCtx) throws BOSException
    {
        return (IWxtoken)BOSObjectFactory.createRemoteBOSObjectWithObjectContext(new BOSObjectType("22EA8B85") , IWxtoken.class, objectCtx);
    }
    public static IWxtoken getLocalInstance(Context ctx) throws BOSException
    {
        return (IWxtoken)BOSObjectFactory.createBOSObject(ctx, new BOSObjectType("22EA8B85"));
    }
    public static IWxtoken getLocalInstance(String sessionID) throws BOSException
    {
        return (IWxtoken)BOSObjectFactory.createBOSObject(sessionID, new BOSObjectType("22EA8B85"));
    }
}
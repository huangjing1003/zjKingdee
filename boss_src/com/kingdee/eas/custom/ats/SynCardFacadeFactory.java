package com.kingdee.eas.custom.ats;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.BOSObjectFactory;
import com.kingdee.bos.util.BOSObjectType;
import com.kingdee.bos.Context;

public class SynCardFacadeFactory
{
    private SynCardFacadeFactory()
    {
    }
    public static ISynCardFacade getRemoteInstance() throws BOSException
    {
        return (ISynCardFacade)BOSObjectFactory.createRemoteBOSObject(new BOSObjectType("049A3ABF") , ISynCardFacade.class);
    }
    
    public static ISynCardFacade getRemoteInstanceWithObjectContext(Context objectCtx) throws BOSException
    {
        return (ISynCardFacade)BOSObjectFactory.createRemoteBOSObjectWithObjectContext(new BOSObjectType("049A3ABF") , ISynCardFacade.class, objectCtx);
    }
    public static ISynCardFacade getLocalInstance(Context ctx) throws BOSException
    {
        return (ISynCardFacade)BOSObjectFactory.createBOSObject(ctx, new BOSObjectType("049A3ABF"));
    }
    public static ISynCardFacade getLocalInstance(String sessionID) throws BOSException
    {
        return (ISynCardFacade)BOSObjectFactory.createBOSObject(sessionID, new BOSObjectType("049A3ABF"));
    }
}
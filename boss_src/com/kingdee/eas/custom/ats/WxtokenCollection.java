package com.kingdee.eas.custom.ats;

import com.kingdee.bos.dao.AbstractObjectCollection;
import com.kingdee.bos.dao.IObjectPK;

public class WxtokenCollection extends AbstractObjectCollection 
{
    public WxtokenCollection()
    {
        super(WxtokenInfo.class);
    }
    public boolean add(WxtokenInfo item)
    {
        return addObject(item);
    }
    public boolean addCollection(WxtokenCollection item)
    {
        return addObjectCollection(item);
    }
    public boolean remove(WxtokenInfo item)
    {
        return removeObject(item);
    }
    public WxtokenInfo get(int index)
    {
        return(WxtokenInfo)getObject(index);
    }
    public WxtokenInfo get(Object key)
    {
        return(WxtokenInfo)getObject(key);
    }
    public void set(int index, WxtokenInfo item)
    {
        setObject(index, item);
    }
    public boolean contains(WxtokenInfo item)
    {
        return containsObject(item);
    }
    public boolean contains(Object key)
    {
        return containsKey(key);
    }
    public int indexOf(WxtokenInfo item)
    {
        return super.indexOf(item);
    }
}
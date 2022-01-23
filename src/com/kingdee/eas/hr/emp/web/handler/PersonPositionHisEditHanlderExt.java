package com.kingdee.eas.hr.emp.web.handler;

import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.IObjectPK;
import com.kingdee.bos.dao.IObjectValue;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.emp.PersonPositionFactory;
import com.kingdee.eas.hr.emp.PersonPositionHisInfo;
import com.kingdee.eas.hr.emp.PersonPositionInfo;
import com.kingdee.eas.hr.emp.PersonPositionOptFacadeFactory;
import com.kingdee.eas.hr.emp.app.util.SHREmpOptPersonPositionUtilExt;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.context.SHRContext;

/**
 * 名称：职业信息历史多行表编辑
 * 模型：com.kingdee.eas.hr.emp.app.PersonPositionHis
 * uipk:com.kingdee.eas.hr.emp.app.PersonPositionHisEdit.form
 * @author  hj
 */
public class PersonPositionHisEditHanlderExt extends PersonPositionHisEditHanlder {
	
   protected IObjectPK runSaveData(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo model)
	        throws Exception
	    {
	   IObjectPK objectPK = super.runSaveData(request, response, model);
       IObjectPK pk = null;
       PersonPositionHisInfo hisInfo = (PersonPositionHisInfo)model;
       String sql = (new StringBuilder()).append("select FHistoryRelateID from T_HR_PersonPositionHis where fid ='").append(hisInfo.getId().toString()).append("'").toString();
       Context ctx = SHRContext.getInstance().getContext();
       IRowSet rs = DbUtil.executeQuery(ctx, sql);
       String historyRelateId = "";
       if(rs.next())
           historyRelateId = rs.getString(1);
       pk = new ObjectUuidPK(historyRelateId);
       PersonPositionInfo personPositionInfo = PersonPositionFactory.getRemoteInstance().getPersonPositionInfo(pk);
       Enumeration e = hisInfo.keys();
       SelectorItemCollection selectors = new SelectorItemCollection();
       do
       {
           if(!e.hasMoreElements())
               break;
           String key = (String)e.nextElement();
           if(!"id".equalsIgnoreCase(key) && !"historyRelateID".equalsIgnoreCase(key) && !"joinDate".equalsIgnoreCase(key))
           {
               personPositionInfo.put(key, hisInfo.get(key));
               selectors.add(key);
           }
       } while(true);
       /***
        * 调用更新职业信息中的工作年限字段
        */
       updateWorkTime(ctx, personPositionInfo, selectors);
       return objectPK;	   
	 }
   
   /**
    * 更新计算工作年限的方法
    * @param ctx
    * @param ppInfo
    * @param selector
    */
   public void updateWorkTime(Context ctx, IObjectValue ppInfo, SelectorItemCollection selector) {
       PersonPositionInfo personPositionInfo = (PersonPositionInfo)ppInfo;
       if(selector == null || selector.containsKey("joindate"))
           personPositionInfo.put("checkjoinDate", Boolean.valueOf(true));
       else
           personPositionInfo.put("checkjoinDate", Boolean.valueOf(false));
       Date lastUpdateTime = new Date();
       Date endDate = personPositionInfo.getLeftCompanyDate();
       if(endDate == null)
           endDate = lastUpdateTime;
       try {
		personPositionInfo.setJoinCompanyYears(SHREmpOptPersonPositionUtilExt.calWorkTimeYears(ctx, personPositionInfo.getJoinDate(), endDate, personPositionInfo.getAdjustCoValue()));
		personPositionInfo.setJoinGroupYears(SHREmpOptPersonPositionUtilExt.calWorkTimeYears(ctx, personPositionInfo.getJoinGroupDate(), lastUpdateTime, personPositionInfo.getAdjustGroupVal()));
		 PersonPositionOptFacadeFactory.getRemoteInstance().updatePartialWithHis(personPositionInfo, selector);
		 
	} catch (EASBizException e) {
		e.printStackTrace();
	} catch (BOSException e) {
		e.printStackTrace();
	}
   }
  
}

package com.kingdee.shr.ats.web.handler.F7;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.metadata.entity.EntityViewInfo;
import com.kingdee.bos.metadata.entity.FilterInfo;
import com.kingdee.bos.metadata.entity.FilterItemInfo;
import com.kingdee.bos.metadata.query.util.CompareType;
import com.kingdee.eas.hr.ats.util.AtsDateUtils;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.ats.bill.util.BillBizUtil;
import com.kingdee.shr.ats.web.filter.AtsItemsOfFastFilter;
import com.kingdee.shr.ats.web.util.AtsPermUtil;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;

/**
 * 选择考勤档案的员工F7过滤
 * @author  hj
 */
public class AttendanceFileListF7ListHandlerExt extends AttendanceFileListF7ListHandler {
	
	private static Logger logger = Logger.getLogger(AttendanceFileListF7ListHandlerExt.class);
	
	   protected EntityViewInfo getDefaultEntityViewInfo(HttpServletRequest request, HttpServletResponse response)
		        throws SHRWebException
		    {
		        Context ctx = SHRContext.getInstance().getContext();
		        String personId = BillBizUtil.getPersonId();
		        EntityViewInfo evi = new EntityViewInfo();
		        FilterInfo domainFilterInfo = getDomainFilter(request);
		        FilterInfo advanceFilter = getAdvanceFilter(request);
		        FilterInfo filterInfo = new FilterInfo();
		        try
		        {
		            filterInfo.mergeFilter(domainFilterInfo, "AND");
		            filterInfo.mergeFilter(advanceFilter, "AND");
		        }
		        catch(BOSException e)
		        {
		            throw new SHRWebException(e.getMessage(), e);
		        }
		        evi.setFilter(filterInfo);
		        //展示考勤档案过滤条件
	            FilterInfo isShowAttendanceFileFilterInfo = new FilterInfo();
                try {
					isShowAttendanceFileFilterInfo.mergeFilter(getIsShowAttendanceHisFilter(), "AND");
					evi.getFilter().mergeFilter(isShowAttendanceFileFilterInfo, "AND");
					
				} catch (BOSException e) {
					e.printStackTrace();
				}
				logger.error((new StringBuilder()).append(" AttendanceFileListF7ListHandler getDefaultEntityViewInfo : ").append(evi.toString()).toString());
		        return evi;
		    }
	   
	   
	    protected FilterInfo getGroupControlFilter(HttpServletRequest request, int isDefaultManage, Set hrOrgIDSet)
	            throws SHRWebException
	        {
	            FilterInfo groupControlFilter = new FilterInfo();
	            if(isDefaultManage == 1)
	                groupControlFilter = AtsPermUtil.getGroupControlFilterIncludeTreeForNameF7(request, super.getTreeDomainFilter(request), "hrOrgUnit.id", "adminUnit.id", "attAdminOrgUnit.id", "attendanceFileHIS.isDefaultManage", hrOrgIDSet, Boolean.valueOf(true));
	            else
	                groupControlFilter = AtsPermUtil.getGroupControlFilterIncludeTreeForNameF7(request, super.getTreeDomainFilter(request), "hrOrgUnit.id", "adminUnit.id", "attAdminOrgUnit.id", "attendanceFileHIS.isDefaultManage", hrOrgIDSet, null);
	            return groupControlFilter;
	        }
	    
	    
	    
	    /**
	     * 获取所有的hr业务组织
	     * @param ctx
	     * @param hrOrgIDSet
	     * @throws BOSException
	     * @throws SQLException
	     */
	    public void getHRO(Context ctx, Set hrOrgIDSet ) throws BOSException, SQLException {
	    	String querySQL = "SELECT fid FROM T_ORG_HRO ";
	    	 IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
	    	 if(iRowSet.next()); {
	    		 String fid = iRowSet.getString("fid");
	    		 hrOrgIDSet.add(fid);
	    	 }
	    }

}

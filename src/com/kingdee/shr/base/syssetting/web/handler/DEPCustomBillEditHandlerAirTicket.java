package com.kingdee.shr.base.syssetting.web.handler;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.util.BOSObjectType;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.util.StringUtils;

/**
 * 模型：com.kingdee.shr.custom.app.AirTicket
 * 名称：机票_表单
 * uipk:com.kingdee.shr.custom.app.AirTicket.form
 * @author  hj
 */
public class DEPCustomBillEditHandlerAirTicket extends  DEPCustomBillEditHandler{
	
	/**
	 * 根据当前单据的id查询对应的来源单据id，返回来源单据id并且返回对应的uipk
	 * @param request
	 * @param response
	 * @param modelMap
	 * @throws BOSException 
	 * @throws SHRWebException 
	 */
	public void getSourceBillAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws BOSException, SHRWebException {
		String billId = request.getParameter("billId");
		Context ctx = SHRContext.getInstance().getContext();
		if(StringUtils.isEmpty(billId)) {
			throw new ShrWebBizException("当前单据状态不支持查看来源单据信息!!");
		}
		String querySql = "select FSOURCEBILLID from CT_CUS_AirTicket where fid = '"+billId+"'";
		IRowSet iRowSet = DbUtil.executeQuery(ctx, querySql);
		String bosType ="";
		HashMap<String, String> resultMap = null;
		try {
			if(iRowSet.next()) {
				String FSOURCEBILLID = iRowSet.getString("FSOURCEBILLID")==null?"":iRowSet.getString("FSOURCEBILLID");
				if(StringUtils.isEmpty(FSOURCEBILLID)) {
					throw new ShrWebBizException("当前单据未关联其他来源单据信息，无法查看!!");
				}else {
					  resultMap = new HashMap<String,String>();
					  bosType = BOSUuid.read(FSOURCEBILLID).getType().toString();
					  resultMap.put("bosType", bosType);
					  resultMap.put("sourceBillId", FSOURCEBILLID);
 				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
		JSONUtils.SUCCESS(resultMap);
	}
	
	/**
	 * 根据人员id获取人员的信息
	 * @param request
	 * @param response
	 * @param modelMap
	 * @throws SHRWebException 
	 */
	public void getPersonInfoAction(HttpServletRequest request, 
			HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
		String personId = request.getParameter("personId");
        Context ctx = SHRContext.getInstance().getContext();
        try {
        	/**2021-01-06
        	 * 调用获取人员的员工编码、
        	 * 护照姓名拼音、部门/项目、
        	 * 职位、成本部门、职员类别、
        	 * 中国手机号码、阿国手机号码、
        	 * 公司邮箱地址
        	 */
        	Map<String, Object> map = PersonUtil.getPersonBasicInfo(ctx, personId);
        	System.out.println("=======获取到的人员信息is:"+map);
        	JSONUtils.SUCCESS(map);
		} catch (BOSException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}  catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
	}
	
	
	
}

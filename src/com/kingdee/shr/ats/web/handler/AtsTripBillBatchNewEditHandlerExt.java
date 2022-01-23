package com.kingdee.shr.ats.web.handler;

import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.ats.AtsTripBillInfo;
import com.kingdee.eas.hr.emp.PersonPositionInfo;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.ats.web.util.SHRBillUtil;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;

/**
 * 名称：批量出差单据
 * 模型：
 * uipk:
 * @author  hj
 */
public class AtsTripBillBatchNewEditHandlerExt  extends AtsTripBillBatchNewEditHandler{
	
	/**
	 * 点击创建多人单据时，给考勤业务组织和出差信息赋值
	 * 当组织的组织类型为项目板块时取他的项目，部门板块时取他的部门
	 */
	@Override
	protected void afterCreateNewModel(HttpServletRequest request, 
			HttpServletResponse  response, CoreBaseInfo coreBaseInfo)
			throws SHRWebException {
		super.afterCreateNewModel(request, response, coreBaseInfo);
		AtsTripBillInfo atsTripBillInfo = (AtsTripBillInfo)coreBaseInfo;//出差单的主单据信息
        PersonInfo personInfo = SHRBillUtil.getCurrPersonInfo();        //获取当前的登录人
        PersonPositionInfo personPositionInfo = SHRBillUtil.getAdminOrgUnit(personInfo.getId().toString());
        if(personInfo==null) {
			throw new ShrWebBizException("当前用户未关联职员，不能发起出差单!!");
        }
        Context ctx = SHRContext.getInstance().getContext();//上下文环境
        String personId = personInfo.getId().toString();    //当前登录人员的id
        /**
         * 获取人员的基本信息字段
         */
	   Map<String, Object> map;
		try {
			map = PersonUtil.getPersonInfo(personId, ctx);
			if(map!=null && map.size()>0) {
				Object ctsIdObj = map.get("ctsId");
				Object ctsName = map.get("ctsName");
				Object nCellObj = map.get("nCell");
				Object officePhoneObj = map.get("officePhone");
				System.out.println("=======ctsId is:"+ctsIdObj);
				String ctsId = ObjectUtils.toString(ctsIdObj, "");
				if(!StringUtils.isBlank(ctsId)) {//不为空时再赋值
					BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
					customInfo.setId(BOSUuid.read(ctsId)); //设置成本部门id
					customInfo.setName(ctsName.toString());//设置成本部门名称
					atsTripBillInfo.put("costCenter", customInfo);
					atsTripBillInfo.put("addrInfo", nCellObj);//联系方式
				}
			}
		} catch (EASBizException e) {
			e.printStackTrace();
		} catch (BOSException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存和提交前的校验，当出差交通票购买方式为公司前方购买和改签休假机票时校验需要填写行程信息
	 */
	@Override
	protected void verifyModel(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo model)
			throws SHRWebException {
		super.verifyModel(request, response, model);
		Context ctx = SHRContext.getInstance().getContext();
		//TripTicketType
		AtsTripBillInfo atsTripBillInfo = (AtsTripBillInfo)model;//出差单的主单据信息
		System.out.println("========atsTripBillInfoi is:"+atsTripBillInfo);
		Object TripTicketTypeObj = atsTripBillInfo.get("TripTicketType");
		   System.out.println("========TripTicketTypeObj is:"+TripTicketTypeObj);
		if(TripTicketTypeObj!=null) {
			   BaseItemCustomInfo customInfo = (BaseItemCustomInfo)TripTicketTypeObj;
			   String  tripTicketTypeId = customInfo.getId().toString();
			   String querySQL = "SELECT FNAME_L2 name,FNUMBER number  FROM CT_CUS_TripTicketType where fid='"+tripTicketTypeId+"'";
			   System.out.println("========querySQL is:"+querySQL);
			   try {
				IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
				if(iRowSet.next()) {
					String name = iRowSet.getString("name");
					String number = iRowSet.getString("number");
					if(number.contains("02") || number.contains("05") ) {
						Object ticketObj = atsTripBillInfo.get("ticket");
						if(ticketObj==null) {
							throw new ShrWebBizException("出差交通票购买方式为【"+name+"】,请填写行程信息!!");
						}
					}
				}
			} catch (BOSException e) {
				e.printStackTrace();
				throw new ShrWebBizException(e.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new ShrWebBizException(e.getMessage());
			}
		}
	}
	
	
	
	
	
	
	
	

}

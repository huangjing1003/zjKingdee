package com.kingdee.shr.ats.web.handler;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.ModelMap;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSObjectType;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.org.PositionFactory;
import com.kingdee.eas.basedata.org.PositionInfo;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisCollection;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisFactory;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisInfo;
import com.kingdee.eas.hr.ats.AtsLeaveBillEntryCollection;
import com.kingdee.eas.hr.ats.AtsLeaveBillEntryInfo;
import com.kingdee.eas.hr.ats.AtsLeaveBillInfo;
import com.kingdee.shr.ats.web.util.NumberCodeRule;
import com.kingdee.shr.ats.web.util.SHRBillUtil;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.util.DateTimeUtils;

/**
 * 名称：请假单-ATS-请假单批量新增form(专员)
 * 模型:com.kingdee.eas.hr.ats.app.AtsLeaveBill
 * uipk:com.kingdee.eas.hr.ats.app.AtsLeaveBillAllBatchForm
 * @author  hj
 */
public class AtsLeaveBillBatchEditHandlerExt  extends AtsLeaveBillBatchEditHandler {
 
	/**
	 * 点击创建按钮后赋值
	 */
	@Override
	protected void afterCreateNewModel(HttpServletRequest request, HttpServletResponse response,
			CoreBaseInfo coreBaseInfo) throws SHRWebException {
		Context ctx = SHRContext.getInstance().getContext();
        AtsLeaveBillInfo atsLeaveBillInfo = (AtsLeaveBillInfo)coreBaseInfo;
        Date nowDate = DateTimeUtils.truncateDate(new Date());
        atsLeaveBillInfo.setApplyDate(nowDate);
        PersonInfo personInfo1 = SHRBillUtil.getCurrPersonInfo();
        if(personInfo1==null) {
			throw new ShrWebBizException("当前用户未关联职员，不能发起请假单!!");
        }
        String personId = personInfo1.getId().toString();
        atsLeaveBillInfo.setProposer(personInfo1);
        AtsHolidayFileHisInfo info = null;
        AtsHolidayFileHisCollection fileHisColl;
		try {
			fileHisColl = AtsHolidayFileHisFactory.getLocalInstance(ctx).
					getAtsHolidayFileHisCollection((new StringBuilder()).
							append("select *,adminOrgUnit.*,hrOrgUnit.*,proposer.number,proposer.name,proposer.id  where proposer.id='")
							.append(personId).append("' order by effdt desc").toString());
			System.out.println("===========fileHisColl is:"+fileHisColl);
			if(fileHisColl != null && fileHisColl.size() > 0)
			{
				info = fileHisColl.get(0);
				PersonInfo  personInfo = new PersonInfo();
				personInfo.setId(info.getProposer().getId());
				personInfo.setName(info.getProposer().getName());
				personInfo.setNumber(info.getProposer().getNumber());
				atsLeaveBillInfo.setAdminOrg(info.getAdminOrgUnit());
				atsLeaveBillInfo.put("person", personInfo);
				atsLeaveBillInfo.setHrOrgUnit(info.getHrOrgUnit());
			}
            atsLeaveBillInfo.setBizDate(nowDate);
//            BOSUuid uid = BOSUuid.create(new BOSObjectType("A0F39678"));
//            atsLeaveBillInfo.setId(uid);
            AtsLeaveBillInfo leaveBill = new AtsLeaveBillInfo();
            boolean hasCodingRule = NumberCodeRule.hasCodingRule(leaveBill, NumberCodeRule.getMainOrgByCu());
            if(hasCodingRule)
            {
                Map initData = (Map)request.getAttribute("view_initData");
                initData.put("leaveBillNumberFieldCanEdit", Boolean.valueOf(false));
            }
            AtsLeaveBillEntryInfo entryInfo = new AtsLeaveBillEntryInfo();
            if(info != null)
            {
            	PositionInfo positionInfo = info.getPosition();
            	if(positionInfo!=null) {
            		String positionId = positionInfo.getId().toString();
            		positionInfo = PositionFactory.getLocalInstance(ctx).getPositionInfo(new ObjectUuidPK(positionId));
            		entryInfo.setPosition(positionInfo);
            	}
                entryInfo.setAdminOrgUnit(info.getAdminOrgUnit());
            }
            /**
             * 获取人员的基本信息字段
             */
 		   Map<String, Object> map = PersonUtil.getPersonInfo(personId, ctx);
		   if(map!=null && map.size()>0) {
			   Object ctsIdObj = map.get("ctsId");
			   Object ctsName = map.get("ctsName");
			   Object fullNamePingYinObj = map.get("fullNamePingYin");
			   Object nCellObj = map.get("nCell");
			   Object officePhoneObj = map.get("officePhone");
			   Object emailObj = map.get("email");
			   System.out.println("=======ctsId is:"+ctsIdObj);
		       String ctsId = ObjectUtils.toString(ctsIdObj, "");
			   if(!StringUtils.isBlank(ctsId)) {//不为空时再赋值
				   BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
				   customInfo.setId(BOSUuid.read(ctsId)); //设置成本部门id
				   customInfo.setName(ctsName.toString());//设置成本部门名称
				   atsLeaveBillInfo.put("costCntr", customInfo);
			   }
			   //护照全拼不为空则设置值
			   if(fullNamePingYinObj!=null) {
				   String fullNamePing = fullNamePingYinObj.toString();
				   personInfo1.setFullNamePingYin(fullNamePing);//护照全拼
			   }
			   /**
			    * 设置电子邮件
			    */
			   if(emailObj!=null) {
				   String email = emailObj.toString();
				   personInfo1.setEmail(email);
			   }
			   /**
			    * 设置officePhone
			    */
			   if(officePhoneObj!=null) {
				   String officePhone = officePhoneObj.toString();
				   personInfo1.setOfficePhone(officePhone);
			   }
			   /**
			    * 设置nCell
			    */
			   personInfo1.put("ncell", nCellObj);
		   }
            entryInfo.setPerson(personInfo1);
            atsLeaveBillInfo.getEntries().add(entryInfo);
		} catch (BOSException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		} catch (EASBizException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
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
        	Map<String, Object> map = PersonUtil.getPersonInfo(personId, ctx);
        	System.out.println("=======获取到的人员信息is:"+map);
        	JSONUtils.SUCCESS(map);
		} catch (BOSException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		} catch (EASBizException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
	}
	
}

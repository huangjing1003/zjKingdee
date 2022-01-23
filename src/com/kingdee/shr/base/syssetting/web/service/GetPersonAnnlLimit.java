package com.kingdee.shr.base.syssetting.web.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.bsf.service.app.IHRMsfService;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.DateUtil;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisCollection;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisFactory;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisInfo;
import com.kingdee.eas.hr.emp.IPersonPosition;
import com.kingdee.eas.hr.emp.PersonPositionFactory;
import com.kingdee.eas.hr.emp.PersonPositionInfo;

/**
 * 名称：取人员的主要任职组织和机票额度起始日期，结束日期，年个人机票额度的计算
 * 2021-02-07
 * @author  hj
 */
public class GetPersonAnnlLimit   implements IHRMsfService{
    
	
	public Object process(Context ctx, Map param) throws EASBizException, BOSException {
        String personID = (String)param.get("personID");//人员id
        String annualSrdLmt = (String)param.get("annualSrdLmt");//年标准额度
        HashMap<String,Object> resultMap = new HashMap<String, Object>();
		/**
		 *  2021-01-06 hj
		 * 查询人员的组织和职位信息
		 */
        AtsHolidayFileHisInfo info = null;
		AtsHolidayFileHisCollection fileHisColl = AtsHolidayFileHisFactory.getLocalInstance(ctx).
				getAtsHolidayFileHisCollection
				((new StringBuilder()).append
						("select *,adminOrgUnit.*,hrOrgUnit.*,proposer.number,proposer.id,"
								+ "proposer.name where proposer.id='").append(personID )
						.append("' order by effdt desc").toString());
        if(fileHisColl != null && fileHisColl.size() > 0) {
            info = fileHisColl.get(0);//获取最新的一条假期档案数据
        	PersonInfo proposer = info.getProposer();
        	AdminOrgUnitInfo adminOrgUnit = info.getAdminOrgUnit();
        	resultMap.put("adminOrg", adminOrgUnit);
        	resultMap.put("person", proposer);
        	String startDateStr="";//开始日期
        	String endDateStr =""; //结束日期
        	Double psnAnnlLimit = 0.0; //个人机票额度
        	Calendar cal = Calendar.getInstance(); 
        	String year = String.valueOf(cal.get(Calendar.YEAR));//当前年份值
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	endDateStr = year+"-12-31";
        	/**
        	 * 查询人员的最近一次来阿日期
        	 */
        	BOSUuid personId = proposer.getId();
        	IPersonPosition iPersonPosition = PersonPositionFactory.getLocalInstance(ctx);
        	PersonPositionInfo personPositionInfo = iPersonPosition.getPersonPositionInfo(" where person = '"+personId+"'");
        	Date joinDate = personPositionInfo.getJoinDate();//获取到的最近一次来阿日期
        	//与当前年的-01-01比较，若大于则额度开始日期则取最近一次来阿日期  
        	//如果"最近一次来阿日期"早于1月1日之前,取1月1日,否则取"最近一次来阿日期"
        	if(joinDate==null) {
        		startDateStr = year+"-01-01";
        	}else {
        		//取到当前年的1月1日
        		String currentStr = year+"-01-01";
        		try {
					Date date = sdf.parse(currentStr);
					long time1 = joinDate.getTime();//最近一次来阿日期
					long time2 = date.getTime();	//当前年的1月1日
					if(time1<time2) {
						startDateStr = currentStr;
					}else {
						startDateStr = sdf.format(joinDate);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
        	}
        	resultMap.put("startDate", startDateStr);
        	resultMap.put("endDate", endDateStr);
        	/**
        	 * 4.个人年机票额度=(年标准额度/12)*月份取整（额度结束日期-额度起始日期）+(年标准额度/12)*当月天数/当月自然日天数
				举例:额度起始日期是2020年3月15日,额度结束日期是2020年6月15日,
				那么计算规则是 :(年标准额度/12)*2+ (年标准额度/12)*(31-15)/31+(年标准额度/12)*(30-15)/30
        	 */
        	//计算额度开始日期和结束日期的月份取整
        	try {
        		psnAnnlLimit = DateUtil.calcPersonAnnlLimit(sdf, annualSrdLmt, startDateStr, endDateStr);
	        	resultMap.put("psnAnnlLimit", psnAnnlLimit);
			} catch (ParseException e) {
				e.printStackTrace();
			}
        }
        System.out.println("========OSF===resultMap is:"+resultMap);
		return resultMap;
	}
}

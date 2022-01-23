package com.kingdee.shr.base.syssetting.web.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.bsf.service.app.IHRMsfService;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.DateUtil;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisCollection;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisFactory;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisInfo;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.util.StringUtils;

/**
 * 取人员的主要任职组织和机票额度起始日期，结束日期，年个人机票额度(从机票预算中取得后计算),
 * 年个人机票金额(取机票台账中的机票金额总和),年个人机票款清算金额
 * @author  hj
 *
 */
public class GetPersonTicketClearout implements IHRMsfService{

	public Object process(Context ctx, Map param) throws EASBizException, BOSException {
        String personID = (String)param.get("personID");//人员id
        String clrtDate = (String)param.get("clrtDate");//机票清算日期
        String mnthSrdLmt = (String)param.get("mnthSrdLmt");//年额度
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
        }
        String startDateStr="";//开始日期
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Date parse;
		try {
			parse = sdf.parse(clrtDate);
			Calendar cal = Calendar.getInstance(); 
			cal.setTime(parse);
			int year = cal.get(Calendar.YEAR);        
			//查询该人员的机票额度开始日期
			StringBuffer subBuffer = new StringBuffer();
			subBuffer.append("select bnsLmtEntry.CFSTARTDATE startDate, bnsLmtEntry.CFENDDATE endDate,");
			subBuffer.append(" bnsLmtEntry.CFPSNANNLLIMIT psnannllimit ");
			subBuffer.append(" from CT_HR_AirticketBnsLmt  bnsLmt  ");
			subBuffer.append(" left join CT_HR_AirticketBnsLmtEntry bnsLmtEntry   on bnsLmtEntry.FBILLID=bnsLmt.fid ");
			subBuffer.append(" where bnsLmtEntry.FPERSONID='"+personID+"' and  year(bnsLmtEntry.CFSTARTDATE) = "+year);
			subBuffer.append(" order by bnsLmtEntry.CFSTARTDATE desc");
			IRowSet rowSet = DbUtil.executeQuery(ctx, subBuffer.toString());
			if(rowSet.next()) {
				String startDate = rowSet.getString("startDate");
				if(!StringUtils.isEmpty(startDate)) {
					startDateStr = sdf.format(sdf.parse(startDate));
				}
	        	resultMap.put("startDate", startDateStr);//设置额度开始时间
	            resultMap.put("endDate", clrtDate);//额度结束日期
	            //根据开始日期和结束日期计算年个人机票额度
	            LocalDate beginDate = LocalDate.parse(startDateStr);
	            LocalDate endDate = LocalDate.parse(clrtDate);
	            BigDecimal budget = BigDecimal.valueOf(Double.parseDouble(mnthSrdLmt));
                BigDecimal psnAnnlLimit = DateUtil.calcAmountBetweenTwoDates(beginDate, endDate, budget).setScale(0, 4).divide(BigDecimal.TEN, 0, 4).multiply(BigDecimal.TEN);
	            //计算年个人机票金额           
	            BigDecimal annlTcktAmt = new BigDecimal(0);
	            StringBuffer priceBuffer =  new StringBuffer();
	            priceBuffer.append("select sum(airTicket.CFTICKETPRICE) price from CT_CUS_AirTicket airTicket ");    
	            priceBuffer.append("left join CT_CUS_TicketType  ticketType on ticketType.fid = airTicket.CFTICKETTYPEID "); 
	            priceBuffer.append("where airTicket.CFPERSONID='"+personID+"' and ticketType.CFSFKC=1"); 
	            priceBuffer.append(" and year(airTicket.FbizDate)= "+year); 
	            IRowSet iRowSet = DbUtil.executeQuery(ctx, priceBuffer.toString());
	            if(iRowSet.next()) {
	            	annlTcktAmt = iRowSet.getBigDecimal("price")==null? new BigDecimal(0):iRowSet.getBigDecimal("price");
	            	 
	            }
	            //计算年个人机票款清算金额
	            BigDecimal annlClrtAmt = psnAnnlLimit.subtract(annlTcktAmt);
	            resultMap.put("psnAnnlLimit", psnAnnlLimit);//设置年个人机票额度
	            resultMap.put("annlTcktAmt", annlTcktAmt);//设置年个人机票金额
	            resultMap.put("annlClrtAmt", annlClrtAmt);//设置年个人机票款清算金额
			}
			
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultMap;
	}

}

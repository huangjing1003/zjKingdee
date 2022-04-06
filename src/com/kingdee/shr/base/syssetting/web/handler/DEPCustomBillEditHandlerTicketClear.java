package com.kingdee.shr.base.syssetting.web.handler;

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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.eas.custom.utils.DateUtil;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.shr.rpts.ctrlreport.osf.OSFExecutor;

/**
 * 机票清算
 * uipk:com.kingdee.eas.hr.affair.app.TcktClearout.form
 * @author  hj
 *
 */
public class DEPCustomBillEditHandlerTicketClear extends DEPCustomBillEditHandler {
	
	/**
	 * 根据人员id获取人员的信息
	 * @param request
	 * @param response
	 * @param modelMap
	 * @throws SHRWebException 
	 * @throws ParseException 
	 */
	public void getPersonInfoAction(HttpServletRequest request, 
			HttpServletResponse response, ModelMap modelMap) throws SHRWebException, ParseException {
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
        	JSONUtils.SUCCESS(map);
		} catch (BOSException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}  catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
	}
	
	
	/**
	 * 计算两个日期之间的人员-个人年度机票额度
	 * @param request
	 * @param response
	 * @param modelMap
	 * @throws SHRWebException 
	 * @throws ParseException 
	 */
	public void calcLimitAction(HttpServletRequest request, 
			HttpServletResponse response, ModelMap modelMap) throws SHRWebException, ParseException {
		Context ctx = SHRContext.getInstance().getContext();
		String mnthSrdLmt = request.getParameter("annualSrdLmt");//标准额度
		String startDateStr = request.getParameter("startDateStr");//开始日期
		String clrtDate = request.getParameter("endDateStr");//结束日期
		String personID = request.getParameter("personId");//人员id
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        HashMap<String,Object> resultMap = new HashMap<String, Object>();
        try {
	        //格式化结束日期
	        Date parse = sdf.parse(clrtDate);
			Calendar cal = Calendar.getInstance(); 
			cal.setTime(parse);
			//获取年份
			int year = cal.get(Calendar.YEAR);  
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
	        priceBuffer.append("where airTicket.CFPERSONID='"+personID+"' and  ticketType.CFSFKC=1"); 
	        priceBuffer.append(" and year(airTicket.FbizDate)= "+year); 
	        IRowSet iRowSet;
				iRowSet = DbUtil.executeQuery(ctx, priceBuffer.toString());
	        if(iRowSet.next()) {
	        	annlTcktAmt = iRowSet.getBigDecimal("price")==null? new BigDecimal(0):iRowSet.getBigDecimal("price");
	        }
	        //计算年个人机票款清算金额
            BigDecimal annlClrtAmt = psnAnnlLimit.subtract(annlTcktAmt);
            resultMap.put("psnAnnlLimit", psnAnnlLimit);//设置年个人机票额度
            resultMap.put("annlTcktAmt", annlTcktAmt);//设置年个人机票金额
            resultMap.put("annlClrtAmt", annlClrtAmt);//设置年个人机票款清算金额
            JSONUtils.SUCCESS(resultMap);
		} catch (BOSException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
	}

}

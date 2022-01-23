package com.kingdee.shr.base.syssetting.web.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.kingdee.eas.custom.utils.DateUtil;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;

/**
 * 计算两个日期之间的人员-个人年度机票额度
 * @author hj
 *
 */
public class DepCustomBillBnsLmtEditHandler extends DEPCustomBillEditHandler{
	
	/**
	 * 计算两个日期之间的人员-个人年度机票额度
	 * @param request
	 * @param response
	 * @param modelMap
	 * @throws SHRWebException 
	 */
	public void calcLimitAction(HttpServletRequest request, 
			HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
		String annualSrdLmt = request.getParameter("annualSrdLmt");//标准额度
		String startDateStr = request.getParameter("startDateStr");//开始日期
		String endDateStr = request.getParameter("endDateStr");//结束日期
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	double	psnAnnlLimit = 0;
		try {
			psnAnnlLimit = DateUtil.calcPersonAnnlLimit(sdf, annualSrdLmt, startDateStr, endDateStr);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ShrWebBizException(e.getMessage());
		}
		JSONUtils.SUCCESS(psnAnnlLimit);
	}
	
	public static void main(String[] args) {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String annualSrdLmt = "240000";//标准额度
		String startDateStr = "2021-04-01";//开始日期
		String endDateStr =  "2021-05-15";//结束日期
		try {
			double	psnAnnlLimit = DateUtil.calcPersonAnnlLimit(sdf, annualSrdLmt, startDateStr, endDateStr);
			System.out.println("======psnAnnlLimit is:"+psnAnnlLimit);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

}

package com.kingdee.eas.custom.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;


public class DateUtil {

	/**
	 * 获取2个日期之间相隔的月数
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 */
	public static long monthsDiff(String startDateStr,String endDateStr) {
		String text1 = "2020-08-02";
        Temporal temporal1 = LocalDate.parse(text1);
        String text2 = "2020-09-01";
        Temporal temporal2 = LocalDate.parse(text2);
        // 方法返回为相差月份
        long month = ChronoUnit.MONTHS.between(temporal1, temporal2);
		return month;
	}
	
	/**
	 * 获取某个日期的最大天数，比如2021-02-19，返回为28
	 * @param date
	 * @return
	 */
	public static int getActualMaximum(Date date) {
	   	Calendar cal = Calendar.getInstance(); 
    	cal.setTime(date);
        //获取某月最大天数
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return lastDay;
	}
	 
	/**
	 * 获取某个日期所在的天,如2021-02-19，返回的结果为19
	 * @param date
	 * @return
	 */
	public static int getOfDay(Date date) {
	   	Calendar cal = Calendar.getInstance(); 
    	cal.setTime(date);
        //获取某月最大天数
        int day = cal.get(Calendar.DAY_OF_MONTH);
		return day;
	}
	
	/**
	 * 获取两个日期之间的整数月
	 * 如2020-03-15到2020-06-15之间，整数月为2
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 * @throws ParseException
	 */
	public static int getMonthByDate(String startDateStr,String endDateStr) throws ParseException {
    	int countMonth = 0;//返回的两个日期之间的整数月
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date startDate = sdf.parse(startDateStr);
		Date endDate = sdf.parse(endDateStr);
		Calendar startCal = Calendar.getInstance(); 
		startCal.setTime(startDate);
		int startMonth = startCal.get(Calendar.MONTH)+1;
    	int startCurrentDay = startCal.get(Calendar.DAY_OF_MONTH);
    	System.out.println("===startMonth is:"+startMonth+"===startCurrentDay is:"+startCurrentDay);
		
		Calendar endCal = Calendar.getInstance(); 
		endCal.setTime(endDate);
		int endMonth = endCal.get(Calendar.MONTH)+1;
		int endlastDay = endCal.getActualMaximum(Calendar.DAY_OF_MONTH);
    	int endCurrentDay = endCal.get(Calendar.DAY_OF_MONTH);
    	System.out.println("===endMonth is:"+endMonth+" =endlastDay is:"+endlastDay+"===endCurrentDay is:"+endCurrentDay);
    	
    	
		for (int i = startMonth; i <= endMonth; i++) {
			if(i==startMonth) {//判断月为开始月，判断天数是否等于1或者当前月的结束日期
				if(startCurrentDay==1 && startMonth!=endMonth) {
					countMonth+=1;
				}
			}else if (i==endMonth) {
				if(endCurrentDay==endlastDay) {
					countMonth+=1;
				}
			}else {
				countMonth+=1;
			}
		}
		return countMonth;
	}
	
	/**
	 * 通过开始日期和结束日期计算个人机票额度
	 * @param sdf
	 * @param annualSrdLmt
	 * @param startDateStr
	 * @param endDateStr
	 * @return
	 * @throws ParseException
	 */
	public static double calcPersonAnnlLimit(SimpleDateFormat sdf ,
			String annualSrdLmt,String startDateStr,String endDateStr ) throws ParseException {
		double psnAnnlLimit = 0.0; //个人机票额度
		Date startDate = sdf.parse(startDateStr);
		Date endDate = sdf.parse(endDateStr);
		int monthsDiff = DateUtil.getMonthByDate(startDateStr, endDateStr);
		if(monthsDiff==12) {
			psnAnnlLimit = Double.valueOf(annualSrdLmt);
		} else {
			/**
			 * 获取额度起始日期的当月天数=当月自然日天数-开始日期所在的天 
			 */
			int startNaturalDay = DateUtil.getActualMaximum(startDate);//当月自然日天数
			int startDay = DateUtil.getOfDay(startDate);//开始日期所在的天数
			int currentStartDay = (startNaturalDay-startDay)+1;//额度起始日期的当月天数-日期所在的天数
			if(currentStartDay==startNaturalDay) {//判断当月自然日天数和额度起始日期的当月天数-日期所在的天数，比较是否相等，等于则为0，不再次计算
				currentStartDay=0;
			}
			/**
			 * 获取额度结束日期的当月天数=当月自然日天数-结束日期所在的天
			 */
			int endNaturalDay = DateUtil.getActualMaximum(endDate);//当月自然日天数
			int endDay = DateUtil.getOfDay(endDate);//结束日期所在的天数
			int currentEndDay = (endNaturalDay-endDay)+1;//额度结束日期的当月天数
			if(endDay!=endNaturalDay) {//结束日期所在的天不等于自然月天，当月天数等于=结束日期所在的天-月初1
				currentEndDay = (endDay-1)+1;
			}else if (endDay==endNaturalDay && startDay!=1) {
				currentEndDay = (endDay-startDay)+1;
			}else if (endDay==endNaturalDay) {
				currentEndDay = (endNaturalDay-1)+1;
			}
			//年标准额度先转化成double
			if(!org.apache.commons.lang3.StringUtils.isEmpty(annualSrdLmt)) {
				Double annualSrdLmtDouble  = Double.valueOf(annualSrdLmt);
				//判断开始日期所在的天不等于1或者结束日期所在的天不等于结束日期的最大天数，则特殊计算
				if((startDay!=1 || endDay!=endNaturalDay) && monthsDiff>0) {
					System.out.println("============monthsDiff is:"+monthsDiff);
					psnAnnlLimit = (annualSrdLmtDouble/12)*monthsDiff+(annualSrdLmtDouble/12)*currentStartDay/startNaturalDay
							+(annualSrdLmtDouble/12)*currentEndDay/endNaturalDay;
					BigDecimal   b   =   new   BigDecimal(psnAnnlLimit);
					psnAnnlLimit   =   b.setScale(0,   RoundingMode.HALF_DOWN).doubleValue();
				}else {//其他的则直接用标准额度/12*整数月
					if(monthsDiff==0) {
						psnAnnlLimit = (annualSrdLmtDouble/12)*currentEndDay/endNaturalDay;
						BigDecimal   b   =   new   BigDecimal(psnAnnlLimit);
						psnAnnlLimit   =   b.setScale(0,   RoundingMode.HALF_DOWN).doubleValue();
					}else {
						psnAnnlLimit = (annualSrdLmtDouble/12)*monthsDiff;
						BigDecimal   b   =   new   BigDecimal(psnAnnlLimit);
						psnAnnlLimit   =   b.setScale(0,   RoundingMode.HALF_DOWN).doubleValue();
					}
				}
			}
		}
		return psnAnnlLimit;
	}
	
	/**
	 * 计算2个日期之间的天数
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws ParseException 
	 */
	public static int getDaysBetween(String startDate,String endDate) throws ParseException {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();  
        cal.setTime(sdf.parse(startDate));  
        long time1 = cal.getTimeInMillis();               
        cal.setTime(sdf.parse(endDate));  
        long time2 = cal.getTimeInMillis();       
        long between_days=(time2-time1)/(1000*3600*24)+1;
		return Integer.parseInt(String.valueOf(between_days));  
	}
	
	
	public static void main(String[] args) {
		String start = "2021-03-15";
		String end = "2021-06-15";
        LocalDate beginDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        BigDecimal budget = BigDecimal.valueOf(Double.parseDouble("1"));
		BigDecimal bigDecimal = calcAmountBetweenTwoDates(beginDate, endDate, budget);
		System.out.println("======daysBetween is:"+bigDecimal);
		 
	}
	
    public static BigDecimal calcAmountBetweenTwoDates(LocalDate beginDate, LocalDate endDate, BigDecimal unitAmount)
    {
        if(beginDate.isEqual(endDate) || endDate.isBefore(beginDate))
            return BigDecimal.ZERO;
        int beginMonthDays = 0;
        int endMonthDay = 0;
        int months = 0;
        if(sameMonth(beginDate, endDate))
        {
            beginMonthDays = (endDate.getDayOfMonth() - beginDate.getDayOfMonth()) + 1;
        } else
        {
            months = endDate.getMonthValue() - beginDate.getMonthValue() - 1;
            if(months < 0)
                months = 0;
            if(beginDate.getDayOfMonth() == 1)
            {
                months++;
            } else
            {
                int totalDays = beginDate.lengthOfMonth();
                beginMonthDays = (totalDays - beginDate.getDayOfMonth()) + 1;
            }
            if(endDate.getDayOfMonth() == endDate.lengthOfMonth())
            {
                endMonthDay = 0;
                months++;
            } else
            {
                endMonthDay = endDate.getDayOfMonth();
            }
        }
        BigDecimal beginMonthAmount = getMonthUnitAmount(beginDate, unitAmount).multiply(BigDecimal.valueOf(beginMonthDays));
        BigDecimal monthsAmount = unitAmount.multiply(BigDecimal.valueOf(months));
        BigDecimal endMonthAmount = getMonthUnitAmount(endDate, unitAmount).multiply(BigDecimal.valueOf(endMonthDay));
        return beginMonthAmount.add(monthsAmount).add(endMonthAmount);
    }

    private static BigDecimal getMonthUnitAmount(LocalDate date, BigDecimal monthAmount)
    {
        int days = date.lengthOfMonth();
        return monthAmount.divide(BigDecimal.valueOf(days), 3, 4);
    }

    private static boolean sameMonth(LocalDate date1, LocalDate date2)
    {
        return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth();
    }
    
    
	
}

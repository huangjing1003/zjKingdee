package com.kingdee.csce.shr.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author xudong.yao
 * @date 2021/4/28
 */
public class CscecDateUtil {

    /**
     * 获取当年第一天的日期
     *
     * @return java.util.Date
     * @author xudong.yao
     * @date 2021/5/6 14:44
     */
    public static Date getCurrentYearFirstDate() {
        Calendar currCal=Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearFirstDate(currentYear);
    }

    /**
     * 获取当年最后一天的日期
     *
     * @return java.util.Date
     * @author xudong.yao
     * @date 2021/5/6 14:44
     */
    public static Date getCurrentYearLastDate(){
        Calendar currCal=Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return getYearLastDate(currentYear);
    }

    /**
     * 获取输入日期的第一天日期
     *
     * @param year :
     * @return java.util.Date
     * @author xudong.yao
     * @date 2021/5/6 14:45
     */
    public static Date getYearFirstDate(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取输入日期的最后一天日期
     *
     * @param year :
     * @return java.util.Date
     * @author xudong.yao
     * @date 2021/5/6 14:45
     */
    public static Date getYearLastDate(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }
    
    /**
     * 2021-12-29更改
     * 计算2个日期之间的金额数据
     * @param beginDate
     * @param endDate
     * @param unitAmount
     * @return
     */
    public static  BigDecimal calcAmountBetweenByTwoDate(LocalDate beginDate, LocalDate endDate, BigDecimal unitAmount) {
        if (beginDate.isEqual(endDate) || endDate.isBefore(beginDate)) {
            return BigDecimal.ZERO;
        }
        int beginMonthDays = 0, endMonthDay = 0, months = 0;
        if (sameMonth(beginDate, endDate)) {
            beginMonthDays = endDate.getDayOfMonth() - beginDate.getDayOfMonth() + 1;
        } else {
            months = endDate.getMonthValue() - beginDate.getMonthValue() - 1;
            if (months < 0) {
                months = 0;
            }
            if (beginDate.getDayOfMonth() == 1) {
                months++;
            } else {
                int totalDays = beginDate.lengthOfMonth();
                beginMonthDays = totalDays - beginDate.getDayOfMonth() + 1;
            }
            if (endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
                endMonthDay = 0;
                months++;
            } else {
                endMonthDay = endDate.getDayOfMonth();
            }
        }
        //月工资*取整月份(计算结束日期-计算开始日期) +月工资*(天数/当月自然天数),其中月工资取自1部分的月工资字段
        BigDecimal beginMonthAmount = divideDays(beginDate,unitAmount.multiply(BigDecimal.valueOf(beginMonthDays)));
        BigDecimal monthsAmount = unitAmount.multiply(BigDecimal.valueOf(months));
        BigDecimal endMonthAmount =divideDays(endDate,unitAmount.multiply(BigDecimal.valueOf(endMonthDay)) );
        return beginMonthAmount.add(monthsAmount).add(endMonthAmount);
    	
    }
    
    private static BigDecimal divideDays(LocalDate date, BigDecimal monthAmount)
    {
        int days = date.lengthOfMonth();
        return monthAmount.divide(BigDecimal.valueOf(days),  3, BigDecimal.ROUND_HALF_UP);
    }
    
    

    /**
     * 根据金额单元值计算两个日期之间的金额
     *
     * @param beginDate :
     * @param endDate :
     * @param unitAmount :
     * @return java.math.BigDecimal
     * @author xudong.yao
     * @date 2021/5/6 14:45
     */
    public static BigDecimal calcAmountBetweenTwoDates(LocalDate beginDate, LocalDate endDate, BigDecimal unitAmount) {
        if (beginDate.isEqual(endDate) || endDate.isBefore(beginDate)) {
            return BigDecimal.ZERO;
        }
        int beginMonthDays = 0, endMonthDay = 0, months = 0;
        if (sameMonth(beginDate, endDate)) {
            beginMonthDays = endDate.getDayOfMonth() - beginDate.getDayOfMonth() + 1;
        } else {
            months = endDate.getMonthValue() - beginDate.getMonthValue() - 1;
            if (months < 0) {
                months = 0;
            }
            if (beginDate.getDayOfMonth() == 1) {
                months++;
            } else {
                int totalDays = beginDate.lengthOfMonth();
                beginMonthDays = totalDays - beginDate.getDayOfMonth() + 1;
            }
            if (endDate.getDayOfMonth() == endDate.lengthOfMonth()) {
                endMonthDay = 0;
                months++;
            } else {
                endMonthDay = endDate.getDayOfMonth();
            }
        }
        BigDecimal beginMonthAmount = getMonthUnitAmount(beginDate, unitAmount).multiply(BigDecimal.valueOf(beginMonthDays));
        BigDecimal monthsAmount = unitAmount.multiply(BigDecimal.valueOf(months));
        BigDecimal endMonthAmount = getMonthUnitAmount(endDate, unitAmount).multiply(BigDecimal.valueOf(endMonthDay));
        return beginMonthAmount.add(monthsAmount).add(endMonthAmount);
    }

    private static BigDecimal getMonthUnitAmount(LocalDate date, BigDecimal monthAmount) {
        int days = date.lengthOfMonth();
        return monthAmount.divide(BigDecimal.valueOf(days), 3, BigDecimal.ROUND_HALF_UP);
    }

    private static boolean sameMonth(LocalDate date1, LocalDate date2) {
        return date1.getYear() == date2.getYear() && date1.getMonth() == date2.getMonth();
    }

}

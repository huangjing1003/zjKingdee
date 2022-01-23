package com.kingdee.eas.hr.emp.app.util;

import java.util.Date;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.base.util.SHRMathUtils;
import com.kingdee.util.DateTimeUtils;

/**
 * 计算职业信息中的工作年限工具类
 */
public class SHREmpOptPersonPositionUtilExt {

    public static float calWorkTimeYears(Context ctx, Date beginDate, Date endDate, float adjVal)
            throws EASBizException, BOSException
        {
            if(beginDate == null)
                return 0.0F;
            if(endDate == null)
                endDate = new Date();
            int yearDiff = DateTimeUtils.getYear(endDate) - DateTimeUtils.getYear(beginDate);
            int monthDiff = DateTimeUtils.getMonth(endDate) - DateTimeUtils.getMonth(beginDate);
            float joinCompanyYears = ((float)yearDiff + (float)monthDiff / 12F) + adjVal;
            if(joinCompanyYears < 0.0F)
                joinCompanyYears = 0.0F;
            return SHRMathUtils.getRoundValueBySysParam(ctx, joinCompanyYears);
        }	
	
}

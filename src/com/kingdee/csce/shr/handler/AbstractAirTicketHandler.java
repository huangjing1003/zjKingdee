package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.csce.shr.utils.CscecDateUtil;
import com.kingdee.csce.shr.utils.PersonUtil;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.base.EmpPostExperienceHisInfo;
import com.kingdee.eas.hr.emp.PersonPositionHisInfo;
import com.kingdee.shr.base.syssetting.web.handler.DEPCustomBillEditHandler;

import java.sql.Date;
import java.util.Map;

public class AbstractAirTicketHandler extends DEPCustomBillEditHandler {
    /**
     * 获取开始日期：
     * 最近一次来阿日期 早于1月1日之前,取1月1日,否则取 最近一次来阿日期
     *
     * @param personId :
     * @return java.sql.Date
     * @author xudong.yao
     * @date 2021/5/6 14:38
     */
    private Date getBeginDate(String personId) throws BOSException, EASBizException {
        PersonPositionHisInfo latestAlCompanyInfo = PersonUtil.getLatestAlCompanyInfo(personId);
        Date latestAlDate;
        Date beginOfYear = new Date(CscecDateUtil.getCurrentYearFirstDate().getTime());
        if (latestAlCompanyInfo == null || latestAlCompanyInfo.getDate("joinDate").before(beginOfYear)) {
            latestAlDate = beginOfYear;
        } else {
            latestAlDate = latestAlCompanyInfo.getDate("joinDate");
        }
        return latestAlDate;
    }

    protected void fillAirTicketRowBaseInfo(String personId, Map<String, Object> result) throws BOSException, EASBizException {
        EmpPostExperienceHisInfo personCurrentPosition = PersonUtil.getPersonCurrentPosition(personId);
        if (personCurrentPosition == null) {
            return;
        }
        result.put("person", personCurrentPosition.getPerson());
        result.put("adminOrg", personCurrentPosition.getAdminOrg());
        Date beginDate = getBeginDate(personId);
        result.put("startDate", beginDate);
    }
}

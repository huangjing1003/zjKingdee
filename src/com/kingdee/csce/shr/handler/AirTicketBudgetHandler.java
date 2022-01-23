package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.csce.shr.utils.CscecDateUtil;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 机票款预算handler
 *
 * @author xudong.yao
 * @date 2021/4/28
 */
public class AirTicketBudgetHandler extends AbstractAirTicketHandler {

    public void getEntryRowInfoAction(HttpServletRequest request,
                                      HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        String personId = request.getParameter("personId");
        String annualSrdLmt = request.getParameter("annualSrdLmt");
        BigDecimal budget = BigDecimal.valueOf(Double.parseDouble(annualSrdLmt));
        Map<String, Object> result = new HashMap<>(8);
        try {
            fillAirTicketRowBaseInfo(personId, result);
            if (result.containsKey("person")) {
                Date beginDate = (Date) result.get("startDate");
                Date endDate = new Date(CscecDateUtil.getCurrentYearLastDate().getTime());
                result.put("endDate", endDate);
                BigDecimal psnAnnlLimit = CscecDateUtil.calcAmountBetweenTwoDates(beginDate.toLocalDate(), endDate.toLocalDate(), budget)
                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                        .divide(BigDecimal.TEN, 0, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.TEN);
                result.put("psnAnnlLimit", psnAnnlLimit);
            }
        } catch (BOSException e) {
            e.printStackTrace();
        } catch (EASBizException e) {
            e.printStackTrace();
        }
        JSONUtils.SUCCESS(result);
    }

    public void calcLimitAction(HttpServletRequest request,
                                HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        String annualSrdLmt = request.getParameter("annualSrdLmt");
        String startDateStr = request.getParameter("startDateStr");
        String endDateStr = request.getParameter("endDateStr");
        BigDecimal psnAnnlLimit;
        try {
            LocalDate beginDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            LocalDate today = LocalDate.now();
            if (beginDate.getYear() != LocalDate.now().getYear()) {
                beginDate = LocalDate.of(today.getYear(), 1, 1);
            }
            if (endDate.getYear() != LocalDate.now().getYear()) {
                endDate = LocalDate.of(today.getYear(), 12, 31);
            }
            BigDecimal budget = BigDecimal.valueOf(Double.parseDouble(annualSrdLmt));
            psnAnnlLimit = CscecDateUtil.calcAmountBetweenTwoDates(beginDate, endDate, budget)
                    .setScale(0, BigDecimal.ROUND_HALF_UP)
                    .divide(BigDecimal.TEN, 0, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.TEN);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ShrWebBizException(e.getMessage());
        }
        JSONUtils.SUCCESS(psnAnnlLimit);
    }



}

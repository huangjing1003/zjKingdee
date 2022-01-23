package com.kingdee.csce.shr.handler;

import cn.hutool.core.util.StrUtil;
import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.csce.shr.utils.CscecDateUtil;
import com.kingdee.csce.shr.utils.CscecLocalDateUtil;
import com.kingdee.csce.shr.utils.PersonUtil;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.base.EmpPosOrgRelationInfo;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class AirTicketClearHandler extends AbstractAirTicketHandler {

    private static final BigDecimal BUDGET = BigDecimal.valueOf(20000L);

    private static final String TICKET_QUERY_FORMAT = "/*dialect*/ SELECT sum(cfticketprice) totalPrice FROM CT_CUS_AirTicket where cfpersonid='{}' and fbizdate >= to_date('{}','yyyy-mm-dd') and fbizdate <= to_date('{}','yyyy-mm-dd') and cftickettypeid in (select fid from CT_CUS_TicketType where fname_l2 in ('congé','congé-changement personnel','mission-changement personnel'))";

    public void getEntryRowInfoAction(HttpServletRequest request,
                                HttpServletResponse response,
                                ModelMap modelMap) throws SHRWebException {
        Map<String, Object> result = new HashMap<>(8);
        String personId = request.getParameter("personId");

        try {
            fillAirTicketRowBaseInfo(personId, result);
            if (result.containsKey("person")) {
                Date beginDate = (Date) result.get("startDate");
                Date endDate = this.getEndDate(personId);
                result.put("endDate", endDate);
                BigDecimal psnAnnlLimit = CscecDateUtil.calcAmountBetweenTwoDates(beginDate.toLocalDate(), endDate.toLocalDate(), BUDGET)
                        .setScale(0, BigDecimal.ROUND_HALF_UP)
                        .divide(BigDecimal.TEN, 0, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.TEN);
                result.put("psnAnnlLimit", psnAnnlLimit);
                BigDecimal totalTicketPrice = getTotalTicketPrice(personId);
                result.put("annlTcktAmt", totalTicketPrice);
                BigDecimal restBudget = psnAnnlLimit.subtract(totalTicketPrice);
                result.put("annlClrtAmt", restBudget);
            }
        } catch (BOSException e) {
            e.printStackTrace();
        } catch (EASBizException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        JSONUtils.SUCCESS(result);
    }

    private Date getEndDate(String personId) throws BOSException {
        EmpPosOrgRelationInfo latestPersonChangeInfo = PersonUtil.getLatestPersonChangeInfo(personId);
        if (latestPersonChangeInfo == null || !latestPersonChangeInfo.isIsInner() || latestPersonChangeInfo.getEmployeeType() == null || latestPersonChangeInfo.getEmployeeType().getName().equals("在岗员工")) {
            return new Date(CscecDateUtil.getCurrentYearLastDate().getTime());
        }
        java.util.Date endDateTime = latestPersonChangeInfo.getEndDateTime();
        return new Date(endDateTime.getTime());
    }

    private BigDecimal getTotalTicketPrice(String personId) throws BOSException, SQLException {
        String beginDate = CscecLocalDateUtil.getFirstDayOfCurrentYear().toString();
        String endDate = CscecLocalDateUtil.getLastDayOfCurrentYear().toString();
        String sql = StrUtil.format(TICKET_QUERY_FORMAT, personId, beginDate, endDate);
        Context ctx = SHRContext.getInstance().getContext();
        IRowSet rowSet = DbUtil.executeQuery(ctx, sql);
        BigDecimal price = BigDecimal.ZERO;
        if (rowSet.next()) {
            price = rowSet.getString("totalPrice") == null ? BigDecimal.ZERO : rowSet.getBigDecimal("totalPrice");
        }
        return price;
    }

}

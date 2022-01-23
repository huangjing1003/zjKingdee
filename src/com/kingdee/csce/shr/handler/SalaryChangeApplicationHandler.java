package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.csce.shr.bo.EmployeeSalaryChangeEntry;
import com.kingdee.csce.shr.vo.EmployeeSalarySummary;
import com.kingdee.eas.framework.CoreBaseCollection;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.emp.*;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.util.MetaDataUtil;
import com.kingdee.shr.base.syssetting.web.handler.DEPCustomBillEditHandler;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.shr.compensation.CmpItemInfo;
import com.kingdee.shr.compensation.FixAdjustSalaryCollection;
import com.kingdee.shr.compensation.FixAdjustSalaryFactory;
import com.kingdee.shr.compensation.FixAdjustSalaryInfo;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SalaryChangeApplicationHandler extends DEPCustomBillEditHandler {

    public void getCurrentSalaryInfoAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        Context ctx = SHRContext.getInstance().getContext();
        String personId = request.getParameter("personId");
        try {
            List<EmployeeSalaryChangeEntry> entries = getSalaryEntries(personId, ctx);
            EmployeeSalarySummary info = new EmployeeSalarySummary();
            Date date = null;
            for (EmployeeSalaryChangeEntry entry : entries) {
                info.populate(entry);
                if (null == date || date.before(entry.getEffectDate())) {
                    info.setEffectDate(entry.getEffectDate());
                    date = entry.getEffectDate();
                }
            }
            info.calcDynamicSalary();
            JSONUtils.SUCCESS(info);
        } catch (BOSException e) {
            e.printStackTrace();
        } catch (SHRWebException e) {
            e.printStackTrace();
        }
    }

    public void calculateSalaryInfoAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        try {
            EmployeeSalarySummary salarySummary = new EmployeeSalarySummary();
            salarySummary.setRatio(BigDecimal.valueOf(Double.parseDouble(request.getParameter("ratio"))));
            salarySummary.setAnnualReward(BigDecimal.valueOf(Double.parseDouble(request.getParameter("annualReward"))));
            salarySummary.setBaseRankSalary(BigDecimal.valueOf(Double.parseDouble(request.getParameter("baseRankSalary"))));
            salarySummary.setOverseaAllowanceCoefficient(BigDecimal.valueOf(Double.parseDouble(request.getParameter("overseaAllowanceCoefficient"))));
            salarySummary.setManagementPositionSalary(BigDecimal.valueOf(Double.parseDouble(request.getParameter("managementPositionSalary"))));
            salarySummary.setEnglishAllowance(BigDecimal.valueOf(Double.parseDouble(request.getParameter("englishAllowance"))));
            salarySummary.setFranceAllowance(BigDecimal.valueOf(Double.parseDouble(request.getParameter("franceAllowance"))));
            salarySummary.setSpecialProfessionalAllowance(BigDecimal.valueOf(Double.parseDouble(request.getParameter("specialProfessionalAllowance"))));
            salarySummary.setSpecialProfessionalAllowance(BigDecimal.valueOf(Double.parseDouble(request.getParameter("specialProfessionalAllowance"))));
            salarySummary.setSiteAllowance(BigDecimal.valueOf(Double.parseDouble(request.getParameter("siteAllowance"))));
            salarySummary.setMonthlyReward(BigDecimal.valueOf(Double.parseDouble(request.getParameter("monthlyReward"))));
            salarySummary.calcDynamicSalary();
            JSONUtils.SUCCESS(salarySummary);
        } catch (SHRWebException e) {
            e.printStackTrace();
        }
    }

    private List<EmployeeSalaryChangeEntry> getSalaryEntries(String personId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> currentSalaryEntries = this.getCurrentSalaryEntries(personId, ctx);
        List<EmployeeSalaryChangeEntry> entries = new ArrayList<>(currentSalaryEntries);

        EmployeeSalaryChangeEntry currentFranceSalary = getCurrentFranceSalary(personId, ctx);
        if (currentFranceSalary != null) {
            entries.add(currentFranceSalary);
        }

        EmployeeSalaryChangeEntry currentEnglishSalary = getCurrentEnglishSalary(personId);
        if (currentEnglishSalary != null) {
            entries.add(currentEnglishSalary);
        }

        List<EmployeeSalaryChangeEntry> currentPositionRank = getCurrentPositionRank(personId, ctx);
        entries.addAll(currentPositionRank);

        EmployeeSalaryChangeEntry currentSiteSalary = getCurrentSiteSalary(personId);
        if (currentSiteSalary != null) {
            entries.add(currentSiteSalary);
        }

        return entries;
    }

    private List<EmployeeSalaryChangeEntry> getCurrentSalaryEntries(String personId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> res = new ArrayList<>();
        String query = "select cmpItem.id, cmpItem.name, effectDay, leffectDay, money where person.id = '" + personId + "' order by cmpItem.name, effectDay desc";
        FixAdjustSalaryCollection fixAdjustSalaryCollection = FixAdjustSalaryFactory.getLocalInstance(ctx).getFixAdjustSalaryCollection(query);
        if (!fixAdjustSalaryCollection.isEmpty()) {
            String curType = "";
            for (int i = 0; i < fixAdjustSalaryCollection.size(); i++) {
                FixAdjustSalaryInfo salary = fixAdjustSalaryCollection.get(i);
                String type = ((CmpItemInfo)salary.get("cmpItem")).getString("name");
                if (curType.equals(type)) {
                    continue;
                }
                curType = type;
                EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry(salary);
                res.add(entry);
            }
        }
        return res;
    }

    private EmployeeSalaryChangeEntry getCurrentFranceSalary(String userId, Context ctx) throws BOSException {
        String query = "SELECT langBnsRank.name, startDate, endDate, person.id, person.name WHERE person.id = '" + userId + "' order by startDate desc";
        PersonLanguageCollection personLanguageCollection = PersonLanguageFactory.getLocalInstance(ctx).getPersonLanguageCollection(query);
        if (!personLanguageCollection.isEmpty()) {
            PersonLanguageInfo salary = personLanguageCollection.get(0);
            return new EmployeeSalaryChangeEntry(salary);
        }
        return null;
    }

    private EmployeeSalaryChangeEntry getCurrentEnglishSalary(String userId) throws BOSException {
        String query = "SELECT engBnsRank.name, startDate, endDate WHERE person.id = '" + userId + "' order by startDate desc";
        CoreBaseCollection englishCollection = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.EngBns").getCollection(query);
        if (!englishCollection.isEmpty()) {
            if (!englishCollection.isEmpty()) {
                CoreBaseInfo salary = englishCollection.get(0);
                return new EmployeeSalaryChangeEntry((EmpMultiInfoCustomInfo) salary);
            }
        }
        return null;
    }

    private List<EmployeeSalaryChangeEntry> getCurrentPositionRank(String userId, Context ctx) throws BOSException {
        String query = "SELECT jobGrade.name, grpJobgrade.name, mngJobGrade.name, EFFDT, LEFFDT WHERE person.id = '" + userId + "' order by EFFDT desc";
        EmpPostRankCollection postRankCollection = EmpPostRankFactory.getLocalInstance(ctx).getEmpPostRankCollection(query);
        if (!postRankCollection.isEmpty()) {
            EmpPostRankInfo salary = postRankCollection.get(0);
            return EmployeeSalaryChangeEntry.parsePositionInfo(salary);
        }
        return new ArrayList<>();
    }

    private EmployeeSalaryChangeEntry getCurrentSiteSalary(String personId) throws BOSException {
        // 获取 成本部门
        String query = "select costCntr.siteBns,isSingle, beginDate where person.id = '" + personId + "' order by beginDate desc";
        CoreBaseCollection personCostCenters = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.Costcenter").getCollection(query);
        if (personCostCenters.isEmpty()) {
            return null;
        }
        CoreBaseInfo personCurrentCostCenter = personCostCenters.get(0);
        BigDecimal siteSalaryValue = personCurrentCostCenter.getBigDecimal("costCntr.siteBns");
        if (siteSalaryValue == null) {
            siteSalaryValue = BigDecimal.ZERO;
        }
        Date beginDate = personCurrentCostCenter.getDate("beginDate");
        EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry();
        entry.setType("现场津贴");
        entry.setMoney(siteSalaryValue);
        entry.setEffectDate(beginDate);
        return entry;
    }


}

package com.kingdee.csce.shr.vo;

import com.kingdee.csce.shr.bo.EmployeeSalaryChangeEntry;
import com.kingdee.eas.framework.CoreBaseInfo;

import java.io.Serializable;
import java.math.BigDecimal;

public class EmployeeSalaryInfo extends CoreBaseInfo implements Serializable {

    public EmployeeSalaryInfo() {
        this.salarySummary = new EmployeeSalarySummary();
    }

    public EmployeeSalaryInfo(EmployeeSalaryChangeEntry entry) {
        this();
        this.salarySummary.setEffectDate(entry.getEffectDate());
        this.salarySummary.populate(entry);
    }

    public EmployeeSalaryInfo(EmployeeSalaryInfo info) {
        this();
        this.salarySummary = new EmployeeSalarySummary(info.salarySummary);
    }

    private EmployeeSalarySummary salarySummary;

    public EmployeeSalarySummary getSalarySummary() {
        return this.salarySummary;
    }

    public void populate(EmployeeSalaryChangeEntry entry) {
        this.salarySummary.populate(entry);
    }

    public void complete() {
        this.salarySummary = null;
    }

    public void calcAndUpdateValue() {
        this.salarySummary.calcDynamicSalary();
        this.updateValue();
    }

    private void updateValue() {
        values.put("baseranksalary", salarySummary.getBaseRankSalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("overseaallowancecoefficient", salarySummary.getOverseaAllowanceCoefficient().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("managementpositionsalary", salarySummary.getManagementPositionSalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("englishallowance", salarySummary.getEnglishAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("franceallowance", salarySummary.getFranceAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("specialprofessionalallowance", salarySummary.getSpecialProfessionalAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("professionalsequenceallowance", salarySummary.getProfessionalSequenceAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("transportationallowance", salarySummary.getTransportationAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("siteallowance", salarySummary.getSiteAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("monthlyreward", salarySummary.getMonthlyReward().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("ratio", salarySummary.getRatio().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("annualreward", salarySummary.getAnnualReward().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("overseaallowance", salarySummary.getOverseaAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("basemonthlysalary", salarySummary.getBaseMonthlySalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("monthlysalary", salarySummary.getMonthlySalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("baseannualsalary", salarySummary.getBaseAnnualSalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("floatingannualsalary", salarySummary.getFloatingAnnualSalary().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("annualsalarybudget", salarySummary.getAnnualSalaryBudget().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("annualallowance", salarySummary.getAnnualAllowance().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("annualincomebudget", salarySummary.getAnnualIncomeBudget().setScale(0, BigDecimal.ROUND_HALF_UP));
        values.put("managementrank", salarySummary.getManagementRank());
        values.put("positionrank", salarySummary.getPositionRank());
        values.put("innerpositionrank", salarySummary.getInnerPositionRank());
        values.put("effectdate", salarySummary.getEffectDate());
    }

}

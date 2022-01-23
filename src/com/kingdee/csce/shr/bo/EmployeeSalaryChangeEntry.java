package com.kingdee.csce.shr.bo;

import com.kingdee.bos.metadata.entity.PropertyInfo;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.emp.EmpMultiInfoCustomInfo;
import com.kingdee.eas.hr.emp.EmpPostRankInfo;
import com.kingdee.eas.hr.emp.PersonLanguageInfo;
import com.kingdee.shr.compensation.CmpItemInfo;
import com.kingdee.shr.compensation.FixAdjustSalaryInfo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EmployeeSalaryChangeEntry implements Serializable {

    public EmployeeSalaryChangeEntry() {
        this.money = BigDecimal.ZERO;
    }

    public EmployeeSalaryChangeEntry(EmployeeSalaryChangeEntry entry) {
        this.type = entry.getType();
        this.effectDate = entry.getEffectDate();
        this.inEffectDate = entry.getInEffectDate();
        this.money = entry.getMoney();
        this.formula = entry.getFormula();
        this.rank = entry.getRank();
    }

    public EmployeeSalaryChangeEntry(FixAdjustSalaryInfo info) {
        Date effectDay = info.getEffectDay();
        String itemName = ((CmpItemInfo)info.get("cmpItem")).getString("name");
        BigDecimal money = info.getBigDecimal("money");
        this.type = itemName;
        this.effectDate = effectDay;
        this.inEffectDate = info.getLeffectDay() == null ? new Date() : info.getLeffectDay();
        this.money = money;
        this.formula = Boolean.FALSE;
        this.rank = "";
    }
    
    public EmployeeSalaryChangeEntry(PersonLanguageInfo info) {
        this.effectDate = info.getDate("startDate");
        this.inEffectDate = info.getDate("endDate") == null ? new Date() : info.getDate("endDate");
        this.type = "法语津贴";
//        this.rank = info.get("langbnsrank").toString();
        this.money = info.getBigDecimal("money");
    }

    public EmployeeSalaryChangeEntry(EmpMultiInfoCustomInfo info) {
        this.effectDate = info.getDate("startDate");
        this.inEffectDate = info.getDate("endDate") == null ? new Date() : info.getDate("endDate");
        this.type = "英语津贴";
        this.money = info.getBigDecimal("money");
//        this.rank = info.get("engbnsRank").toString();
//        populateMoneyAndFormula();
    }
    
    /**
     * 2021-12-23 增加现场津贴处理
     * @param info
     */
    public EmployeeSalaryChangeEntry(PropertyInfo info) {
        this.effectDate = info.getDate("startDate");
        this.inEffectDate = info.getDate("endDate") == null ? new Date() : info.getDate("endDate");
        this.type = "现场津贴";
        this.money = info.getBigDecimal("money");
    }    
    

    public EmployeeSalaryChangeEntry(EmpPostRankInfo info) {
        this.effectDate = info.getEFFDT();
        this.inEffectDate = info.getLEFFDT() == null ? new Date() : info.getLEFFDT();
        this.type = "管理岗薪等级::职级::内部职级别";
        String v1 = null == info.getString("mngJobGrade") ? " " : info.getString("mngJobGrade");
        String v2 = null == info.getString("jobGrade") ? " " : info.getString("jobGrade");
        String v3 = null == info.getString("grpJobgrade") ? " " : info.getString("grpJobgrade");
        StringBuilder sb = new StringBuilder();
        sb.append(v1).append("::").append(v2).append("::").append(v3);
        this.rank = sb.toString();
    }
    
    public static List<EmployeeSalaryChangeEntry> parsePositionInfo(EmpPostRankInfo info) {
        List<EmployeeSalaryChangeEntry> result = new ArrayList<>();
        Object mngJobGrade = info.get("mngJobGrade");
        Object jobGrade = info.get("jobGrade");
        Object grpJobgrade = info.get("grpJobgrade");

        if (null != mngJobGrade) {
            EmployeeSalaryChangeEntry mngJobGradeEntry = new EmployeeSalaryChangeEntry();
            mngJobGradeEntry.type = "管理岗薪等级";
            mngJobGradeEntry.effectDate = info.getEFFDT();
            mngJobGradeEntry.inEffectDate = info.getLEFFDT() == null ? new Date() : info.getLEFFDT();
            mngJobGradeEntry.value = mngJobGrade;
            result.add(mngJobGradeEntry);
        }

        if (null != jobGrade) {
            EmployeeSalaryChangeEntry jobGradeEntry = new EmployeeSalaryChangeEntry();
            jobGradeEntry.type = "职级";
            jobGradeEntry.effectDate = info.getEFFDT();
            jobGradeEntry.inEffectDate = info.getLEFFDT() == null ? new Date() : info.getLEFFDT();
            jobGradeEntry.value = jobGrade;
            result.add(jobGradeEntry);
        }

        if (null != grpJobgrade) {
            EmployeeSalaryChangeEntry grpJobgradeEntry = new EmployeeSalaryChangeEntry();
            grpJobgradeEntry.type = "内部职级别";
            grpJobgradeEntry.effectDate = info.getEFFDT();
            grpJobgradeEntry.inEffectDate = info.getLEFFDT() == null ? new Date() : info.getLEFFDT();
            grpJobgradeEntry.value = grpJobgrade;
            result.add(grpJobgradeEntry);
        }

        return result;
    }

    /**
     * 类型
     */
    private String type;
    /**
     * 生效日期
     */
    private Date effectDate;
    /**
     * 失效日期
     */
    private Date inEffectDate;
    /**
     * 金额
     */
    private BigDecimal money;
    /**
     * 通过公式计算金额，仅对法语津贴生效
     */
    private Boolean formula;
    /**
     * 语言津贴级别
     * 管理岗等级::职级::内部职级
     */
    private String rank;
    /**
     * 管理岗等级/职级/内部职级
     */
    private Object value;

    public Object getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getEffectDate() {
        return effectDate;
    }

    public void setEffectDate(Date effectDate) {
        this.effectDate = effectDate;
    }

    public Date getInEffectDate() {
        return inEffectDate;
    }

    public void setInEffectDate(Date inEffectDate) {
        this.inEffectDate = inEffectDate;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Boolean getFormula() {
        return formula;
    }

    public String getRank() {
        return rank;
    }

    public void evictRank() {
        this.rank = "";
    }

    private void populateMoneyAndFormula() {
        switch (rank) {
            // 法语津贴
            case "翻译类二等":
                this.money = BigDecimal.valueOf(1000);
                this.formula = false;
                break;
            case "翻译类一等":
                this.money = BigDecimal.valueOf(2500);
                this.formula = false;
                break;
            case "普通类1级":
                this.money = BigDecimal.valueOf(400);
                this.formula = false;
                break;
            case "普通类2级":
                this.money = BigDecimal.valueOf(0.6);
                this.formula = true;
                break;
            case "普通类3级":
                this.money = BigDecimal.valueOf(0.8);
                this.formula = true;
                break;
            case "普通类4级":
                this.money = BigDecimal.valueOf(1.2);
                this.formula = true;
                break;
            case "普通类5级":
                this.money = BigDecimal.valueOf(1.4);
                this.formula = true;
                break;
            case "普通类6级":
                this.money = BigDecimal.valueOf(1.6);
                this.formula = true;
                break;
            // 英语津贴
            case "英语一等":
            case "英语一级":
                this.money = BigDecimal.valueOf(1500);
                this.formula = false;
                break;
            case "英语二等":
            case "英语二级":
                this.money = BigDecimal.valueOf(1000);
                this.formula = false;
                break;
            default:
                break;
        }
    }
}

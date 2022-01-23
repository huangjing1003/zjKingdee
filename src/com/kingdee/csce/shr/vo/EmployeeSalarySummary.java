package com.kingdee.csce.shr.vo;

import com.kingdee.csce.shr.bo.EmployeeSalaryChangeEntry;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class EmployeeSalarySummary implements Serializable {
	

    /**
     * 职级基薪
     */
    private BigDecimal baseRankSalary;
    /**
     * 海外津贴系数
     */
    private BigDecimal overseaAllowanceCoefficient;
    /**
     * 管理岗薪
     */
    private BigDecimal managementPositionSalary;
    /**
     * 英语津贴
     */
    private BigDecimal englishAllowance;
    /**
     * 法语津贴
     */
    private BigDecimal franceAllowance;
    /**
     * 现场津贴
     */
    private BigDecimal siteAllowance;
    /**
     * 特殊专业津贴
     */
    private BigDecimal specialProfessionalAllowance;
    /**
     * 专业序列津贴
     */
    private BigDecimal professionalSequenceAllowance;
    /**
     * 交通医疗电脑补助
     */
    private BigDecimal transportationAllowance;
    /**
     * 月度福利费
     */
    private BigDecimal monthlyReward;
    /**
     * 浮动比例
     */
    private BigDecimal ratio;
    /**
     * 年福利
     */
    private BigDecimal annualReward;
    /**
     * 海外津贴
     */
    private BigDecimal overseaAllowance;
    /**
     * 月基薪
     */
    private BigDecimal baseMonthlySalary;
    /**
     * 月工资
     */
    private BigDecimal monthlySalary;
    /**
     * 基本年薪
     */
    private BigDecimal baseAnnualSalary;
    /**
     * 浮动年薪
     */
    private BigDecimal floatingAnnualSalary;
    /**
     * 年薪预算
     */
    private BigDecimal annualSalaryBudget;
    /**
     * 年津补贴
     */
    private BigDecimal annualAllowance;
    /**
     * 年收入预算
     */
    private BigDecimal annualIncomeBudget;
    /**
     * 管理岗薪等级
     */
    private String managementRank;
    /**
     * 管理岗薪等级
     */
    private Object managementRankEntity;
    /**
     * 职级
     */
    private String positionRank;
    /**
     * 职级
     */
    private Object positionRankEntity;

    /**
     * 内部职级
     */
    private String innerPositionRank;
    /**
     * 职级
     */
    private Object innerPositionRankEntity;
    /**
     * 生效日期
     */
    private Date effectDate;
    /**
     * 通过公式计算金额，仅对法语津贴生效
     */
    private Boolean formula;

    public EmployeeSalarySummary() {
        // static
        setBaseRankSalary(BigDecimal.ZERO);
        setOverseaAllowanceCoefficient(new BigDecimal(0));
        setManagementPositionSalary(new BigDecimal(0));
        setSpecialProfessionalAllowance(new BigDecimal(0));
        setProfessionalSequenceAllowance(new BigDecimal(0));
        setTransportationAllowance(new BigDecimal(0));
        setMonthlyReward(new BigDecimal(0));
        setRatio(new BigDecimal(0));
        setAnnualReward(new BigDecimal(0));
        setSiteAllowance(new BigDecimal(0));
        // dynamic
        setOverseaAllowance(new BigDecimal(0));
        setBaseMonthlySalary(new BigDecimal(0));
        setMonthlySalary(new BigDecimal(0));
        setBaseAnnualSalary(new BigDecimal(0));
        setFloatingAnnualSalary(new BigDecimal(0));
        setAnnualSalaryBudget(new BigDecimal(0));
        setAnnualAllowance(new BigDecimal(0));
        setAnnualIncomeBudget(new BigDecimal(0));
        // language
        setEnglishAllowance(BigDecimal.ZERO);
        setFranceAllowance(BigDecimal.ZERO);
        formula = false;
    }

    public EmployeeSalarySummary(EmployeeSalarySummary info) {
        // static
        setBaseRankSalary(info.getBaseRankSalary());
        setOverseaAllowanceCoefficient(info.getOverseaAllowanceCoefficient());
        setManagementPositionSalary(info.getManagementPositionSalary());
        setSpecialProfessionalAllowance(info.getSpecialProfessionalAllowance());
        setProfessionalSequenceAllowance(info.getProfessionalSequenceAllowance());
        setTransportationAllowance(info.getTransportationAllowance());
        setMonthlyReward(info.getMonthlyReward());
        setRatio(info.getRatio());
        setAnnualReward(info.annualReward);
        setSiteAllowance(info.getSiteAllowance());
        // dynamic
        setOverseaAllowance(info.getOverseaAllowance());
        setBaseMonthlySalary(info.getBaseMonthlySalary());
        setMonthlySalary(info.getMonthlySalary());
        setBaseAnnualSalary(info.getBaseAnnualSalary());
        setFloatingAnnualSalary(info.getFloatingAnnualSalary());
        setAnnualSalaryBudget(info.getAnnualSalaryBudget());
        setAnnualAllowance(info.getAnnualAllowance());
        setAnnualIncomeBudget(info.getAnnualIncomeBudget());
        // language
        setEnglishAllowance(info.getEnglishAllowance());
        setFranceAllowance(info.getFranceAllowance());
        setManagementRank(info.getManagementRank());
        setPositionRank(info.getPositionRank());
        setInnerPositionRank(info.getInnerPositionRank());
        formula = info.formula;
    }


    public BigDecimal getBaseRankSalary() {
        return baseRankSalary;
    }

    public void setBaseRankSalary(BigDecimal baseRankSalary) {
        this.baseRankSalary = baseRankSalary;
    }

    public BigDecimal getOverseaAllowanceCoefficient() {
        return overseaAllowanceCoefficient;
    }

    public void setOverseaAllowanceCoefficient(BigDecimal overseaAllowanceCoefficient) {
        this.overseaAllowanceCoefficient = overseaAllowanceCoefficient;
    }

    public BigDecimal getManagementPositionSalary() {
        return managementPositionSalary;
    }

    public void setManagementPositionSalary(BigDecimal managementPositionSalary) {
        this.managementPositionSalary = managementPositionSalary;
    }

    public BigDecimal getEnglishAllowance() {
        return englishAllowance;
    }

    public void setEnglishAllowance(BigDecimal englishAllowance) {
        this.englishAllowance = englishAllowance;
    }

    public BigDecimal getFranceAllowance() {
        return franceAllowance;
    }

    public void setFranceAllowance(BigDecimal franceAllowance) {
        this.franceAllowance = franceAllowance;
    }

    public BigDecimal getSiteAllowance() {
        return siteAllowance;
    }

    public void setSiteAllowance(BigDecimal siteAllowance) {
        this.siteAllowance = siteAllowance;
    }

    public BigDecimal getSpecialProfessionalAllowance() {
        return specialProfessionalAllowance;
    }

    public void setSpecialProfessionalAllowance(BigDecimal specialProfessionalAllowance) {
        this.specialProfessionalAllowance = specialProfessionalAllowance;
    }

    public BigDecimal getProfessionalSequenceAllowance() {
        return professionalSequenceAllowance;
    }

    public void setProfessionalSequenceAllowance(BigDecimal professionalSequenceAllowance) {
        this.professionalSequenceAllowance = professionalSequenceAllowance;
    }

    public BigDecimal getTransportationAllowance() {
        return transportationAllowance;
    }

    public void setTransportationAllowance(BigDecimal transportationAllowance) {
        this.transportationAllowance = transportationAllowance;
    }

    public BigDecimal getMonthlyReward() {
        return monthlyReward;
    }

    public void setMonthlyReward(BigDecimal monthlyReward) {
        this.monthlyReward = monthlyReward;
    }

    public BigDecimal getRatio() {
        return ratio;
    }

    public void setRatio(BigDecimal ratio) {
        this.ratio = ratio;
    }

    public BigDecimal getAnnualReward() {
        return annualReward;
    }

    public void setAnnualReward(BigDecimal annualReward) {
        this.annualReward = annualReward;
    }

    public BigDecimal getOverseaAllowance() {
        return overseaAllowance;
    }

    public void setOverseaAllowance(BigDecimal overseaAllowance) {
        this.overseaAllowance = overseaAllowance;
    }

    public BigDecimal getBaseMonthlySalary() {
        return baseMonthlySalary;
    }

    public void setBaseMonthlySalary(BigDecimal baseMonthlySalary) {
        this.baseMonthlySalary = baseMonthlySalary;
    }

    public BigDecimal getMonthlySalary() {
        return monthlySalary;
    }

    public void setMonthlySalary(BigDecimal monthlySalary) {
        this.monthlySalary = monthlySalary;
    }

    public BigDecimal getBaseAnnualSalary() {
        return baseAnnualSalary;
    }

    public void setBaseAnnualSalary(BigDecimal baseAnnualSalary) {
        this.baseAnnualSalary = baseAnnualSalary;
    }

    public BigDecimal getFloatingAnnualSalary() {
        return floatingAnnualSalary;
    }

    public void setFloatingAnnualSalary(BigDecimal floatingAnnualSalary) {
        this.floatingAnnualSalary = floatingAnnualSalary;
    }

    public BigDecimal getAnnualSalaryBudget() {
        return annualSalaryBudget;
    }

    public void setAnnualSalaryBudget(BigDecimal annualSalaryBudget) {
        this.annualSalaryBudget = annualSalaryBudget;
    }

    public BigDecimal getAnnualAllowance() {
        return annualAllowance;
    }

    public void setAnnualAllowance(BigDecimal annualAllowance) {
        this.annualAllowance = annualAllowance;
    }

    public BigDecimal getAnnualIncomeBudget() {
        return annualIncomeBudget;
    }

    public void setAnnualIncomeBudget(BigDecimal annualIncomeBudget) {
        this.annualIncomeBudget = annualIncomeBudget;
    }

    public Date getEffectDate() {
        return effectDate;
    }

    public String getManagementRank() {
        return managementRank;
    }

    public void setManagementRank(String managementRank) {
        this.managementRank = managementRank;
    }

    public String getPositionRank() {
        return positionRank;
    }

    public void setPositionRank(String positionRank) {
        this.positionRank = positionRank;
    }

    public String getInnerPositionRank() {
        return innerPositionRank;
    }

    public void setInnerPositionRank(String innerPositionRank) {
        this.innerPositionRank = innerPositionRank;
    }

    public void setEffectDate(Date effectDate) {
        this.effectDate = effectDate;
    }

    public Object getManagementRankEntity() {
        return managementRankEntity;
    }

    public Object getPositionRankEntity() {
        return positionRankEntity;
    }

    public Object getInnerPositionRankEntity() {
        return innerPositionRankEntity;
    }

    public void populate(EmployeeSalaryChangeEntry info) {
        String itemName = info.getType();
        BigDecimal money = info.getMoney();
        switch (itemName) {
            case "职级基薪":
                setBaseRankSalary(money);
                break;
            case "海外津贴系数":
                setOverseaAllowanceCoefficient(money);
                break;
            case "管理岗薪":
                setManagementPositionSalary(money);
                break;
            case "现场津贴":
                setSiteAllowance(money);
                break;
            case "特殊专业津贴":
                setSpecialProfessionalAllowance(money);
                break;
            case "专业序列津贴":
                setProfessionalSequenceAllowance(money);
                break;
            case "交通医药电脑补助（国外）":
                setTransportationAllowance(money);
                break;
            case "月度福利费":
                setMonthlyReward(money);
                break;
            case "浮动比例":
                setRatio(money);
                break;
            case "年福利":
                setAnnualReward(money);
                break;
            case "英语津贴":
                setEnglishAllowance(money);
                break;
            case "法语津贴":
                setFranceAllowance(money);
                break;
            case "管理岗薪等级":
                this.managementRankEntity = info.getValue();
                break;
            case "职级":
                this.positionRankEntity = info.getValue();
                break;
            case "内部职级别":
                this.innerPositionRankEntity = info.getValue();
                break;
            case "管理岗薪等级::职级::内部职级别":
                if (info.getRank() == null || info.getRank().equals("")) {
                    this.managementRank = "";
                    this.positionRank = "";
                    this.innerPositionRank = "";
                } else {
                    String[] arr = info.getRank().split("::");
                    this.managementRank = arr[0];
                    this.positionRank = arr[1];
                    this.innerPositionRank = arr[2];
                }
                break;
            default:
                break;
        }
    }

    public void calcDynamicSalary() {
//        if (formula) {
//        	//法语津贴
//            setFranceAllowance(getFranceAllowance().multiply(getBaseRankSalary()));
//        }
        setOverseaAllowance(getBaseRankSalary().multiply(getOverseaAllowanceCoefficient()));
        //5月基薪=1职级基薪+3海外津贴+4管理岗薪；
        setBaseMonthlySalary(getBaseRankSalary()
                .add(getOverseaAllowance())
                .add(getManagementPositionSalary()));
        //13月工资=5月基薪+6现场津贴+7英语津贴+8法语津贴+9特殊专业津贴+10专业序列津贴+11交通医药电脑补助（国外）+12月度福利费（国内）
        setMonthlySalary(getBaseMonthlySalary()//月基薪
                .add(getSiteAllowance())//现场津贴
                .add(getSpecialProfessionalAllowance()) //特殊专业津贴
                .add(getProfessionalSequenceAllowance())//专业序列津贴
                .add(getTransportationAllowance())		//交通医药电脑补助（国外）
                .add(getMonthlyReward())				//月度福利费
                .add(getEnglishAllowance())//英语津贴
                .add(getFranceAllowance()));//法语津贴
        //基本年薪= 月基薪*12
        setBaseAnnualSalary(getBaseMonthlySalary().multiply(BigDecimal.valueOf(12)));
        //浮动年薪
        BigDecimal floatAnnualSalary = getBaseAnnualSalary()
                .multiply(getRatio())
                .divide(BigDecimal.ONE.subtract(getRatio()), 3, BigDecimal.ROUND_HALF_UP);
        setFloatingAnnualSalary(floatAnnualSalary);
        //年薪预算
        setAnnualSalaryBudget(getBaseAnnualSalary().add(getFloatingAnnualSalary()));
        //年津补贴
        BigDecimal monthAllowance = getSpecialProfessionalAllowance()
                .add(getProfessionalSequenceAllowance())
                .add(getTransportationAllowance())
                .add(getMonthlyReward());
        setAnnualAllowance(monthAllowance.multiply(BigDecimal.valueOf(12)));
        //年收入预算
        setAnnualIncomeBudget(getAnnualSalaryBudget().add(getAnnualAllowance()).add(getAnnualReward()));

    }
}

package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.csce.shr.utils.CscecDateUtil;
import com.kingdee.csce.shr.utils.PersonUtil;
import com.kingdee.csce.shr.vo.EmployeeSalarySummary;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.affair.app.RiseBizBillControllerBean;
import com.kingdee.eas.hr.ats.AtsLeaveBillEntryCollection;
import com.kingdee.eas.hr.ats.HolidayLimitCollection;
import com.kingdee.eas.hr.ats.HolidayLimitInfo;
import com.kingdee.eas.hr.base.EmpPostExperienceHisInfo;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryCollection;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryInfo;
import com.kingdee.eas.hr.emp.*;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.ats.web.handler.AtsTripBillBatchNewEditHandler;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.web.handler.DEPCustomBillEditHandler;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.shr.compensation.CmpEmpAccountInfo;
import com.kingdee.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 回国（离职）申请表单api
 *
 * @author xudong.yao
 * @date 2021-01-15 11:58:27
 */
public class ResignApplicationHandler extends DEPCustomBillEditHandler {

    private static Logger log = LoggerFactory.getLogger(ResignApplicationHandler.class);

    @Override
    protected void afterCreateNewModel(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo coreBaseInfo) throws SHRWebException {
        super.afterCreateNewModel(request, response, coreBaseInfo);
        try {
            PersonInfo person = this.getCurrPersonInfo();
            BOSUuid id = person.getId();
            String personId = id.toString();
            PersonInfo personInfo = PersonUtil.getPersonInfo(personId);
            SHRBillBaseTemplateEntryInfo entry = new SHRBillBaseTemplateEntryInfo();
            entry.put("person", personInfo);
            entry.put("fullNamePingYin", personInfo.getString("fullNamePingYin"));
            entry.put("receiverFullname", personInfo.getString("fullNamePingYin"));
            entry.put("officePhone", personInfo.getString("nCell"));
            entry.put("address", personInfo.getString("addressTx"));
            entry.put("email", personInfo.getString("email"));

            // 获取部门和职位：任职历史最新的一条
            EmpPostExperienceHisInfo currentExperience = PersonUtil.getPersonCurrentPosition(personId);
            if (currentExperience != null) {
                entry.put("adminOrg", currentExperience.get("adminOrg"));
                entry.put("position", currentExperience.get("position"));
            }

            // 获取 入职日期（司龄）
            PersonPositionInfo enrollInfo = PersonUtil.getEnrollInfo(personId);
            if (enrollInfo != null) {
                entry.put("enterDate", enrollInfo.getDate("joinGroupDate"));
            }
            // 获取 最近一次来阿日期（司龄）
            PersonPositionHisInfo latestAlCompanyInfo = PersonUtil.getLatestAlCompanyInfo(personId);
            if (latestAlCompanyInfo != null) {
                entry.put("lastEnterAlDate", latestAlCompanyInfo.getDate("joinDate"));
            }
            // 获取 专业序列 任职历史中Z开头的兼职部门
            AdminOrgUnitInfo professionalDepartment = PersonUtil.getProfessionalDepartment(personId);
            if (null != professionalDepartment) {
                entry.put("professionalDep", professionalDepartment);
            }

            // 获取 成本部门
            BaseItemCustomInfo costDepartment = PersonUtil.getCostDepartment(personId);
            if (null != costDepartment) {
                entry.put("costCntr", costDepartment);
            }
            // 获取会签信息填充
            EmployeeContractInfo latestContractInfo = PersonUtil.getLatestContractInfo(personId);
            if (null != latestContractInfo) {
                entry.put("labContractFirstParty", latestContractInfo.get("labContractFirstParty"));
                entry.put("conStartDate", latestContractInfo.getDate("effectDate"));
                entry.put("conEndDate", latestContractInfo.getDate("endDate"));
            }

            // 获取会违约息填充
            entry.put("trnFee", personInfo.get("trnFee"));
            entry.put("trnFeeExpDate", personInfo.getDate("trnFeeExpDate"));
            entry.put("talentIntrFee", personInfo.get("talentIntrFee"));
            entry.put("IntrFeeExpDate", personInfo.getDate("IntrFeeExpDate"));

            // 管理岗薪等级/内部职级/职级
            EmpPostRankInfo latestPostRankInfo = PersonUtil.getLatestPostRankInfo(personId);
            if (null != latestPostRankInfo) {
                entry.put("mngJobGrade", latestPostRankInfo.get("mngJobGrade"));
                entry.put("grpJobgrade", latestPostRankInfo.get("grpJobgrade"));
                entry.put("jobGrade", latestPostRankInfo.get("jobGrade"));
            }

            // 获取薪水信息
            Date today = new Date();
            entry.put("applyLeftComDate", today);
            entry.put("finalResignDate", today);
            //根据人员id和日期计算薪酬项目
            EmployeeSalarySummary employeeSalaryInfo = PersonUtil.getEmployeeSalaryInfo(personId, today);
            //月工资
            entry.put("mnthWage", employeeSalaryInfo.getMonthlySalary());
            //职级基薪
            entry.put("rankBaseComp", employeeSalaryInfo.getBaseRankSalary());

            Map<String, BigDecimal> totalHolidayInfo = getTotalHolidayInfo(personId);
            entry.put("totalLevDays", totalHolidayInfo.get("realLimit"));
            entry.put("takenLevDays", totalHolidayInfo.get("usedLimit"));
            entry.put("restLevDays", totalHolidayInfo.get("remainLimit"));

            // 收款人账号信息
            CmpEmpAccountInfo cmpEmpAccountInfo = PersonUtil.getBankInfo(personId);
            if (cmpEmpAccountInfo != null) {
                entry.put("receiverFullname", cmpEmpAccountInfo.get("rcvrNmPingyin"));
                entry.put("bankCode", cmpEmpAccountInfo.get("bankCode"));
                entry.put("AccoutCode", cmpEmpAccountInfo.get("accountNum"));
                entry.put("address", cmpEmpAccountInfo.get("rcvrAdd"));
            }

            PersonContactMethodInfo personContactMethod = PersonUtil.getPersonContactMethod(personId);
            if (null != personContactMethod) {
                entry.put("cnPhone", personContactMethod.get("officePhone"));
                entry.put("psnEmail", personContactMethod.get("personalEmail"));
                if (personContactMethod.get("nCell") != null) {
                    String mobile = "";
                    if(personContactMethod.get("globalroaming") != null) {
                        mobile = personContactMethod.getString("globalroaming") + "-";
                    }
                    mobile = mobile + personContactMethod.getString("nCell");
                    entry.put("mobile", mobile);
                }
            }

            SHRBillBaseTemplateEntryCollection entrys = (SHRBillBaseTemplateEntryCollection) coreBaseInfo.get("entrys");
            entrys.add(entry);

//            AtsLeaveBillEntryCollection leaveBillCollection = PersonUtil.getLeaveBillCollection(personId);
//            if (leaveBillCollection != null && leaveBillCollection.size() > 0) {
//                BillEntryCustomCollection entry1 = new BillEntryCustomCollection();
//                for (int i = 0; i < leaveBillCollection.size(); i++) {
//                    AtsLeaveBillEntryInfo atsLeaveBillEntryInfo = leaveBillCollection.get(i);
//                    BillEntryCustomInfo row = new BillEntryCustomInfo();
//                    row.put("id", null);
//                    row.put("startdate", atsLeaveBillEntryInfo.getDate("beginTime"));
//                    row.put("enddate", atsLeaveBillEntryInfo.getDate("beginTime"));
//                    row.put("parent", coreBaseInfo);
//                    row.put("holidaytype", atsLeaveBillEntryInfo.get("policy"));
//                    row.put("days", atsLeaveBillEntryInfo.get("leaveLength"));
//                    entry1.add(row);
//                }
//                coreBaseInfo.put("entry1", entry1);
//            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (BOSException e) {
            e.printStackTrace();
        } catch (EASBizException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}
    }
    /**
     * 获取人员选取后的回填信息
     *
     * @param request  : 请求
      * @param response : 返回
     * @param modelMap : map
     * @author xy
     * @date 2021/1/22 19:50
     */
    public void getPersonFillingInfoAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        Context ctx = SHRContext.getInstance().getContext();
        String personId = request.getParameter("personId");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date leaveDate = null;
        try {
            leaveDate = format.parse(request.getParameter("leaveDate"));
        } catch (Exception e) {
            leaveDate = new Date();
        }
        Map<String, Object> result = new HashMap<String, Object>();

        try {
            fillingPersonInfo(personId, ctx, result);
            fillingContractInfo(personId, ctx, result);
            fillingWeiyueInfo(personId, ctx, result);
            fillingFinancialInfo(personId, leaveDate, ctx, result);
            JSONUtils.SUCCESS(result);
        } catch (EASBizException e) {
            e.printStackTrace();
        } catch (BOSException e) {
            log.error("BOS业务异常");
            e.printStackTrace();
            throw new SHRWebException(e.getMessage());
        } catch (SQLException e) {
            log.error("SQL执行异常");
            e.printStackTrace();
            throw new SHRWebException(e.getMessage());
        } catch (Exception e) {
            log.error("其他异常");
            e.printStackTrace();
            throw new SHRWebException(e.getMessage());
        }
    }

    /**
     * 获取是否必填项内容
     *
     * @param request :
     * @param response :
     * @param modelMap :
     * @author xy
     * @date 2021/1/23 15:58
     */
    public void getSettleItemInfoAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        Context ctx = SHRContext.getInstance().getContext();
        String settleItemId = request.getParameter("settleItemId");
        Map<String, Object> result = new HashMap<String, Object>();
        String query = "select * from CT_CUS_SettleItem where fid = '" + settleItemId + "'";
        try {
            IRowSet rowSet = DbUtil.executeQuery(ctx, query);
            if (rowSet.next()) {
                String id = (rowSet.getString("FID") == null) ? "" : rowSet.getString("FID");
                String name = (rowSet.getString("FNAME_L2") == null) ? "" : rowSet.getString("FNAME_L2");
                Boolean isAddItem = rowSet.getBoolean("CFISADDITEM");
                result.put("id", id);
                result.put("name", name);
                result.put("isAddItem", isAddItem);
            }
            JSONUtils.SUCCESS(result);
        } catch (BOSException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取单行的结果
     *
     * @param request :
     * @param response :
     * @param modelMap :
     * @return void
     * @author xy
     * @date 2021/6/4 15:20
     */
    public void getRowAmountAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        String type = request.getParameter("type");
        Context ctx = SHRContext.getInstance().getContext();
        BigDecimal result = BigDecimal.ZERO;
        Map<String, Object> resultMap = new HashMap<>(2);
        String startDateStr = request.getParameter("beginDate");
        String endDateStr = request.getParameter("endDate");
        String unitAmountStr = request.getParameter("unitAmount");
        //最终回国（离职）日期
        String finalResignDate = request.getParameter("finalResignDate");
        //年休假剩余天数
        String restLevDays = request.getParameter("restLevDays");
        //人员id
        String personId = request.getParameter("personId");
        //培训费
        String trnFee = request.getParameter("trnFee");
        //人才引进费
        String talentIntrFee = request.getParameter("talentIntrFee");
        //培训费期限
        String trnFeeExpDate = request.getParameter("trnFeeExpDate");
        //人才引进费期限
        String intrFeeExpDate = request.getParameter("intrFeeExpDate");
        //入职日期
        String enterDate = request.getParameter("enterDate");
        
        if(!StringUtils.isEmpty(startDateStr) && !StringUtils.isEmpty(endDateStr)) {
        	LocalDate beginDate = LocalDate.parse(startDateStr);
        	LocalDate endDate = LocalDate.parse(endDateStr);
        	//默认返回0 
        	resultMap.put("amount", result);
        	try {
        		if ("正常出勤工资".equals(type) || "补休假工资".equals(type)) {
        			//月工资
        			BigDecimal unitAmount = BigDecimal.valueOf(Double.parseDouble(unitAmountStr));
        			//金额 = 月工资*取整月份(计算结束日期-计算开始日期) +月工资*(天数)/当月自然天数,其中月工资取自1部分的月工资字段.
        			result =  PersonUtil.calcCompensatoryPay(beginDate, endDate, unitAmount);
        			resultMap.put("amount", result);
        		} else if("剩余假期2倍工资补发调差".equals(type)) {
        			//月工资
        			double unitAmount2 = Double.parseDouble(unitAmountStr);
        			//补休假工资
        			BigDecimal compensatoryPay =  PersonUtil.calcCompensatoryPay(beginDate, endDate,BigDecimal.valueOf(unitAmount2));
        			//当月回国工作日天数
        			double workDay = 0;
        			if(!StringUtils.isEmpty(finalResignDate)) {
        				String dateStr = finalResignDate.substring(0,7);
        				workDay = PersonUtil.getWorkingDays(ctx, personId, dateStr);
        			}
        			//年休假剩余天数
        			double restLevDay = 0;
        			if(!StringUtils.isEmpty(restLevDays)) {
        				restLevDay = Double.valueOf(restLevDays);
        			}
        			//开始计算
        			if(workDay>0) {
        				long round = Math.round((unitAmount2/workDay*restLevDay*2)-compensatoryPay.doubleValue());
        				resultMap.put("amount", round);
        			}else {
        				resultMap.put("amount", 0);
        			}
        		}else if("五险二金扣款".equals(type)) {
        			//开始日期是某月1日，结束日期是某月31日，系统自动判断月数，根据员工社保档案规则，计算这几个月的五险二金个人部分总额
        			int beginMonth = beginDate.getMonthValue();
        			int endMonth = endDate.getMonthValue();
        			double fiveRisks = PersonUtil.getFiveRisks(ctx, personId, startDateStr,endDateStr);
        			double round = fiveRisks*(endMonth-beginMonth+1);
        			BigDecimal bigDecimal = new BigDecimal(round).setScale(2,BigDecimal.ROUND_HALF_UP);
        			resultMap.put("amount", bigDecimal);
        		}else if ("超休年休假工资扣款".equals(type)) {
        			//round(月工资/回国当月工作日天数*年休假剩余天数)
        			//月工资
        			double unitAmount2 = Double.parseDouble(unitAmountStr);
        			//当月回国工作日天数
        			double workDay = 0;
        			if(!StringUtils.isEmpty(finalResignDate)) {
        				String dateStr = finalResignDate.substring(0,7);
        				workDay = PersonUtil.getWorkingDays(ctx, personId, dateStr);
        			}
        			//年休假剩余天数
        			double restLevDay = 0;
        			if(!StringUtils.isEmpty(restLevDays)) {
        				restLevDay = Double.valueOf(restLevDays);
        			}
        			//开始计算
        			if(workDay>0) {
        				long round = Math.round((unitAmount2/workDay*restLevDay));
        				resultMap.put("amount", round);
        			}else {
        				resultMap.put("amount", 0);
        			}
        		} else if ("培训费扣款".equals(type)) {
        			//系统需要根据职员信息档案中培训费的期限和额度，按天进行折算应承担部分，
        			//额度计算逻辑=round（培训费额度/应履行的工作天数*未履行的工作天数，2）。
        			//其中应履行的工作天数为“入职日期”和“培训费期限”之间的自然日天数，
        			//未履行的工作天数需要计算已经履行的工作天数，即“计算开始日期”“计算结束日期”之间的自然日天数
        			if(!StringUtils.isEmpty(trnFeeExpDate) && !StringUtils.isEmpty(trnFee)) {
        				double trnFeeAmount = Double.parseDouble(trnFee);
        				//应履行的工作天数为“入职日期”和“培训费期限”之间的自然日天数，
        				int fulfillDays = 0;
        				fulfillDays = PersonUtil.getTwoDays(enterDate, trnFeeExpDate);
        				//未履行的工作天数需要计算已经履行的工作天数，即“计算开始日期”“计算结束日期”之间的自然日天数
        				int UnfulfilledDays = 0;
        				UnfulfilledDays = PersonUtil.getTwoDays(startDateStr, endDateStr);
        				double round= trnFeeAmount/fulfillDays*UnfulfilledDays;
        				BigDecimal bigDecimal = new BigDecimal(round).setScale(2,BigDecimal.ROUND_HALF_UP);
        				resultMap.put("amount", bigDecimal);
        			}
        			
        		}else if("人才引进费扣款".equals(type)) {
        			if(!StringUtils.isEmpty(talentIntrFee) && !StringUtils.isEmpty(intrFeeExpDate)) {
        				double trnFeeAmount = Double.parseDouble(talentIntrFee);
        				//应履行的工作天数为“入职日期”和“培训费期限”之间的自然日天数，
        				int fulfillDays = 0;
        				fulfillDays = PersonUtil.getTwoDays(enterDate, intrFeeExpDate);
        				//未履行的工作天数需要计算已经履行的工作天数，即“计算开始日期”“计算结束日期”之间的自然日天数
        				int UnfulfilledDays = 0;
        				UnfulfilledDays = PersonUtil.getTwoDays(startDateStr, endDateStr);
        				//额度计算逻辑=round（人才引进费额度/应履行的工作天数*未履行的工作天数，2）
        			    double round= trnFeeAmount/fulfillDays*UnfulfilledDays;
        				BigDecimal bigDecimal = new BigDecimal(round).setScale(2,BigDecimal.ROUND_HALF_UP);
        				resultMap.put("amount", bigDecimal);
        			}
        		}
        	} catch (Exception e) {
        		log.info("计算失败: " + e.getMessage());
        		 throw new SHRWebException(e.getMessage());
        	}
        }else {
        	resultMap.put("amount", result);
        }
        JSONUtils.SUCCESS(resultMap);
    }

    private Map<String, BigDecimal> getTotalHolidayInfo(String personId) throws BOSException {
        BigDecimal realLimit = BigDecimal.ZERO;
        BigDecimal usedLimit = BigDecimal.ZERO;
        BigDecimal remainLimit = BigDecimal.ZERO;
        HolidayLimitCollection holidayLimitCollection = PersonUtil.getHolidayLimitCollection(personId);
        if (!holidayLimitCollection.isEmpty()) {
            for(int i = 0; i < holidayLimitCollection.size(); i++) {
                HolidayLimitInfo holidayLimitInfo = holidayLimitCollection.get(i);
                BigDecimal real = holidayLimitInfo.getBigDecimal("reallimit");
                BigDecimal used = holidayLimitInfo.getBigDecimal("usedlimit");
                BigDecimal remain = holidayLimitInfo.getBigDecimal("remainlimit");
                realLimit = realLimit.add(real);
                usedLimit = usedLimit.add(used);
                remainLimit = remainLimit.add(remain);
            }
        }
        Map<String, BigDecimal> result = new HashMap<>(4);
        result.put("realLimit", realLimit);
        result.put("usedLimit", usedLimit);
        result.put("remainLimit", remainLimit);
        return result;
    }

    // 处理员工信息分栏
    private void fillingPersonInfo(String personId, Context ctx, Map<String, Object> result) throws BOSException, EASBizException, SQLException {
        // 获取个人基本信息
        PersonInfo personInfo = PersonUtil.getPersonInfo(personId);
        result.put("person", personInfo);

        // 获取 入职日期（司龄）
        String query = "select joinGroupDateCur where person.id = '" + personId + "' order by joinGroupDateCur desc";
        PersonPositionHisCollection inRollPositionList = PersonPositionHisFactory.getLocalInstance(ctx).getPersonPositionHisCollection(query);
        if (!inRollPositionList.isEmpty()) {
            PersonPositionHisInfo personPositionHisInfo = inRollPositionList.get(0);
            result.put("takeOfficeInfo", personPositionHisInfo);
        } else {
            result.put("takeOfficeInfo", null);
        }
        // 获取 专业序列
        AdminOrgUnitInfo professionalDepartment = PersonUtil.getProfessionalDepartment(personId);
        if (null != professionalDepartment) {
            result.put("professionalDep", professionalDepartment);
        }

        PersonPositionHisInfo latestAlCompanyInfo = PersonUtil.getLatestAlCompanyInfo(personId);
        if (null != latestAlCompanyInfo) {
            result.put("lastEnterAlDate", latestAlCompanyInfo.getDate("joinDate"));
        }

        // 获取 成本部门
        BaseItemCustomInfo costDepartment = PersonUtil.getCostDepartment(personId);
        result.put("costCenter", costDepartment);
    }

    // 获取会签信息填充
    private void fillingContractInfo(String personId, Context ctx, Map<String, Object> result) throws BOSException {
        String query = "select labContractFirstParty.name, effectDate, endDate, trnFee, trnFeeExpDate, talentIntrFee, IntrFeeExpDate where employee.id = '" + personId + "' order by effectDate desc";
        EmployeeContractCollection employeeContractCollection = EmployeeContractFactory.getLocalInstance(ctx).getEmployeeContractCollection(query);
        if (!employeeContractCollection.isEmpty()) {
            EmployeeContractInfo employeeContractInfo = employeeContractCollection.get(0);
            result.put("contract", employeeContractInfo);
        } else {
            result.put("contract", null);
        }
    }

    // 获取违约信息填充
    private void fillingWeiyueInfo(String personId, Context ctx, Map<String, Object> result) throws BOSException, EASBizException {
        PersonInfo person = PersonUtil.getPersonInfo(personId);
        result.put("trnFee", person.get("trnFee"));
        result.put("trnFeeExpDate", person.getDate("trnFeeExpDate"));
        result.put("talentIntrFee", person.get("talentIntrFee"));
        result.put("IntrFeeExpDate", person.getDate("IntrFeeExpDate"));
    }

    // 获取结算信息
    private void fillingFinancialInfo(String personId, Date leaveDate, Context ctx, Map<String, Object> result) throws BOSException, SQLException, EASBizException, ParseException {
        // 管理岗薪等级/内部职级/职级
        String query = "select mngJobGrade.name, grpJobgrade.name, jobGrade.name where isLatest = 1 and person.id = '" + personId + "' order by EFFDT desc";
        EmpPostRankCollection empPostRankCollection = EmpPostRankFactory.getLocalInstance(ctx).getEmpPostRankCollection(query);
        if (!empPostRankCollection.isEmpty()) {
            EmpPostRankInfo empPostRankInfo = empPostRankCollection.get(0);
            result.put("positionRank", empPostRankInfo);
        } else {
            result.put("positionRank", null);
        }

        // 获取薪水信息
        EmployeeSalarySummary employeeSalaryInfo = PersonUtil.getEmployeeSalaryInfo(personId, leaveDate);
        result.put("salaryInfo", employeeSalaryInfo);

        // 获取假期信息
        AtsLeaveBillEntryCollection leaveBillCollection = PersonUtil.getLeaveBillCollection(personId);
        if (!leaveBillCollection.isEmpty()) {
            result.put("leaveBillInfo", leaveBillCollection.toArray());
        } else {
            result.put("leaveBillInfo", null);
        }

        Map<String, BigDecimal> totalHolidayInfo = getTotalHolidayInfo(personId);
        result.putAll(totalHolidayInfo);

        // 收款人账号信息
        CmpEmpAccountInfo cmpEmpAccountInfo =PersonUtil.getBankInfo(personId);
        result.put("bankAccountInfo", cmpEmpAccountInfo);

        PersonContactMethodInfo personContactMethod = PersonUtil.getPersonContactMethod(personId);
        result.put("personContactMethod", personContactMethod);
    }

}

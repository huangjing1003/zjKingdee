 package com.kingdee.csce.shr.utils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.metadata.entity.PropertyInfo;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;
import com.kingdee.bos.orm.ORMCoreException;
import com.kingdee.csce.shr.bo.EmployeeSalaryChangeEntry;
import com.kingdee.csce.shr.vo.EmployeeSalarySummary;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonFactory;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.framework.CoreBaseCollection;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.ats.AtsLeaveBillEntryCollection;
import com.kingdee.eas.hr.ats.AtsLeaveBillEntryFactory;
import com.kingdee.eas.hr.ats.HolidayLimitCollection;
import com.kingdee.eas.hr.ats.HolidayLimitFactory;
import com.kingdee.eas.hr.base.EmpLaborRelationInfo;
import com.kingdee.eas.hr.base.EmpPosOrgRelationCollection;
import com.kingdee.eas.hr.base.EmpPosOrgRelationFactory;
import com.kingdee.eas.hr.base.EmpPosOrgRelationInfo;
import com.kingdee.eas.hr.base.EmpPostExperienceHisCollection;
import com.kingdee.eas.hr.base.EmpPostExperienceHisFactory;
import com.kingdee.eas.hr.base.EmpPostExperienceHisInfo;
import com.kingdee.eas.hr.emp.EmpMultiInfoCustomInfo;
import com.kingdee.eas.hr.emp.EmpPostRankCollection;
import com.kingdee.eas.hr.emp.EmpPostRankFactory;
import com.kingdee.eas.hr.emp.EmpPostRankInfo;
import com.kingdee.eas.hr.emp.EmployeeContractCollection;
import com.kingdee.eas.hr.emp.EmployeeContractFactory;
import com.kingdee.eas.hr.emp.EmployeeContractInfo;
import com.kingdee.eas.hr.emp.PersonContactMethodCollection;
import com.kingdee.eas.hr.emp.PersonContactMethodFactory;
import com.kingdee.eas.hr.emp.PersonContactMethodInfo;
import com.kingdee.eas.hr.emp.PersonLanguageInfo;
import com.kingdee.eas.hr.emp.PersonPositionFactory;
import com.kingdee.eas.hr.emp.PersonPositionHisCollection;
import com.kingdee.eas.hr.emp.PersonPositionHisFactory;
import com.kingdee.eas.hr.emp.PersonPositionHisInfo;
import com.kingdee.eas.hr.emp.PersonPositionInfo;
import com.kingdee.eas.hr.emp.app.util.SHREmpOptEmpLaborRelationUtil;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.util.MetaDataUtil;
import com.kingdee.shr.compensation.CmpEmpAccountCollection;
import com.kingdee.shr.compensation.CmpEmpAccountFactory;
import com.kingdee.shr.compensation.CmpEmpAccountInfo;
import com.kingdee.shr.compensation.FixAdjustSalaryCollection;
import com.kingdee.shr.compensation.FixAdjustSalaryFactory;
import com.kingdee.util.StringUtils;

import cn.hutool.core.util.StrUtil;

public class PersonUtil {

    /**
     * ??????????????????
     *
     * @param personId : ??????Id
     * @return com.kingdee.eas.basedata.person.PersonInfo
     * @author xudong.yao
     * @date 2021/4/25 10:06
     */
    public static PersonInfo getPersonInfo(String personId, SelectorItemCollection selectorItemCollection) throws BOSException, EASBizException {
        Context ctx = SHRContext.getInstance().getContext();
        return PersonFactory.getLocalInstance(ctx).getPersonInfo(new ObjectUuidPK(personId), selectorItemCollection);
    }

    /**
     * ??????????????????
     *
     * @param personId : ??????Id
     * @return com.kingdee.eas.basedata.person.PersonInfo
     * @author xudong.yao
     * @date 2021/4/25 10:06
     */
    public static PersonInfo getPersonInfo(String personId) throws BOSException, EASBizException {
        Context ctx = SHRContext.getInstance().getContext();
        return PersonFactory.getLocalInstance(ctx).getPersonInfo(new ObjectUuidPK(personId));
    }

    /***
     * ?????????????????????????????????
     * ????????????????????????
     *
     * @param personId : ??????Id
     * @return com.kingdee.eas.hr.base.EmpPostExperienceHisInfo
     * @author xudong.yao
     * @date 2021/5/9 10:06
     */
    public static EmpPosOrgRelationInfo getLatestPersonChangeInfo(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "SELECT person.name, isSystem, endDateTime, employeeType.name, assignType, id, laborRelation.laborRelationState.id, laborRelation.laborRelationState.name, isInner WHERE person.id = '" + personId + "' AND isSystem = 1 ORDER BY endDateTime DESC";
        EmpPosOrgRelationCollection empPosOrgRelationCollection = EmpPosOrgRelationFactory.getLocalInstance(ctx).getEmpPosOrgRelationCollection(query);
        if (empPosOrgRelationCollection.isEmpty()) {
            return null;
        }
        EmpPosOrgRelationInfo empPosOrgRelationInfo = empPosOrgRelationCollection.get(0);
        if (empPosOrgRelationInfo.isIsInner()) {
            Date endDateTime = empPosOrgRelationInfo.getEndDateTime();
            EmpLaborRelationInfo laborRelationInfo = SHREmpOptEmpLaborRelationUtil.getEmpLaborRelationByEndDateTime(ctx, personId, endDateTime);
            empPosOrgRelationInfo.setLaborRelation(laborRelationInfo);
            empPosOrgRelationInfo.setEmployeeType(laborRelationInfo.getLaborRelationState());
        }
        return empPosOrgRelationInfo;
    }

    /***
     * ????????????????????????????????????
     * ???????????????????????????
     *
     * @param personId : ??????Id
     * @return com.kingdee.eas.hr.base.EmpPostExperienceHisInfo
     * @author xudong.yao
     * @date 2021/4/25 10:06
     */
    public static EmpPostExperienceHisInfo getPersonCurrentPosition(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select person.name, adminOrg.name, assignType, position.name where assignType = '1' and person.id = '" + personId + "' order by startDateTime desc";
        EmpPostExperienceHisCollection experienceHisCollection = EmpPostExperienceHisFactory.getLocalInstance(ctx).getEmpPostExperienceHisCollection(query);
        if (!experienceHisCollection.isEmpty()) {
            EmpPostExperienceHisInfo currentExperience = experienceHisCollection.get(0);
            return currentExperience;
        } else {
            return null;
        }
    }

    /**
     * ??????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.PersonPositionInfo
     * @author xudong.yao
     * @date 2021/4/25 11:22
     */
    public static PersonPositionInfo getEnrollInfo(String personId) throws BOSException, EASBizException {
        String oql = "where person =  '" + personId + "'";
        return PersonPositionFactory.getRemoteInstance().getPersonPositionInfo(oql);
    }

    /**
     * ???????????????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.PersonPositionHisInfo
     * @author xudong.yao
     * @date 2021/4/25 11:26
     */
    public static PersonPositionHisInfo getLatestAlCompanyInfo(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select where person.id = '" + personId + "' order by joinDate desc";
        PersonPositionHisCollection inRollPositionList = PersonPositionHisFactory.getLocalInstance(ctx).getPersonPositionHisCollection(query);
        if (!inRollPositionList.isEmpty()) {
            return inRollPositionList.get(0);
        } else {
            return null;
        }
    }

    /**
     * ??????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.base.EmpPostExperienceHisInfo
     * @author xudong.yao
     * @date 2021/4/25 11:32
     */
    public static AdminOrgUnitInfo getProfessionalDepartment(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select id, assignType, adminOrg.number, adminOrg.name, position.name where assignType = '0' and adminOrg.number like 'Z%' and person.id = '" + personId + "' order by startDateTime desc";
        EmpPostExperienceHisCollection experienceHisCollection = EmpPostExperienceHisFactory.getLocalInstance(ctx).getEmpPostExperienceHisCollection(query);
        if (!experienceHisCollection.isEmpty()) {
            EmpPostExperienceHisInfo posOrgRelationInfo = experienceHisCollection.get(0);
            return  (AdminOrgUnitInfo) posOrgRelationInfo.get("adminOrg");
        } else {
            return null;
        }
    }

    /**
     * ??????????????????
     *
     * @param personId :
     * @return java.lang.Object
     * @author xudong.yao
     * @date 2021/4/25 11:36
     */
    public static BaseItemCustomInfo getCostDepartment(String personId) throws BOSException {
        String query = "select costCntr.id, costCntr.name where person.id = '" + personId + "' order by beginDate desc";
        CoreBaseCollection costCenterCollection = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.Costcenter").getCollection(query);
        if (!costCenterCollection.isEmpty()) {
            CoreBaseInfo costCenter = costCenterCollection.get(0);
            return ( BaseItemCustomInfo )costCenter.get("costCntr");
        } else {
            return null;
        }
    }

    /**
     * ?????????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.EmployeeContractInfo
     * @author xudong.yao
     * @date 2021/4/25 11:41
     */
    public static EmployeeContractInfo getLatestContractInfo(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select labContractFirstParty.id, labContractFirstParty.name, effectDate, endDate, trnFee, trnFeeExpDate, talentIntrFee, IntrFeeExpDate where employee.id = '" + personId + "' order by effectDate desc";
        EmployeeContractCollection employeeContractCollection = EmployeeContractFactory.getLocalInstance(ctx).getEmployeeContractCollection(query);
        if (!employeeContractCollection.isEmpty()) {
            return employeeContractCollection.get(0);
        }
        return null;
    }

    /**
     * ????????????????????????/????????????/??????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.EmpPostRankInfo
     * @author xudong.yao
     * @date 2021/4/25 11:41
     */
    public static EmpPostRankInfo getLatestPostRankInfo(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select mngJobGrade.name, grpJobgrade.name, jobGrade.name where isLatest = 1 and person.id = '" + personId + "' order by EFFDT desc";
        EmpPostRankCollection empPostRankCollection = EmpPostRankFactory.getLocalInstance(ctx).getEmpPostRankCollection(query);
        if (!empPostRankCollection.isEmpty()) {
            return empPostRankCollection.get(0);
        }
        return null;
    }

    /**
     * ?????????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.EmpPostRankInfo
     * @author xudong.yao
     * @date 2021/4/25 11:41
     */
    public static CmpEmpAccountInfo getBankInfo(String personId) throws BOSException {
        // ?????????????????????
        Context ctx = SHRContext.getInstance().getContext();
        String query = "where person.id = '" + personId + "' order by lastupdatetime desc";
        CmpEmpAccountCollection cmpEmpAccountCollection = CmpEmpAccountFactory.getLocalInstance(ctx).getCmpEmpAccountCollection(query);
        if (!cmpEmpAccountCollection.isEmpty()) {
            return cmpEmpAccountCollection.get(0);
        } else {
            return null;
        }
    }

    /**
     * ?????????????????????
     *
     * @param personId :
     * @return com.kingdee.eas.hr.emp.EmpPostRankInfo
     * @author xudong.yao
     * @date 2021/4/25 11:41
     */
    public static PersonContactMethodInfo getPersonContactMethod(String personId) throws BOSException {
        // ?????????????????????
        Context ctx = SHRContext.getInstance().getContext();
        String query = "where person.id = '" + personId + "' order by lastupdatetime desc";
        PersonContactMethodCollection personContactMethodCollection = PersonContactMethodFactory.getLocalInstance(ctx).getPersonContactMethodCollection(query);
        if (!personContactMethodCollection.isEmpty()) {
            return personContactMethodCollection.get(0);
        } else {
            return null;
        }
    }

    /**
     * ??????????????????
     *
     * @param personId :
     * @param leaveDate :
     * @return com.kingdee.eas.hr.emp.EmpPostRankInfo
     * @author xudong.yao
     * @throws ParseException 
     * @date 2021/4/25 11:41
     */
    public static EmployeeSalarySummary getEmployeeSalaryInfo(String personId, Date leaveDate) throws BOSException, SQLException, ParseException {
        Context ctx = SHRContext.getInstance().getContext();
        EmployeeSalarySummary result = new EmployeeSalarySummary();
        EmployeeSalaryChangeEntry entry;
        // ????????????????????????
        FixAdjustSalaryCollection fixAdjustSalaryCollection = getCurrentFixAdjustSalaryCollection(personId, ctx);
        if (!fixAdjustSalaryCollection.isEmpty()) {
            for (int i = 0; i < fixAdjustSalaryCollection.size(); i++) {
            	//??????????????????????????????
                entry = new EmployeeSalaryChangeEntry(fixAdjustSalaryCollection.get(i));
                result.populate(entry);
            }
        }

        /**2021-12-23
         * ??????????????????????????????
         */
        entry = getSiteAllowanceInfo(personId,ctx);
        if (null != entry) {
            result.populate(entry);
        }
        // ????????????????????????-????????????
        entry = getFranceAllowanceInfo(personId, leaveDate, ctx);
        if (null != entry) {
            result.populate(entry);
        }
        //????????????
        entry = getEnglishAllowanceInfo(personId, leaveDate, ctx);
        if (null != entry) {
            result.populate(entry);
        }
        // ??????????????????
        entry = getPositionRankInfo(personId, ctx);
        if (null != entry) {
            result.populate(entry);
        }
        //??????????????????
        result.calcDynamicSalary();
        return result;
    }

    /**
     * ??????????????????
     *
     * @param personId :
     * @author xudong.yao
     * @date 2021/6/2 13:41
     */
    public static AtsLeaveBillEntryCollection getLeaveBillCollection(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        String query = "select policy.name, beginTime, endTime, leaveLength where person.id = '" + personId + "'";
        return AtsLeaveBillEntryFactory.getLocalInstance(ctx).getAtsLeaveBillEntryCollection(query);
    }

    /**
     * ????????????????????????
     *
     * @param personId :
     * @author xudong.yao
     * @date 2021/6/10 13:41
     */
    public static HolidayLimitCollection getHolidayLimitCollection(String personId) throws BOSException {
        Context ctx = SHRContext.getInstance().getContext();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        Date date = new Date();
        String currentYear = sdf.format(date);
        String endDate = currentYear + "-12-31";
        String query = StrUtil.format("select where proposer.id = '{}' and cycleenddate = '{}' order by lastupdatetime desc", personId, endDate);
        return HolidayLimitFactory.getLocalInstance(ctx).getHolidayLimitCollection(query);
    }

    private static FixAdjustSalaryCollection getCurrentFixAdjustSalaryCollection(String personId, Context ctx) throws BOSException, SQLException {
        // ????????????????????????
        StringBuilder sb = new StringBuilder("select tt.fid from T_HR_SFixAdjustSalary tt ")
                .append("inner join ")
                .append("(select max(tt.FeffectDay) effdt, FCmpItemId from T_HR_SFixAdjustSalary tt ")
                .append("where tt.FPersonId = '")
                .append(personId)
                .append("' group by FCmpItemId) t1 ")
                .append("on tt.FeffectDay = t1.effdt ")
                .append("and tt.FCmpItemId = t1.FCmpItemId ")
                .append("where tt.FPersonId = '")
                .append(personId)
                .append("'");
        IRowSet r = DbUtil.executeQuery(ctx, sb.toString());
        sb = new StringBuilder();

        while(r.next()) {
            sb.append("'").append(r.getString("fid")).append("',");
        }
        String ids = sb.length() > 0 ? sb.subSequence(0, sb.length() - 1).toString() : "''";
        String query = "select " +
                "id, cmpItem.name, money " +
                "where person.id = '" +
                personId +
                "' " +
                "and id in (" + ids + ")";
        return FixAdjustSalaryFactory.getLocalInstance(ctx).getFixAdjustSalaryCollection(query);
    }

    
    /**
     * 2021-12-23??????????????????????????????
     * ????????????????????????
     * @param personId
     * @param ctx
     * @return
     * @throws BOSException 
     * @throws SQLException 
     * @throws ParseException 
     */
    private static EmployeeSalaryChangeEntry getSiteAllowanceInfo(String personId, Context ctx) throws BOSException, SQLException, ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        EmployeeSalaryChangeEntry entry = null;
        StringBuffer buffer = new StringBuffer();
        buffer.append(" select cost.CFBEGINDATE beginDate,cstCenter.cfsiteBns money from CT_MP_Costcenter  cost ");
        buffer.append(" left join CT_MP_CstCenter   cstCenter on cstCenter.fid = cost.CFCOSTCNTRID ");
        buffer.append(" where  cost.FPERSONID='"+personId+"'  and  cost.FISSINGLE=1 order  by  CFBEGINDATE");
        IRowSet iRowSet = DbUtil.executeQuery(ctx, buffer.toString());
        if(iRowSet.next()) {
        	PropertyInfo propertyInfo = new PropertyInfo();
        	String beginDateStr = iRowSet.getString("beginDate");
        	if(!StringUtils.isEmpty(beginDateStr)) {
        		Date beginDate = sdf.parse(beginDateStr);
        		propertyInfo.put("startDate", beginDate);
        	}
        	BigDecimal money = iRowSet.getBigDecimal("money")==null?new BigDecimal(0):iRowSet.getBigDecimal("money");
    		propertyInfo.put("money", money);
        	entry = new EmployeeSalaryChangeEntry(propertyInfo);
       	}  
        return entry;
    }
    
    
 
    
    /**
     * ???????????????????????? 2021-12-25
     * @param userId
     * @param leaveDate
     * @param ctx
     * @return
     * @throws BOSException
     * @throws SQLException 
     */
    private static EmployeeSalaryChangeEntry getFranceAllowanceInfo(String userId, Date leaveDate, Context ctx) throws BOSException, SQLException {
        EmployeeSalaryChangeEntry entry = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(leaveDate);
        String querySql =  "/*dialect*/select get_FrBnsAmt('"+userId+"',TO_DATE('"+dateStr+"','yyyy-MM-dd'))  frBnsAmt from dual";
        IRowSet rowSet = DbUtil.executeQuery(ctx, querySql);
        if(rowSet.next()) {
        	BigDecimal money = rowSet.getBigDecimal("frBnsAmt")==null?new BigDecimal(0):rowSet.getBigDecimal("frBnsAmt");
        	PersonLanguageInfo languageInfo = new PersonLanguageInfo();
        	languageInfo.put("endDate", leaveDate);
        	languageInfo.put("money", money);
        	 entry = new EmployeeSalaryChangeEntry(languageInfo);
        }
        
        return entry;
    }

    /**
     * ????????????
     * @param userId
     * @param leaveDate
     * @return
     * @throws BOSException
     * @throws SQLException 
     * @throws ORMCoreException 
     */
    private static EmployeeSalaryChangeEntry getEnglishAllowanceInfo(String userId, Date leaveDate,Context ctx) throws BOSException, ORMCoreException, SQLException {
//        EmployeeSalaryChangeEntry entry = null;
//        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//        String dateStr = format.format(leaveDate);
//        String query = StrUtil.format("SELECT engBnsRank.name, startDate, endDate WHERE person.id = '{}' and endDate > '{}' order by startDate asc", userId, dateStr);
//        CoreBaseCollection englishCollection = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.EngBns").getCollection(query);
//        if (!englishCollection.isEmpty()) {
//            entry = new EmployeeSalaryChangeEntry((EmpMultiInfoCustomInfo) englishCollection.get(0));
//        }
        EmployeeSalaryChangeEntry entry = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(leaveDate);
        String querySql =  "/*dialect*/select get_EngBnsAmt('"+userId+"',TO_DATE('"+dateStr+"','yyyy-MM-dd'))   engBnsAmt from dual";
        IRowSet rowSet = DbUtil.executeQuery(ctx, querySql);
        if(rowSet.next()) {
        	BigDecimal money = rowSet.getBigDecimal("engBnsAmt")==null?new BigDecimal(0):rowSet.getBigDecimal("engBnsAmt");
        	EmpMultiInfoCustomInfo languageInfo = new EmpMultiInfoCustomInfo();
        	languageInfo.put("endDate", leaveDate);
        	languageInfo.put("money", money);
        	 entry = new EmployeeSalaryChangeEntry(languageInfo);
        }
        return entry;
    }

    private static EmployeeSalaryChangeEntry getPositionRankInfo(String userId, Context ctx) throws BOSException {
        EmployeeSalaryChangeEntry entry = null;
        String query = "SELECT jobGrade.name, grpJobgrade.name, mngJobGrade.name, EFFDT, LEFFDT WHERE person.id = '" + userId + "' order by EFFDT asc";
        EmpPostRankCollection postRankCollection = EmpPostRankFactory.getLocalInstance(ctx).getEmpPostRankCollection(query);
        if (!postRankCollection.isEmpty()) {
            entry = new EmployeeSalaryChangeEntry(postRankCollection.get((0)));
        }
        return entry;
    }

    /**
     * ???????????????????????????
     * @param beginDate
     * @param endDate
     * @param unitAmount
     * @return
     */
    public static BigDecimal calcCompensatoryPay(LocalDate beginDate, LocalDate endDate,BigDecimal unitAmount) {
    	BigDecimal result = CscecDateUtil.calcAmountBetweenByTwoDate(beginDate, endDate, unitAmount)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    	return result;
    	
    }
    
    /**
     * ???????????????????????????????????????
     * @param ctx
     * @param personId
     * @param dateStr
     * @return
     * @throws BOSException 
     * @throws SQLException 
     */
    public static double getWorkingDays(Context ctx,String personId,String dateStr) throws BOSException, SQLException {
    	StringBuffer buffer = new StringBuffer("/*dialect*/ ");
		buffer.append(" SELECT  sum(day) workDay,FPROPOSERID personId  FROM (  ");
		buffer.append(" SELECT case when  FISHALFDAY=1 then  0.5 else 1 end  day, ");
		buffer.append(" FPROPOSERID  FROM T_HR_ATS_ScheduleShift where FPROPOSERID ='").append(personId).append("'");
		buffer.append(" and   to_char(FATTENDDATE,'yyyy-MM') = '"+dateStr+"' and  FDAYTYPE=0 ");
		buffer.append(" ) group by FPROPOSERID");
		IRowSet iRowSet = DbUtil.executeQuery(ctx, buffer.toString());
		if(iRowSet.next()) {
			String workDay = iRowSet.getString("workDay")==null?"0":iRowSet.getString("workDay");
			Double valueOf = Double.valueOf(workDay);
			return valueOf;
		}
		return 0;
    	
    }
    
    /**
     * ??????????????????????????????
     * @param ctx
     * @param personId
     * @param dateStr
     * @return
     * @throws BOSException
     * @throws NumberFormatException
     * @throws SQLException
     */
    public static double getFiveRisks(Context ctx,String personId,String startDateStr,String endDateStr) throws BOSException, NumberFormatException, SQLException {
    	String empSocFilesId = "";
    	
    	String checkExistSql = "SELECT  distinct  empSocFiles.Fid  empSocFilesId,empSocFilesEntry.FEFFECTDAY " + 
    			" FROM T_HR_SSocEmpSocFiles  empSocFiles " + 
    			" left join T_HR_SSocEmpSocFilesEntry  empSocFilesEntry on empSocFilesEntry.FBILLID = empSocFiles.Fid  " + 
    			" left join T_HR_SSocTypeItemHis typeItem on typeItem.fid = empSocFilesEntry.FSOCTYPEITEMID  " + 
    			" left join T_HR_SSocItem  item on item.fid = typeItem.FSOCITEMID where empSocFiles.FPERSONID ='"+personId+"' " ; 
		String checkExistOneSql = checkExistSql  
				+ " and empSocFilesEntry.FEFFECTDAY<={ts'"+endDateStr+"'}  order by empSocFilesEntry.FEFFECTDAY desc ";
		IRowSet iRowSet1 = DbUtil.executeQuery(ctx, checkExistOneSql);
    	if(iRowSet1.next()) {
    		empSocFilesId = iRowSet1.getString("empSocFilesId");
    	}else {
    		//??????????????????????????????????????????????????????????????????
    		String checkExistTwoSql = checkExistSql+" order by empSocFilesEntry.FEFFECTDAY  desc ";
    		IRowSet iRowSet2 = DbUtil.executeQuery(ctx, checkExistTwoSql);
    		if(iRowSet2.next()) {
    			empSocFilesId = iRowSet2.getString("empSocFilesId");
    		}
    	}
    	//?????????id????????????????????????0
    	if(!StringUtils.isEmpty(empSocFilesId)) {
     		StringBuffer buffer = new StringBuffer("/*dialect*/ ");
    		buffer.append(" SELECT sum(total) money FROM ( ");
    		buffer.append(" SELECT empSocFiles.FPERSONID  personId,typeItem.FSELFFIXEDPAY ,item.fname_l2, ");
    		buffer.append(" ((empSocFilesEntry.FSELFBASE*(typeItem.FSELFPREC/100))+typeItem.FSELFFIXEDPAY) total ");
    		buffer.append(" FROM T_HR_SSocEmpSocFiles  empSocFiles ");
    		buffer.append(" left join T_HR_SSocEmpSocFilesEntry  empSocFilesEntry on empSocFilesEntry.FBILLID = empSocFiles.Fid ");
    		buffer.append(" left join T_HR_SSocTypeItemHis typeItem on typeItem.fid = empSocFilesEntry.FSOCTYPEITEMID  ");
    		buffer.append(" left join T_HR_SSocItem  item on item.fid = typeItem.FSOCITEMID ");
    		buffer.append(" where empSocFiles.FPERSONID ='"+personId+"'  and empSocFiles.Fid  = '"+empSocFilesId+"')  t group by t.personId ");
    		IRowSet iRowSet = DbUtil.executeQuery(ctx, buffer.toString());
    		System.out.println("=====================?????????????????????????????? is:"+buffer.toString());
    		if(iRowSet.next()) {
    			String workDay = iRowSet.getString("money")==null?"0":iRowSet.getString("money");
    			Double valueOf = Double.valueOf(workDay);
    			return valueOf;
    		}else {
    			return 0;
    		}
    	}else {
    		return 0;
    	}
    }
 
    /**
     * ??????2????????????????????????
     * @param startDateStr
     * @param endDateStr
     * @return
     * @throws ParseException
     */
    public static int getTwoDays(String startDateStr,String endDateStr) throws ParseException {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	Date nowDate = sdf.parse(startDateStr);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTime(nowDate);
    	long startTimeInMillis = calendar.getTimeInMillis();
    	Date endDate = sdf.parse(endDateStr);
    	calendar.setTime(endDate);
    	long endTimeInMillis = calendar.getTimeInMillis();
    	int between_days=(int) ((endTimeInMillis-startTimeInMillis)/(1000*3600*24))+1;
		return between_days;
    }
    
    
}

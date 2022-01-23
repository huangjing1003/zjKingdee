package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.csce.shr.bo.EmployeeSalaryChangeEntry;
import com.kingdee.csce.shr.vo.EmployeeSalaryInfo;
import com.kingdee.eas.framework.CoreBaseCollection;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.emp.*;
import com.kingdee.eas.hr.emp.web.handler.EmployeeMultiRowHandler;
import com.kingdee.eas.hr.emp.web.handler.EmployeeResumeSummaryMainHandler;
import com.kingdee.eas.hr.emp.web.util.EmployeeWebUtil;
import com.kingdee.eas.util.app.ContextUtil;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.util.MetaDataUtil;
import com.kingdee.shr.base.syssetting.web.dynamic.model.UIViewInfo;
import com.kingdee.shr.compensation.FixAdjustSalaryCollection;
import com.kingdee.shr.compensation.FixAdjustSalaryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

/**
 * 我的个人档案API
 *
 * @author xudong.yao
 * @date 2021-01-15 11:58:27
 */
public class EmployeeSalaryHistoryHandler extends EmployeeMultiRowHandler {

    private static final Logger log = LoggerFactory.getLogger(EmployeeResumeSummaryMainHandler.class);

    @Override
    public List<CoreBaseInfo> getModelCollection(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap, String billId) throws SHRWebException {
        Context ctx = SHRContext.getInstance().getContext();
        try {
            return this.listSalaryHistory(billId, ctx);
        } catch (BOSException e) {
            log.error("获取用户{}的历史薪酬履历失败", ContextUtil.getCurrentUserInfo(ctx).getId().toString(), e);
            throw new SHRWebException("获取用户的历史薪酬履历失败");
        }
    }

    @Override
    protected String getRelatedFieldId(HttpServletRequest request, UIViewInfo uiViewInfo) throws SHRWebException {
        return EmployeeWebUtil.getCurrentPersonId(request);
    }

    @Override
    protected void initModelCollection(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        UIViewInfo uiViewInfo = this.getUIViewInfo(request);
        String relatedFieldId = this.getRelatedFieldId(request, uiViewInfo);
        Context ctx = SHRContext.getInstance().getContext();
        CoreBaseCollection collection = new CoreBaseCollection();
        List<CoreBaseInfo> list = new ArrayList<>();
        try {
            list = this.listSalaryHistory(relatedFieldId, ctx);
        } catch (BOSException e) {
            e.printStackTrace();
        }
        int i = 0;

        for(int size = collection.size(); i < size; ++i) {
            collection.get(i).put("attachmentID", "attachment_" + i);
            list.add(collection.get(i));
        }

        modelMap.addAttribute("model_collection", list);
        modelMap.addAttribute("operateState", "VIEW");
        modelMap.addAttribute("relatedField", "person.id");
        modelMap.addAttribute("relatedFieldId", relatedFieldId);
    }

    private List<CoreBaseInfo> listSalaryHistory(String userId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> salaryList = new ArrayList<>();
        List<CoreBaseInfo> result = new ArrayList<>();
        List<EmployeeSalaryChangeEntry> fixAdjustSalaryList = this.getFixAdjustSalaryList(userId, ctx);
        List<EmployeeSalaryChangeEntry> franceChangeList = this.getFranceChangeList(userId, ctx);
        List<EmployeeSalaryChangeEntry> englishChangeList = this.getEnglishChangeList(userId, ctx);
        List<EmployeeSalaryChangeEntry> positionRankList = this.getPositionRankList(userId, ctx);
        EmployeeSalaryChangeEntry currentSiteSalary = this.getCurrentSiteSalary(userId);
        salaryList.addAll(fixAdjustSalaryList);
        salaryList.addAll(franceChangeList);
        salaryList.addAll(englishChangeList);
        salaryList.addAll(positionRankList);
        if (currentSiteSalary != null) {
            salaryList.add(currentSiteSalary);
        }

        List<EmployeeSalaryChangeEntrySortWrapper> wrapperList = this.cloneAndSortSalaryChangeEntry(salaryList);
        Collections.sort(wrapperList, (o1, o2) -> {
            if (o1.getDate().equals(o2.getDate())) {
                return o2.getDateType() - o1.getDateType();
            }
            return o1.getDate().compareTo(o2.getDate());
        });

        // 处理薪酬变更记录
        List<EmployeeSalaryInfo> rawResult = new ArrayList<>();
        if (!wrapperList.isEmpty()) {
            Date datePivot = null;
            EmployeeSalaryInfo employeeSalaryInfo = null;
            for (EmployeeSalaryChangeEntrySortWrapper wrapper : wrapperList) {
                if (null == datePivot) {
                    // first entry
                    datePivot = wrapper.getDate();
                    employeeSalaryInfo = new EmployeeSalaryInfo(wrapper);
                    rawResult.add(employeeSalaryInfo);
                } else {
                    Date currentDate = wrapper.getDate();
                    if (currentDate.equals(datePivot)) {
                        employeeSalaryInfo.populate(wrapper);
                    } else {
                        datePivot = currentDate;
                        employeeSalaryInfo = new EmployeeSalaryInfo(employeeSalaryInfo);
                        employeeSalaryInfo.getSalarySummary().setEffectDate(datePivot);
                        employeeSalaryInfo.populate(wrapper);
                        rawResult.add(employeeSalaryInfo);
                    }
                }
            }
        }
        for (EmployeeSalaryInfo raw : rawResult) {
            raw.calcAndUpdateValue();
            boolean needSkip = raw.getSalarySummary().getBaseAnnualSalary().equals(BigDecimal.ZERO);
            if (!needSkip) {
                result.add(raw);
            }
            raw.complete();
        }
        return result;
    }

    private List<EmployeeSalaryChangeEntry> getFixAdjustSalaryList(String userId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> res = new ArrayList<>();
        String query = "select cmpItem.id, cmpItem.name, effectDay, leffectDay, money where person.id = '" + userId + "' order by cmpItem.name, effectDay asc";
        FixAdjustSalaryCollection fixAdjustSalaryCollection = FixAdjustSalaryFactory.getLocalInstance(ctx).getFixAdjustSalaryCollection(query);
        if (!fixAdjustSalaryCollection.isEmpty()) {
            for (int i = 0; i < fixAdjustSalaryCollection.size(); i++) {
                EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry(fixAdjustSalaryCollection.get(i));
                res.add(entry);
            }
        }
        return res;
    }

    private List<EmployeeSalaryChangeEntry> getFranceChangeList(String userId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> res = new ArrayList<>();
        String query = "SELECT langBnsRank.name, startDate, endDate, person.id, person.name WHERE person.id = '" + userId + "' order by startDate asc";
        PersonLanguageCollection personLanguageCollection = PersonLanguageFactory.getLocalInstance(ctx).getPersonLanguageCollection(query);
        if (!personLanguageCollection.isEmpty()) {
            for (int i = 0; i < personLanguageCollection.size(); i++) {
                EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry(personLanguageCollection.get(i));
                res.add(entry);
            }
        }
        return res;
    }

    private List<EmployeeSalaryChangeEntry> getEnglishChangeList(String userId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> res = new ArrayList<>();
        String query = "SELECT engBnsRank.name, startDate, endDate WHERE person.id = '" + userId + "' order by startDate asc";
        CoreBaseCollection englishCollection = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.EngBns").getCollection(query);
        if (!englishCollection.isEmpty()) {
            for (int i = 0; i < englishCollection.size(); i++) {
                EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry((EmpMultiInfoCustomInfo) englishCollection.get(i));
                res.add(entry);
            }
        }
        return res;
    }

    private List<EmployeeSalaryChangeEntry> getPositionRankList(String userId, Context ctx) throws BOSException {
        List<EmployeeSalaryChangeEntry> res = new ArrayList<>();
        String query = "SELECT jobGrade.name, grpJobgrade.name, mngJobGrade.name, EFFDT, LEFFDT WHERE person.id = '" + userId + "' order by EFFDT asc";
        EmpPostRankCollection postRankCollection = EmpPostRankFactory.getLocalInstance(ctx).getEmpPostRankCollection(query);
        if (!postRankCollection.isEmpty()) {
            for (int i = 0; i < postRankCollection.size(); i++) {
                EmployeeSalaryChangeEntry entry = new EmployeeSalaryChangeEntry(postRankCollection.get((i)));
                res.add(entry);
            }
        }
        return res;
    }

    private EmployeeSalaryChangeEntry getCurrentSiteSalary(String personId) throws BOSException {
        // 获取 成本部门
        String query = "select costCntr.siteBns,isSingle, beginDate where person.id = '" + personId + "' order by beginDate desc";
        CoreBaseCollection personCostCenters = MetaDataUtil.getBizInterface("com.kingdee.eas.hr.emp.emp_page.app.Costcenter").getCollection(query);
        if (personCostCenters.isEmpty()) {
            return null;
        }
        CoreBaseInfo personCurrentCostCenter = personCostCenters.get(0);
        BigDecimal siteSalaryValue = ((BaseItemCustomInfo)personCurrentCostCenter.get("costCntr")).getBigDecimal("sitebns");
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

    private List<EmployeeSalaryChangeEntrySortWrapper> cloneAndSortSalaryChangeEntry(List<EmployeeSalaryChangeEntry> entryList) {
        List<EmployeeSalaryChangeEntrySortWrapper> wrapperList = new ArrayList<>();
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.YEAR, 2);
        Date utilDate = calendar.getTime();
        java.sql.Date yearLimit = new java.sql.Date(utilDate.getTime());
        for (EmployeeSalaryChangeEntry entry : entryList) {
            EmployeeSalaryChangeEntrySortWrapper beginWrapper = new EmployeeSalaryChangeEntrySortWrapper(entry, 1);
            wrapperList.add(beginWrapper);
            String type = entry.getType();
            boolean needSkip = type.equals("英语津贴")
                    || type.equals("法语津贴")
                    || entry.getInEffectDate() == null
                    || entry.getInEffectDate().after(yearLimit);
            if (needSkip) {
                continue;
            }
            EmployeeSalaryChangeEntrySortWrapper endWrapper = new EmployeeSalaryChangeEntrySortWrapper(entry, 2);
            wrapperList.add(endWrapper);
        }
        return wrapperList;
    }

    private static class EmployeeSalaryChangeEntrySortWrapper extends EmployeeSalaryChangeEntry {
        /**
         * 排序日期
         */
        private Date date;
        private Integer dateType;

        public EmployeeSalaryChangeEntrySortWrapper(EmployeeSalaryChangeEntry entry, Integer type) {
            super(entry);
            this.dateType = type;
            if (type.equals(1)) {
                date = entry.getEffectDate();
            } else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(entry.getInEffectDate());
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date utilDate = calendar.getTime();
                this.date = new java.sql.Date(utilDate.getTime());
                setMoney(BigDecimal.ZERO);
                evictRank();
            }
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public Integer getDateType() {
            return dateType;
        }
    }
}

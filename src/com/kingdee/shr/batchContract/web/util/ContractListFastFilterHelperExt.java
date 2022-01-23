package com.kingdee.shr.batchContract.web.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.metadata.entity.FilterInfo;
import com.kingdee.bos.metadata.entity.FilterItemInfo;
import com.kingdee.bos.metadata.query.util.CompareType;
import com.kingdee.util.StringUtils;

/**
 * 合同快速过滤帮助类
 * @author  hj
 * @date    2021/11/22
 *
 */
public class ContractListFastFilterHelperExt extends ContractListFastFilterHelper {
	

    public static FilterInfo convertContractFilter(Map filterItemMap)
        throws BOSException
    {
        FilterInfo filterInfo = null;
        String curViewDimension = (String)filterItemMap.get("dataItem");
        String checkItems = (String)filterItemMap.get("values");
        if(curViewDimension.equals("currentContract"))
            filterInfo = getCurrentContractFilterInfo(checkItems);
        else
        if(curViewDimension.equals("unEffectContract"))
            filterInfo = getUnEffectContractFilterInfo(checkItems);
        else
        if(curViewDimension.equals("historyContract"))
            filterInfo = getHistoryContractFilterInfo(checkItems);
        else
        if(curViewDimension.equals("employee"))
            filterInfo = getEmployeeFilterInfo(checkItems);
        else
        if(curViewDimension.equals("allContract"))
            filterInfo = getAllContractFilterInfo(checkItems);
        return filterInfo;
    }
    
    private static FilterInfo getCurrentContractFilterInfo(String viewParams)
            throws BOSException
        {
            FilterInfo filter = new FilterInfo();
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(1)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(2)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(3)));
            filter.getFilterItems().add(new FilterItemInfo("effectDateLessThanCurDate", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("contractType.isLaborContract", Boolean.valueOf(true)));
            filter.setMaskString("(#0 OR #1 OR #2) and #3 and #4");
            mergeCheckItemFilter(viewParams, filter);
            return filter;
        }

        private static FilterInfo getUnEffectContractFilterInfo(String viewParams)
            throws BOSException
        {
            FilterInfo filter = new FilterInfo();
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(0)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(1)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(2)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(3)));
            filter.getFilterItems().add(new FilterItemInfo("effectDateLaterThanCurDate", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("contractType.isLaborContract", Boolean.valueOf(true)));
            filter.setMaskString("(#0 OR ((#1 OR #2 OR #3) AND #4 and #5))");
            mergeCheckItemFilter(viewParams, filter);
            return filter;
        }

        private static FilterInfo getHistoryContractFilterInfo(String viewParams)
            throws BOSException
        {
            FilterInfo filter = new FilterInfo();
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(4)));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(5)));
            filter.getFilterItems().add(new FilterItemInfo("contractType.isLaborContract", Boolean.valueOf(true)));
            filter.setMaskString("(#0 OR #1) and #2");
            mergeCheckItemFilter(viewParams, filter);
            return filter;
        }

        private static FilterInfo getEmployeeFilterInfo(String viewParams)
            throws BOSException
        {
            FilterInfo filter = new FilterInfo();
            Set stateSet = new HashSet();
            stateSet.add(Integer.valueOf(0));
            stateSet.add(Integer.valueOf(1));
            stateSet.add(Integer.valueOf(3));
            stateSet.add(Integer.valueOf(2));
            stateSet.add(Integer.valueOf(4));
            stateSet.add(Integer.valueOf(5));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", stateSet, CompareType.INNER));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", null, CompareType.IS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.id", null, CompareType.IS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.isNeedSignContract", new Integer(0), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.isNewestContract", new Integer(1), CompareType.EQUALS));
            filter.setMaskString("((#0 OR #1) and (#2 OR #3 OR #4))");
            mergeCheckItemFilter(viewParams, filter);
            return filter;
        }

        private static FilterInfo getAllContractFilterInfo(String viewParams)
            throws BOSException
        {
            FilterInfo filter = new FilterInfo();
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.id", null, CompareType.ISNOT));
            filter.getFilterItems().add(new FilterItemInfo("contractType.isLaborContract", Boolean.valueOf(true)));
            filter.setMaskString("#0 AND #1");
            mergeCheckItemFilter(viewParams, filter);
            return filter;
        }

        private static void mergeCheckItemFilter(String viewParams, FilterInfo filter)
            throws BOSException
        {
            FilterInfo checkItemFilter = new FilterInfo();
            if(!StringUtils.isEmpty(viewParams) && !viewParams.equals("contract-select"))
            {
                String checkItems[] = viewParams.split(",");
                for(int i = 0; i < checkItems.length; i++)
                    if(!StringUtils.isEmpty(checkItems[i]))
                        checkItemFilter.mergeFilter(combineFilterInfo(checkItems[i]), "OR");

                filter.mergeFilter(checkItemFilter, "AND");
            }
        }
 
    
    private static FilterInfo combineFilterInfo(String checkItem)
    {
    	
        FilterInfo filter = new FilterInfo();
        if(checkItem.equals("fixedLimitSignTwoTime"))
        {
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.contractLimitType", "1"));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.continuousSignedCount", Integer.valueOf(2)));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        } else
        if(checkItem.equals("notFixedLimit"))
        {
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.contractLimitType", "2"));
            filter.getFilterItems().add(new FilterItemInfo("contractType.isLaborContract", Boolean.valueOf(true)));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        } else
        if(checkItem.equals("laborContractEndOneMonth"))
        {
            filter.getFilterItems().add(new FilterItemInfo("contractEndInOneMonth", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        } else
        if(checkItem.equals("laborContractEndThreeMonth"))
        {
            filter.getFilterItems().add(new FilterItemInfo("contractEndInThreeMonth", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        } else
        if(checkItem.equals("teminatedContract"))
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(4)));
        else
        if(checkItem.equals("freedContract"))
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(5)));
        else
        if(checkItem.equals("notSign"))
        {
            Set typeSet = new HashSet();
            typeSet.add(Integer.valueOf(0));
            typeSet.add(Integer.valueOf(2));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.id", null, CompareType.IS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", typeSet, CompareType.INNER));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.isNeedSignContract", Boolean.valueOf(true)));
            filter.getFilterItems().add(new FilterItemInfo("contractNotEffect", Integer.valueOf(0)));
            filter.setMaskString("(#0 or (#1 and #2))and #3");
        } else
        if(checkItem.equals("noAnyEmployeeContract"))
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.id", null, CompareType.IS));
        else
        if(checkItem.equals("noAnyEmpContract_or_curContractEnd"))
        {
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.id", null, CompareType.IS));
            filter.getFilterItems().add(new FilterItemInfo("isPersonCurContract", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(4)));
            filter.setMaskString("( #0 or (#1 and #2))");
        } else
        if(checkItem.equals("dontSign"))
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.isNeedSignContract", Boolean.valueOf(false)));
        else
        if(checkItem.equals("endNotSign"))
        {
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(4)));
            filter.getFilterItems().add(new FilterItemInfo("isPersonCurContract", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.effectDate", new Date(), CompareType.LESS_EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.effectDate", null, CompareType.IS));
            filter.setMaskString("( #0 and #1 and( #2 or #3))");
        } else
        if(checkItem.equals("tenYearEmployee"))
        {
            filter.getFilterItems().add(new FilterItemInfo("joinCompanyThanTenYears", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        }else if(checkItem.equals("laborContractEnd45Day")) {
        	//45天内
            filter.getFilterItems().add(new FilterItemInfo("contractEnd45Day", new Integer(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("employeeContract.state", Integer.valueOf(1), CompareType.EQUALS));
            filter.getFilterItems().add(new FilterItemInfo("personContractPage.hasSignContract", Integer.valueOf(2), CompareType.NOTEQUALS));
        }
        return filter;
    }
	
	
}

package com.kingdee.eas.hr.affair.app;

import java.util.Date;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.IObjectCollection;
import com.kingdee.bos.dao.IObjectPK;
import com.kingdee.bos.dao.IObjectValue;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.affair.AffairBizCheckFacadeFactory;
import com.kingdee.eas.hr.affair.AffairBizException;
import com.kingdee.eas.hr.affair.HRAffairBizBillEntryInfo;
import com.kingdee.eas.hr.affair.HRAffairBizBillInfo;
import com.kingdee.eas.hr.affair.IAffairBizCheckFacade;
import com.kingdee.eas.hr.base.EmpAffairBizBillException;
import com.kingdee.eas.hr.base.HRBillBaseEntryInfo;
import com.kingdee.eas.hr.base.HRBillBaseInfo;
import com.kingdee.eas.hr.base.batch.BatchProcessedResult;
import com.kingdee.eas.hr.base.util.CheckEmployeeTypeOperatorUtil;
import com.kingdee.util.StringUtils;

/**
 * 职等调整实体bean
 * @author hj
 *
 */
public class RiseBillBizBeanEx extends RiseBillBizBean {
	
 
/**
 * 校验，去掉单据的校验信息
 */
  @Override
   public void _check(Context ctx, IObjectPK pk, IObjectValue model)
	        throws BOSException, EASBizException
	    {
	        HRBillBaseInfo info = (HRBillBaseInfo)model;
	        String entryField = getEntryField();
	        IObjectCollection entries = (IObjectCollection)info.get(entryField);
	        HRBillBaseEntryInfo entry = null;
	        if(entries != null)
	        {
	            String personField = getPersonField();
	            PersonInfo person = null;
	            String billID = null;
	            if(pk != null)
	                billID = pk.toString();
	            StringBuffer personIds = new StringBuffer();
	            int i = 0;
	            for(int size = entries.size(); i < size; i++)
	            {
	                entry = (HRBillBaseEntryInfo)entries.getObject(i);
	                if(!StringUtils.isEmpty(personField))
	                {
	                    person = (PersonInfo)entry.get(personField);
	                    if(person != null && person.getId() != null)
	                    {
	                        String personID = person.getId().toString();
	                        personIds.append(personID);
	                        personIds.append(",");
                    }
                }
                _check(ctx, entry);
                if(isNeedCheckJobLevelGradeRange())
                    checkJobLevelGradeRange(ctx, (HRAffairBizBillInfo)info, (HRAffairBizBillEntryInfo)entry);
            }

            _check(ctx, info);
            IAffairBizCheckFacade checkFacade = AffairBizCheckFacadeFactory.getLocalInstance(ctx);
            BatchProcessedResult result = null;
//		            result = checkFacade.isPersonInAffairProcess(personIds.toString(), billID);
            if(result != null && result.isHasValidInfo())
                throw new AffairBizException(AffairBizException.BILL_CHECK_FAILED, new Object[] {
                    result.getInvalidInfo()
                });
            if(isCheckPersonInPool())
            {
                result = checkFacade.isPersonInPool(personIds.toString());
                if(result != null && result.isHasValidInfo())
                    throw new AffairBizException(AffairBizException.BILL_CHECK_FAILED, new Object[] {
                        result.getInvalidInfo()
                    });
            }
            if(isCheckPersonLoan())
            {
                result = checkFacade.checkExistPresentPersonLoan(personIds.toString());
                if(result != null && result.isHasValidInfo())
                    throw new AffairBizException(AffairBizException.BILL_CHECK_FAILED, new Object[] {
                        result.getInvalidInfo()
                    });
            }
            if(isCheckPersonFlucOut())
            {
                result = checkFacade.isPersonFlucOut(personIds.toString(), billID);
                if(result != null && result.isHasValidInfo())
                    throw new AffairBizException(AffairBizException.BILL_CHECK_FAILED, new Object[] {
                        result.getInvalidInfo()
                    });
            }
            if(isCheckPersonLoanOut())
            {
                result = checkFacade.isPersonLoanOut(personIds.toString(), billID);
                if(result != null && result.isHasValidInfo())
                    throw new AffairBizException(AffairBizException.BILL_CHECK_FAILED, new Object[] {
                        result.getInvalidInfo()
                    });
            }
        }
    }
  
  
  @Override
	protected void checkHRBizDefineData(Context ctx, HRAffairBizBillEntryInfo entry)
			throws EASBizException, BOSException {
//        HRAffairBizBillEntryValidateHelper helper = new HRAffairBizBillEntryValidateHelper();
//        helper.validateFieldNotNull(ctx, entry, (Date)entry.get(getEntryEffectDateField()));
	}
  
  protected void checkPersonAfterDateModify(Context ctx, HRBillBaseEntryInfo entry)
	        throws EASBizException, BOSException
	    {
	        StringBuffer exceptionMsg = new StringBuffer();
            String bizdateField = getEntryEffectDateField();
            String personField = getPersonField();
            String personId = "";
            if(!StringUtils.isEmpty(personField))
            {
                PersonInfo person = (PersonInfo)entry.get(personField);
                if(person != null && person.getId() != null)
                    personId = person.getId().toString();
            }
            Date bizDate = (Date)entry.get(bizdateField);
            String targetEmpTypeId = null;
            String targetPositionId = null;
            String targetOrgAdminId = null;
            String actionId = null;
            if(entry.get("empType") != null)
                targetEmpTypeId = ((CoreBaseInfo)entry.get("empType")).getId().toString();
            if(entry.get("position") != null)
                targetPositionId = ((CoreBaseInfo)entry.get("position")).getId().toString();
            if(entry.get("adminOrg") != null)
                targetOrgAdminId = ((CoreBaseInfo)entry.get("adminOrg")).getId().toString();
            if(entry.get("hrBizDefine") != null)
                actionId = ((CoreBaseInfo)entry.get("hrBizDefine")).getId().toString();
//	            CheckEmployeeTypeOperatorUtil.checkPersonAfterDateModify(ctx, personId, bizDate, actionId, targetEmpTypeId, targetPositionId, targetOrgAdminId);
//	         
	        if(!StringUtils.isEmpty(exceptionMsg.toString()))
	            throw new EmpAffairBizBillException(EmpAffairBizBillException.ANYTHING, new Object[] {
	                exceptionMsg
	            });
	        else
	            return;
	    }

}

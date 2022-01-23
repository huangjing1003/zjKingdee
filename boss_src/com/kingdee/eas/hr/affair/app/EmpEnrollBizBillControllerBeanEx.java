package com.kingdee.eas.hr.affair.app;

import java.util.Iterator;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.org.HROrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.affair.EmpEnrollBizBillEntryCollection;
import com.kingdee.eas.hr.affair.EmpEnrollBizBillEntryInfo;
import com.kingdee.eas.hr.affair.EmpEnrollBizBillFactory;
import com.kingdee.eas.hr.affair.EmpEnrollBizBillInfo;
import com.kingdee.eas.hr.base.HRBillBaseEntryInfo;
import com.kingdee.eas.hr.emp.PersonOptFacadeFactory;
import com.kingdee.eas.util.app.DbUtil;

/**
 * 入职反写
 * @author hj
 *
 */
public class EmpEnrollBizBillControllerBeanEx extends  EmpEnrollBizBillControllerBean{

	@Override
	protected void _entryEffect(Context ctx, HRBillBaseEntryInfo entry)
			throws BOSException, EASBizException {
		super._entryEffect(ctx, entry);
		EmpEnrollBizBillInfo info = (EmpEnrollBizBillInfo) entry.get("bill");
		String billID = info.getId().toString();
		EmpEnrollBizBillInfo billInfo = EmpEnrollBizBillFactory
				.getLocalInstance(ctx).getEmpEnrollBizBillInfo(
						new ObjectUuidPK(billID));
		EmpEnrollBizBillEntryCollection entrys = billInfo.getEntrys();
		HROrgUnitInfo hrOrgUnit = billInfo.getHrOrgUnit();
		Iterator entryInter = entrys.iterator();
		while (entryInter.hasNext()) {
			EmpEnrollBizBillEntryInfo entryInfo = (EmpEnrollBizBillEntryInfo) entryInter
					.next();
			if (entryInfo.getPerson() != null) {
				PersonInfo personInfo = entryInfo.getPerson();
				//根据人员的id反写人员的人事业务组织
				if(hrOrgUnit!=null){
					BOSUuid personId = personInfo.getId();
					String hrOrgUnitId = hrOrgUnit.getId().toString();
					/**
					 * 更新人员基本信息表
					 */
					String updateSQL = "update T_BD_Person set FHRORGUNITID = '"+hrOrgUnitId+"' where fid = '"+personId+"'";
					DbUtil.execute(ctx, updateSQL);
					/**
					 * 更新人员基本信息历史表
					 */
					String updateHisSQL = " update T_BD_PersonHis set FHRORGUNITID = '"+hrOrgUnitId+"'  where  FHISTORYRELATEID = '"+personId+"'";
					DbUtil.execute(ctx, updateHisSQL);
					
				}
			}
		}
	}
	
	
	
}

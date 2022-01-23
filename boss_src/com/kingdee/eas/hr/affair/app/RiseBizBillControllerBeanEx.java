package com.kingdee.eas.hr.affair.app;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.IObjectValue;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;
import com.kingdee.bos.metadata.entity.SelectorItemInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.affair.HRAffairBizBillEntryInfo;
import com.kingdee.eas.hr.affair.HRAffairBizBillInfo;
import com.kingdee.eas.hr.affair.HRAffairException;
import com.kingdee.eas.hr.affair.RiseBizBillEntryCollection;
import com.kingdee.eas.hr.affair.RiseBizBillEntryInfo;
import com.kingdee.eas.hr.affair.RiseBizBillInfo;
import com.kingdee.eas.hr.base.app.IHRBillBizBean;
import com.kingdee.eas.hr.base.util.HRParamUtil;
import com.kingdee.eas.hr.base.util.HRUtilExtend;
import com.kingdee.eas.hr.org.JobGradeFactory;
import com.kingdee.eas.hr.org.JobGradeInfo;
import com.kingdee.eas.hr.org.JobLevelFactory;
import com.kingdee.eas.hr.org.JobLevelInfo;

/**
 * 扩展职等调整，去掉校验职级没有改变
 * @author  hj
 */
public class RiseBizBillControllerBeanEx extends RiseBizBillControllerBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5507344799282690965L;

	/**
	 * 提交之前增加校验
	 */
	@Override
	protected void checkBeforeSubmit(Context ctx, IObjectValue model)
			throws EASBizException, BOSException {
		new HRAffairBizBillControllerBean().checkBeforeSubmit(ctx, model);
//		super.checkBeforeSubmit(ctx, model);
		RiseBizBillInfo info = (RiseBizBillInfo) model;
		RiseBizBillEntryCollection eColl = info.getEntrys();
		RiseBizBillEntryInfo eInfo = null;
		for (int i = 0; i < eColl.size(); ++i) {
			eInfo = eColl.get(i);

			if ((eInfo.getOldJobGrade() != null)
					&& (eInfo.getJobGrade() == null)) {
				eInfo.setJobGrade(eInfo.getOldJobGrade());
			}
			if ((eInfo.getOldJobLevel() != null)
					&& (eInfo.getJobLevel() == null)) {
				eInfo.setJobLevel(eInfo.getOldJobLevel());
			}

			if ((eInfo.getJobGrade() == null) && (eInfo.getJobLevel() == null)) {
				throw new HRAffairException(
						HRAffairException.JOBGRADE_LEVEL_CANT_NULL);
			}
		}
	}
	
	
	
	/**
	 * 保存之前增加校验
	 */
	@Override
	protected HRAffairBizBillEntryInfo storefieldsForEntry(Context ctx,
			HRAffairBizBillInfo hrBillInfo, HRAffairBizBillEntryInfo entryInfo)
			throws EASBizException, BOSException {
		RiseBizBillInfo billInfo = (RiseBizBillInfo) hrBillInfo;
		entryInfo.put("bill", billInfo);
		String clearJobGrade = (String) billInfo.get("isClearJobGrade");
		if ((entryInfo.getJobLevel() == null)
				&& (entryInfo.getJobGrade() == null)) {
			throw new HRAffairException(
					HRAffairException.JOBGRADE_LEVEL_CANT_NULL);
		}
		JobGradeInfo jobGradeInfo = entryInfo.getJobGrade();
		JobLevelInfo jobLevelInfo = entryInfo.getJobLevel();
		new HRAffairBizBillControllerBean().storefieldsForEntry(ctx, billInfo, entryInfo);
//		super.storefieldsForEntry(ctx, billInfo, entryInfo);
		if ("true".equals(clearJobGrade)) {
			jobLevelInfo = JobLevelFactory.getLocalInstance(ctx)
					.getJobLevelInfo(new ObjectUuidPK(jobLevelInfo.getId()));
			if ((jobGradeInfo == null) && (jobLevelInfo != null)
					&& (jobLevelInfo.getJobGrade() == null)
					&& (jobLevelInfo.getLowestJobGrade() == null)) {
				entryInfo.setJobGrade(jobGradeInfo);
			}
		}
//		if (isJobLevelAndJobGradNotChange((RiseBizBillEntryInfo) entryInfo)) {
//			throw new HRAffairException(
//					HRAffairException.JOBGRADE_LEVEL_NO_CHANGE);
//		}
		checkJobGradeLevelRange(ctx, (RiseBizBillEntryInfo) entryInfo);
		setJobSystemInfo(ctx, (RiseBizBillEntryInfo) entryInfo);
		return entryInfo;
	}
	
	
	private void setJobSystemInfo(Context ctx, RiseBizBillEntryInfo entryInfo)
	throws BOSException, EASBizException {
		if ((entryInfo.getJobGrade() != null)
				&& (entryInfo.getJobGrade().getId() != null)
				&& (entryInfo.getJobGradeModule() == null)) {
			JobGradeInfo jobGradeInfo = JobGradeFactory.getLocalInstance(ctx)
					.getJobGradeInfo(
							new ObjectUuidPK(entryInfo.getJobGrade().getId()));
			entryInfo.setJobGradeModule(jobGradeInfo.getJobGradeModule());
		}
		if ((entryInfo.getJobLevel() != null)
				&& (entryInfo.getJobLevel().getId() != null)
				&& (entryInfo.getJobLevelProject() == null)) {
			JobLevelInfo jobLevelInfo = JobLevelFactory.getLocalInstance(ctx)
					.getJobLevelInfo(
							new ObjectUuidPK(entryInfo.getJobLevel().getId()));
			entryInfo.setJobLevelProject(jobLevelInfo.getJobLevelProject());
		}
	}
	
	private void checkJobGradeLevelRange(Context ctx,
			RiseBizBillEntryInfo entryInfo) throws EASBizException,
			BOSException {
		boolean isRelatePositionJobRange = HRParamUtil
				.isRelatePositionJobRange(ctx);
		if ((!(isRelatePositionJobRange)) && (entryInfo.getJobLevel() != null)
				&& (entryInfo.getJobGrade() != null)) {
			SelectorItemCollection sic = new SelectorItemCollection();
			sic.add(new SelectorItemInfo("id"));
			sic.add(new SelectorItemInfo("lowestJobGrade.index"));
			sic.add(new SelectorItemInfo("jobGrade.index"));
			sic.add(new SelectorItemInfo("jobLevelProject.jobGradeModule.id"));
			JobLevelInfo jlInfo = JobLevelFactory.getLocalInstance(ctx)
					.getJobLevelInfo(
							new ObjectUuidPK(entryInfo.getJobLevel().getId()),
							sic);
			JobGradeInfo jgInfo = JobGradeFactory.getLocalInstance(ctx)
					.getJobGradeInfo(
							new ObjectUuidPK(entryInfo.getJobGrade().getId()));
			int index = jgInfo.getIndex();
			if ((jlInfo.getJobLevelProject() == null)
					|| (jlInfo.getJobLevelProject().getJobGradeModule() == null)
					|| (jlInfo.getJobLevelProject().getJobGradeModule().getId() == null)
					|| ((HRUtilExtend.isObjectEquelsByPkValue(jgInfo
							.getJobGradeModule(), jgInfo.getJobGradeModule()))
							&& (jlInfo.getLowestJobGrade() != null)
							&& (index >= jlInfo.getLowestJobGrade().getIndex())
							&& (jlInfo.getJobGrade() != null) && (index <= jlInfo
							.getJobGrade().getIndex()))) {
				return;
			}
			throw new HRAffairException(
					HRAffairException.JOBGREADE_MUST_IN_MODULE);
		}
	}
	

	@Override
	protected IHRBillBizBean getBizBean() {
		return new RiseBillBizBeanEx();
	}
	
	
}

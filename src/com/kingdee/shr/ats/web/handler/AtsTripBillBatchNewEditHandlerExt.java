package com.kingdee.shr.ats.web.handler;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.kingdee.eas.basedata.org.HROrgUnitInfo;
import com.kingdee.eas.hr.ats.*;
import com.kingdee.eas.hr.ats.util.AtsAttendanceFileUtils;
import com.kingdee.eas.hr.base.HRBillStateEnum;
import com.kingdee.shr.ats.web.util.AtsWebUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.ui.ModelMap;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.ats.util.AtsDateUtils;
import com.kingdee.eas.hr.ats.util.common.MLUtile;
import com.kingdee.eas.hr.emp.PersonPositionInfo;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.ats.web.util.HRTimeWebUtils;
import com.kingdee.shr.ats.web.util.SHRBillUtil;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;

/**
 * 名称：批量出差单据
 * 模型：
 * uipk:
 * @author  hj
 */
public class AtsTripBillBatchNewEditHandlerExt  extends AtsTripBillBatchNewEditHandler{

	private static Logger logger = Logger.getLogger(AtsTripBillBatchNewEditHandlerExt.class);
	private Context ctx = SHRContext.getInstance().getContext();
	/**
	 * 点击创建多人单据时，给考勤业务组织和出差信息赋值
	 * 当组织的组织类型为项目板块时取他的项目，部门板块时取他的部门
	 */
	@Override
	protected void afterCreateNewModel(HttpServletRequest request, 
			HttpServletResponse  response, CoreBaseInfo coreBaseInfo)
			throws SHRWebException {
		super.afterCreateNewModel(request, response, coreBaseInfo);
		//出差单的主单据信息
		AtsTripBillInfo atsTripBillInfo = (AtsTripBillInfo)coreBaseInfo;
		//获取当前的登录人
        PersonInfo personInfo = SHRBillUtil.getCurrPersonInfo();
        PersonPositionInfo personPositionInfo = SHRBillUtil.getAdminOrgUnit(personInfo.getId().toString());
        if(personInfo==null) {
			throw new ShrWebBizException("当前用户未关联职员，不能发起出差单!!");
        }
		//上下文环境
        Context ctx = SHRContext.getInstance().getContext();
		//当前登录人员的id
        String personId = personInfo.getId().toString();
        /**
         * 获取人员的基本信息字段
         */
	   Map<String, Object> map;
		try {
			map = PersonUtil.getPersonInfo(personId, ctx);
			if(map!=null && map.size()>0) {
				Object ctsIdObj = map.get("ctsId");
				Object ctsName = map.get("ctsName");
				Object nCellObj = map.get("nCell");
				Object officePhoneObj = map.get("officePhone");
				String ctsId = ObjectUtils.toString(ctsIdObj, "");
				if(!StringUtils.isBlank(ctsId)) {//不为空时再赋值
					BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
					customInfo.setId(BOSUuid.read(ctsId)); //设置成本部门id
					customInfo.setName(ctsName.toString());//设置成本部门名称
					atsTripBillInfo.put("costCenter", customInfo);
					atsTripBillInfo.put("addrInfo", nCellObj);//联系方式
				}
			}
		} catch (EASBizException e) {
			e.printStackTrace();
		} catch (BOSException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 保存和提交前的校验，当出差交通票购买方式为公司前方购买和改签休假机票时校验需要填写行程信息
	 */
	@Override
	protected void verifyModel(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo model)
			throws SHRWebException {
		super.verifyModel(request, response, model);
		Context ctx = SHRContext.getInstance().getContext();
		//TripTicketType
		AtsTripBillInfo atsTripBillInfo = (AtsTripBillInfo)model;//出差单的主单据信息
		Object TripTicketTypeObj = atsTripBillInfo.get("TripTicketType");
		if(TripTicketTypeObj!=null) {
			   BaseItemCustomInfo customInfo = (BaseItemCustomInfo)TripTicketTypeObj;
			   String  tripTicketTypeId = customInfo.getId().toString();
			   String querySQL = "SELECT FNAME_L2 name,FNUMBER number  FROM CT_CUS_TripTicketType where fid='"+tripTicketTypeId+"'";
			   try {
				IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
				if(iRowSet.next()) {
					String name = iRowSet.getString("name");
					String number = iRowSet.getString("number");
					if(number.contains("02") || number.contains("05") ) {
						Object ticketObj = atsTripBillInfo.get("ticket");
						if(ticketObj==null) {
							throw new ShrWebBizException("出差交通票购买方式为【"+name+"】,请填写行程信息!!");
						}
					}
				}
			} catch (BOSException e) {
				e.printStackTrace();
				throw new ShrWebBizException(e.getMessage());
			} catch (SQLException e) {
				e.printStackTrace();
				throw new ShrWebBizException(e.getMessage());
			}
		}
	}


	@Override
	public void DBRepeatDataCheckedAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException, BOSException, EASBizException, SQLException {
		String delimiter = request.getParameter("delimiter").trim();
		String[] id = request.getParameter("id").split(delimiter);
		String[] rowNum = request.getParameter("rowNum").split(delimiter);
		String[] personID = request.getParameter("personID").split(delimiter);
		String[] tripStartTime = request.getParameter("tripStartTime").split(delimiter);
		String[] tripEndTime = request.getParameter("tripEndTime").split(delimiter);
		String[] tripDays = request.getParameter("tripDays").split(delimiter);
		boolean deletedFlag = Boolean.parseBoolean(request.getParameter("deletedFlag"));
		boolean isElasticCalLen = request.getParameter("isElasticCalLen") == null ? false : Boolean.valueOf(request.getParameter("isElasticCalLen"));
		String deletedList = "";
		if (deletedFlag) {
			deletedList = request.getParameter("deletedList");
			deletedList = "('" + deletedList.replace(",", "','") + "')";
		}

		int len = id.length;
		StringBuffer errorSb = new StringBuffer("");

		int i;
		String split;
		String personId;
		String beginTimes;
		String endTimes;
		String begin_Time;
//		for(i = 0; i < len; ++i) {
//			split = " ";
//			personId = personID[i];
//			beginTimes = tripStartTime[i];
//			endTimes = tripEndTime[i];
//			begin_Time = tripDays[i];
//			if (beginTimes.length() != 19) {
//				beginTimes = beginTimes + ":00";
//			}
//
//			if (endTimes.length() != 19) {
//				endTimes = endTimes + ":00";
//			}

//			Timestamp beginTimeCount = HRTimeWebUtils.stringToTimestamp(beginTimes);
//			Timestamp endTimeCount = HRTimeWebUtils.stringToTimestamp(endTimes);
//			AtsLeaveBillEditHandler handler = new AtsLeaveBillEditHandler();
//			double realLength = handler.getRealLeaveLengthInfo(this.ctx, personId, "", beginTimeCount, endTimeCount, isElasticCalLen);
//			double day = Double.parseDouble(begin_Time);
//			BigDecimal days = (new BigDecimal(day)).setScale(2, RoundingMode.HALF_UP);
//			BigDecimal realLengths = (new BigDecimal(realLength)).setScale(2, RoundingMode.HALF_UP);
//			if (days.compareTo(realLengths) != 0) {
//				errorSb.append(MLUtile.getRes(AtsOrdinalCommonResEnum.IncomformTripStartEndTimeRow, this.ctx, new Object[]{rowNum[i]}));
//			}
//		}

		for(i = 0; i < len; ++i) {
			split = " ";
			personId = personID[i];
			beginTimes = tripStartTime[i];
			endTimes = AtsDateUtils.getFullTime(AtsDateUtils.getTimeFromLongString(beginTimes));
			begin_Time = AtsDateUtils.getDateFromLongString(beginTimes) + split + endTimes;
			String endTime = tripEndTime[i];
			String endtime = AtsDateUtils.getFullTime(AtsDateUtils.getTimeFromLongString(endTime));
			String end_Time = AtsDateUtils.getDateFromLongString(endTime) + split + endtime;
			String billId = id[i];
			String sql = "";
			if (billId.length() <= 4) {
				sql = " select bill.fid,bill.fapplydate,bill.fbillstate,bill.fproposer,bill.fnumber,entry.FTripStartTime,entry.FTripEndTime,entry.FRealTripStartTime,entry.FRealTripEndTime,entry.fpersonid  from T_HR_ATS_TripBill  bill left join T_HR_ATS_TripBillEntry entry  on  bill.fid = entry.fbillid  where   entry.fpersonid='" + personId + "' " + " and   bill.fbillstate != 0 " + " and   bill.fbillstate != 4 ";
				if (deletedFlag) {
					sql = sql + "and entry.fid not in " + deletedList;
				}

				sql = sql + " order  by bill.fapplydate desc ";
			}

			if (billId.length() > 4) {
				sql = " select bill.fid,bill.fapplydate,bill.fbillstate,bill.fproposer,bill.fnumber,entry.FTripStartTime,entry.FTripEndTime,entry.FRealTripStartTime,entry.FRealTripEndTime,entry.fpersonid  from T_HR_ATS_TripBill  bill left join T_HR_ATS_TripBillEntry entry  on  bill.fid = entry.fbillid  where   entry.fpersonid='" + personId + "' " + " and   entry.fid !='" + billId + "' " + " and   bill.fbillstate != 0 " + " and   bill.fbillstate != 4 ";
				if (deletedFlag) {
					sql = sql + "and entry.fid not in " + deletedList;
				}

				sql = sql + " order  by bill.fapplydate desc ";
			}

			IRowSet row = null;
			row = DbUtil.executeQuery(this.ctx, sql);
			AtsTripBillCollection atsTripBillCollection = new AtsTripBillCollection();

			while(row.next()) {
				AtsTripBillInfo billInfo = new AtsTripBillInfo();
				billInfo.setId(BOSUuid.read(row.getString("FID")));
				PersonInfo pInfo = new PersonInfo();
				pInfo.setId(BOSUuid.read(row.getString("FPROPOSER")));
				billInfo.setProposer(pInfo);
				billInfo.setApplyDate(row.getDate("FAPPLYDATE"));
				billInfo.setNumber(row.getString("FNumber"));
				AtsTripBillEntryInfo entryInfo = new AtsTripBillEntryInfo();
				entryInfo.setTripStartTime(row.getTimestamp("FTripStartTime"));
				entryInfo.setTripEndTime(row.getTimestamp("FTripEndTime"));
				entryInfo.setRealTripStartTime(row.getTimestamp("FRealTripStartTime"));
				entryInfo.setRealTripEndTime(row.getTimestamp("FRealTripEndTime"));
				PersonInfo info = new PersonInfo();
				info.setId(BOSUuid.read(row.getString("FPersonID")));
				entryInfo.setPerson(info);
				billInfo.getEntries().add(entryInfo);
				atsTripBillCollection.add(billInfo);
			}

			int sum = 0;
			String billNo = "";
			String personName = "";
			String tripBeginDate = "";
			String tripEndDate = "";
			if (atsTripBillCollection.size() == 0) {
				break;
			}

			if (atsTripBillCollection.size() > 0) {
				String resValue = "0";
				int j = 0;

				for(int size = atsTripBillCollection.size(); j < size; ++j) {
					Date beginDate_DB = new Date();
					if (atsTripBillCollection.get(j).getEntries().get(0).getTripStartTime() != null) {
						beginDate_DB = HRTimeWebUtils.timestampToDate(atsTripBillCollection.get(j).getEntries().get(0).getRealTripStartTime());
					}

					Date endDate_DB = new Date();
					if (atsTripBillCollection.get(j).getEntries().get(0).getTripEndTime() != null) {
						endDate_DB = HRTimeWebUtils.timestampToDate(atsTripBillCollection.get(j).getEntries().get(0).getRealTripEndTime());
					}

					resValue = HRTimeWebUtils.getTimeCoincidence(HRTimeWebUtils.stringToLongDate(begin_Time), HRTimeWebUtils.stringToLongDate(end_Time), beginDate_DB, endDate_DB);
					if (Double.parseDouble(resValue) > 0.0D) {
						++sum;
						billNo = atsTripBillCollection.get(j).getNumber();
						String personid = atsTripBillCollection.get(j).getEntries().get(0).getPerson().getId().toString();
						PersonPositionInfo positionInfo = SHRBillUtil.getAdminOrgUnit(personid);
						personName = positionInfo.getPerson().getName();
						tripBeginDate = HRTimeWebUtils.dateLongToString(beginDate_DB);
						tripEndDate = HRTimeWebUtils.dateLongToString(endDate_DB);
						errorSb.append(MLUtile.getRes(AtsTripBillResEnum.OverlapTripBillTimeRow, this.ctx, new Object[]{rowNum[i], billNo, personName, tripBeginDate, tripEndDate}));
					} else if (Double.parseDouble(resValue) == 0.0D) {
						sum += 0;
					} else {
						sum = -1;
					}

					if (sum > 0) {
						break;
					}
				}
			}
		}

		Map<String, Object> res = new HashMap();
		res.put("errorTag", errorSb.toString().equals(""));
		res.put("errorLog", errorSb.toString());
		JSONUtils.writeJson(response, res);
	}

	@Override
	protected void beforeSave(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo model) throws SHRWebException {
		this.verifyModel(request, response, model);
		AtsTripBillInfo billInfo = (AtsTripBillInfo)model;
		String errorString = "";

		try {
			errorString = super.validateIsFillTrip(this.ctx, billInfo);
			if (!errorString.equals("")) {
				throw new ShrWebBizException(errorString);
			}
		} catch (EASBizException var7) {
			var7.printStackTrace();
		} catch (BOSException var8) {
			var8.printStackTrace();
		}

		if (billInfo.getBillState() == null) {
			billInfo.setBillState(HRBillStateEnum.SAVED);
		}

		this.storefields(request, model);
	}

	private AtsTripBillInfo storefields(HttpServletRequest request, CoreBaseInfo model) throws SHRWebException {
		AtsTripBillInfo billInfo = (AtsTripBillInfo)model;
		String hrorgunitid = billInfo.getHrOrgUnit().getId().toString();

		for(int i = 0; i < billInfo.getEntries().size(); ++i) {
			AtsTripBillEntryInfo entry = billInfo.getEntries().get(i);
			String temp_personID = entry.getPerson().getId().toString();
			Timestamp beginTime = entry.getTripStartTime();
			Timestamp endTime = entry.getTripEndTime();

			try {
				AttendanceFileHISInfo fileHisInfo = null;
				fileHisInfo = AtsAttendanceFileUtils.getAttendanceFileHISInfoByAttenceDate(this.ctx, beginTime, endTime, temp_personID);
				if (fileHisInfo == null) {
					throw new SHRWebException((new AtsFileBizException(AtsFileBizException.NOTEXISTFILEHIS)).getMessage());
				}

//				if (!hrorgunitid.equals(fileHisInfo.getHrOrgUnit().getString("id"))) {
//					throw new BOSException((new AtsBillBizException(AtsBillBizException.COMMITCROSSHRORG)).getMessage());
//				}
				entry.setAttAdminOrgUnit(fileHisInfo.getAttAdminOrgUnit());
				entry.setIsDefaultManage(fileHisInfo.isIsDefaultManage());
				entry.setAdminOrgUnit(fileHisInfo.getAdminOrgUnit());
				entry.setPosition(fileHisInfo.getPosition());
				entry.setRealTripStartTime(entry.getTripStartTime());
				entry.setRealTripEndTime(entry.getTripEndTime());
				entry.setRealTripDays(entry.getTripDays());
				entry.setIsCancelTrip(TripbillCancelEnum.notconfirm);
				entry.setIsCalOt(false);
			} catch (Exception var11) {
				throw new ShrWebBizException(var11.getMessage());
			}

			entry.setIsCancelTrip(TripbillCancelEnum.notconfirm);
		}

		billInfo.setIsMultiEntry(Boolean.TRUE);
		HROrgUnitInfo hrInfoinner = AtsWebUtils.getHROrgUnitInfo(this.ctx, hrorgunitid);
		billInfo.setHrOrgUnit(hrInfoinner);
		PersonInfo personInfo_main = SHRBillUtil.getCurrPersonInfo();
		PersonPositionInfo personPositionInfo_main = SHRBillUtil.getAdminOrgUnit(personInfo_main.getId().toString());
		billInfo.setAdminOrg(personPositionInfo_main.getPersonDep());
		billInfo.setProposer(SHRBillUtil.getCurrPersonInfo());
		billInfo.setBillType(BillSubmitTypeEnum.batch);
		return billInfo;
	}

	@Override
	protected void beforeSubmit(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo model) throws SHRWebException {
		this.verifyModel(request, response, model);
		AtsTripBillInfo billInfo = this.storefields(request, model);
		String errorString = "";

		try {
			errorString = super.validateIsFillTrip(this.ctx, billInfo);
			if (!errorString.equals("")) {
				throw new ShrWebBizException(errorString);
			}
		} catch (EASBizException var7) {
			var7.printStackTrace();
		} catch (BOSException var8) {
			var8.printStackTrace();
		}

		billInfo.setBillState(HRBillStateEnum.SUBMITED);
		String operateStatus = request.getParameter("operateState");
		if (!org.apache.axis.utils.StringUtils.isEmpty(operateStatus) && "ADDNEW".equalsIgnoreCase(operateStatus)) {
			billInfo.setExtendedProperty("isAddNew", "isAddNew");
		}

	}
}

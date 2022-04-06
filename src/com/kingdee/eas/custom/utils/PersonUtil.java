 //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.kingdee.eas.custom.utils;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.hraux.WedInfo;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.org.IPosition;
import com.kingdee.eas.basedata.org.PositionFactory;
import com.kingdee.eas.basedata.org.PositionInfo;
import com.kingdee.eas.basedata.person.PersonFactory;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisCollection;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisFactory;
import com.kingdee.eas.hr.ats.AtsHolidayFileHisInfo;
import com.kingdee.eas.hr.base.EmployeeClassifyFactory;
import com.kingdee.eas.hr.base.EmployeeClassifyInfo;
import com.kingdee.eas.hr.base.IEmployeeClassify;
import com.kingdee.eas.hr.emp.ContractTempletInfo;
import com.kingdee.eas.hr.emp.LabContractFirstPartyInfo;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.util.StringUtils;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;

public class PersonUtil {
	public PersonUtil() {
	}

	public static Map<String, Object> getPersonInfo(String personId, Context ctx) throws BOSException, EASBizException, SQLException {
		HashMap<String, Object> resultMap = new HashMap();
		if (!StringUtils.isEmpty(personId)) {
			AtsHolidayFileHisInfo info = null;
			AtsHolidayFileHisCollection fileHisColl = AtsHolidayFileHisFactory.getLocalInstance(ctx).getAtsHolidayFileHisCollection("select *,adminOrgUnit.*,hrOrgUnit.*,proposer.number,proposer.name where proposer.id='" + personId + "' order by effdt desc");
			String personName;
			String ctsNumber;
			String fstPartyNumber;
			String psnCntrtSQL;
			if (fileHisColl != null && fileHisColl.size() > 0) {
				info = fileHisColl.get(0);
				PersonInfo proposer = info.getProposer();
				AdminOrgUnitInfo adminOrgUnit = info.getAdminOrgUnit();
				PositionInfo position = info.getPosition();
				if (position != null) {
					personName = position.getId().toString();
					IPosition iPosition = PositionFactory.getLocalInstance(ctx);
					position = iPosition.getPositionInfo(new ObjectUuidPK(personName));
				}

				resultMap.put("adminOrgUnit", adminOrgUnit);
				resultMap.put("position", position);
				if (proposer != null) {
					proposer = PersonFactory.getLocalInstance(ctx).getPersonInfo(new ObjectUuidPK(personId));
				}

				personName = proposer.getName();
				psnCntrtSQL = proposer.getNumber();
				ctsNumber = proposer.getFullNamePingYin() == null ? "" : proposer.getFullNamePingYin();
				resultMap.put("fullNamePingYin", ctsNumber);
				resultMap.put("personnumber", psnCntrtSQL);
				resultMap.put("personName", personName);
				EmployeeClassifyInfo employeeClassify = proposer.getEmployeeClassify();
				if (employeeClassify != null) {
					fstPartyNumber = employeeClassify.getId().toString();
					IEmployeeClassify iEmployeeClassify = EmployeeClassifyFactory.getLocalInstance(ctx);
					employeeClassify = iEmployeeClassify.getEmployeeClassifyInfo(new ObjectUuidPK(fstPartyNumber));
					resultMap.put("employeeClassify", employeeClassify);
				}
			}

			String querySQL = "select  fnCell,fofficePhone,femail from T_HR_PersonContactMethod where fpersonId = '" + personId + "'";
			IRowSet rowSet = DbUtil.executeQuery(ctx, querySQL);
			String costCenterSQL;
			if (rowSet.next()) {
				costCenterSQL = rowSet.getString("fnCell") == null ? "" : rowSet.getString("fnCell");
				personName = rowSet.getString("fofficePhone") == null ? "" : rowSet.getString("fofficePhone");
				psnCntrtSQL = rowSet.getString("femail") == null ? "" : rowSet.getString("femail");
				resultMap.put("nCell", costCenterSQL);
				resultMap.put("officePhone", personName);
				resultMap.put("email", psnCntrtSQL);
			}

			costCenterSQL = "SELECT  cts.fname_l2 ctsName,cts.fnumber ctsNumber,cts.fid ctsId  FROM CT_MP_Costcenter costcenter  left join CT_MP_CstCenter cts on cts.fid = costcenter.CFCOSTCNTRID  where costcenter.FPERSONID='" + personId + "'  order by  CFBEGINDATE desc";
			IRowSet iRowSet = DbUtil.executeQuery(ctx, costCenterSQL);
			String fstPartyName;
			if (iRowSet.next()) {
				psnCntrtSQL = iRowSet.getString("ctsName") == null ? "" : iRowSet.getString("ctsName");
				ctsNumber = iRowSet.getString("ctsNumber") == null ? "" : iRowSet.getString("ctsNumber");
				fstPartyName = iRowSet.getString("ctsId") == null ? "" : iRowSet.getString("ctsId");
				resultMap.put("ctsName", psnCntrtSQL);
				resultMap.put("ctsNumber", ctsNumber);
				resultMap.put("ctsId", fstPartyName);
			}

			psnCntrtSQL = "SELECT  fstParty.fname_l2 fstPartyName,fstParty.fnumber fstPartyNumber,fstParty.fid fstPartyId,  ContractTemplet.fname_l2 ContractTempletName,ContractTemplet.fnumber ContractTempletNumber,ContractTemplet.fid ContractTempletId FROM T_HR_EmployeeContract psnContrct  left join T_HR_ContractTemplet ContractTemplet on psnContrct.FCONTRACTTEMPLETID= ContractTemplet.fid  left join  T_HR_LabContractFirstParty fstParty on psnContrct.FCONTFIRSTPARTYID =fstParty.fid where psnContrct.femployeeid='" + personId + "'  order by  psnContrct.feffectdate desc";
			IRowSet iCntrtRowSet = DbUtil.executeQuery(ctx, psnCntrtSQL);
			if (iCntrtRowSet.next()) {
				fstPartyName = iCntrtRowSet.getString("fstPartyName") == null ? "" : iCntrtRowSet.getString("fstPartyName");
				fstPartyNumber = iCntrtRowSet.getString("fstPartyNumber") == null ? "" : iCntrtRowSet.getString("fstPartyNumber");
				String fstPartyId = iCntrtRowSet.getString("fstPartyId") == null ? "" : iCntrtRowSet.getString("fstPartyId");
				String ContractTempletName = iCntrtRowSet.getString("ContractTempletName") == null ? "" : iCntrtRowSet.getString("ContractTempletName");
				String ContractTempletNumber = iCntrtRowSet.getString("ContractTempletNumber") == null ? "" : iCntrtRowSet.getString("ContractTempletNumber");
				String ContractTempletId = iCntrtRowSet.getString("ContractTempletId") == null ? "" : iCntrtRowSet.getString("ContractTempletId");
				resultMap.put("fstPartyName", fstPartyName);
				resultMap.put("fstPartyNumber", fstPartyNumber);
				resultMap.put("fstPartyId", fstPartyId);
				resultMap.put("ContractTempletName", ContractTempletName);
				resultMap.put("ContractTempletNumber", ContractTempletNumber);
				resultMap.put("ContractTempletId", ContractTempletId);
			}

			return resultMap;
		} else {
			return null;
		}
	}

	public static void main(String[] args) {
		HashMap<String, Object> resultMap = new HashMap();
		resultMap.put("ctsId", "");
		Object object = resultMap.get("ctsId");
		System.out.println("=======object is:" + object);
		String str = ObjectUtils.toString(object, "");
		Boolean flag = StringUtils.isEmpty(str);
		System.out.println("==========flag is:" + flag);
	}

	public static Map<String, Object> getPersonBasicInfo(Context ctx, String personId) throws BOSException, SQLException, ParseException {
		HashMap<String, Object> resultMap = new HashMap();
		if (StringUtils.isEmpty(personId)) {
			return null;
		} else {
			String querySQL = "SELECT   adminOrgUnit.fid adminOrgUnitId,  adminOrgUnit.fname_l2 adminOrgName,pos.fid posId,pos.fname_l2 posName, type.fid typeId,type.Fname_l2 typeName, person.fid personId,person.fname_l2 personName ,  psnCntct.FNCELL nCell,person.FFullNamePingYin fullNamePingYin,person.FBirthday birthday,person.FPassportNO passportNO,  person.fidCardNO idcardNo,person.FGender Gender,person.CFPpBirthPlace1 PpBirthPlace,wed.fid wedId,wed.Fname_l2 wedName, person.CFPpIssDate PpIssDate,person.CFPpexpiryDate PpexpiryDate,person.CFPpIssuePlace PpIssuePlace  FROM T_BD_Person person  left join T_HR_PersonPosition pp on pp.FPERSONID  = person.fid  Left join t_org_admin adminOrgUnit on adminOrgUnit.fid = pp.FPERSONDEP  left join t_org_position  pos on pos.fid = pp.FPRIMARYPOSITIONID  left join CT_HR_FranceBnsPsnType type on type.fid = person.CFFRANCEBNSPSNTYPE left join T_HR_PersonContactMethod psnCntct on person.fid=psnCntct.fpersonid left join t_bd_hrwed wed on person.FWEDID =wed.fid where person.fid = '" + personId + "'";
			IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
			if (iRowSet.next()) {
				String adminOrgUnitId = iRowSet.getString("adminOrgUnitId") == null ? "" : iRowSet.getString("adminOrgUnitId").toString();
				String adminOrgName = iRowSet.getString("adminOrgName") == null ? "" : iRowSet.getString("adminOrgName").toString();
				String posName = iRowSet.getString("posName") == null ? "" : iRowSet.getString("posName").toString();
				String posId = iRowSet.getString("posId") == null ? "" : iRowSet.getString("posId").toString();
				String typeId = iRowSet.getString("typeId") == null ? "" : iRowSet.getString("typeId").toString();
				String typeName = iRowSet.getString("typeName") == null ? "" : iRowSet.getString("typeName").toString();
				String nCell = iRowSet.getString("nCell") == null ? "" : iRowSet.getString("nCell").toString();
				String fullNamePingYin = iRowSet.getString("fullNamePingYin") == null ? "" : iRowSet.getString("fullNamePingYin").toString();
				String birthday = iRowSet.getString("birthday") == null ? "" : iRowSet.getString("birthday").toString();
				String passportNO = iRowSet.getString("passportNO") == null ? "" : iRowSet.getString("passportNO").toString();
				String PpBirthPlace = iRowSet.getString("PpBirthPlace") == null ? "" : iRowSet.getString("PpBirthPlace").toString();
				String PpIssDate = iRowSet.getString("PpIssDate") == null ? "" : iRowSet.getString("PpIssDate").toString();
				String PpexpiryDate = iRowSet.getString("PpexpiryDate") == null ? "" : iRowSet.getString("PpexpiryDate").toString();
				String PpIssuePlace = iRowSet.getString("PpIssuePlace") == null ? "" : iRowSet.getString("PpIssuePlace").toString();
				String idcardNo = iRowSet.getString("idcardNo") == null ? "" : iRowSet.getString("idcardNo").toString();
				String wedId = iRowSet.getString("wedId") == null ? "" : iRowSet.getString("wedId").toString();
				String wedName = iRowSet.getString("wedName") == null ? "" : iRowSet.getString("wedName").toString();
				AdminOrgUnitInfo adminOrgUnitInfo = new AdminOrgUnitInfo();
				resultMap.put("adminOrgUnit", adminOrgUnitInfo);
				resultMap.put("fullNamePingYin", fullNamePingYin);
				resultMap.put("nCell", nCell);
				resultMap.put("passportNO", passportNO);
				resultMap.put("birthday", birthday);
				resultMap.put("idcardNo", idcardNo);
				resultMap.put("PpBirthPlace", PpBirthPlace);
				resultMap.put("PpIssDate", PpIssDate);
				resultMap.put("PpexpiryDate", PpexpiryDate);
				resultMap.put("PpIssuePlace", PpIssuePlace);
				if (!StringUtils.isEmpty(adminOrgUnitId) && !StringUtils.isEmpty(adminOrgName)) {
					adminOrgUnitInfo.setId(BOSUuid.read(adminOrgUnitId));
					adminOrgUnitInfo.setName(adminOrgName);
				}

				PositionInfo positionInfo;
				if (!StringUtils.isEmpty(posId) && !StringUtils.isEmpty(posName)) {
					positionInfo = new PositionInfo();
					positionInfo.setId(BOSUuid.read(posId));
					positionInfo.setName(posName);
					resultMap.put("position", positionInfo);
				} else {
					positionInfo = new PositionInfo();
					resultMap.put("position", positionInfo);
				}

				if (!StringUtils.isEmpty(typeId) && !StringUtils.isEmpty(typeName)) {
					BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
					customInfo.setId(BOSUuid.read(typeId));
					customInfo.setName(typeName);
					resultMap.put("FranceBnsPsnType", customInfo);
				}

				if (!StringUtils.isEmpty(wedId) && !StringUtils.isEmpty(wedName)) {
					WedInfo wedInfo = new WedInfo();
					wedInfo.setId(BOSUuid.read(wedId));
					wedInfo.setName(wedName);
					resultMap.put("wed", wedInfo);
				}

				String costCenterSQL = "SELECT  cts.fname_l2 ctsName,cts.fnumber ctsNumber,cts.fid ctsId  FROM CT_MP_Costcenter costcenter  left join CT_MP_CstCenter cts on cts.fid = costcenter.CFCOSTCNTRID  where costcenter.FPERSONID='" + personId + "'  order by  CFBEGINDATE desc";
				IRowSet iRowSet1 = DbUtil.executeQuery(ctx, costCenterSQL);
				String ctsName;
				if (iRowSet1.next()) {
					ctsName = iRowSet1.getString("ctsName") == null ? "" : iRowSet1.getString("ctsName");
					String ctsId = iRowSet1.getString("ctsId") == null ? "" : iRowSet1.getString("ctsId");
					if (!StringUtils.isEmpty(ctsId) && !StringUtils.isEmpty(ctsName)) {
						BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
						customInfo.setId(BOSUuid.read(ctsId));
						customInfo.setName(ctsName);
						resultMap.put("cts", customInfo);
					}
				}

				ctsName = " select  FJOINgroupDATE from T_HR_PersonPosition  where FPERSONID ='" + personId + "'";
				IRowSet iRowSetJ = DbUtil.executeQuery(ctx, ctsName);
				String psnCntrtSQL;
				if (iRowSetJ.next()) {
					psnCntrtSQL = iRowSetJ.getString("FJOINgroupDATE") == null ? "" : iRowSetJ.getString("FJOINgroupDATE").toString();
					resultMap.put("cur_joinGroupDate", psnCntrtSQL);
				}

				psnCntrtSQL = "SELECT  fstParty.fname_l2 fstPartyName,fstParty.fid fstPartyId,  ContractTemplet.fname_l2 ContractTempletName,ContractTemplet.fid ContractTempletId FROM T_HR_EmployeeContract psnContrct  left join T_HR_ContractTemplet ContractTemplet on psnContrct.FCONTRACTTEMPLETID= ContractTemplet.fid  left join  T_HR_LabContractFirstParty fstParty on psnContrct.FCONTFIRSTPARTYID =fstParty.fid where psnContrct.femployeeid='" + personId + "'  order by  psnContrct.feffectdate desc";
				IRowSet iCntrtRowSet = DbUtil.executeQuery(ctx, psnCntrtSQL);
				if (iCntrtRowSet.next()) {
					String ContractTempletName = iCntrtRowSet.getString("ContractTempletName") == null ? "" : iCntrtRowSet.getString("ContractTempletName");
					String ContractTempletId = iCntrtRowSet.getString("ContractTempletId") == null ? "" : iCntrtRowSet.getString("ContractTempletId");
					String fstPartyName = iCntrtRowSet.getString("fstPartyName") == null ? "" : iCntrtRowSet.getString("fstPartyName");
					String fstPartyId = iCntrtRowSet.getString("fstPartyId") == null ? "" : iCntrtRowSet.getString("fstPartyId");
					if (!StringUtils.isEmpty(ContractTempletId) && !StringUtils.isEmpty(ContractTempletName)) {
						ContractTempletInfo cntrctInfo = new ContractTempletInfo();
						cntrctInfo.setId(BOSUuid.read(ContractTempletId));
						cntrctInfo.setName(ContractTempletName);
						resultMap.put("ContractTemplet", cntrctInfo);
					}

					if (!StringUtils.isEmpty(fstPartyId) && !StringUtils.isEmpty(fstPartyName)) {
						LabContractFirstPartyInfo labContractFirstPartyInfo = new LabContractFirstPartyInfo();
						labContractFirstPartyInfo.setId(BOSUuid.read(fstPartyId));
						labContractFirstPartyInfo.setName(fstPartyName);
						resultMap.put("labContractFirstParty", labContractFirstPartyInfo);
					}
				}
			}

			return resultMap;
		}
	}

	public void getPersonJoinDate(Context ctx, String personId) throws BOSException, SQLException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String querySQL = " select  FJOINDATE from T_HR_PersonPosition  where FPERSONID ='" + personId + "'";
		IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
		if (iRowSet.next()) {
			String jOINDATEStr = iRowSet.getString("FJOINDATE");
			sdf.parse(jOINDATEStr);
		}

	}
}

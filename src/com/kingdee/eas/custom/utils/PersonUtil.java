package com.kingdee.eas.custom.utils;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSUuid;
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
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.util.StringUtils;

/**
 * 获取人员信息的工具类
 * @author  hj
 */
public class PersonUtil {

	/**
	 * 根据人员id获取人员的信息
	 * @param personId
	 * @param ctx
	 * @return
	 * @throws BOSException 
	 * @throws EASBizException 
	 * @throws SQLException 
	 */
	public static  Map<String, Object>  getPersonInfo(String personId,Context ctx) throws BOSException, EASBizException, SQLException {
    	HashMap<String, Object> resultMap = new HashMap<String ,Object>();
    	if(!StringUtils.isEmpty(personId)) {
//    		GetOrgTreeService
    		/**
    		 *  2021-01-06 hj
    		 * 查询人员的组织和职位信息
    		 */
            AtsHolidayFileHisInfo info = null;
    		AtsHolidayFileHisCollection fileHisColl = AtsHolidayFileHisFactory.getLocalInstance(ctx).getAtsHolidayFileHisCollection
    				((new StringBuilder()).append
    						("select *,adminOrgUnit.*,hrOrgUnit.*,proposer.number,"
    								+ "proposer.name where proposer.id='").append(personId )
    						.append("' order by effdt desc").toString());
            if(fileHisColl != null && fileHisColl.size() > 0) {
                info = fileHisColl.get(0);//获取最新的一条假期档案数据
            	PersonInfo proposer = info.getProposer();
            	AdminOrgUnitInfo adminOrgUnit = info.getAdminOrgUnit();
            	PositionInfo position = info.getPosition();
            	if(position!=null) {
            		String positionId = position.getId().toString();
            		IPosition iPosition = PositionFactory.getLocalInstance(ctx);
            		position = iPosition.getPositionInfo(new ObjectUuidPK(positionId));
            	}
            	resultMap.put("adminOrgUnit", adminOrgUnit);
            	resultMap.put("position", position);
            	if(proposer!=null) {//人员不等于空的情况下
            		proposer = PersonFactory.getLocalInstance(ctx).getPersonInfo(new ObjectUuidPK(personId));
            	}
            	String personName = proposer.getName();//员工姓名
            	String personnumber = proposer.getNumber();//员工编码
            	String fullNamePingYin = proposer.getFullNamePingYin()==null?"":proposer.getFullNamePingYin();//护照姓名拼音
            	resultMap.put("fullNamePingYin", fullNamePingYin);
            	resultMap.put("personnumber", personnumber);
            	resultMap.put("personName", personName);
            	EmployeeClassifyInfo employeeClassify = proposer.getEmployeeClassify();//职员类别
            	//职员类别
            	if(employeeClassify!=null) {
            		//职员类别的id
            		String employeeClassifyId = employeeClassify.getId().toString();
            		IEmployeeClassify iEmployeeClassify = EmployeeClassifyFactory.getLocalInstance(ctx);
            		employeeClassify = iEmployeeClassify.getEmployeeClassifyInfo(new ObjectUuidPK(employeeClassifyId));
                	resultMap.put("employeeClassify", employeeClassify);
            	}
            }
            /**
             * 2021-01-06 hj
             * 查询人员的中国手机号、阿国手机号、公司邮箱地址
             */
            String querySQL = "select  fnCell,fofficePhone,femail from T_HR_PersonContactMethod where fpersonId = '"+personId+"'";
            IRowSet rowSet = DbUtil.executeQuery(ctx, querySQL);
            if(rowSet.next()) {
            	String nCell = rowSet.getString("fnCell")==null?"":rowSet.getString("fnCell");
            	String officePhone = rowSet.getString("fofficePhone")==null?"":rowSet.getString("fofficePhone");
            	String email = rowSet.getString("femail")==null?"":rowSet.getString("femail");
            	resultMap.put("nCell", nCell);
            	resultMap.put("officePhone", officePhone);
            	resultMap.put("email", email);
            }
            /**
             * 2021-01-06 hj
             * 查询人员的成本部门信息，查询最新的一条
             */
            String costCenterSQL = "SELECT  cts.fname_l2 ctsName,cts.fnumber ctsNumber,cts.fid ctsId " + 
				            		" FROM CT_MP_Costcenter costcenter " + 
				            		" left join CT_MP_CstCenter cts on cts.fid = costcenter.CFCOSTCNTRID " + 
				            		" where costcenter.FPERSONID='"+personId+"'  order by  CFBEGINDATE desc";
            IRowSet iRowSet = DbUtil.executeQuery(ctx, costCenterSQL);
            if(iRowSet.next()) {
            	String ctsName = iRowSet.getString("ctsName")==null?"":iRowSet.getString("ctsName");
            	String ctsNumber = iRowSet.getString("ctsNumber")==null?"":iRowSet.getString("ctsNumber");
            	String ctsId = iRowSet.getString("ctsId")==null?"":iRowSet.getString("ctsId");
            	resultMap.put("ctsName", ctsName);
            	resultMap.put("ctsNumber", ctsNumber);
            	resultMap.put("ctsId", ctsId);
            }
            /**
             * 2021-04-18 czq
             * 查询人员的协议信息，查询最新的一条
             */
            String psnCntrtSQL = "SELECT  fstParty.fname_l2 fstPartyName,fstParty.fnumber fstPartyNumber,fstParty.fid fstPartyId, " + 
            						" ContractTemplet.fname_l2 ContractTempletName,ContractTemplet.fnumber ContractTempletNumber,ContractTemplet.fid ContractTempletId"+
				            		" FROM T_HR_EmployeeContract psnContrct " + 
				            		" left join T_HR_ContractTemplet ContractTemplet on psnContrct.FCONTRACTTEMPLETID= ContractTemplet.fid " + 
				            		" left join  T_HR_LabContractFirstParty fstParty on psnContrct.FCONTFIRSTPARTYID =fstParty.fid" +
				            		" where psnContrct.femployeeid='"+personId+"'  order by  psnContrct.feffectdate desc";
            IRowSet iCntrtRowSet = DbUtil.executeQuery(ctx, psnCntrtSQL);
            if(iCntrtRowSet.next()) {
            	String fstPartyName = iCntrtRowSet.getString("fstPartyName")==null?"":iCntrtRowSet.getString("fstPartyName");
            	String fstPartyNumber = iCntrtRowSet.getString("fstPartyNumber")==null?"":iCntrtRowSet.getString("fstPartyNumber");
            	String fstPartyId = iCntrtRowSet.getString("fstPartyId")==null?"":iCntrtRowSet.getString("fstPartyId");
            	String ContractTempletName = iCntrtRowSet.getString("ContractTempletName")==null?"":iCntrtRowSet.getString("ContractTempletName");
            	String ContractTempletNumber = iCntrtRowSet.getString("ContractTempletNumber")==null?"":iCntrtRowSet.getString("ContractTempletNumber");
            	String ContractTempletId = iCntrtRowSet.getString("ContractTempletId")==null?"":iCntrtRowSet.getString("ContractTempletId");
            	resultMap.put("fstPartyName", fstPartyName);
            	resultMap.put("fstPartyNumber", fstPartyNumber);
            	resultMap.put("fstPartyId", fstPartyId);
            	resultMap.put("ContractTempletName", ContractTempletName);
            	resultMap.put("ContractTempletNumber", ContractTempletNumber);
            	resultMap.put("ContractTempletId", ContractTempletId);
            }
            return resultMap;
    	}else {
    		return null;
    	}
	}
	
	public static void main(String[] args) {
    	HashMap<String, Object> resultMap = new HashMap<String ,Object>();
    	resultMap.put("ctsId", "");
    	Object object = resultMap.get("ctsId");
        System.out.println("=======object is:"+object);    	
        String str = ObjectUtils.toString(object, "");
        Boolean flag = StringUtils.isEmpty(str);
        System.out.println("==========flag is:"+flag);
	}
	
	
	/**
	 * 根据人员id获取人员的当前任职组织
	 * 护照姓名拼音
	 * 职位和法语津贴职员类别
	 * @param ctx
	 * @param personId
	 * @throws BOSException  
	 * @throws SQLException 
	 */
	public static Map<String, Object>  getPersonBasicInfo(Context ctx,String personId) throws BOSException, SQLException {
    	HashMap<String, Object> resultMap = new HashMap<String ,Object>();
    	if(!StringUtils.isEmpty(personId)) {
    		String querySQL = "SELECT " + 
    				"  adminOrgUnit.fid adminOrgUnitId,  adminOrgUnit.fname_l2 adminOrgName,pos.fid posId,pos.fname_l2 posName, type.fid typeId,type.Fname_l2 typeName, person.fid personId,person.fname_l2 personName ," + 
    				"  psnCntct.FNCELL nCell,person.FFullNamePingYin fullNamePingYin,person.FBirthday birthday,person.FPassportNO passportNO " + 
    				" FROM T_BD_Person person " + 
    				" left join T_HR_PersonPosition pp on pp.FPERSONID  = person.fid " + 
    				" Left join t_org_admin adminOrgUnit on adminOrgUnit.fid = pp.FPERSONDEP " + 
    				" left join t_org_position  pos on pos.fid = pp.FPRIMARYPOSITIONID " + 
    				" left join CT_HR_FranceBnsPsnType type on type.fid = person.CFFRANCEBNSPSNTYPE " + 
    				"left join T_HR_PersonContactMethod psnCntct on person.fid=psnCntct.fpersonid" +
    				" where person.fid = '"+personId+"'";
    		IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
    		if(iRowSet.next()) {
    			String adminOrgUnitId = iRowSet.getString("adminOrgUnitId")==null?"":iRowSet.getString("adminOrgUnitId").toString();
    			String adminOrgName = iRowSet.getString("adminOrgName")==null?"":iRowSet.getString("adminOrgName").toString();
    			String posName = iRowSet.getString("posName")==null?"":iRowSet.getString("posName").toString();
    			String posId = iRowSet.getString("posId")==null?"":iRowSet.getString("posId").toString();
    			String typeId = iRowSet.getString("typeId")==null?"": iRowSet.getString("typeId").toString();
    			String typeName = iRowSet.getString("typeName")==null?"":iRowSet.getString("typeName").toString();
    			String nCell = iRowSet.getString("nCell")==null?"":iRowSet.getString("nCell").toString();
    			String fullNamePingYin = iRowSet.getString("fullNamePingYin")==null?"":iRowSet.getString("fullNamePingYin").toString();
    			String birthday = iRowSet.getString("birthday")==null?"":iRowSet.getString("birthday").toString();
    			String passportNO = iRowSet.getString("passportNO")==null?"":iRowSet.getString("passportNO").toString();
    			AdminOrgUnitInfo adminOrgUnitInfo = new AdminOrgUnitInfo();
    			/**
    			 * 所属组织id和所属组织名称
    			 */
    			if(!StringUtils.isEmpty(adminOrgUnitId) && !StringUtils.isEmpty(adminOrgName)) {
    				adminOrgUnitInfo.setId(BOSUuid.read(adminOrgUnitId));
    				adminOrgUnitInfo.setName(adminOrgName);
    			}
    			resultMap.put("adminOrgUnit", adminOrgUnitInfo);
    			resultMap.put("fullNamePingYin", fullNamePingYin);//护照姓名全拼
    			resultMap.put("nCell", nCell);//常用电话
    			resultMap.put("passportNO", passportNO);//护照号码
    			resultMap.put("birthday", birthday);//出生日期
    			/**
    			 * 所属岗位id和所属岗位名称
    			 */
    			if(!StringUtils.isEmpty(posId) && !StringUtils.isEmpty(posName)) {
    				PositionInfo positionInfo = new PositionInfo();
    				positionInfo.setId(BOSUuid.read(posId));
    				positionInfo.setName(posName);
    				resultMap.put("position", positionInfo);
    			}else {
    				PositionInfo positionInfo = new PositionInfo();
    				resultMap.put("position", positionInfo);
    			}
    			/**
    			 * 职员类别id和职员类别名称不为空
    			 */
    			if(!StringUtils.isEmpty(typeId) && !StringUtils.isEmpty(typeName)) {
    				BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
    				customInfo.setId(BOSUuid.read(typeId)); //设置职员类别id
    				customInfo.setName(typeName);		    //设置职员类别名称
    				resultMap.put("FranceBnsPsnType", customInfo);
    			}
                /**
                 * 2021-01-06 hj
                 * 查询人员的成本部门信息，查询最新的一条
                 */
                String costCenterSQL = "SELECT  cts.fname_l2 ctsName,cts.fnumber ctsNumber,cts.fid ctsId " + 
    				            		" FROM CT_MP_Costcenter costcenter " + 
    				            		" left join CT_MP_CstCenter cts on cts.fid = costcenter.CFCOSTCNTRID " + 
    				            		" where costcenter.FPERSONID='"+personId+"'  order by  CFBEGINDATE desc";
                IRowSet iRowSet1 = DbUtil.executeQuery(ctx, costCenterSQL);
                if(iRowSet1.next()) {
                	String ctsName = iRowSet1.getString("ctsName")==null?"":iRowSet1.getString("ctsName");
                	String ctsId = iRowSet1.getString("ctsId")==null?"":iRowSet1.getString("ctsId");
                	if(!StringUtils.isEmpty(ctsId) && !StringUtils.isEmpty(ctsName)) {
                		BaseItemCustomInfo customInfo = new BaseItemCustomInfo();
                		customInfo.setId(BOSUuid.read(ctsId)); //设置成本部门id
                		customInfo.setName(ctsName);		   //设置成本部门名称
                		resultMap.put("cts", customInfo);
                	}
                }
    		}
    		return resultMap;
    	}else {
    		return null;
    	}
	}
	
	/**
	 * 根据人员id获取最近一次来阿时间，判断机票额度的起始日期
	 * @param ctx
	 * @param personId
	 * @throws BOSException  
	 * @throws SQLException 
	 * @throws ParseException 
	 */
	public void getPersonJoinDate(Context ctx,String personId) throws BOSException, SQLException, ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String querySQL = " select  FJOINDATE from T_HR_PersonPosition  where FPERSONID ='"+personId+"'";
		IRowSet iRowSet = DbUtil.executeQuery(ctx, querySQL);
		if(iRowSet.next()) {
			String jOINDATEStr = iRowSet.getString("FJOINDATE");
			Date joinDate = sdf.parse(jOINDATEStr);
			
		}
	}
}




package com.kingdee.shr.base.syssetting.web.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.hraux.WedInfo;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.eas.basedata.org.HROrgUnitInfo;
import com.kingdee.eas.basedata.org.PositionInfo;
import com.kingdee.eas.basedata.person.IPerson;
import com.kingdee.eas.basedata.person.PersonFactory;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.utils.PersonUtil;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.base.HRBillBaseInfo;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryCollection;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryInfo;
import com.kingdee.eas.hr.emp.LabContractFirstPartyInfo;
import com.kingdee.shr.affair.web.util.SHRBillUtil;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 第三国签证
 * @author  hj
 *
 */
public class DEPCustomBillEditHandlerExt extends DEPCustomBillEditHandler
{
  protected void afterCreateNewModel(HttpServletRequest request, HttpServletResponse response, CoreBaseInfo coreBaseInfo)
    throws SHRWebException
  {
    super.afterCreateNewModel(request, response, coreBaseInfo);
    HROrgUnitInfo currentHRUnit = SHRBillUtil.getCurrentHRUnit();
    Context ctx = SHRContext.getInstance().getContext();
    PersonInfo currPersonInfo = getCurrPersonInfo();
    HRBillBaseInfo billInfo = (HRBillBaseInfo)coreBaseInfo;
    billInfo.setHrOrgUnit(currentHRUnit);
    System.out.println("======申请表的billInfo is:" + billInfo);
    Object object = billInfo.get("entrys");
    System.out.println("============object is:" + object + "=========null==object is:" + null == object);
    IPerson remoteInstance = null;
    try {
      remoteInstance = PersonFactory.getRemoteInstance();
    }
    catch (BOSException e1) {
      e1.printStackTrace();
    }
    try {
    	currPersonInfo = remoteInstance.getPersonInfo(new ObjectUuidPK(currPersonInfo.getId().toString()));
    }
    catch (EASBizException e1) {
      e1.printStackTrace();
    }
    catch (BOSException e1) {
      e1.printStackTrace();
    }

    if (object == null) {
      SHRBillBaseTemplateEntryCollection entryColl = new SHRBillBaseTemplateEntryCollection();
      SHRBillBaseTemplateEntryInfo entryInfo = new SHRBillBaseTemplateEntryInfo();
      try
      {
        Map basicMap = PersonUtil.getPersonBasicInfo(ctx, currPersonInfo.getId().toString());
        System.out.println("======打印czq:" + basicMap);
        Object adminOrgUnitObj = basicMap.get("adminOrgUnit");
        Object positionObj = basicMap.get("position");
        Object FranceBnsPsnTypeObj = basicMap.get("FranceBnsPsnType");
        Object FullNamePingYinObj = basicMap.get("fullNamePingYin");
        Object idcardNoObj = basicMap.get("idcardNo");
        Object birthdayObj = basicMap.get("birthday");
        Object NCellObj = basicMap.get("nCell");
        Object wedObj = basicMap.get("wed");
        Object PassportNOObj = basicMap.get("passportNO");
        Object PpBirthPlaceObj = basicMap.get("PpBirthPlace");
        Object contractTempletObj = basicMap.get("contractTemplet");
        Object labContractFirstPartyObj = basicMap.get("labContractFirstParty");
        Object cur_joinGroupDateObj = basicMap.get("cur_joinGroupDate");
        entryInfo.put("person", currPersonInfo);
        //性别，hj 2021-09-23添加查询
        entryInfo.put("gender", currPersonInfo.getGender());
        if (adminOrgUnitObj != null) {
          AdminOrgUnitInfo info = (AdminOrgUnitInfo)adminOrgUnitObj;
          entryInfo.put("adminOrg", info);
        }

        if (positionObj != null) {
          PositionInfo info = (PositionInfo)positionObj;
          entryInfo.put("position", info);
        }

        if (FranceBnsPsnTypeObj != null) {
          BaseItemCustomInfo info = (BaseItemCustomInfo)FranceBnsPsnTypeObj;
          entryInfo.put("FranceBnsPsnType", info);
        }

        if (FullNamePingYinObj != null)
        {
          entryInfo.put("fullNamePingYin", FullNamePingYinObj);
        }

        if (idcardNoObj != null)
        {
          entryInfo.put("idcardNo", idcardNoObj);
        }

        if (birthdayObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date birthdayDate = sdf.parse(birthdayObj.toString());
            currPersonInfo.setBirthday(birthdayDate);
            entryInfo.put("birthday", birthdayObj);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        }

        if (wedObj != null) {
          WedInfo info = (WedInfo)wedObj;
          entryInfo.put("wed", info);
        }

        if (NCellObj != null) {
          currPersonInfo.setCell(NCellObj.toString());
          entryInfo.put("nCell", NCellObj);
        }

        if (PassportNOObj != null)
        {
          entryInfo.put("passportNO", PassportNOObj);
        }

        if (PpBirthPlaceObj != null)
        {
          entryInfo.put("PpBirthPlace", PpBirthPlaceObj);
        }

        if (cur_joinGroupDateObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date cur_joinGroupDate = sdf.parse(cur_joinGroupDateObj.toString());
            entryInfo.put("cur_joinGroupDate", cur_joinGroupDate);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        }

        if (contractTempletObj != null) {
          BaseItemCustomInfo info = (BaseItemCustomInfo)contractTempletObj;
          entryInfo.put("contractTemplet", info);
        }

        if (labContractFirstPartyObj != null) {
          BaseItemCustomInfo info = (BaseItemCustomInfo)labContractFirstPartyObj;
          entryInfo.put("labContractFirstParty", info);
        }
      }
      catch (BOSException e) {
        e.printStackTrace();
        throw new ShrWebBizException(e.getMessage());
      } catch (SQLException e) {
        e.printStackTrace();
        throw new ShrWebBizException(e.getMessage());
      }
      entryColl.add(entryInfo);
      billInfo.put("entrys", entryColl);
    } else {
      SHRBillBaseTemplateEntryCollection entryColl = (SHRBillBaseTemplateEntryCollection)object;
      System.out.println("===========entryColl is:" + entryColl);
      int size = entryColl.size();
      SHRBillBaseTemplateEntryInfo entryInfo = null;
      if (size == 0) {
        entryColl = new SHRBillBaseTemplateEntryCollection();
        entryInfo = new SHRBillBaseTemplateEntryInfo();
      } else if (entryColl.size() > 0) {
        entryInfo = entryColl.get(0);
      }
      try
      {
        Map basicMap = PersonUtil.getPersonBasicInfo(ctx, currPersonInfo.getId().toString());
        Object adminOrgUnitObj = basicMap.get("adminOrgUnit");
        Object positionObj = basicMap.get("position");
        Object FranceBnsPsnTypeObj = basicMap.get("FranceBnsPsnType");
        Object FullNamePingYinObj = basicMap.get("fullNamePingYin");
        Object idcardNoObj = basicMap.get("idcardNo");
        Object birthdayObj = basicMap.get("birthday");
        Object wedObj = basicMap.get("wed");
        Object NCellObj = basicMap.get("nCell");
        Object PassportNOObj = basicMap.get("passportNO");
        Object PpBirthPlaceObj = basicMap.get("PpBirthPlace");
        Object PpIssDateObj = basicMap.get("PpIssDate");
        Object PpexpiryDateObj = basicMap.get("PpexpiryDate");
        Object PpIssuePlaceObj = basicMap.get("PpIssuePlace");
        Object contractTempletObj = basicMap.get("contractTemplet");
        Object labContractFirstPartyObj = basicMap.get("labContractFirstParty");
        Object cur_joinGroupDateObj = basicMap.get("cur_joinGroupDate");
        entryInfo.put("person", currPersonInfo);
        //性别，hj 2021-09-23添加查询
        entryInfo.put("gender", currPersonInfo.getGender());
        
        if (adminOrgUnitObj != null) {
          AdminOrgUnitInfo info = (AdminOrgUnitInfo)adminOrgUnitObj;
          entryInfo.put("adminOrg", info);
        }

        if (positionObj != null) {
          PositionInfo info = (PositionInfo)positionObj;
          entryInfo.put("position", info);
        }

        if (FranceBnsPsnTypeObj != null) {
          BaseItemCustomInfo info = (BaseItemCustomInfo)FranceBnsPsnTypeObj;
          entryInfo.put("FranceBnsPsnType", info);
        }

        if (FullNamePingYinObj != null)
        {
          entryInfo.put("fullNamePingYin", FullNamePingYinObj);
        }

        if (PassportNOObj != null)
        {
          entryInfo.put("passportNO", PassportNOObj);
        }

        if (PpBirthPlaceObj != null)
        {
          entryInfo.put("PpBirthPlace", PpBirthPlaceObj);
        }

        if (PpIssDateObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date PpIssDate = sdf.parse(PpIssDateObj.toString());

            entryInfo.put("PpIssDate", PpIssDateObj);
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }

        if (PpexpiryDateObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date PpexpiryDate = sdf.parse(PpexpiryDateObj.toString());

            entryInfo.put("PpexpiryDate", PpexpiryDateObj);
          } catch (ParseException e) {
            e.printStackTrace();
          }
        }

        if (PpIssuePlaceObj != null)
        {
          entryInfo.put("PpIssuePlace", PpIssuePlaceObj);
        }

        if (idcardNoObj != null)
        {
          entryInfo.put("idcardNo", idcardNoObj);
        }

        if (birthdayObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date birthdayDate = sdf.parse(birthdayObj.toString());

            entryInfo.put("birthday", birthdayObj);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        }

        if (wedObj != null) {
          WedInfo info = (WedInfo)wedObj;
          entryInfo.put("wed", info);
        }

        if (NCellObj != null) {
          currPersonInfo.setCell(NCellObj.toString());
          entryInfo.put("nCell", NCellObj);
        }

        if (cur_joinGroupDateObj != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
          try {
            Date cur_joinGroupDate = sdf.parse(cur_joinGroupDateObj.toString());
            entryInfo.put("cur_joinGroupDate", cur_joinGroupDate);
          } catch (ParseException e) {
            e.printStackTrace();
          }

        }

        if (contractTempletObj != null) {
          BaseItemCustomInfo info = (BaseItemCustomInfo)contractTempletObj;
          entryInfo.put("contractTemplet", info);
        }

        if (labContractFirstPartyObj != null) {
          LabContractFirstPartyInfo info = (LabContractFirstPartyInfo)labContractFirstPartyObj;
          entryInfo.put("labContractFirstParty", info);
        }

        entryColl.add(entryInfo);
        billInfo.put("entrys", entryColl);
      } catch (BOSException e) {
        e.printStackTrace();
        throw new ShrWebBizException(e.getMessage());
      } catch (SQLException e) {
        e.printStackTrace();
        throw new ShrWebBizException(e.getMessage());
      }
      System.out.println("===========添加完之后 billInfo is:" + billInfo + "=====entryColl is:" + entryColl);
    }
  }
  
  
}
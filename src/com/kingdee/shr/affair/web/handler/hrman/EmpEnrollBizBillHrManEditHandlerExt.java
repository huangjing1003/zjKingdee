package com.kingdee.shr.affair.web.handler.hrman;

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.ui.ModelMap;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.metadata.entity.EntityViewInfo;
import com.kingdee.bos.metadata.entity.FilterInfo;
import com.kingdee.bos.metadata.entity.FilterItemInfo;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;
import com.kingdee.bos.metadata.entity.SelectorItemInfo;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.hraux.WedInfo;
import com.kingdee.eas.basedata.org.HROrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonCollection;
import com.kingdee.eas.basedata.person.PersonFactory;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.base.EmpPosOrgRelationInfo;
import com.kingdee.eas.hr.base.EnrollAgainCodingEnum;
import com.kingdee.eas.hr.base.HRBizDefineInfo;
import com.kingdee.eas.hr.base.HRBizDefineTypeEnum;
import com.kingdee.eas.hr.base.VariationReasonCollection;
import com.kingdee.eas.hr.base.VariationReasonFactory;
import com.kingdee.eas.hr.base.util.HRParamUtil;
import com.kingdee.eas.hr.emp.IPersonOtherInfo;
import com.kingdee.eas.hr.emp.IPersonPhoto;
import com.kingdee.eas.hr.emp.PersonOtherInfoCollection;
import com.kingdee.eas.hr.emp.PersonOtherInfoFactory;
import com.kingdee.eas.hr.emp.PersonPhotoCollection;
import com.kingdee.eas.hr.emp.PersonPhotoFactory;
import com.kingdee.eas.hr.emp.PersonPhotoInfo;
import com.kingdee.eas.hr.emp.app.util.SHREmpOptEmpOrgRelationTool;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.eas.util.client.EASResource;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.shr.preentry.PreEntryCollection;
import com.kingdee.shr.preentry.PreEntryFactory;
import com.kingdee.shr.preentry.PreEntryInfo;
import com.kingdee.shr.preentry.PreEntryPersonInfo;
import com.kingdee.util.StringUtils;

/**
 * 模型：com.kingdee.eas.hr.affair.app.EmpEnrollBizBill
 * 名称:入职申请form（专员）
 * uipk:com.kingdee.eas.hr.affair.app.EmpEnrollBizBill.form
 * @author hj
 *
 */
public class EmpEnrollBizBillHrManEditHandlerExt extends EmpEnrollBizBillHrManEditHandler{

	/**
	 * 获取预入职单据的字段信息
	 */
	@Override
   public void getPreEntryInitDataAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap)
	        throws BOSException, SHRWebException
	    {
			Context ctx = SHRContext.getInstance().getContext();
	        String billId = "";
	        if(request.getParameter("billId") != null && !request.getParameter("billId").equals(""))
	            billId = request.getParameter("billId");
	        PreEntryInfo preEntryInfo = new PreEntryInfo();
	        StringBuffer sql = new StringBuffer();
	        sql.append("select *,talent.*").append(",position.id,position.name")
	        .append(",hrOrgUnit.id,hrOrgUnit.name")//HR业务组织
	        .append(",personDepP.id,personDepP.name")//专业序列
	        .append(",wed.id,wed.name")//婚姻状况
	        .append(",folk.id,folk.name")//民族
	        .append(",personDep.id,personDep.name")//应聘部门/项目
	        .append(",pPosition.id,pPosition.name")//专业序列职位
	        .append(",politicalFace.id,politicalFace.name")//政治面貌
	        .append(",JobGrade.id,JobGrade.name")//职级
	        .append(",employeeClassify.id,employeeClassify.name")//职员类别
	        .append(",pConclusion.id,pConclusion.name")//专业序列面试意见
	        .append(",eConclusion.id,eConclusion.name")//用人单位面试意见
	        .append(",HRConclusion.id,HRConclusion.name")//人力资源部面试意见
	        .append(",mngJobGrade.id,mngJobGrade.name")//管理岗薪等级
	        .append(",grpJobGrade.id,grpJobGrade.name")//内部职级
	        .append(",pstnClass.id,pstnClass.name")//岗位序列
	        .append(",highestDegree.id,highestDegree.name")//最高学历
	        .append(",enrollSource.id,enrollSource.name,enrollSource.number")
	        .append(",talent.folk.id,talent.folk.name,talent.zodiac.id,talent.zodiac.name,talent.constellation.id,talent.constellation.name")
	        .append(",sourceBillType").append(" where id = '").append(billId).append("'");
	        PreEntryCollection coll = PreEntryFactory.getRemoteInstance().getPreEntryCollection(sql.toString());
	        if(coll.size() > 0)
	        {
	            preEntryInfo = coll.get(0);
	            if(preEntryInfo.getEnrollSource() != null && preEntryInfo.getEnrollSource().getNumber() != null)
	            {
	                VariationReasonCollection vrColl = VariationReasonFactory.getRemoteInstance().getVariationReasonCollection((new StringBuilder()).append("select id,name,number where number='").append(preEntryInfo.getEnrollSource().getNumber()).append("'").toString());
	                if(vrColl.size() > 0)
	                    preEntryInfo.put("enrollSource", vrColl.get(0));
	            }
	            try
	            {
	                assemblePreEntryEnrollAgainInfo(preEntryInfo.getTalent(), preEntryInfo.getHrOrgUnit(), modelMap);
	            }
	            catch(EASBizException e1)
	            {
	                e1.printStackTrace();
	                throw new ShrWebBizException(e1.getMessage(), e1);
	            }
	            if(preEntryInfo.getTalent() != null && preEntryInfo.getTalent().getPerson() != null && preEntryInfo.getTalent().getPerson().getId() != null)
	            {
	                IPersonPhoto iPersonPhoto = PersonPhotoFactory.getRemoteInstance();
	                PersonPhotoCollection phcoll = iPersonPhoto.getPersonPhotoCollection((new StringBuilder()).append("where person = '").append(preEntryInfo.getTalent().getPerson().getId().toString()).append("'").toString());
	                if(phcoll.size() > 0)
	                {
	                    String tempid = request.getParameter("tempId");
	                    PersonPhotoInfo personPhotoInfo = phcoll.get(0);
	                    PersonPhotoInfo addPhoto = new PersonPhotoInfo();
	                    PersonInfo pInfo = new PersonInfo();
	                    pInfo.setId(BOSUuid.read(tempid));
	                    addPhoto.setPerson(pInfo);
	                    addPhoto.setImageDataSource(personPhotoInfo.getImageDataSource());
	                    addPhoto.setImageData(personPhotoInfo.getImageData());
	                    addPhoto.setSourceImageHeight(personPhotoInfo.getSourceImageHeight());
	                    addPhoto.setSourceImageWidth(personPhotoInfo.getSourceImageWidth());
	                    addPhoto.setImageContentType(personPhotoInfo.getImageContentType());
	                    try
	                    {
	                        iPersonPhoto.addnew(addPhoto);
	                    }
	                    catch(EASBizException e)
	                    {
	                        e.printStackTrace();
	                        throw new ShrWebBizException(e.getMessage(), e);
	                    }
	                }
	            }
	        }
	        System.out.println("===========sql is:"+sql);
	        modelMap.put("preEntryInfo", preEntryInfo);
	        JSONUtils.writeJson(response, modelMap);
	    }

	    private void assemblePreEntryEnrollAgainInfo(PreEntryPersonInfo preEntryPerson, HROrgUnitInfo hrOrg, ModelMap modelMap)
	        throws BOSException, EASBizException
	    {
	        if(preEntryPerson == null)
	            return;
	        EntityViewInfo viewInfo = new EntityViewInfo();
	        FilterInfo filter = new FilterInfo();
	        if(!StringUtils.isEmpty(preEntryPerson.getPassportNO()))
	            filter.getFilterItems().add(new FilterItemInfo("passportNo", preEntryPerson.getPassportNO()));
	        if(!StringUtils.isEmpty(preEntryPerson.getIdCardNO()))
	        {
	            String maskStr = "#0 or #1";
	            if(filter.getFilterItems().size() > 0)
	                maskStr = "#0 or #1 or #2";
	            filter.getFilterItems().add(new FilterItemInfo("idCardNO", preEntryPerson.getIdCardNO().toUpperCase()));
	            filter.getFilterItems().add(new FilterItemInfo("idCardNO", preEntryPerson.getIdCardNO()));
	            filter.setMaskString(maskStr);
	        }
	        if(filter.getFilterItems().size() > 0)
	        {
	            viewInfo.setFilter(filter);
	            SelectorItemCollection selector = new SelectorItemCollection();
	            selector.add(new SelectorItemInfo("id"));
	            selector.add(new SelectorItemInfo("number"));
	            selector.add(new SelectorItemInfo("employeeType.id"));
	            selector.add(new SelectorItemInfo("employeeType.isInCount"));
	            viewInfo.setSelector(selector);
	            PersonCollection pColl = PersonFactory.getRemoteInstance().getPersonCollection(viewInfo);
	            if(pColl.size() > 0)
	            {
	                PersonInfo pInfo = pColl.get(0);
	                if(!pInfo.getEmployeeType().isIsInCount())
	                {
	                    String personId = pInfo.getId().toString();
	                    Context ctx = SHRContext.getInstance().getContext();
	                    EmpPosOrgRelationInfo empPosOrgInfo = SHREmpOptEmpOrgRelationTool.getCurrntMainEmpOrgRelationInfo(ctx, personId);
	                    HRBizDefineInfo actionInfo = empPosOrgInfo.getAction();
	                    if(judgeActionCanEnrollAgain(actionInfo))
	                    {
	                        Map personOldInfo = fillPersonBaseInfoData(ctx, preEntryPerson, pInfo, empPosOrgInfo, hrOrg);
	                        modelMap.put("personOldInfo", personOldInfo);
	                        modelMap.put("personExist", "true");
	                    }
	                }
	            }
	        }
	    }
	    
	   /**
	    * 判是否为二次入职
	    * @param actionInfo
	    * @return
	    */
	    private boolean judgeActionCanEnrollAgain(HRBizDefineInfo actionInfo)
	    {
	        return !HRBizDefineTypeEnum.ENROLL.equals(actionInfo.getBizDefineType()) && !HRBizDefineTypeEnum.ENROLLAGAIN.equals(actionInfo.getBizDefineType()) && !HRBizDefineTypeEnum.EMPHIRE.equals(actionInfo.getBizDefineType()) && !HRBizDefineTypeEnum.FLUCTUACTION.equals(actionInfo.getBizDefineType()) && !HRBizDefineTypeEnum.REINVITE.equals(actionInfo.getBizDefineType());
	    }
	    
	    /**
	     * 填充的基本字段信息
	     * @param ctx
	     * @param preEntryPerson
	     * @param personInfo
	     * @param empPosOrgInfo
	     * @param hrOrg
	     * @return
	     * @throws BOSException
	     * @throws EASBizException
	     */
	    private Map fillPersonBaseInfoData(Context ctx, PreEntryPersonInfo preEntryPerson, PersonInfo personInfo, EmpPosOrgRelationInfo empPosOrgInfo, HROrgUnitInfo hrOrg)
	            throws BOSException, EASBizException
	        {
	            Map personOldInfo = new HashMap();
	            personOldInfo.put("empId", personInfo.getId().toString());
	            personOldInfo.put("empNumber", personInfo.getNumber());
	            personOldInfo.put("oldEmpType", personInfo.getEmployeeType().getId().toString());
	            EnrollAgainCodingEnum codingEnum = EnrollAgainCodingEnum.getEnum(Integer.parseInt((String)HRParamUtil.getParamByKey(ctx, null, "ENROLL_AGAIN_CODING_RULE")));
	            personOldInfo.put("useOldNumber", Integer.valueOf(codingEnum.getValue()));
	            if(empPosOrgInfo != null)
	            {
	                personOldInfo.put("oldPersonDepId", empPosOrgInfo.getAdminOrg().getId().toString());
	                personOldInfo.put("oldPersonPosId", empPosOrgInfo.getPosition().getId().toString());
	            }
	            IPersonOtherInfo iPersonOtherInfo = PersonOtherInfoFactory.getLocalInstance(ctx);
	            PersonOtherInfoCollection personOtherInfoCollection = iPersonOtherInfo.getPersonOtherInfoCollection((new StringBuilder()).append("where person = '").append(personInfo.getId().toString()).append("'").toString());
	            if(personOtherInfoCollection.size() > 0)
	                personOldInfo.put("jobStartDate", personOtherInfoCollection.get(0).getJobStartDate());
	            String msg = EASResource.getString("com.kingdee.eas.hr.affair.EnrollResource.personEnrollAgainNumber");
	            if(EnrollAgainCodingEnum.USE_NEW_NUMBER.equals(codingEnum))
	            {
	                Object params[] = new Object[4];
	                params[0] = personInfo.getNumber();
	                params[1] = preEntryPerson.getName();
	                params[2] = hrOrg.getName();
	                params[3] = EnrollAgainCodingEnum.USE_NEW_NUMBER.getAlias();
	                msg = MessageFormat.format(msg, params);
	            } else
	            {
	                Object params[] = new Object[4];
	                params[0] = personInfo.getNumber();
	                params[1] = preEntryPerson.getName();
	                params[2] = hrOrg.getName();
	                params[3] = EnrollAgainCodingEnum.USE_OLD_NUMBER.getAlias();
	                msg = MessageFormat.format(msg, params);
	            }
	            personOldInfo.put("personNumberMsg", msg);
	            return personOldInfo;
	        }
	
}

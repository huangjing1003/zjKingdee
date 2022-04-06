 package com.kingdee.eas.custom.prfnlReview.web.util.io;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.ICommonBOSType;
import com.kingdee.bos.dao.IObjectPK;
import com.kingdee.bos.metadata.IMetaDataLoader;
import com.kingdee.bos.metadata.MetaDataLoaderFactory;
import com.kingdee.bos.metadata.MetaDataPK;
import com.kingdee.bos.metadata.entity.EntityObjectInfo;
import com.kingdee.bos.util.BOSObjectType;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.base.permission.UserInfo;
import com.kingdee.eas.basedata.org.HROrgUnitInfo;
import com.kingdee.eas.basedata.person.PersonFactory;
import com.kingdee.eas.basedata.person.PersonInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.ep.CoreBillBaseCustomInfo;
import com.kingdee.eas.ep.DataBaseCustomInfo;
import com.kingdee.eas.framework.CoreBaseInfo;
import com.kingdee.eas.hr.ats.BillSubmitTypeEnum;
import com.kingdee.eas.hr.base.HRBillBaseInfo;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryCollection;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateEntryInfo;
import com.kingdee.eas.hr.base.SHRBillBaseTemplateInfo;
import com.kingdee.eas.hr.emp.PersonPositionCollection;
import com.kingdee.eas.hr.emp.PersonPositionFactory;
import com.kingdee.eas.hr.emp.PersonPositionInfo;
import com.kingdee.shr.ats.web.util.NumberCodeRule;
import com.kingdee.shr.ats.web.util.SHRBillUtil;
import com.kingdee.shr.ats.web.util.io.IOValidaateUtilNew;
import com.kingdee.shr.base.permission.api.auth.SHRUserOrgPermissionApi;
import com.kingdee.shr.base.syssetting.BaseItemCustomInfo;
import com.kingdee.shr.base.syssetting.app.io.fileImport.BaseColumnInfo;
import com.kingdee.shr.base.syssetting.app.io.fileImport.BaseImportService;
import com.kingdee.shr.base.syssetting.app.io.fileImport.BaseRowInfo;
import com.kingdee.shr.base.syssetting.app.io.fileImport.ImportException;
import com.kingdee.shr.base.syssetting.app.io.fileImport.ValueCovertUtils;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.ml.SHRWebResource;
import com.kingdee.shr.ml.util.SHRServerResource;
import com.kingdee.util.StringUtils;

/**
 * 专业序列人才盘点记录表导入类开发
 * @author ASUS
 */
public class PrfnlReviewNewService extends BaseImportService {
    private Map<String, Object> currRowMap = null;
    private Set<String> hrRangeIdSet = null;
    private Set<String> adminOrgUnitSet = null;
    private boolean hasCodingRule;
    private PersonInfo personInfo = null;
    private PersonPositionInfo personPositionInfo = null;
    CoreBaseInfo billInfo = new HRBillBaseInfo();
//     OverTimeFileNewService
    @Override
    protected void initService() throws ImportException {
        super.initService();
        this.init();
    }

    @Override
    protected void initDownload() throws SHRWebException {
        super.initDownload();
        this.init();
    }

    protected void init() {
        try {
            this.personInfo = SHRBillUtil.getCurrPersonInfo(this.getContext());
            this.personPositionInfo = IOValidaateUtilNew.getAdminOrgUnit(this.personInfo.getId().toString(), this.getContext());
            UserInfo currentUserInfo = (UserInfo)this.getContext().get("UserInfo");
            String userId = currentUserInfo.getId().toString();
            String permItemId = this.getPermItemId();
            this.hrRangeIdSet = SHRUserOrgPermissionApi.getUserHROrgRangeIdSet(this.getContext(), userId, permItemId);
            this.adminOrgUnitSet = SHRUserOrgPermissionApi.getUserAdminRangeIdSet(this.getContext(), userId, permItemId);
            CoreBaseInfo baseInfo = this.createInfo("com.kingdee.shr.custom.app.PrfnlReview",this.getContext());
            this.hasCodingRule = IOValidaateUtilNew.hasCodingRule(baseInfo, IOValidaateUtilNew.getMainOrgByCu(this.getContext()), this.getContext());
        } catch (Exception var4) {
            throw new ImportException("Init PrfnlReviewNewService failed ,please try again!", var4);
        }
    }

    @Override
    protected void verifyRowBiz(BaseRowInfo row) throws ImportException {
        try {
            this.validData(row);
        } catch (EASBizException e) {
            e.printStackTrace();
        } catch (BOSException e) {
            e.printStackTrace();
        }
        super.verifyRowBiz(row);
    }
    protected void validData(BaseRowInfo row) throws EASBizException, BOSException {
        this.currRowMap = this.getRow(row);
        if (!StringUtils.isEmpty(this.currRowMap.get("Person.number").toString()) && !StringUtils.isEmpty(this.currRowMap.get("Person.name").toString())) {
            IOValidaateUtilNew.isPerson(this.getContext(), this.currRowMap.get("Person.number").toString(), this.currRowMap.get("Person.name").toString());
        }
    }

    private Map<String, Object> getRow(BaseRowInfo row) {
        Map<String, Object> rowMap = new HashMap();
        this.transRowToObejct(row);
        Map<String, BaseColumnInfo> columnInfoMaps = row.getMapColumnInfo();
        Iterator iterator = columnInfoMaps.entrySet().iterator();

        while(iterator.hasNext()) {
            BaseColumnInfo columnInfo = (BaseColumnInfo)((Map.Entry)iterator.next()).getValue();
            if (columnInfo != null) {
                String value = row.getValueOfStringByIndex(columnInfo.getColumnIndex());
                if (value != null) {
                    String valueName = ValueCovertUtils.getComplexNameOfValue(value);
                    rowMap.put(columnInfo.getPropName(), valueName);
                }
            }
        }
        return rowMap;
    }

    @Override
    protected void importNew(BaseRowInfo row) throws ImportException {
        try {
            CoreBaseInfo reviewEntryInfo = getPrfnlReviewEntryInfo(row);
            CoreBaseInfo billInfo = getBillInfo(row);
            SHRBillBaseTemplateInfo SHRBillInfo = (SHRBillBaseTemplateInfo)billInfo;
            SHRBillBaseTemplateEntryInfo entryInfo = (SHRBillBaseTemplateEntryInfo)reviewEntryInfo;
            if(null==SHRBillInfo.getHrOrgUnit()) {
            	//获取人员设置hr组织
            	Object hrOrgUnitObj = entryInfo.get("hrOrgUnit");
            	if(hrOrgUnitObj!=null){
            		HROrgUnitInfo hrOrgUnitInfo = (HROrgUnitInfo)hrOrgUnitObj;
            		SHRBillInfo.setHrOrgUnit(hrOrgUnitInfo);
            	}
            }
            entryInfo.setBill(SHRBillInfo);
            SHRBillBaseTemplateEntryCollection entryColl= SHRBillInfo.getEntrys();
            entryInfo.setBill(SHRBillInfo);
            entryColl.add(entryInfo);
            //保存到数据库
            this.saveData(SHRBillInfo);
        } catch (BOSException | ShrWebBizException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void submitData(CoreBaseInfo coreBaseInfo) throws ImportException {
        ImportException importException;
        try {
            IObjectPK pk = this.getCoreBase().save(coreBaseInfo);
            coreBaseInfo.setId(BOSUuid.read(pk.toString()));
        } catch (EASBizException var4) {
            importException = new ImportException(var4.getMessage(this.getContext().getLocale()), var4);
            throw importException;
        } catch (BOSException var5) {
            importException = new ImportException(SHRServerResource.getString("com.kingdee.shr.base.syssetting.CommonserviceResource", "save_fails_bos", this.getContext()), var5);
            throw importException;
        } catch (Exception var6) {
            importException = new ImportException(SHRServerResource.getString("com.kingdee.shr.base.syssetting.CommonserviceResource", "save_submit_fails", this.getContext()), var6);
            throw importException;
        }
    }

    /**
     * 获取人才盘点记录表的分录info信息
     * @param row
     * @return
     */
    private CoreBaseInfo getPrfnlReviewEntryInfo(BaseRowInfo row) throws BOSException {
        String personNumber = this.currRowMap.get("Person.number").toString().trim();
        CoreBaseInfo baseInfo = this.transRowToObejct(row);
        Object object = baseInfo.get("prfnlreviewentry");
        if(object!=null) {
        	CoreBaseInfo entryInfo = (CoreBaseInfo)object;
        	PersonPositionInfo personPositionInfo = getPersonPositionInfo(personNumber);
        	entryInfo.put("person",personPositionInfo.getPerson());
        	entryInfo.put("position",personPositionInfo.getPrimaryPosition());
        	entryInfo.put("adminOrg",personPositionInfo.getPersonDep());
        	entryInfo.put("hrOrgUnit",personPositionInfo.getHrOrgUnit());
        	BOSUuid uid = BOSUuid.create(new BOSObjectType("BF9717E3"));
        	entryInfo.setId(uid);
        	return entryInfo;
        }else {
        	return null;
        }
    }
    private CoreBaseInfo getBillInfo(BaseRowInfo row) throws ShrWebBizException {
        String billNumber = this.currRowMap.get("number").toString().trim();
        billInfo = this.transRowToObejct(row);
        BOSUuid uid = BOSUuid.create(new BOSObjectType("D359C9CF"));
        billInfo.setId(uid);
        if(org.apache.commons.lang3.StringUtils.isEmpty(billNumber)) {
        	if (this.hasCodingRule) {
        		try {
        			billInfo.put("number",NumberCodeRule.getCodeRuleNumber(this.billInfo, NumberCodeRule.getMainOrgByCu(this.getContext())));
        		} catch (Exception var5) {
        			var5.printStackTrace();
        		}
        	} else {
        		billInfo.put("number",ReviewNumber());
        	}
        }else {
        	billInfo.put("number",billNumber);
        }
        billInfo.put("proposer",this.personInfo);
        billInfo.put("adminOrg",this.personPositionInfo.getPersonDep());
        billInfo.put("applyDate",new Date());
        billInfo.put("billSubmitType",BillSubmitTypeEnum.common);
        return billInfo;
    }
    /**
     * 获取人员职业信息
     * @param personNumber
     * @return
     * @throws BOSException
     */
    public PersonPositionInfo getPersonPositionInfo(String personNumber ) throws BOSException {
        String oqlpp = " select *  where person.number = '" + personNumber + "'";
        PersonPositionCollection positionColl  = PersonPositionFactory.getLocalInstance
                (this.getContext()).getPersonPositionCollection(oqlpp);
        if(null!=positionColl && positionColl.size()>0){
            return positionColl.get(0);
        }else{
            return new PersonPositionInfo();
        }
    }

    private CoreBaseInfo createInfo(String entityFullName,Context ctx) {
        IMetaDataLoader loader = MetaDataLoaderFactory.getMetaDataLoader(ctx);
        MetaDataPK metaDataPK = MetaDataPK.create(entityFullName);
        EntityObjectInfo eoInfo = loader.getEntity(metaDataPK);
        String valueClassName = eoInfo.getObjectValueClass();
        CoreBaseInfo info = null;

        try {
            Class<?> c = Class.forName(valueClassName);
            Constructor<?> cst = c.getConstructor();
            info = (CoreBaseInfo)cst.newInstance();
            if (info instanceof DataBaseCustomInfo) {
                ((DataBaseCustomInfo)info).setBOSType(eoInfo.getType());
            } else if (info instanceof BaseItemCustomInfo) {
                ((BaseItemCustomInfo)info).setBOSType(eoInfo.getType());
            } else if (info instanceof CoreBillBaseCustomInfo) {
                ((CoreBillBaseCustomInfo)info).setBOSType(eoInfo.getType());
            } else if (eoInfo.isDynamic() && info instanceof ICommonBOSType) {
                ICommonBOSType commonObject = (ICommonBOSType)info;
                commonObject.setBOSType(eoInfo.getType());
                commonObject.setPK(metaDataPK);
            }

            return info;
        } catch (Exception var10) {
            ImportException importException = new ImportException(SHRWebResource.getString("com.kingdee.shr.base.syssetting.CommonserviceResource", "creat_object_fails",ctx), var10);
            throw importException;
        }
    }

    public String ReviewNumber() {
        List<String> list = new ArrayList();
        String number = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        int num = (int)((Math.random() * 9.0D + 1.0D) * 10000.0D);
        number = "Review-" + df.format(new Date()) + "-" + num;
        return number;
    }


}

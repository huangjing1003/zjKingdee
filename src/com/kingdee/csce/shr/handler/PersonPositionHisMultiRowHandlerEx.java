package com.kingdee.csce.shr.handler;

import com.kingdee.bos.metadata.entity.EntityViewInfo;
import com.kingdee.eas.hr.emp.web.handler.EmployeePageMultiRowHandler;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;

public class PersonPositionHisMultiRowHandlerEx extends EmployeePageMultiRowHandler {

    @Override
    protected EntityViewInfo getEntityViewInfo(String relateField, String billId) throws SHRWebException {
        EntityViewInfo entityViewInfo = super.getEntityViewInfo(relateField, billId);
        entityViewInfo.getFilter().appendFilterItem("hrOrgUnit.id", "00000000-0000-0000-0000-000000000000CCE7AED4");
        return entityViewInfo;
    }
}

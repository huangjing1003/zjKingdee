package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.eas.basedata.org.AdminOrgUnitFactory;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.web.handler.DEPCustomBillEditHandlerExt;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class StaffDemandApplicationHandler extends DEPCustomBillEditHandlerExt {

    public void getReqPositionFilterAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        Context ctx = SHRContext.getInstance().getContext();
        String adminOrgId = request.getParameter("adminOrgId");
        Map<String, String> result = new HashMap<>();
        try {
            String longNumber = AdminOrgUnitFactory.getLocalInstance(ctx).getAdminOrgUnitInfo(new ObjectUuidPK(adminOrgId)).getLongNumber();
            String filter = "adminOrgUnit.longNumber = '" + longNumber + "' OR adminOrgUnit.longNumber LIKE '" + longNumber + "%'";
            result.put("filter", filter);
            JSONUtils.SUCCESS(result);
        } catch (BOSException | SHRWebException | EASBizException e) {
            e.printStackTrace();
        }
    }

}

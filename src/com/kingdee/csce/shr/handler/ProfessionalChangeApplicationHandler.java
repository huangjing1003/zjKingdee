package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.csce.shr.utils.PersonUtil;
import com.kingdee.eas.basedata.org.AdminOrgUnitInfo;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.web.handler.DEPCustomBillEditHandler;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ProfessionalChangeApplicationHandler extends DEPCustomBillEditHandler {

    public void getProfessionalDepartmentAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String personId = request.getParameter("personId");
        try {
            AdminOrgUnitInfo professionalDepartment = PersonUtil.getProfessionalDepartment(personId);
            JSONUtils.SUCCESS(professionalDepartment);
        } catch (BOSException e) {
            e.printStackTrace();
        } catch (SHRWebException e) {
            e.printStackTrace();
        }
    }

}

package com.kingdee.csce.shr.handler;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.util.BOSUuid;
import com.kingdee.eas.basedata.org.*;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.hr.org.JobGradeModuleFactory;
import com.kingdee.eas.hr.org.JobGradeModuleInfo;
import com.kingdee.shr.affair.web.handler.EmpPreEntryEditHandler;
import com.kingdee.shr.base.syssetting.context.SHRContext;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 预入职表单api
 *
 * @author xudong.yao
 * @date 2021-01-15 11:58:27
 */
public class PreEntryHandler extends EmpPreEntryEditHandler {

    public void getJobGradeModuleInfoAction(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) throws SHRWebException {
        Context ctx = SHRContext.getInstance().getContext();
        String positionId = request.getParameter("positionId");
        try {
            PositionInfo positionInfo = PositionFactory.getLocalInstance(ctx).getPositionInfo(new ObjectUuidPK(positionId));
            JobGradeModuleInfo jobGradeModuleInfo = (JobGradeModuleInfo) positionInfo.get("positionClass");
            if (null != jobGradeModuleInfo) {
                String jobGradeId = ((BOSUuid) jobGradeModuleInfo.get("id")).toString();
                jobGradeModuleInfo = JobGradeModuleFactory.getLocalInstance(ctx).getJobGradeModuleInfo(new ObjectUuidPK(jobGradeId));
                JSONUtils.SUCCESS(jobGradeModuleInfo);
            }
        } catch (EASBizException e) {
            e.printStackTrace();
        } catch (BOSException e) {
            e.printStackTrace();
        }
    }

}

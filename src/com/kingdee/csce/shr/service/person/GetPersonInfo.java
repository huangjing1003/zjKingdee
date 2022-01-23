package com.kingdee.csce.shr.service.person;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.bsf.service.app.IHRMsfService;
import com.kingdee.csce.shr.utils.PersonUtil;
import com.kingdee.eas.common.EASBizException;

import java.util.Map;

/**
 * 获取人员信息
 *
 * @author xudong.yao
 * @date 2021/5/6
 */
public class GetPersonInfo implements IHRMsfService {
    @Override
    public Object process(Context context, Map map) throws EASBizException, BOSException {
        String personId = (String)map.get("personId");
        return PersonUtil.getPersonInfo(personId);
    }
}

package com.kingdee.shr.batchContract.web.handler;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.kingdee.bos.BOSException;
import com.kingdee.bos.metadata.entity.FilterInfo;
import com.kingdee.shr.base.syssetting.exception.SHRWebException;
import com.kingdee.shr.base.syssetting.exception.ShrWebBizException;
import com.kingdee.shr.base.syssetting.util.MetaDataUtil;
import com.kingdee.shr.base.syssetting.util.SysSettingSHRBaseItemUtil;
import com.kingdee.shr.base.syssetting.web.json.JSONUtils;
import com.kingdee.shr.batchContract.web.util.ContractListFastFilterHelper;
import com.kingdee.shr.batchContract.web.util.ContractListFastFilterHelperExt;
import com.kingdee.util.StringUtils;

/**
 * 合同列表
 * @author hj
 *
 */
public class ContractCompositionQueryHandlerExt extends ContractCompositionQueryHandler {
	
    private static Logger logger = Logger.getLogger("com/kingdee/shr/batchContract/web/handler/ContractCompositionQueryHandlerExt");
	
    
	/**
	 * 获取快速过滤条件
	 */
    protected FilterInfo getFastFilter(HttpServletRequest request)
            throws SHRWebException
        {
            String fastFilterItems = request.getParameter("fastFilterItems");
            FilterInfo contractViewDimensionFilter = null;
            if(StringUtils.isEmpty(fastFilterItems))
                if(SysSettingSHRBaseItemUtil.isSHRBaseItem(MetaDataUtil.getEntityObjectByEntityName(getUIViewInfo(request).getEntityName()).getBaseEntity()))
                    return getBaseItemFastFilter(request);
                else
                    return null;
            Map map = JSONUtils.convertJsonToObject(fastFilterItems);
            if(null == map || map.size() <= 0)
                if(SysSettingSHRBaseItemUtil.isSHRBaseItem(MetaDataUtil.getEntityObjectByEntityName(getUIViewInfo(request).getEntityName()).getBaseEntity()))
                    return getBaseItemFastFilter(request);
                else
                    return null;
            FilterInfo filter = null;
            Iterator i$ = map.keySet().iterator();
            do
            {
                if(!i$.hasNext())
                    break;
                Object key = i$.next();
                if(map.get(key) instanceof Map)
                {
                    Map subMap = (Map)map.get(key);
                    if("contract".equals(key))
                        try
                        {
                        	//改成自己的扩展类
                            contractViewDimensionFilter =ContractListFastFilterHelperExt.convertContractFilter(subMap);
                        }
                        catch(BOSException e)
                        {
                            logger.error(e);
                            throw new ShrWebBizException(e);
                        }
                    else
                        request.setAttribute((String)key, subMap.get("values"));
                }
            } while(true);
            if(SysSettingSHRBaseItemUtil.isSHRBaseItem(MetaDataUtil.getEntityObjectByEntityName(getUIViewInfo(request).getEntityName()).getBaseEntity()))
                filter = getBaseItemFastFilter(request);
            else
                filter = getBusinessFastFilter(request);
            if(contractViewDimensionFilter != null)
                try
                {
                    filter.mergeFilter(contractViewDimensionFilter, "AND");
                }
                catch(BOSException e)
                {
                    logger.error(e);
                    throw new ShrWebBizException(e);
                }
            return filter;
        }

}

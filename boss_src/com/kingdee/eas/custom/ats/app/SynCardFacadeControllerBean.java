  package com.kingdee.eas.custom.ats.app;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.kingdee.bos.BOSException;
import com.kingdee.bos.Context;
import com.kingdee.bos.dao.ormapping.ObjectUuidPK;
import com.kingdee.bos.metadata.entity.FilterInfo;
import com.kingdee.bos.metadata.entity.FilterItemInfo;
import com.kingdee.bos.metadata.entity.SelectorItemCollection;
import com.kingdee.bos.metadata.entity.SelectorItemInfo;
import com.kingdee.bos.metadata.query.util.CompareType;
import com.kingdee.eas.base.permission.UserInfo;
import com.kingdee.eas.common.EASBizException;
import com.kingdee.eas.custom.ats.IWxtoken;
import com.kingdee.eas.custom.ats.WxtokenCollection;
import com.kingdee.eas.custom.ats.WxtokenFactory;
import com.kingdee.eas.custom.ats.WxtokenInfo;
import com.kingdee.eas.custom.resp.PunchCardRecordResp;
import com.kingdee.eas.custom.resp.WxTokenResp;
import com.kingdee.eas.custom.utils.ConfigUtils;
import com.kingdee.eas.custom.utils.Constants;
import com.kingdee.eas.framework.CoreBaseCollection;
import com.kingdee.eas.hr.ats.*;
import com.kingdee.eas.util.app.ContextUtil;
import com.kingdee.eas.util.app.DbUtil;
import com.kingdee.jdbc.rowset.IRowSet;
import com.kingdee.util.StringUtils;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.tools.ant.util.DateUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 同步企业微信打卡记录
 * @author hj
 */
public class SynCardFacadeControllerBean extends AbstractSynCardFacadeControllerBean
{
    private static Logger logger =
        Logger.getLogger("com.kingdee.eas.custom.ats.app.SynCardFacadeControllerBean");
    /**
     * 获取接口配置信息HttpUtil
     */
    private   Map<String,String> configMap = ConfigUtils.loadConfig();
    //过期时间，7000s
    private static long expires_in =7000L;

    /**
     * 同步企业微信打卡记录
     * @param ctx
     * @param days  前沿天数
     * @param userNumber 用户账号
     * @throws BOSException
     */
    @Override
    protected void _synData(Context ctx, int days, String userNumber) throws BOSException {
        super._synData(ctx, days, userNumber);
        try {
            long startTime = getStartTime(days);
            long endTime = getEndTime();
            //每页条数
            int pageSize =100;
            try {
                //页数
                int pageNo = getPageNo(getSynPersonTotalSize(ctx), pageSize);
                IAttendanceFile iAttendanceFile = AttendanceFileFactory.getLocalInstance(ctx);
                for (int i=1;i<=pageNo;i++){
                    ArrayList<String> userIdsList = new ArrayList<>();
                    IRowSet pagingDataRowSet = getPagingData(ctx, (i - 1) * pageSize, pageSize * i);
                    //userdId对应的考勤档案info
                    Map<String, AttendanceFileInfo> attendanceFileMap = new HashMap<>();
                    while(pagingDataRowSet.next()){
                        String userId = pagingDataRowSet.getString("userId");
                        String attendanceFileId = pagingDataRowSet.getString("attendanceFileId");
                        AttendanceFileInfo attendanceFileInfo = getAttendanceFile(iAttendanceFile,attendanceFileId);
                        if(!userIdsList.contains(userId)){
                            userIdsList.add(userId);
                            attendanceFileMap.put(userId,attendanceFileInfo);
                        }
                    }
                    if(userIdsList.size()>0){
                        //获取打卡记录并处理
                        getPunchCardRecord(ctx,userIdsList,startTime,endTime,attendanceFileMap);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new BOSException(e.getMessage());
            } catch (EASBizException e) {
                e.printStackTrace();
                throw new BOSException(e.getMessage());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new BOSException(e.getMessage());
        }
    }


    /**
     * 获取考勤档案信息
     * @param iAttendanceFile
     * @param attendanceFileId
     * @return
     * @throws EASBizException
     * @throws BOSException
     */
    public AttendanceFileInfo getAttendanceFile(IAttendanceFile iAttendanceFile,  String attendanceFileId) throws EASBizException, BOSException {
        SelectorItemCollection selectorItemColl = new SelectorItemCollection();
        selectorItemColl.add(new SelectorItemInfo("id"));
        selectorItemColl.add(new SelectorItemInfo("attendanceNum"));
        selectorItemColl.add(new SelectorItemInfo("attPosition"));
        selectorItemColl.add(new SelectorItemInfo("proposer"));
        selectorItemColl.add(new SelectorItemInfo("adminOrgUnit"));
        selectorItemColl.add(new SelectorItemInfo("position"));
        selectorItemColl.add(new SelectorItemInfo("hrOrgUnit"));
        AttendanceFileInfo attendanceFileInfo =
                iAttendanceFile.getAttendanceFileInfo(new ObjectUuidPK(attendanceFileId),selectorItemColl);
        return attendanceFileInfo;
    }

    /**
     * 调用企业微信接口获取用户的打卡记录
     * @param ctx
     * @param userIdsList
     * @param startTime
     * @param endTime
     */
    public void getPunchCardRecord(Context ctx,ArrayList<String> userIdsList,
                   long startTime, long endTime,Map<String, AttendanceFileInfo> attendanceFileMap)
            throws BOSException {
        String token = null;
        try {
            token = addAccessToken(ctx);
        } catch (EASBizException e) {
            e.printStackTrace();
            logger.error("获取打卡token异常"+e.getMessage());
            throw new BOSException("获取token异常"+e.getMessage());
        }
        if (!StringUtils.isEmpty(token)){
            String paramJson = buildPunchParam(userIdsList, startTime, endTime);
            //调用接口获取结果
            String responseStr =parseResult(HttpUtil.post(String.format(Constants.QYWX_PUNCHCARDRECORD_URL, token),
                    paramJson, Constants.TIME_OUT))  ;
            PunchCardRecordResp  recordResp = JSON.parseObject(responseStr, PunchCardRecordResp.class);
            //处理打卡记录信息，同步到数据库
            dealWithPunchCardRecord(ctx,recordResp,attendanceFileMap);
        }
    }
    /**
     * 处理打卡记录同步到金蝶数据库
     * @param recordResp
     */
    public void dealWithPunchCardRecord(Context ctx,PunchCardRecordResp  recordResp,
                                        Map<String, AttendanceFileInfo> attendanceFileMap) throws BOSException {
        List<PunchCardRecordResp.CheckindataBean> checkinDataList = recordResp.getCheckindata();
        CoreBaseCollection recordColl = new CoreBaseCollection();
        IPunchCardRecord iPunchCardRecord = PunchCardRecordFactory.getLocalInstance(ctx);
        //当前用户信息
        UserInfo userInfo= ContextUtil.getCurrentUserInfo(ctx);
        checkinDataList.stream().forEach(item->{
            String userid = item.getUserid();
            long checkin_time = item.getCheckin_time()*1000L;
            AttendanceFileInfo fileInfo = attendanceFileMap.get(userid);
            Timestamp timestamp = new Timestamp(checkin_time);
            //判断人员打卡记录是否已经存在，存在则不插入
            FilterInfo filterInfo = new FilterInfo();
            String personId = fileInfo.getProposer().getId().toString();
            filterInfo.getFilterItems().add(new FilterItemInfo("proposer", personId));
            filterInfo.getFilterItems().add(new FilterItemInfo("punchCardTime",timestamp, CompareType.EQUALS));
            try {
                boolean exists = iPunchCardRecord.exists(filterInfo);
                if(!exists){
                    Date  punchCardDate = DateUtils.parseIso8601Date(DateUtils.format(checkin_time, "yyyy-MM-dd"));
                    PunchCardRecordInfo punchCardRecordInfo = new PunchCardRecordInfo();
                    punchCardRecordInfo.setAdminOrgUnit(fileInfo.getAdminOrgUnit());
                    punchCardRecordInfo.setProposer(fileInfo.getProposer());
                    punchCardRecordInfo.setAttAdminOrgUnit(fileInfo.getAttAdminOrgUnit());
                    punchCardRecordInfo.setAttendanceNum(fileInfo.getAttendanceNum());
                    punchCardRecordInfo.setPunchCardPlace(item.getLocation_title());
                    punchCardRecordInfo.setPunchCardDate( punchCardDate);
                    punchCardRecordInfo.setPunchCardTime(timestamp);
                    punchCardRecordInfo.setHrOrgUnit(fileInfo.getHrOrgUnit());
                    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
                    punchCardRecordInfo.setCreateTime(currentTime);
                    punchCardRecordInfo.setLastUpdateTime(currentTime);
                    punchCardRecordInfo.setCreator(userInfo);
                    punchCardRecordInfo.setLastUpdateUser(userInfo);
                    recordColl.add(punchCardRecordInfo);
                }else{
                    logger.error("员工编码：["+fileInfo.getProposer().getNumber()+"]，打卡时间："+timestamp+" 已经存在");
                }
            } catch (BOSException e) {
                e.printStackTrace();
            } catch (EASBizException e) {
                e.printStackTrace();
            }catch (ParseException e) {
                e.printStackTrace();
            }
        });
        try {
            iPunchCardRecord.saveBatchData(recordColl);
        } catch (EASBizException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取企业微信token
     */
    public String addAccessToken(Context ctx ) throws BOSException, EASBizException {
        //先查询表里面是否存在token数据
        IWxtoken iWxtoken = WxtokenFactory.getLocalInstance(ctx);
        WxtokenCollection tokenColl =iWxtoken.getWxtokenCollection();
        if(tokenColl!=null && tokenColl.size()>0){
            WxtokenInfo wxtokenInfo = tokenColl.get(0);
            //和当前时间比较是否过期
            System.currentTimeMillis();
            long currentTime = System.currentTimeMillis()/ 1000;
            long updateTime = wxtokenInfo.getLastUpdateTime().getTime() / 1000;
            if((currentTime-updateTime)>expires_in){
                String token = getAccess_token();
                if(!StringUtils.isEmpty(token)){
                    wxtokenInfo.setToken(token);
                    iWxtoken.update(new ObjectUuidPK(wxtokenInfo.getId().toString()),wxtokenInfo);
                    return token;
                }else{
                    return null;
                }
            }else{
                return wxtokenInfo.getToken();
            }
        }else{
            //不存在token的时候就获取并写入中间表保存
            String refreshToken = getAccess_token();
            if(!StringUtils.isEmpty(refreshToken)){
                WxtokenInfo wxtokenInfo = new WxtokenInfo();
                wxtokenInfo.setToken(refreshToken);
                iWxtoken.save(wxtokenInfo);
                return refreshToken;
            }
            return null;
        }
    }

    /**
     * 刷新获取token
     * @return
     */
    public String getAccess_token() throws BOSException {
        //企业ID
        String appId = configMap.get("corpid");
        //打卡应用的凭证密钥
        String secret = configMap.get("corpsecret");
        //企业微信的host
        String tokenResp = parseResult(HttpUtil.get(String.format(Constants.QYWX_ACCESS_TOKEN_URL, appId, secret)));
        WxTokenResp wxTokenResp = JSON.parseObject(tokenResp, WxTokenResp.class);
        if(wxTokenResp!=null){
           return wxTokenResp.getAccess_token();
        }else{
           throw new BOSException("获取token出错");
        }
    }

    /**
     * 构建调用打卡记录的参数
     * @param userIdsList
     * @param startTime
     * @param endTime
     * @return
     */
    public String buildPunchParam(ArrayList<String> userIdsList,long startTime, long endTime){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("opencheckindatatype",3);
        paramMap.put("starttime",startTime);
        paramMap.put("endtime",endTime);
        paramMap.put("useridlist",userIdsList);
        return  JSON.toJSONString(paramMap);
    }

    /**
     * 统一处理企业微信调用回来的结果数据
     * @param resultJson
     * @return
     */
    public String parseResult(String resultJson) throws BOSException {
        JSONObject jsonObject = JSONObject.fromObject(resultJson);
        if(jsonObject.containsKey(Constants.ERRCODE)){
            if(jsonObject.getInt(Constants.ERRCODE)==Constants.WX_CODE){
                return resultJson;
            }else{
                throw new BOSException(jsonObject.getString(Constants.ERRMSG));
            }
        }
        return resultJson;
    }



    /**
     * 获取总共页数
     * @param totalSize
     * @param pageSize
     * @return
     */
    public int getPageNo(int totalSize,int pageSize){
        int pageNo = totalSize / pageSize;
        if(totalSize%pageSize>0){
            pageNo +=1;
        }
        return pageNo;
    }


    /**
     * 获取分页数据
     * @param ctx
     * @param startSize
     * @param endSize
     * @return
     * @throws BOSException
     */
    public IRowSet getPagingData(Context ctx,int startSize,int endSize) throws BOSException {
        StringBuffer buffer = getSynPersonSql(false);
        buffer.append(" where temp.rn >").append(startSize);
        buffer.append(" and temp.rn<=").append(endSize);
        IRowSet rowSet = DbUtil.executeQuery(ctx, buffer.toString());
        return rowSet;
    }

    /**
     * 获取当前时间的前几天
     * 转化为秒的时间戳
     * @param days 前几天
     */
    private  long getStartTime(int days) throws ParseException {
        //当前时间
        Date dNow = new Date();
        Date dBefore = new Date();
        //得到日历
        Calendar calendar = Calendar.getInstance();
        //把当前时间赋给日历
        calendar.setTime(dNow);
        //设置为前几天
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        //得到前days天的时间
        dBefore = calendar.getTime();
        SimpleDateFormat startSdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String format = startSdf.format(dBefore);
        return startSdf.parse(format).getTime()/1000L;
    }

    /**
     * 获取结束时间戳
     * 默认为当天日期+1天
     * @return
     * @throws ParseException
     */
    private long getEndTime() throws ParseException {
        Calendar  calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DAY_OF_MONTH,1);
        SimpleDateFormat endSdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String format = endSdf.format(calendar.getTime());
        return endSdf.parse(format).getTime() / 1000L;
    }

    /**
     * 查询需要同步打卡记录的人员
     * @param isQuerySize
     * @return
     */
    public StringBuffer getSynPersonSql(boolean isQuerySize){
        StringBuffer buffer = new StringBuffer("SELECT  * FROM (");
        if(isQuerySize){
            buffer =  new StringBuffer("SELECT count(1) total FROM (");
        }
        buffer.append(" SELECT t.*,rownum rn FROM (");
        buffer.append(" SELECT   users.FNUMBER userId, person.fid personId,person.fname_l2 personName,");
        buffer.append(" person.FNUMBER  personNumber,attendanceFile.Fid  attendanceFileId ");
        buffer.append(" FROM ");
        buffer.append(" t_pm_user users  ");
        buffer.append(" left join  t_bd_person person  on person.fid = users.FPERSONID ");
        buffer.append(" left join  T_HR_ATS_AttendanceFile attendanceFile on attendanceFile.FPROPOSERID =person.fid ");
        buffer.append(" where FFORBIDDEN='0' and person.fid is not null ");
        buffer.append(" and attendanceFile.FHRORGUNITID in ").append(configMap.get("hrOrgUnitId"));
        buffer.append(" and users.FNUMBER is not null order by person.FNUMBER )t ) temp");
        return buffer;
    }
    /**
     * 获取需要同步打卡记录的总人数
     * @param ctx
     * @return
     * @throws BOSException
     * @throws SQLException
     */
    public int getSynPersonTotalSize(Context ctx) throws BOSException, SQLException {
        StringBuffer personSql = getSynPersonSql(true);
        IRowSet iRowSet = DbUtil.executeQuery(ctx, personSql.toString());
        if(iRowSet.next()){
            return iRowSet.getInt("total");
        }
        return 0;
    }

}
/**
 * 人员需求申请表form
 *
 * @name: csce.shr.StaffDemandApplication
 * @model: com.kingdee.eas.hr.affair.app.StaffingAdd
 * @uipk: com.kingdee.eas.hr.affair.app.StaffingAdd.form
 * @author: xy
 * @date: 2021/1/20 21:51
 */
shr.defineClass(
  "csce.shr.StaffDemandApplication", shr.custom.bizbill.DepCustomBillEditExt, {
    initalizeDOM: function () {
      csce.shr.StaffDemandApplication.superClass.initalizeDOM.call(this)
      const _self = this
      const formType = _self.getOperateState().toUpperCase()
      if ("VIEW" !== formType) {
        _self.initReqPosition()
      }
    },
    initReqPosition: function () {
      const _self = this
      const adminValue = _self.getField("entrys.adminOrg").shrPromptBox("getValue")
      if (null != adminValue) {
        const param = {
          adminOrgId: adminValue.id
        }
        _self.remoteCall({
          type: "post",
          method: "getReqPositionFilter",
          param: param,
          async: false,
          success: function (result) {
            if (result.filter) {
              _self.getField("entrys.reqPosition").shrPromptBox("setFilter", result.filter)
            }
          },
        })
      }
    }
  }
);

/**
 * 人员需求申请表form
 *
 * @name: csce.shr.ProfessionalChangeApplication
 * @model: com.kingdee.eas.hr.affair.app.ProChange
 * @uipk: com.kingdee.eas.hr.affair.app.ProChange.form
 * @author: xy
 * @date: 2021/1/20 21:51
 */
shr.defineClass(
  "csce.shr.ProfessionalChangeApplication", shr.custom.bizbill.DepCustomBillEditExt, {
    initalizeDOM: function () {
      csce.shr.ProfessionalChangeApplication.superClass.initalizeDOM.call(this)
      const _self = this
      const formType = _self.getOperateState().toUpperCase()
      if ("VIEW" !== formType) {
        _self.hrOrgUnitChangeEvent()
        _self.personChange()
      }
    },
    hrOrgUnitChangeEvent: function() {
      const _self = this
      _self.getField("hrOrgUnit").shrPromptBox("option", {
        onchange: function (e, value) {
          const currentValue = value.current;
          if (currentValue === null) {
            return
          }
          const filter = " HROrgUnit.id = '" + currentValue.id + "'"
          _self.getField("entrys.person").shrPromptBox("setFilter",filter)
        }
      })
    },
    personChange: function() {
      const _self = this
      _self.getField("entrys.person").shrPromptBox("option", {
        onchange: function (e, value) {
          const currentValue = value.current;
          if (currentValue === null) {
            return
          }
          const adminOrg = {
            id: currentValue['adminOrgUnit.id'],
            name: currentValue['adminOrgUnit.name']
          }
          const position = {
            id: currentValue['position.id'],
            name: currentValue['position.name']
          }
          _self.getField("entrys.person.number").shrTextField("setValue", currentValue.number)
          _self.getField("entrys.adminOrg").shrPromptBox("setValue", adminOrg)
          _self.getField("entrys.position").shrPromptBox("setValue", position)
          const param = {
            personId: currentValue.id
          }
          _self.remoteCall({
            type: "post",
            method: "getProfessionalDepartment",
            param: param,
            async: false,
            success: function (result) {
              if (result.personInfo) {
              }
              _self.getField("entrys.oldProOrg").shrPromptBox("setValue", result)
            },
          })
        }
      })
    }
  }
);

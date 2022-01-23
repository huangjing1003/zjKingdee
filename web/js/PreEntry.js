/**
 *
 *
 * @name: shr.emp.EmpPreEntryEditEx
 * @description: 预入职_员工预入职form
 * @model: com.kingdee.shr.preentry.app.PreEntry
 * @uipk: emp.PreEntry.form
 * @author  xy
 * @date: 2021/1/19 16:51
 */
shr.defineClass("csce.shr.PreEntry", shr.emp.EmpPreEntryEdit, {
  empBizTypeID: "SGuJm3nXW0GshQfKRXenomWJ1dE=",

  initalizeDOM: function () {
    csce.shr.PreEntry.superClass.initalizeDOM.call(this);
    var that = this;
    /**
     * 不等于查看的时候，绑定事件
     */
    that.hrOrgUnitF7ChangeEvent();
    that.viewInit();
    if (that.getOperateState() != "VIEW") {
      that.adminOrgChange();
      that.positionChange();
      that.personDepPChange();
      that.politicalFaceChange();
      that.salaryCalcBind();
      that.talentIdCardNoChange();
      that.pstnClassChange();
    }
  },
  /**
   * Override父类方法
   * 人事业务组织 改变后 改变薪水可见性
   */
  hrOrgUnitF7ChangeEvent: function () {
    var _self = this;
    if (_self.getOperateState().toUpperCase() === "VIEW") {
      return;
    }
    if (_self.getOperateState().toUpperCase() !== "VIEW") {
      _self
        .getField("position")
        .shrPromptBox(
          "setFilter",
          " bizManageType.id='" +
          _self.empBizTypeID +
          "' and hrOrgUnit.id = '" +
          $("#hrOrgUnit_el").val() +
          "' and adminOrgBURelation.state = 1"
        );
    }
    if (_self.getOperateState().toUpperCase() == "EDIT") {
      $("#hrOrgUnit").shrPromptBox("disable");
    }
    _self.getField("hrOrgUnit").shrPromptBox("option", {
      onchange: function (e, value) {
        var hrOrgUnitF7Value = value.current;
        if (null != hrOrgUnitF7Value) {
          var hrOrgUnitName = hrOrgUnitF7Value.name;
          _self.setSalaryStandardVisibility(hrOrgUnitName);
        }
        // 设置Filter
        var filter = _self.getPositionFilter(hrOrgUnitF7Value, null);
        _self.getField("position").shrPromptBox("setFilter", filter);

        // 设置可见性
        if (hrOrgUnitF7Value.name === '属地化国际化管理板块') {
          $("#politicalFace").shrPromptBox("setValue", {
            id: '',
            name: ''
          });
          $("#JionPtyDate").shrDateTimePicker("setValue", '');
          $("#politicalFace").shrPromptBox("disable");
          $("#JionPtyDate").shrDateTimePicker("disable");
        } else {
          $("#politicalFace").shrPromptBox("enable");
          $("#JionPtyDate").shrDateTimePicker("enable");
        }
      },
    });
  },
  /**
   * 应聘部门/项目 改变后限制 应聘职位 的范围
   */
  adminOrgChange: function () {
    var _self = this;
    $("#adminOrg").shrPromptBox("option", {
      onchange: function (e, value) {
        // 防止递归overflow
        $("#position").val(null);
        var adminOrgF7Value = value.current;
        var hrOrgUnitF7Value = $("#hrOrgUnit").shrPromptBox("getValue");
        var filter = _self.getPositionFilter(hrOrgUnitF7Value, adminOrgF7Value);
        _self.getField("position").shrPromptBox("setFilter", filter);
      },
    });
  },

  /**
   * 获取filter信息
   */
  getPositionFilter: function (hrOrgUnitF7Value, adminOrgF7Value) {
    var filter;
    if (hrOrgUnitF7Value && typeof hrOrgUnitF7Value.id != "undefined") {
      if (adminOrgF7Value && typeof adminOrgF7Value.id != "undefined") {
        filter =
          " bizManageType.id='" +
          _self.empBizTypeID +
          "' and hrOrgUnit.id = '" +
          hrOrgUnitF7Value.id +
          "' and adminOrgBURelation.state = 1" +
          " and adminOrgUnit.id = '" +
          adminOrgF7Value.id +
          "'";
      } else {
        filter =
          " bizManageType.id='" +
          _self.empBizTypeID +
          "' and hrOrgUnit.id = '" +
          hrOrgUnitF7Value.id +
          "' and adminOrgBURelation.state = 1";
      }
    } else {
      filter = " id = null";
    }
    return filter;
  },

  /**
   * 应聘职位 改变同步 岗位序列
   */
  positionChange: function () {
    var that = this;
    $("#position").shrPromptBox("option", {
      onchange: function (e, value) {
        if (value && value.current && value.current.id) {
          that.setAdminOrgValueByPosition(value.current.id);
        } else {
          that.setOrgUnitLayerValue("");
        }
        $("#pstnClass").shrPromptBox("setValue", {
          id: "",
          name: "",
        });
        var info = value.current;
        if (null != info) {
          var positionId = info.id;
          that.remoteCall({
            type: "post",
            method: "getJobGradeModuleInfo",
            param: {positionId: positionId},
            async: false,
            success: function (res) {
              if (res != null) {
                $("#pstnClass").shrPromptBox("setValue", res);
              }
            },
          });
        }
      },
    });
  },
  /**
   * 岗位序列修改后逻辑
   */
  pstnClassChange: function() {
    var _self = this;
    $("#pstnClass").shrPromptBox("option", {
      onchange: function (e, value) {
        let currentVal = value.current;
        _self.setPositionInfoEditable(currentVal.name);
      }
    })
  },
  /**
   * 根据岗位序列值修改专业序列和专业序列职位
   */
  setPositionInfoEditable: function(pstnClassName) {
    if ("操作序列" === pstnClassName) {
      $("#personDepP").shrPromptBox("disable");
      $("#pPosition").shrPromptBox("disable");
    } else {
      $("#personDepP").shrPromptBox("enable");
      $("#pPosition").shrPromptBox("enable");
    }
  },
  setAdminOrgValueByPosition: function (positionId) {
    var self = this;
    if (!$.isEmptyObject(positionId)) {
      shr.callService({
        serviceName: "getOrgUnitLayerInfoByPositionService",
        param: {
          positionId: positionId,
        },
        async: false,
        success: function (data) {
          self.setOrgUnitLayerValue(data);
        },
      });
    }
  },
  setOrgUnitLayerValue: function (data) {
    var self = this;

    var hrOrgUnitValue = {
      id: data["adminOrgUnit.manageHrOrg.id"],
      name: data["adminOrgUnit.manageHrOrg.name"],
    };
    self.getField("hrOrgUnit").shrPromptBox("setValue", hrOrgUnitValue);

    self.setAdminOrgValue({
      id: data["adminOrgUnit.id"],
      name: data["adminOrgUnit.name"],
    });
    self.setCompanyValue({
      id: data["adminOrgUnit.company.id"],
      name: data["adminOrgUnit.company.name"],
    });
    self.setDepartmentValue({
      id: data["adminOrgUnit.department.id"],
      name: data["adminOrgUnit.department.name"],
    });
    self.setOfficeValue({
      id: data["adminOrgUnit.office.id"],
      name: data["adminOrgUnit.office.name"],
    });
    self.setGroupValue({
      id: data["adminOrgUnit.group.id"],
      name: data["adminOrgUnit.group.name"],
    });
  },
  setAdminOrgValue: function (data) {
    this.setF7Value("adminOrg", data);
  },

  setGroupValue: function (data) {
    this.setF7Value("position.adminOrgUnit.levelFourGroup", data);
  },

  setCompanyValue: function (data) {
    this.setF7Value("position.adminOrgUnit.company", data);
  },

  setDepartmentValue: function (data) {
    this.setF7Value("position.adminOrgUnit.department", data);
  },

  setOfficeValue: function (data) {
    this.setF7Value("position.adminOrgUnit.office", data);
  },
  setF7Value: function (field, data) {
    //判断是否控件初始化
    var fld = this.getField("" + field);
    if (fld == null || fld.length == 0) {
      return;
    }
    if (fld.is("input:hidden")) {
      fld.val(data.id);
    } else if (fld.attr("ctrlrole") == "promptBox") {
      fld.shrPromptBox("setValue", {
        id: data.id,
        name: data.name,
      });
    }
  },
  /**
   * 身份证号码改变同步生日和出生日期
   */
  talentIdCardNoChange: function() {
    $("#talent_idCardNO").change(function() {
      let value = $("#talent_idCardNO").val();
      try {
        let birthday = `19${value.substring(6, 8)}-${value.substring(9, 10)}-${value.substring(11, 12)}`;
        $("#talent_birthday").shrDateTimePicker("setValue", birthday);
        let year = value.substring(6, 10);
        let currentYear = new Date().getFullYear();
        let age = currentYear - year;
        $("#age").shrNumberField("setValue", age);
      } catch (e) {

      }
    })
  },
  /**
   * 专业序列 改变同步 专业序列职位
   */
  personDepPChange: function () {
    let _self = this;
    $("#personDepP").shrPromptBox("option", {
      onchange: function (e, value) {
        let id = value.current.id;
        _self.setPPoistionFilter(id);
      },
    });
  },
  setPPoistionFilter(id) {
    if (id === "") {
      filter = "number like 'Z%'";
    } else {
      filter = "number like 'Z%' AND adminOrgUnit.id = '" + id + "'";
    }
    $("#pPosition").shrPromptBox("setFilter", filter);
  },
  /**
   * 政治面貌 改变 入党时间 逻辑
   */
  politicalFaceChange: function () {
    var that = this;
    $("#politicalFace").shrPromptBox("option", {
      onchange: function (e, value) {
        $("#JionPtyDate").val(null);
        var info = value.current;
        if (null != info) {
          var faceName = info.name;
          that.setJoinPtyDateStatus(faceName);
        }
      },
    });
  },
  viewInit: function () {
    var _self = this;
    let hrOrgUnitName;
    // 初始化薪酬类别可见性
    if (_self.getOperateState().toUpperCase() === "VIEW") {
      hrOrgUnitName = $("#hrOrgUnit").text();
    } else {
      hrOrgUnitName = $("#hrOrgUnit").shrPromptBox("getValue").name;
    }
    _self.setSalaryStandardVisibility(hrOrgUnitName);

    if (_self.getOperateState().toUpperCase() !== "VIEW") {
      // 初始化专业序列相关可编辑性
      let pstnClass = $("#pstnClass").shrPromptBox("getValue");
      _self.setPositionInfoEditable(pstnClass.name);

      // 初始化入党时间是否可编辑
      _self.setPoliticalEditable(hrOrgUnitName);

      // 初始化应聘职位可选择范围
      var hrOrgUnit = $("#hrOrgUnit").shrPromptBox("getValue");
      var adminOrg = $("#adminOrg").shrPromptBox("getValue");
      var filter = _self.getPositionFilter(hrOrgUnit, adminOrg);
      _self.getField("position").shrPromptBox("setFilter", filter);

      // 设置pPositionFilter
      let id = $("#personDepP").shrPromptBox("getValue").id;
      _self.setPPoistionFilter(id);
    }
  },

  setSalaryStandardVisibility: function (hrOrgUnitName) {
    // 设置可见性
    $("#foreign").hide();
    $("#local").hide();
    if ("属地化国际化管理板块" === hrOrgUnitName) {
      $("#local").show();
    } else if (
      "国内人力板块" === hrOrgUnitName ||
      "中建阿尔及利亚" === hrOrgUnitName
    ) {
      $("#foreign").show();
    }
  },

  setPoliticalEditable: function (hrOrgUnitName) {
    let _self = this;
    $("#politicalFace").shrPromptBox("disable");
    $("#JionPtyDate").shrDateTimePicker("disable");
    if (hrOrgUnitName !== '属地化国际化管理板块') {
      $("#politicalFace").shrPromptBox("enable");
      let politicalFace = $("#politicalFace").shrPromptBox("getValue").name;
      _self.setJoinPtyDateStatus(politicalFace);
      if ("中共党员" === politicalFace || "" === politicalFace) {
        $("#JionPtyDate").shrDateTimePicker("enable");
      }
    }
  },

  setJoinPtyDateStatus: function (politicalFace) {
    if (
        null === politicalFace ||
        "中共党员" === politicalFace ||
        "" === politicalFace
    ) {
      $("#JionPtyDate").shrDateTimePicker("enable");
    } else {
      $("#JionPtyDate").shrDateTimePicker("disable");
    }
  },

  salaryCalcBind: function () {
    var that = this;
    $("#wageRank").change(function (event) {
      that.calcOverseaAllwnc();
    });
    $("#ovrseaAllwncRatio").change(function (event) {
      that.calcOverseaAllwnc();
    });
    $("#ovrseaAllwnc").change(function (event) {
      that.calcMnthBasicWage();
    });
    $("#adPstnWage").change(function (event) {
      that.calcMnthBasicWage();
    });
    $("#mnthBasicWage").change(function (event) {
      that.calcMonthWage();
      that.calcAnlWage();
    });
    $("#siteAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#EnAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#FrAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#specialAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#pAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#trvlMedComAllwnc").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#monthBnft").change(function (event) {
      that.calcMonthWage();
      that.calcAnlAllwnc();
    });
    $("#anlWage").change(function (event) {
      that.calcFloatAnlWage();
      that.calcBgAnlWage();
    });
    $("#floatPercent").change(function (event) {
      that.calcFloatAnlWage();
    });
    $("#floatAnlWage").change(function (event) {
      that.calcBgAnlWage();
    });
    $("#anlAllwnc").change(function (event) {
      that.calcAnlIncmBg();
    });
    $("#BgAnlWage").change(function (event) {
      that.calcAnlIncmBg();
    });
    $("#anlBnft").change(function (event) {
      that.calcAnlIncmBg();
    });
  },
  /**
   * 海外津贴计算
   */
  calcOverseaAllwnc: function () {
    var _self = this;
    var wageRank = _self.getNumberFieldValueOrDefault("#wageRank");
    var ovrseaAllwncRatio = _self.getNumberFieldValueOrDefault(
      "#ovrseaAllwncRatio"
    );
    var overseaAllwnc = math
      .chain(math.bignumber(wageRank))
      .multiply(math.bignumber(ovrseaAllwncRatio))
      .done();
    $("#ovrseaAllwnc").shrNumberField("setValue", overseaAllwnc);
  },
  /**
   * 月基薪计算
   */
  calcMnthBasicWage: function () {
    var _self = this;
    var wageRank = _self.getNumberFieldValueOrDefault("#wageRank");
    var overseaAllwnc = _self.getNumberFieldValueOrDefault("#ovrseaAllwnc");
    var adPstnWage = _self.getNumberFieldValueOrDefault("#adPstnWage");
    var mnthBasicWage = math
      .chain(math.bignumber(wageRank))
      .add(math.bignumber(overseaAllwnc))
      .add(math.bignumber(adPstnWage))
      .done();
    $("#mnthBasicWage").shrNumberField("setValue", mnthBasicWage);
  },
  /**
   * 月工资计算
   */
  calcMonthWage: function () {
    var _self = this;
    var mnthBasicWage = _self.getNumberFieldValueOrDefault("#mnthBasicWage");
    var siteAllwnc = _self.getNumberFieldValueOrDefault("#siteAllwnc");
    var EnAllwnc = _self.getNumberFieldValueOrDefault("#EnAllwnc");
    var FrAllwnc = _self.getNumberFieldValueOrDefault("#FrAllwnc");
    var specialAllwnc = _self.getNumberFieldValueOrDefault("#specialAllwnc");
    var pAllwnc = _self.getNumberFieldValueOrDefault("#pAllwnc");
    var trvlMedComAllwnc = _self.getNumberFieldValueOrDefault(
      "#trvlMedComAllwnc"
    );
    var monthBnft = _self.getNumberFieldValueOrDefault("#monthBnft");
    var monthWage = math
      .chain(math.bignumber(mnthBasicWage))
      .add(math.bignumber(siteAllwnc))
      .add(math.bignumber(EnAllwnc))
      .add(math.bignumber(FrAllwnc))
      .add(math.bignumber(specialAllwnc))
      .add(math.bignumber(pAllwnc))
      .add(math.bignumber(trvlMedComAllwnc))
      .add(math.bignumber(monthBnft))
      .done();
    $("#monthWage").shrNumberField("setValue", monthWage);
  },
  /**
   * 基本年薪计算
   */
  calcAnlWage: function () {
    var _self = this;
    var mnthBasicWage = _self.getNumberFieldValueOrDefault("#mnthBasicWage");
    var anlWage = math
      .chain(math.bignumber(mnthBasicWage))
      .multiply(math.bignumber(12.0))
      .done();
    $("#anlWage").shrNumberField("setValue", anlWage);
  },
  /**
   * 浮动年薪计算
   */
  calcFloatAnlWage: function () {
    var _self = this;
    var anlWage = _self.getNumberFieldValueOrDefault("#anlWage");
    var floatPercent = _self.getNumberFieldValueOrDefault("#floatPercent");
    var floatAnlWage = 0;
    if (floatPercent < 1) {
      var var1 = math
        .chain(math.bignumber(1.0))
        .subtract(math.bignumber(floatPercent))
        .done();
      var var2 = math
        .chain(math.bignumber(anlWage))
        .multiply(math.bignumber(floatPercent))
        .done();
      var accurateWage1000Multiply = math
        .chain(math.bignumber(var2))
        .divide(math.bignumber(var1))
        .multiply(math.bignumber(1000.0))
        .done();
      var ceiled1000MultiplyValue = Math.ceil(accurateWage1000Multiply);
      floatAnlWage = math
        .chain(math.bignumber(ceiled1000MultiplyValue))
        .divide(math.bignumber(1000.0));
    }
    $("#floatAnlWage").shrNumberField("setValue", floatAnlWage);
  },
  /**
   * 年薪预算计算
   */
  calcBgAnlWage: function () {
    var _self = this;
    var anlWage = _self.getNumberFieldValueOrDefault("#anlWage");
    var floatAnlWage = _self.getNumberFieldValueOrDefault("#floatAnlWage");
    var BgAnlWage = math
      .chain(math.bignumber(anlWage))
      .add(math.bignumber(floatAnlWage))
      .done();
    $("#BgAnlWage").shrNumberField("setValue", BgAnlWage);
  },
  /**
   * 年津补贴计算
   */
  calcAnlAllwnc: function () {
    var siteAllwnc = _self.getNumberFieldValueOrDefault("#siteAllwnc");
    var EnAllwnc = _self.getNumberFieldValueOrDefault("#EnAllwnc");
    var FrAllwnc = _self.getNumberFieldValueOrDefault("#FrAllwnc");
    var specialAllwnc = _self.getNumberFieldValueOrDefault("#specialAllwnc");
    var pAllwnc = _self.getNumberFieldValueOrDefault("#pAllwnc");
    var trvlMedComAllwnc = _self.getNumberFieldValueOrDefault(
      "#trvlMedComAllwnc"
    );
    var monthBnft = _self.getNumberFieldValueOrDefault("#monthBnft");
    var baseVar = math
      .chain(math.bignumber(siteAllwnc))
      .add(math.bignumber(EnAllwnc))
      .add(math.bignumber(FrAllwnc))
      .add(math.bignumber(specialAllwnc))
      .add(math.bignumber(pAllwnc))
      .add(math.bignumber(trvlMedComAllwnc))
      .add(math.bignumber(monthBnft))
      .done();
    var anlAllwnc = math.chain(12).multiply(baseVar).done();
    $("#anlAllwnc").shrNumberField("setValue", anlAllwnc);
  },
  /**
   * 年收入预算计算
   */
  calcAnlIncmBg: function () {
    var BgAnlWage = _self.getNumberFieldValueOrDefault("#BgAnlWage");
    var anlBnft = _self.getNumberFieldValueOrDefault("#anlBnft");
    var anlAllwnc = _self.getNumberFieldValueOrDefault("#anlAllwnc");
    var AnlIncmBg = math
      .chain(math.bignumber(BgAnlWage))
      .add(math.bignumber(anlBnft))
      .add(math.bignumber(anlAllwnc))
      .done();
    $("#AnlIncmBg").shrNumberField("setValue", AnlIncmBg);
  },
  getNumberFieldValueOrDefault: function (jNavigator) {
    var _self = this;
    var number = $(jNavigator).shrNumberField("getValue");
    return _self.isValidNumber(number) ? number : 0;
  },
  isValidNumber: function (number) {
    return number && number != "";
  }
});

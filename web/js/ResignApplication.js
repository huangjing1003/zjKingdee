/**
 * 回国（离职）申请表form
 *
 * @name: csce.shr.ResignApplication
 * @model: com.kingdee.shr.custom.app.RsgnApplc
 * @uipk: com.kingdee.shr.custom.app.RsgnApplc.form
 * @author: xy
 * @date: 2021/1/20 21:51
 */
shr.defineClass(
  "csce.shr.ResignApplication",
  shr.custom.bizbill.DepCustomBillEdit,
  {
    initalizeDOM: function () {
      csce.shr.ResignApplication.superClass.initalizeDOM.call(this);
      const _self = this;
      const formType = _self.getOperateState().toUpperCase();
      if ("VIEW" !== formType) {
        _self.entrysBuyTicketTypeEvent();
        _self.entrysPersonEvent();
        _self.leaveDateEvent();
        _self.entry2Event();
        _self.entrysMnthWageEvent();
        _self.entrysTotalLevDaysEvent();
        _self.entrysTakenLevDaysEvent();
        _self.hrOrgUnitChangeEvent();
        _self.entry1Event();
      }
    },

    validate: function () {
      let flag = shr.custom.bizbill.DepCustomBillEdit.superClass.validate.call(
        this
      );

      // 检查 结算项目是正常出勤工资或补休假工资时，请填写计算开始日期和计算结束日期
      if (flag) {
        let grid = waf("#entry2");
        let gridList = grid.jqGrid("getAllRowData");
        for (let i = 0; i < gridList.length; i++) {
          let rowInfo = gridList[i];
          let settleItem = rowInfo.settleItem;
          if (
            null !== settleItem &&
            ("正常出勤工资" === settleItem.name || "补休假工资" === settleItem.name) ) {
            let startDate = rowInfo.startDate;
            let endDate = rowInfo.endDate;
            if (
              null === startDate ||
              null === endDate ||
              "" === startDate ||
              "" === endDate
            ) {
              flag = false;
            }
          }
        }
        if (!flag) {
          shr.showWarning({
            message:
              "结算项目是正常出勤工资或补休假工资时，请填写计算开始日期和计算结束日期",
            hideAfter: 5,
          });
        }
      }
      return flag;
    },
    hrOrgUnitChangeEvent: function() {
      const _self = this;
      _self.getField("hrOrgUnit").shrPromptBox("option", {
        onchange: function (e, value) {
          const currentValue = value.current;
          if (currentValue === null) {
            return;
          }
          const filter = " HROrgUnit.id = '" + currentValue.id + "'";
          _self.getField("entrys.person").shrPromptBox("setFilter",filter);
        }
      });
    },
    /*
     * 回国机票购买方式字段属性
     */
    entrysBuyTicketTypeEvent: function () {
      const _self = this;
      let entrysBuyTicketTpyeF7Value = $("#entrys_buyTicketTpye").shrPromptBox(
        "getValue"
      );
      _self.flightInfoVisibility(entrysBuyTicketTpyeF7Value.name);
      _self.getField("entrys_buyTicketTpye").shrPromptBox("option", {
        onchange: function (e, value) {
          let currentEntrysBuyTicketTpyeF7Value = value.current;
          if (null != currentEntrysBuyTicketTpyeF7Value) {
            _self.flightInfoVisibility(
              currentEntrysBuyTicketTpyeF7Value.name
            );
          } else {
            _self.flightInfoVisibility("");
          }
        },
      });
    },
    /*
     * 机票信息多行表可见性
     */
    flightInfoVisibility: function (entrysBuyTicketTpyeValue) {
      let _self = this;
      let fightInfoHeaderDiv = $("#flight_header_div");
      let fightInfoDetailDiv = $("#entry_cont");
      fightInfoHeaderDiv.hide();
      fightInfoDetailDiv.hide();
      if ("公司购买" === entrysBuyTicketTpyeValue) {
        fightInfoHeaderDiv.show();
        fightInfoDetailDiv.show();
      }
    },

    /*
     * 个人详细信息回填
     */
    entrysPersonEvent: function () {
      const _self = this;
      _self.getField("entrys_person").shrPromptBox("option", {
        onchange: function (e, value) {
          let leaveDate = _self.getField("entrys_finalResignDate").shrDateTimePicker("getValue");
          if (leaveDate === "") {
            const today = new Date()
            leaveDate = today.getFullYear()+"-"+(today.getMonth()+1)+"-"+today.getDate()
          }
          let entrysPersonF7Value = value.current;
          if (null != entrysPersonF7Value) {
            let personId = entrysPersonF7Value.id;
            // 设置不用发后端请求的基础信息
            let personNumber = entrysPersonF7Value.number;
            $("#entrys_person_number").val(personNumber);
            let adminOrgId = entrysPersonF7Value["adminOrgUnit.id"];
            let adminUnitName = entrysPersonF7Value["adminOrgUnit.name"];
            let positionName = entrysPersonF7Value["position.name"];
            let positionId = entrysPersonF7Value["position.id"];
            let typeId = entrysPersonF7Value["FranceBnsPsnType.id"];
            let typeName = entrysPersonF7Value["FranceBnsPsnType.name"];
            //组织
            $("#entrys_adminOrg").shrPromptBox("setValue", {
              id: adminOrgId,
              name: adminUnitName,
            });
            //职位
            $("#entrys_position").shrPromptBox("setValue", {
              id: positionId,
              name: positionName,
            });
            //类别
            $("#entrys_FranceBnsPsnType").shrPromptBox("setValue", {
              id: typeId,
              name: typeName,
            });
            _self.fillingPersonInfo(personId, leaveDate)
          }
        },
      });
    },
    leaveDateEvent: function () {
      const _self = this;
      $("#entrys_finalResignDate").change(function() {
      // _self.getField("entrys_finalResignDate").shrDateTimePicker("option", {
      //   onchange: function (e, value) {
        const personId = _self.getField("entrys_person").shrPromptBox("getValue").id
        let leaveDate = _self.getField("entrys_finalResignDate").shrDateTimePicker("getValue")
        if (leaveDate === "") {
          const today = new Date()
          leaveDate = today.getFullYear()+"-"+(today.getMonth()+1)+"-"+today.getDate()
        }
        if (personId !== '') {
          _self.fillingPersonInfo(personId, leaveDate)
        }
      });
    },
    fillingPersonInfo: function(personId, leaveDate) {
      _self.remoteCall({
        type: "post",
        method: "getPersonFillingInfo",
        param: {
          personId: personId,
          leaveDate: leaveDate
        },
        async: false,
        success: function (result) {
          if (result.person) {
            let person = result.person;
            // 护照姓名拼音
            _self.getField("entrys_fullNamePingYin").shrTextField("setValue", person.fullNamePingYin);
            // 邮箱
            _self.getField("entrys_email").shrTextField("setValue", person.email);
            // 阿国手机号码
            _self.getField("entrys_officePhone").shrTextField("setValue", person.nCell);
            // 家庭地址
            _self.getField("entrys_address").shrTextField("setValue", person.addressTx);
          }
          // 成本部门
          _self.getField("entrys_costCntr").shrPromptBox("setValue", result.costCenter);
          // 专业序列
          _self.getField("entrys_professionalDep").shrPromptBox("setValue", result.professionalDep);
          // 入职日期
          if (result.takeOfficeInfo) {
            $("#entrys_enterDate").shrDateTimePicker(
                "setValue",
                result.takeOfficeInfo.joinGroupDateCur
            );
          } else {
            $("#entrys_enterDate").shrDateTimePicker("setValue", null);
          }
          // 最近一次来阿日期
          $("#entrys_lastEnterAlDate").shrDateTimePicker("setValue", result.lastEnterAlDate);
          // 结算信息 todo
          // 管理岗薪等级
          if (result.positionRank) {
            _self
                .getField("entrys_mngJobGrade")
                .shrPromptBox("setValue", result.positionRank.mngJobGrade);
            _self
                .getField("entrys_grpJobGrade")
                .shrPromptBox("setValue", result.positionRank.grpJobgrade);
            _self
                .getField("entrys_jobGrade")
                .shrPromptBox("setValue", result.positionRank.jobGrade);
          } else {
            _self.getField("entrys_mngJobGrade").shrPromptBox("setValue", null);
            _self.getField("entrys_grpJobGrade").shrPromptBox("setValue", null);
            _self.getField("entrys_jobGrade").shrPromptBox("setValue", null);
          }
          // 月工资
          if (result.salaryInfo) {
            _self.getField("entrys_mnthWage").shrNumberField("setValue", result.salaryInfo.monthlySalary);
            _self.getField("entrys_rankBaseComp").shrNumberField("setValue", result.salaryInfo.baseRankSalary);
          } else {
            _self.getField("entrys_mnthWage").shrNumberField("setValue", null);
            _self.getField("entrys_rankBaseComp").shrNumberField("setValue", null);
          }
          // 假期信息
          const leaveBillInfo = result.leaveBillInfo;
          _self.getField("entrys_totalLevDays").shrNumberField("setValue", result.realLimit)
          _self.updateEntry1(leaveBillInfo)
          _self.getField("entrys_takenLevDays").shrNumberField("setValue", result.usedLimit)
          _self.getField("entrys_restLevDays").shrNumberField("setValue", result.remainLimit)
          // 银行信息
          if (result.bankAccountInfo) {
            _self.getField("entrys_receiverFullname").shrTextField("setValue", result.bankAccountInfo.rcvrNmPingyin);
            _self.getField("entrys_bankCode").shrTextField("setValue", result.bankAccountInfo.bankCode);
            _self.getField("entrys_AccoutCode").shrTextField("setValue", result.bankAccountInfo.accountNum);
            _self.getField("entrys_address").shrTextField("setValue", result.bankAccountInfo.rcvrAdd);
          } else {
            _self.getField("entrys_receiverFullname").shrTextField("setValue", null);
            _self.getField("entrys_bankCode").shrTextField("setValue", null);
            _self.getField("entrys_AccoutCode").shrTextField("setValue", null);
            _self.getField("entrys_address").shrTextField("setValue", null);
          }
          // 银行信息
          if (result.personContactMethod) {
            _self.getField("entrys_cnPhone").shrTextField("setValue", result.personContactMethod.officePhone);
            _self.getField("entrys_psnEmail").shrTextField("setValue", result.personContactMethod.personalEmail);
            if (result.personContactMethod.nCell) {
              let mobile = ''
              if (result.personContactMethod.globalRoaming) {
                mobile = result.personContactMethod.globalRoaming + '-'
              }
              mobile = mobile + result.personContactMethod.nCell
              _self.getField("entrys_mobile").shrTextField("setValue", mobile);
            } else {
              _self.getField("entrys_mobile").shrTextField("setValue", null);
            }
          } else {
            _self.getField("entrys_cnPhone").shrTextField("setValue", null);
            _self.getField("entrys_psnEmail").shrTextField("setValue", null);
          }
          // 会签信息
          if (result.contract) {
            _self.getField("entrys_labContractFirstParty").shrPromptBox("setValue", result.contract.labContractFirstParty);
            _self.getField("entrys_conStartDate").shrDateTimePicker("setValue", result.contract.effectDate);
            _self.getField("entrys_conEndDate").shrDateTimePicker("setValue", result.contract.endDate);
          } else {
            _self.getField("entrys_labContractFirstParty").shrPromptBox("setValue", null);
            _self.getField("entrys_conStartDate").shrDateTimePicker("setValue", null);
            _self.getField("entrys_conEndDate").shrDateTimePicker("setValue", null);
          }
          _self.getField("entrys_trnFee").shrNumberField("setValue", result.trnFee);
          _self.getField("entrys_trnFeeExpDate").shrDateTimePicker("setValue", result.trnFeeExpDate);
          _self.getField("entrys_talentIntrFee").shrNumberField("setValue", result.talentIntrFee);
          _self.getField("entrys_IntrFeeExpDate").shrDateTimePicker("setValue", result.IntrFeeExpDate);
        },
      });
    },
    updateEntry1: function(leaveBillInfo) {
      const _self = this;
      const leaveGrid = waf("#entry1");
      const gridList = leaveGrid.jqGrid("clearGridData");
      for (let i = 0; i < gridList.length; i++) {
        const row = gridList[i];
        leaveGrid.jqGrid("delRow", row.id)
      }
      if (leaveBillInfo) {
        for (let i = 0; i < leaveBillInfo.length; i++) {
          let rowInfo = {};
          rowInfo.days = leaveBillInfo[i].leaveLength;
          rowInfo.HolidayType = leaveBillInfo[i].policy;
          rowInfo.startDate = leaveBillInfo[i].beginTime;
          rowInfo.endDate = leaveBillInfo[i].endTime;
          leaveGrid.jqGrid("addRowData", i, rowInfo, "last");
        }
      }
      _self.updateEntrysTakenLevDays()
    },
    /*
     * 应休假天数修改事件
     */
    entrysTotalLevDaysEvent: function () {
      const _self = this;
      $("#entrys_totalLevDays").change(function (event) {
        _self.updateEntrysRestLevDays();
      });
    },
    /*
     * 实休年休假天数修改事件
     */
    entrysTakenLevDaysEvent: function () {
      const _self = this;
      $("#entrys_takenLevDays").change(function (event) {
        _self.updateEntrysRestLevDays();
      });
    },
    /*
     * 剩余休假天数 = 应休假天数 -  实休年休假天数
     */
    updateEntrysRestLevDays: function () {
      let totalLeveDays = $("#entrys_totalLevDays").shrNumberField("getValue");
      let takenLevDays = $("#entrys_takenLevDays").shrNumberField("getValue");
      totalLeveDays = null === totalLeveDays ? 0 : parseInt(totalLeveDays);
      takenLevDays = null === takenLevDays ? 0 : parseInt(takenLevDays);
      let restLevDays = math
        .chain(math.bignumber(totalLeveDays))
        .subtract(math.bignumber(takenLevDays))
        .done();
      $("#entrys_restLevDays").shrNumberField(
        "setValue",
        math.number(restLevDays)
      );
    },
    /*
     * 假期多行表修改事件
     */
    entry1Event: function () {
      const _self = this;
      const grid = waf("#entry1");
      grid.wafGrid("option", {
        afterSaveCell: function (
          afterSrowid,
          cellname,
          value,
          iRow,
          iColaveCell
        ) {
          _self.updateEntrysTakenLevDays();
        },
      });
    },
    /*
     * 计算实际休假天数
     */
    updateEntrysTakenLevDays: function () {
      let _self = this;
      let taken = 0;
      let grid = waf("#entry1");
      let gridList = grid.jqGrid("getAllRowData");
      for (let i = 0; i < gridList.length; i++) {
        let row = gridList[i];
        let dayGap = null === row.days ? 0 : row.days;
        taken = taken + dayGap;
      }
      _self.getField("entrys_takenLevDays").shrNumberField("setValue", taken);
      const total = _self.getField("entrys_totalLevDays").shrNumberField("getValue");
      let remain = total - taken
      _self.getField("entrys_restLevDays").shrNumberField("setValue", remain);
    },
    /*
     * 工资结算信息多行表修改事件
     */
    entry2Event: function () {
      const _self = this;
      const grid = waf("#entry2");
      grid.wafGrid("option", {
        beforeSaveCell: function (
          afterSrowid,
          cellname,
          value,
          iRow,
          iColaveCell
        ) {
          if ("settleItem" === cellname) {
            if (null !== value && value.isAddItem) {
              const isAddItem = value.isAddItem
              grid.jqGrid("setCell", afterSrowid, "isAddItem", isAddItem);
            } else {
              grid.jqGrid("setCell", afterSrowid, "isAddItem", false);
            }
          }
        },
        afterSaveCell: function (
          afterSrowid,
          cellname,
          value,
          iRow,
          iColaveCell
        ) {
          if (
            "settleItem" === cellname ||
            "startDate" === cellname ||
            "endDate" === cellname
          ) {
            // 重新计算行金额和总金额
            _self.updateRowSalaryAmount(afterSrowid);
            _self.updateEntrysTotalStlAmt();
          } else if ("amt" === cellname) {
            _self.updateEntrysTotalStlAmt();
          }
        },
      });
    },
    /*
     * 月工资修改事件
     */
    entrysMnthWageEvent: function () {
      const _self = this;
      $("#entrys_mnthWage").change(function (event) {
        _self.updateEntry2Amt();
        _self.updateEntrysTotalStlAmt();
      });
    },
    /*
     * 自动计算entry2对应grid的amt
     */
    updateEntry2Amt: function () {
      let _self = this;
      let grid = waf("#entry2");
      let gridList = grid.jqGrid("getAllRowData");
      for (let i = 0; i < gridList.length; i++) {
        let row = gridList[i];
        let settleItem = row.settleItem;
        if (
          settleItem &&
          ("正常出勤工资" === settleItem.name ||
            "补休假工资" === settleItem.name)
        ) {
          _self.updateRowSalaryAmount(i + 1);
        }
      }
    },
    /*
     * 计算工资结算信息多行表单行的金额
     */
    updateRowSalaryAmount: function (rowId) {
      const _self = this
      const grid = waf("#entry2");
      const rowInfo = grid.jqGrid("getRowData", rowId);
      //月工资
      const monthSalary =
          _self.getField("entrys_mnthWage").shrNumberField("getValue") === null ? 0 : _self.getField("entrys_mnthWage").shrNumberField("getValue");
      //最终回国（离职）日期
      const finalResignDate = $("#entrys_finalResignDate").val();
      //入职日期
      const enterDate = $("#entrys_enterDate").val();
      // 培训费
      const trnFee =
    	  _self.getField("entrys_trnFee").shrNumberField("getValue") === null ? 0 : _self.getField("entrys_trnFee").shrNumberField("getValue");
      // 人才引进费
      const  talentIntrFee =
    	  _self.getField("entrys_talentIntrFee").shrNumberField("getValue") === null ? 0 : _self.getField("entrys_talentIntrFee").shrNumberField("getValue");
      //培训费期限
      const trnFeeExpDate = $("#entrys_trnFeeExpDate").val();
      //人才引进费期限
      const  intrFeeExpDate = $("#entrys_IntrFeeExpDate").val();
      //人员id
      const personId = $("#entrys_person_el").val();
      //年休假剩余天数
      const restLevDays =  _self.getField("entrys_restLevDays").shrNumberField("getValue") === null ? 0 : _self.getField("entrys_restLevDays").shrNumberField("getValue");
      
      const param = {
        type: rowInfo.settleItem.name,
        beginDate: rowInfo.startDate,
        endDate: rowInfo.endDate,
        unitAmount: monthSalary,
        finalResignDate:finalResignDate,
        restLevDays:restLevDays,
        personId:personId,
        trnFee:trnFee,
        talentIntrFee:talentIntrFee,
        trnFeeExpDate:trnFeeExpDate,
        intrFeeExpDate:intrFeeExpDate,
        enterDate:enterDate
      }
      _self.remoteCall({
        type: "post",
        method: "getRowAmount",
        param: param,
        async: false,
        success: function (result) {
          const amount = result.amount;
          grid.jqGrid("setCell", rowId, "amt", amount);
        },
      });
    },
    /*
     * 计算工资结算总额
     */
    updateEntrysTotalStlAmt: function () {
      let _self = this;
      let result = 0.0;
      let grid = waf("#entry2");
      let gridList = grid.jqGrid("getAllRowData");
      for (let i = 0; i < gridList.length; i++) {
        let row = gridList[i];
        let amt = row.amt;
        let isAddItem = row.isAddItem;
        if ("0" === isAddItem) {
          amt = math
            .chain(math.bignumber(0.0))
            .subtract(math.bignumber(amt))
            .done();
        }
        result = math
          .chain(math.bignumber(amt))
          .add(math.bignumber(result))
          .done();
      }
      _self.getField("entrys_totalStlAmt").shrNumberField("setValue", result);
    }
  }
);

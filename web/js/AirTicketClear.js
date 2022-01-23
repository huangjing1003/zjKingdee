/**
 * 名称:机票清算_表单
 * 模型：com.kingdee.eas.hr.affair.app.TcktClearout
 * uipk：com.kingdee.eas.hr.affair.app.TcktClearout.form
 **
*/
shr.defineClass("csce.shr.AirTicketClear", shr.custom.bizbill.DepCustomBillEdit, {
	$editGrid : null,
	initalizeDOM : function () {
		csce.shr.AirTicketClear.superClass.initalizeDOM.call(this);
		const _self = this;
		if(_self.getOperateState() !== 'VIEW') {
			_self.initAddPersonBtn();
			_self.entrysEvent();
		}
	},
	addRowAction : function () {
		const _self = this;
		const hrOrgUnitId = shr.getFieldValue("hrOrgUnit");
		if(!hrOrgUnitId){
			shr.showError({
				message:"请先选择假期业务组织!!",
				hideAfter:5
			});
			return;
		}
		_self.doInitAddPersonBtn();
		$("#addRow_entrys").shrPromptGrid("open");
	},
	initAddPersonBtn: function () {
		const _self = this;
		$("#addRow_entrys").html("添加员工");
		if(!$("#addRow_entrys").data('f7init')){
			_self.doInitAddPersonBtn();
		}
	},
	doInitAddPersonBtn: function () {
		const _self = this;
		let hrOrgId = $("#hrOrgUnit_el").val();
		let filter = " hrOrgUnit.id = '"+hrOrgId+"'";
		$("#addRow_entrys").shrPromptGrid({
			title: "添加员工",
			uipk: "com.kingdee.eas.hr.ats.app.ExistHolidayFileHisForAdmin.F7.Lmt",
			filter: filter,
			multiselect: true,
			permItemId: _self.currentPagePermItemId,
			afterCommitClick: function(event, f7result) {
				$.each(f7result.datas, function(n, obj){
					_self.fillRowDataByPersonId(obj.id);
				});
			}
		});
		$("#addRow_entrys").data('f7init',true);
	},
	fillRowDataByPersonId: function(personId) {
		const _self = this;
		let entryDatas = waf("#entrys").jqGrid('getRowData');
		if(!entryDatas.length) {
			_self.doBeforeFillRowData(personId);
		} else {
			let exist = false;
			$.each(entryDatas, function(i, value){
				if(value.person.id == personId){
					exist = true;
				}
			});
			if(!exist){
				_self.doBeforeFillRowData(personId);
			}
		}
	},
	doBeforeFillRowData : function(personId){
		const _self = this;
		_self.remoteCall({
			type:"post",
			method:"getEntryRowInfo",
			param:{
				personId: personId
			},
			async: false,
			success : function (data) {
				if (data) {
					_self.setInfos(data);
				}
			}
		});
	},
	/**
	 * 设置其他信息
	 */
	setInfos: function(rowData) {
		const _self = this;
		let initialRow = _self.findNextId();
		const editGrid = waf("#entrys");
		editGrid.jqGrid("addRowData", initialRow, rowData, "last");

	},
	/**
	 * 生成新的rowId
	 */
	findNextId : function() {
		let maxId = 0;
		const editGrid = waf("#entrys");
		editGrid.find("tr").each(function () {
			let id = $(this).attr("id");
			if(/^[0-9]*$/.test(id)){
				try {
					id = parseInt(id);
					if (id > maxId) {
						maxId = id;
					}
				} catch (e) {
				}
			}
		});
		maxId = parseInt(maxId) + 1;
		return maxId;
	},
	/*
     * 多行表修改事件
     */
	entrysEvent: function () {
		let _self = this;
		let grid = waf("#entrys");
		grid.wafGrid("option", {
			afterSaveCell: function (
				afterSrowid,
				cellname,
				value,
				iRow,
				iColaveCell
			) {
				if ("person" === cellname) {
					const personId = value.id;
					if (id === '') {
						_self.clearRowInfo(afterSrowid);
					}
					_self.fillRow(afterSrowid, personId);
				}
			},
		});
	},
	fillRow: function(rowId, personId) {
		const _self = this;
		_self.remoteCall({
			type:"post",
			method:"getEntryRowInfo",
			param:{
				personId: personId
			},
			async: false,
			success: function(res){
				if(res != null){
					$("#entrys").jqGrid("setRowData", rowId, res);
				}
			}
		});
	},
	clearRowInfo: function(rowId) {
		const grid = waf("#entrys");
		const rowInfo = {
			"person": {
				"id": "",
				"name": ""
			},
			"adminOrg": {
				"id": "",
				"name": ""
			},
			"startDate": "",
			"endDate": "",
			"psnAnnlLimit": "",
			"annlTcktAmt": "",
			"annlClrtAmt": ""
		};
		grid.jqGrid('setRowData', rowId, rowInfo);
	}
});
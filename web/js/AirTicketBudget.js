/**
 * 名称:机票款额度_表单
 * 模型：com.kingdee.eas.hr.affair.app.AirticketBnsLmt
 * uipk：com.kingdee.eas.hr.affair.app.AirticketBnsLmt.form
 **
*/
// shr.framework.Edit  shr.custom.bizbill.DepCustomBillEdit
shr.defineClass("csce.shr.AirTicketBudget", shr.custom.bizbill.DepCustomBillEdit, {
	$editGrid : null,
	initalizeDOM : function () {
		csce.shr.AirTicketBudget.superClass.initalizeDOM.call(this);
		var _self = this;
		if(_self.getOperateState() != 'VIEW'){
			$("#addRow_entrys").html("添加员工");
			if(!$("#addRow_entrys").data('f7init')){
				_self.initAddPersonBtn();
			}
			_self.monitorEntry();
		}
	},
	/**
	 * 添加员工事件
	 */
	addRowAction : function(){
		var self = this;
		var hrOrgUnitId = shr.getFieldValue("hrOrgUnit");
		var annualSrdLmt = $("#annualSrdLmt").val();
		/**
		 * 校验假期业务组织是否已经填写
		 */
		if(!hrOrgUnitId){
			shr.showError({
				message:"请先选择假期业务组织!!",
				hideAfter:5
			});
			return;
		}
		/**
		 * 校验年标准额度是否已经填写
		 */
		if(annualSrdLmt=="" || annualSrdLmt=="0"){
			shr.showError({
				message:"请先填写年标准额度且不能为0!!",
				hideAfter:5
			});
			return;
		}
//		if(!$("#addRow_entrys").data('f7init')){
			self.initAddPersonBtn();
//		}
		$("#addRow_entrys").shrPromptGrid("open");
	},
	/**
	 * 初始化添加员工按钮
	 */
	initAddPersonBtn:function(){
		var self = this;
		var hrOrgId = $("#hrOrgUnit_el").val();
		var filter = " hrOrgUnit.id = '"+hrOrgId+"'";
		$("#addRow_entrys").shrPromptGrid({
			title: "添加员工", 
			uipk: "com.kingdee.eas.hr.ats.app.ExistHolidayFileHisForAdmin.F7.Lmt",
			filter: filter,
			multiselect: true,
			permItemId:self.currentPagePermItemId,
			afterCommitClick: function(event, f7result) {
				$.each(f7result.datas, function(n, obj){
					self.fillRowDataByPersonId(obj.id);
				});
			}
		});
		$("#addRow_entrys").data('f7init',true);
	},
	/**
	 * 分录添加数据
	 */
	fillRowDataByPersonId:function(personId){
		var that = this;
		var entryDatas = that.getEntrysGrid().jqGrid('getRowData');
		if(!entryDatas.length){
			that.doBeforeFillRowData(personId);
		}else{
			var exist = false;
			$.each(entryDatas, function(i, value){
				if(value.person.id == personId){
					exist = true;
				}
			});
			if(!exist){
				that.doBeforeFillRowData(personId);
			}
		}
	},
	/**
	 * 获取分录的表格gird
	 */
	getEntrysGrid : function(){
		return $("#entrys");
	},
	doBeforeFillRowData : function(personId){
		var that = this;
		var annualSrdLmt = $("#annualSrdLmt").val();
		_self.remoteCall({
			type:"post",
			method:"getEntryRowInfo",
			param:{
				personId: personId,
				annualSrdLmt:annualSrdLmt
			},
			async: false,
			success : function (data) {
				if (data) {
					that.doFillRowData(data);
				}
			}
		});

	},
	/**
	 * 分录实际添加数据方法
	 */
	doFillRowData : function(data){ 
		var self = this, personId = data.person.id;
		if(self.getOperateState() != 'VIEW'){
			self.fillBillEntryData({}, data);
		}
	},
	/**
	 * 填充分录数据的方法
	 */
	fillBillEntryData : function(data, rowdata){
		var self = this;
		var rowdata = self.assembleCustomRowData(rowdata);
		self.setInfos(rowdata);
	},
	/**
	 * 供子类覆写，将data或defaultValue中的数据添加到rowdata
	 */
	assembleCustomRowData : function(rowdata){
		return rowdata;
	},
	/**
	 * 设置其他信息
	 */
	setInfos:function(rowdata){
		var self = this;
		var initialRow = self.findNextId();
		$editGrid = self.getField("entrys");
		var row = $editGrid.jqGrid("addRowData", initialRow, rowdata, "last");
		
	},
	/**
	 * 生成新的rowId
	 */
	findNextId : function() {
        var self = this;
		var maxId = 0;
		$editGrid = self.getField("entrys");
        $editGrid.find("tr").each(function () {
            var id = $(this).attr("id");
            if(/^[0-9]*$/.test(id)){//如果ids[i]是纯数字则认为是未保存时生成的临时行序号，进行比较，不加这个判断数字开头的id也能成功parseInt()
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
	monitorEntry:function(){
		var _self = this;
		$("#entrys").jqGrid("option", {
			  afterEditCell:function (rowid, cellname, value, iRow, iCol) {	
			  } 
			  ,afterSaveCell:function(rowid, cellname, value, iRow, iCol) {
			  	  _self.afterSaveCellTrigger(rowid, cellname, value, iRow, iCol);
			  }
		});
	},
    /**
     * 计算额度数据
     */
	afterSaveCellTrigger : function(rowid, cellname, value, iRow, iCol)
	{
		var _self = this;
		var tripBillGrid=$("#entrys");
		if(cellname=="startDate" || cellname=="endDate" ){
			const annualSrdLmt = $("#annualSrdLmt").val();
			let startDateStr = $("#entrys").jqGrid("getCell", rowid, "startDate").substr(0, 10);
			let endDateStr = $("#entrys").jqGrid("getCell", rowid, "endDate").substr(0, 10);
			_self.remoteCall({
				type:"post",
				method:"calcLimit",
				param:{
					annualSrdLmt:annualSrdLmt,
					startDateStr:startDateStr,
					endDateStr:endDateStr
				},
				async: false,
				success:function(res){
					if(res!=null){
						//给额度赋值
						$("#entrys").jqGrid("setCell", rowid,"psnAnnlLimit",res);
					}
				}
			});
		}
	}
});
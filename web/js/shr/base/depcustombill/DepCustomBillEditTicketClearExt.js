
/**
 * 机票清算_表单
 * uipk:com.kingdee.eas.hr.affair.app.TcktClearout.form
 */
shr.defineClass("shr.custom.bizbill.DepCustomBillEditTicketClearExt", shr.custom.bizbill.DepCustomBillEdit, {
	initalizeDOM : function () {
		shr.custom.bizbill.DepCustomBillEditTicketClearExt.superClass.initalizeDOM.call(this);
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
	 * 添加员工事件,先校验是否填写了假期业务组织
	 */
	addRowAction : function(){
		var self = this;
		var hrOrgUnitId = shr.getFieldValue("hrOrgUnit");
		var mnthSrdLmt = $("#mnthSrdLmt").val();
		//机票清算日期
		var clrtDate = $("#clrtDate").val();

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
		 * 校机票清算日期是否已经填写
		 */
		if(!clrtDate){
			shr.showError({
				message:"请先选择票清算日期!!",
				hideAfter:5
			});
			return;
		}
		
		/**
		 * 校验月标准额度是否已经填写
		 */
		if(mnthSrdLmt=="" || mnthSrdLmt=="0" ||mnthSrdLmt=="0.00"){
			shr.showError({
				message:"请先填写月标准额度且不能为0!!",
				hideAfter:5
			});
			return;
		}
		//初始化添加员工按钮
		self.initAddPersonBtn();
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
			var annualSrdLmt = $("#mnthSrdLmt").val();
			var startDateStr = $("#entrys").jqGrid("getCell", rowid,"startDate");
			var endDateStr = $("#entrys").jqGrid("getCell", rowid,"endDate");
			var person = $("#entrys").jqGrid("getCell", rowid,"person");
			_self.remoteCall({
				type:"post",
				method:"calcLimit",
				param:{
					annualSrdLmt:annualSrdLmt,
					startDateStr:startDateStr,
					endDateStr:endDateStr,
					personId:person.id
				},
				async: false,
				success:function(res){
					if(res!=null){
						var psnAnnlLimit = res.psnAnnlLimit;
						var annlTcktAmt = res.annlTcktAmt;
						var annlClrtAmt = res.annlClrtAmt;
						//给额度赋值
						$("#entrys").jqGrid("setCell", rowid,"psnAnnlLimit",psnAnnlLimit);
						//计算年个人机票款清算金额
						$("#entrys").jqGrid("setCell", rowid,"annlClrtAmt",annlClrtAmt);
						//个人机票额度
						$("#entrys").jqGrid("setCell", rowid,"annlTcktAmt",annlTcktAmt);
					}
				}
			});
		}
	},
	/**
	 * 获取分录的表格gird
	 */
	getEntrysGrid : function(){
		return $("#entrys");
	},
	//这里需要重新计算，根据机票款清算日期，人员获取额度起始日期，额度结束日期等于机票款清算日期
	doBeforeFillRowData : function(personId){
		var that = this;
		//获取清算日期
		var clrtDate = $("#clrtDate").val();
		//标准月额度
		var mnthSrdLmt =$("#mnthSrdLmt").val();
		shr.callService({
			serviceName : "getPersonTicketClearout",
			param :{
				personID: personId,
				clrtDate:clrtDate,
				mnthSrdLmt:mnthSrdLmt
			},
			async : false,
			success : function (data) {
				if (data) {
					that.doFillRowData(data);
				}
			}
		})
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
    }
	
});
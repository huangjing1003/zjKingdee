/**
 * 名称：请假单-ATS-请假单表单form多分录(个人)
 * 模型:com.kingdee.eas.hr.ats.app.AtsLeaveBill
 * uipk:com.kingdee.eas.hr.ats.app.AtsLeaveBillFormNew
 * @author  hj
 */
shr.defineClass("shr.ats.atsLeaveBillEditNewExt", shr.ats.atsLeaveBillEditNew, {
	initalizeDOM: function () {
		shr.ats.atsLeaveBillEditNewExt.superClass.initalizeDOM.call(this);
		var that = this;
		/**
		 * 不等于查看和编辑的时候，绑定人员改变事件
		 */
		if(that.getOperateState() != 'VIEW'){
			that.personChange();		 //绑定人员改变事件
			that.leaveTicketTypeChange();//绑定休假机票购买方式改变事件
		}
	},
	/**
	 * 休假机票购买方式
	 * 当选择为不购买时则隐藏，机票信息，其他的就显示
	 */
	leaveTicketTypeChange:function(){
		var that = this;
		$("#leaveTicketType").shrPromptBox("option", {
			onchange : function(e, value) {
				var info = value.current;
				var typeId = info.id;//编码
				var typeName = info.name;//名称
				//01和04为购买机票时，显示机票信息
				if(typeId=="qGkAAAAAamkrhZRS" || typeId=="qGkAAAAAam8rhZRS"){
					$("#ticket_cont").show();
				}else{
					$("#ticket_cont").hide();
				}
			}
		});
	},
	/**
	 * 绑定人员改变事件
	 */
	personChange:function(){
		var that = this;
		$("#person").shrPromptBox("option", {
			onchange : function(e, value) {
				var info = value.current;
				var personId= info.id;
				var personNumber = info.number;
				that.remoteCall({
					type: "post",
					method: "getPersonInfo",
					param: { personId: personId},
					async: false,
					success: function (res) {
						if(res!=null){
							/**
							 * 先清空已经填充的人员字段信息
							 */
							that.clearPersonInfo();
							/**
							 * 获取后台查询返回的值
							 */
							var adminOrgUnit = res.adminOrgUnit;//所属组织
							var position = res.position;		//所属职位
							var fullNamePingYin = res.fullNamePingYin;//拼音全拼
							var employeeClassify = res.employeeClassify;//职员类别
							var nCell = res.nCell;//中国手机号码
							var officePhone = res.officePhone;//阿国手机号码
							var email = res.email;//公司邮箱地址
							var ctsId = res.ctsId;//成本部门id
							var ctsName = res.ctsName;//成本部门name
							
							$("#entries_person_number").val(personNumber);
							/**
							 * 开始赋值
							 * 拼音全拼
							 */
							if(fullNamePingYin!="" && fullNamePingYin!=undefined){
								$("#entries_person_fullNamePingYin").val(fullNamePingYin);
							}else{
								$("#entries_person_fullNamePingYin").val("");
							}
							/**
							 * 中国手机号码
							 */
							if(nCell!="" &&  nCell!=undefined){
								$("#entries_person_ncell").val(nCell);
							}else{
								$("#entries_person_ncell").val("");
							}
							/**
							 * 阿国手机号码
							 */
							if(officePhone!="" && officePhone!=undefined){
								$("#entries_person_officePhone").val(officePhone);
							}else{
								$("#entries_person_officePhone").val("");
							}
							/**
							 * 邮箱
							 */
							if(email!="" && email!=undefined){
								$("#entries_person_email").val(email);
							}else{
								$("#entries_person_email").val("");
							}
							/**
							 * 所属行政组织
							 */
							if(adminOrgUnit!="" && adminOrgUnit!=undefined ){
								$("#entries_adminOrgUnit").shrPromptBox("setValue",adminOrgUnit);
							}else{
								$("#entries_adminOrgUnit").shrPromptBox("setValue",null);
							}
							/**
							 * 所属职位
							 */
							if(position!="" && position!=undefined){
								$("#entries_position").shrPromptBox("setValue",position);
							}else{
								$("#entries_position").shrPromptBox("setValue",null);
							}						
							/**
							 * 职员类别
							 */
							if(employeeClassify!="" && employeeClassify!=undefined){
								$("#entries_person_employeeClassify").val(employeeClassify);
							}else{
								$("#entries_person_employeeClassify").shrPromptBox("setValue",null);
							}	
							/**
							 *成本部门字段的填充
							 */
							if((ctsId!="" && ctsId!=undefined) && (ctsName!="" && ctsName!=undefined)){
								var ctsData = {id:ctsId,name:ctsName};
								$("#costCntr").shrPromptBox("setValue",ctsData);
							}else{
								$("#costCntr").shrPromptBox("setValue",null);
							}
						}
					}
				});
			}
		});
	},
	/**
	 * 清空人员基本信息
	 */
	clearPersonInfo:function(){
		$("#entries_person_number").val("");
		$("#entries_person_fullNamePingYin").val("");
		$("#entries_person_ncell").val("");
		$("#entries_person_officePhone").val("");
		$("#entries_person_email").val("");
		$("#entries_person_employeeClassify").shrPromptBox("setValue",null);
		$("#entries_position").shrPromptBox("setValue",null);
		$("#entries_adminOrgUnit").shrPromptBox("setValue",null);
		$("#costCntr").shrPromptBox("setValue",null);

	},
	//新增 jqgrid 行 方法， 复写
	  addRowAction: function (event) {
		//增加自己的逻辑
		var that = this;
		if (!that.checkRowIsOver()) {
			return;
		}

		var source = event.currentTarget,
			$editGrid = this.getEditGrid(source);

		var data = this.createNewEntryModel();
		if (typeof data === 'undefined') {
			data = {};
		}

		var editGridCont = this._getEditGridCont(source);
		if (editGridCont.data('editType') == 'inline') {
			/**
			 * 给分录的人员赋默认值
			 */
			var personId = $("#person_el").val();
			var personName = $("#person").val();
			var personInfo = {id:personId,name:personName};
			data.person = personInfo;
 			// 表格内编辑
			$editGrid.jqGrid('addRow', { data: data });
		} else {
			$editGrid.wafGrid('addForm');
		}
		//$editGrid.not-editable-cell
		var event = document.createEvent('HTMLEvents');
		event.initEvent("editComplete_" + $editGrid.attr("id"), true, true);
		event.eventType = 'message';
		document.dispatchEvent(event);
	},
	/**
	 * 初始化当前的HR业务组织
	 */
	  initCurrentHrOrgUnit: function (hrOrgUnitId) {
		var that = this;
		that.initQuerySolutionHrOrgUnit(hrOrgUnitId);
		//不等于中建阿尔及利亚的时候，机票部分信息不显示，且购票方式不为必填
		if(hrOrgUnitId!="00000000-0000-0000-0000-000000000000CCE7AED4"){
			$("#ticketInfo").hide();
			$("#leaveTicketType").shrPromptBox({required:false});
		}else{//选中建阿尔及利亚的时候，购票方式为必填
			$("#ticketInfo").show();
			$("#leaveTicketType").shrPromptBox({required:true});
		}
	},
	/**
	 * 初始化HR业务组织
	 */
	 initQuerySolutionHrOrgUnit: function (hrOrgUnitId) {
		var that = this;
		that.remoteCall({
			type: "post",
			method: "initQuerySolution",
			param: {
				hrOrgUnitId: hrOrgUnitId
			},
			async: true,
			success: function (res) {
			}
		});
	}
	
});
/**
 * 名称：出差单-ATS-出差单表单-批量新增form
 * uipk:com.kingdee.eas.hr.ats.app.AtsTripBillBatchNew
 * 模型:com.kingdee.eas.hr.ats.app.AtsTripBill
 */
shr.defineClass("shr.ats.atsTripBillBatchNewEditExt", shr.ats.atsTripBillBatchNewEdit, {
	initalizeDOM : function () {
		shr.ats.atsTripBillBatchNewEditExt.superClass.initalizeDOM.call(this);
		var that = this;
		if (that.getOperateState() != 'VIEW') {
			that.isOutBgChange();//绑定是否超预算字段，来动态控制原因是否要填写
			//绑定金额字段的on事件，监控预算总额的计算
			that.moneyChangeEvent();
			//绑定分录的人员改变事件
			that.personF7ChangeEvent();
			that.leaveTicketTypeChange();//绑定休假机票购买方式改变事件
		}
	},
	/**
	 * 是否超预算改变事件
	 */
	isOutBgChange:function(){
		$("#isOutBg").on('change', function (e){
			var isOutBg = $("#isOutBg_el").val();
			if(isOutBg=="1"){//是超出预算
				$("#overBudgetRsn").shrTextarea({required:true});
			}else{//未超出预算
				$("#overBudgetRsn").shrTextarea({required:false});
			}
		});
	},
	/**
	 * 预算金额改变事件，需要动态计算预算总额
	 */
	moneyChangeEvent:function(){
	   $("#tripExpense,#stayExpense,#foodExpense,#cityExpense,#otherExpense").on('input',function(e){
		  var tripExpense =  $("#tripExpense").val();
		  var stayExpense =  $("#stayExpense").val();
		  var foodExpense =  $("#foodExpense").val();
		  var cityExpense =  $("#cityExpense").val();
		  var otherExpense =  $("#otherExpense").val();
		  var total = Number(tripExpense)+Number(stayExpense)+Number(foodExpense)+Number(cityExpense)+Number(otherExpense);
		  $("#totalBudget").val(total);
	   });
	},
	/**
	 * 绑定分录的人员改变事件
	 */
	personF7ChangeEvent:function(){
		var _self = this;
		var $table = waf("table[id^='entries']"); 
		$table.delegate(".ui-promptBox-layout","keyup.shrPromptGrid click",function(){
			$("input[id$='_person']").shrPromptBox('option', {
				onchange: function(e, val){
					var info = val.current;
					var rowid = $($(e.target).parents('tr')[0]).attr('id');
					//出差开始时间
					var entries_tripStartTime = $("#entries_tripStartTime").val();
					//出差结束时间
					var entries_tripEndTime = $("#entries_tripEndTime").val();
					$table.wafGrid("setCell",rowid,"fullNamePinYin",info["person.fullNamePinYin"]);
					$table.wafGrid("setCell",rowid,"adminOrgUnit",{id : info["adminOrgUnit.id"], name : info["adminUnit.name"]});
					//出差开始时间填充
					$table.wafGrid("setCell",rowid,"tripStartTime",entries_tripStartTime);
					//出差结束时间填充
					$table.wafGrid("setCell",rowid,"tripEndTime",entries_tripEndTime);
					/**
					 * 计算出差天数
					 */
					var cal_days = _self.calculataTripDays(rowid);
					if (cal_days!=null && cal_days>=0) {
						$("#entries").jqGrid("setCell", rowid,"tripDays",cal_days);
					}
				}
			});
		});
	},
	/**
	 * 休假机票购买方式
	 * 当选择为不购买时则隐藏，机票信息，其他的就显示
	 */
	leaveTicketTypeChange:function(){
		var that = this;
		$("#TripTicketType").shrPromptBox("option", {
			onchange : function(e, value) {
				var info = value.current;
				var typeId = info.id;//编码
				var typeName = info.name;//名称
				//02公司前方购买和05改签休假机票时，显示行程信息
				if(typeId=="qGkAAAAAan41p43W" || typeId=="qGkAAAAAaoQ1p43W"){
					$("#ticket_cont").show();
				}else{
					$("#ticket_cont").hide();
				}
			}
		});
	} 
	
});
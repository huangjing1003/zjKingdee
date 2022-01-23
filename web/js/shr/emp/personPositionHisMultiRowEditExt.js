/**
 * 名称:职业信息历史多行表查看
 * 模型:com.kingdee.eas.hr.emp.app.PersonPositionHis
 * uipk:com.kingdee.eas.hr.emp.app.PersonPositionHisMultiRow.form
 */
shr.defineClass("shr.emp.PersonPositionHisMultiRowEditExt", shr.emp.PersonPositionHisMultiRowEdit, {
	initalizeDOM:function(){
		var self = this;
		shr.emp.PersonPositionHisMultiRowEditExt.superClass.initalizeDOM.call(this);
	},
	countJoinCompanyYears : function(){
		var self = this;
		var joinDateStr = self.getFieldValue("joinDate");
		var leftCompanyDateStr = self.getFieldValue("leftCompanyDate");
		var adjustCoValueStr = self.getFieldValue("adjustCoValue");
		var joinCompanyYears = self.calculateWorkTimeVal(joinDateStr,leftCompanyDateStr,adjustCoValueStr);
		if(joinCompanyYears < 0){
			joinCompanyYears = 0;
		}
		self.getField("joinCompanyYears").shrTextField("setValue",joinCompanyYears);
	},
	//计算工作年限
	calculateWorkTimeVal : function(beginDateStr,endDateStr,adjVal){//计算工作年限
		var self = this;
		if(beginDateStr!=null && beginDateStr!=""){
			var beginDate = new Date(beginDateStr);
			var endDate;
			if(endDateStr==null || endDateStr==""){
				endDate = new Date();
			}else{
				endDate = new Date(endDateStr);
			}
			var yearVal = endDate.getFullYear()-beginDate.getFullYear();
			var monthVal = endDate.getMonth()-beginDate.getMonth();
			if(adjVal==null || adjVal==""){
				adjVal=0.0;
			}else{
				adjVal = eval(adjVal);
			}
//			var workTime = yearVal+monthVal/12.0-adjVal;
			var workTime = yearVal+monthVal/12.0+adjVal;//现在直接改成加法
			var workTime_Trunc = self.getRoundValueBySysParam(workTime);
			return workTime_Trunc;
		}else{
			return 0.0;
		}
	} 
	
});

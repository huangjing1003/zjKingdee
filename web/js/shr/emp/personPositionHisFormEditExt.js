/**
 * 名称:职业信息历史多行表编辑
 * 模型:com.kingdee.eas.hr.emp.app.PersonPositionHis
 * uipk:com.kingdee.eas.hr.emp.app.PersonPositionHisEdit.form
 */
shr.defineClass("shr.emp.PersonPositionHisFormEditExt", shr.emp.PersonPositionHisFormEdit, {
	initalizeDOM:function(){
		var self = this;
		shr.emp.PersonPositionHisFormEditExt.superClass.initalizeDOM.call(this);
	},
	/**
	 * 计算工作年限
	 */
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
//			var workTime = yearVal+monthVal/12.0-adjVal;//原来的写法
			var workTime = yearVal+monthVal/12.0+adjVal;//现在直接改成加法
			var workTime_Trunc = self.getRoundValueBySysParam(workTime);
			return workTime_Trunc;
		}else{
			return 0.0;
		}
	}
});

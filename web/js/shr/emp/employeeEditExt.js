/**
 * 名称:员工编辑
 * 模型:com.kingdee.eas.basedata.person.app.Person
 * uipk:com.kingdee.eas.basedata.person.app.Person.form
 */
﻿shr.defineClass("shr.emp.EmployeeEditExt", shr.emp.EmployeeEdit, {
	initalizeDOM:function(){
		var _self = this;
		shr.emp.EmployeeEditExt.superClass.initalizeDOM.call(this);
	},
	/**
	 * 计算工作时长
	 */
	calculateWorkTimeVal:function(beginDateStr,endDateStr,adjVal){//计算工作年限
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
//			var workTime = yearVal+monthVal/12.0-adjVal;原来的写法
			var workTime = yearVal+monthVal/12.0+adjVal;//直接换成加法计算
			var workTime_Trunc = self.getRoundValueBySysParam(workTime);
			return workTime_Trunc;
		}else{
			return "";
		}
	}
	
});

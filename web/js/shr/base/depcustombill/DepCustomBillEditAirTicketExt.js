/**
 * 机票信息-自定义扩展单据
 */
shr.defineClass("shr.custom.bizbill.DepCustomBillEditAirTicketExt", shr.custom.bizbill.DepCustomBillEdit, {
	initalizeDOM : function () {
		shr.custom.bizbill.DepCustomBillEditAirTicketExt.superClass.initalizeDOM.call(this);
		var that = this;
		var operateState = that.getOperateState();
		/**
		 * 不等于新增的时候，展示查看来源按钮
		 */
		if(operateState != 'ADDNEW'){
			$("#viewSource").show();
		}else{
			$("#viewSource").hide();
		}
		/**
		 * 不等于查看和编辑的时候，绑定人员改变事件
		 */
		if(operateState != 'VIEW'){
			/**
			 * 初始化设置人员过滤条件
			 */
    		var hrOrgUnitId = $("#hrOrgUnit_el").val();
    		if(hrOrgUnitId!="" && hrOrgUnitId!=undefined){
    			$("#person").shrPromptBox("setFilter"," HROrgUnit.id = '"+hrOrgUnitId+"'");
    		}
			that.personChangeEvent();//绑定人员改变事件
			that.hrOrgUnitChange();//绑定人事业务组织改变事件
		}
	},
	/**
	 * 查看档案按钮
	 */
	viewSourceAction:function(){
		var that=this;
		var billId = that.billId;
    	//异步获取人员的简历id
		that.remoteCall({
            method : 'getSourceBill',
            param : {
            	billId : billId,
            },
            success:function(res){
                if(res != undefined && res != ''&& res !=null) {
                        var bostype =res.bosType;
                        var sourceBillId =res.sourceBillId;
                        if(bostype=="80EF7DED" || bostype=="A0F39678"){//type为请假单据则直接跳转到请假
                        	//查看来源的请假单页面
                        	_self.reloadPage({
                        		uipk : "com.kingdee.eas.hr.ats.app.AtsLeaveBillAllBatchForm",
                        		billId : sourceBillId
                        	});
                        }else if (bostype=="2A78C372"){//等于出差则跳到出差
                        	//查看来源的出差单页面
                        	_self.reloadPage({
                        		uipk : "com.kingdee.eas.hr.ats.app.AtsTripBillBatchNew",
                        		billId : sourceBillId
                        	});
                        }
                    }else{
            			shr.showWarning({
            				message: "当前单据未关联其他来源单据信息，无法查看!!",
            				hideAfter: null
            			});
                    }
                }
            });
        },
    	/**
    	 * 绑定人员改变事件
    	 */
    	personChangeEvent:function(){
    		var that = this;
    		$("#person").shrPromptBox("option", {
    			onchange : function(e, value) {
    				var info = value.current;
    				var personId= info.id;
    				var personNumber = info.number;
    				var adminOrgId = info["adminOrgUnit.id"];
    				var adminUnitName = info["adminOrgUnit.name"];
    				/**
    				 * 异步查询其他字段信息
    				 */
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
    							var fullNamePingYin = res.fullNamePingYin;//拼音全拼
    							var cts = res.cts;//成本部门 
    							//设置组织
    		    				$("#adminOrg").shrPromptBox("setValue",{id:adminOrgId,name:adminUnitName});
    							/**
    							 * 设置成本部门
    							 */
    							if(cts!="" && cts!=undefined){
    								//设置成本中心
    								$("#costCntr").shrPromptBox("setValue",cts);
    							}else{
    								$("#costCntr").shrPromptBox("setValue",null);
    							}
    							/**
    							 * 设置护照姓名全拼
    							 */
    							if(fullNamePingYin!="" && fullNamePingYin!=undefined){
    								$("#fullNamePingYin").val(fullNamePingYin);
    							}else{
    								$("#fullNamePingYin").val("");
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
    		$("#fullNamePingYin").val("");
    		$("#adminOrg").shrPromptBox("setValue",null);
    		$("#costCntr").shrPromptBox("setValue",null);
    	},
    	/**
    	 * 人事业务组织发生改变
    	 */
    	hrOrgUnitChange:function(){
    		var that = this;
    		$("#hrOrgUnit").shrPromptBox("option", {
    			onchange : function(e, value) {
    				var info = value.current;
    				var hrOrgUnitId= info.id;
					/**
					 * 先清空已经填充的人员字段信息
					 */
					that.clearPersonInfo();
					//给人员设置过滤条件
					$("#person").shrPromptBox("setFilter"," HROrgUnit.id = '"+hrOrgUnitId+"'");
    			}
    		});
    	}
});
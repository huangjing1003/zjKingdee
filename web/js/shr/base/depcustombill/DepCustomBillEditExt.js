/**
 * dep自定义扩展单据
 */
shr.defineClass("shr.custom.bizbill.DepCustomBillEditExt", shr.custom.bizbill.DepCustomBillEdit, {
	initalizeDOM : function () {
		shr.custom.bizbill.DepCustomBillEditExt.superClass.initalizeDOM.call(this);
		var that = this;
		var operateState = that.getOperateState();
		/**
		 * 不等于查看和编辑的时候，绑定人员改变事件
		 */
		if(operateState != 'VIEW'){
			that.personChangeEvent();//绑定人员改变事件
		}
	},
	/**
	 * 绑定人员改变事件
	 */
	personChangeEvent:function(){
		var that = this;
		$("#entrys_person").shrPromptBox("option", {
			onchange : function(e, value) {
				var info = value.current;
				var personId= info.id;
				var personNumber = info.number;
				$("#entrys_person_number").val(personNumber);
				var personidCardNO = info.idCardNO;
				$("#entrys_person_idCardNO").val(personidCardNO);
				 var specialty = info["PersonDegree.specialty"];
				$("#entrys_specialty").val(specialty);
				var adminOrgId = info["adminOrgUnit.id"];
				var adminUnitName = info["adminOrgUnit.name"];
				var positionName = info["position.name"];
				var positionId = info["position.id"];
				var typeId = info["FranceBnsPsnType.id"];
				var typeName = info["FranceBnsPsnType.name"];
				var ContractTempletid = info["ContractTemplet.id"];
				var ContractTempletname = info["ContractTemplet.name"];
				var LabContractFirstPartyid = info["LabContractFirstParty.id"];
				var LabContractFirstPartyname = info["LabContractFirstParty.name"];
				var cstCenterId = info["CstCenter.id"];
				var cstCenterName = info["CstCenter.name"];
				var CmpStdLevelid = info["CmpStdLevel.id"];
				var CmpStdLevelname = info["CmpStdLevel.name"];
				var JobGradeid = info["JobGrade.id"];
				var JobGradename = info["JobGrade.name"];
				var JobGrade1id = info["JobGrade1.id"];
				var JobGrade1name = info["JobGrade1.name"];
				var EngBnsRankid = info["EngBnsRank.id"];
				var EngBnsRankname = info["EngBnsRank.name"];
				var LangBnsRankid = info["LangBnsRank.id"];
				var LangBnsRankname = info["LangBnsRank.name"];
				var Diplomaid = info["Diploma.id"];
				var Diplomaname = info["Diploma.name"];
				//性别2021-09-17 hj
				var gender = info["gender"];
				var enterDate = info["pp.joinGroupDateCur"];
				//设置组织
				$("#entrys_adminOrg").shrPromptBox("setValue",{id:adminOrgId,name:adminUnitName});
				//设置职位
				$("#entrys_position").shrPromptBox("setValue",{id:positionId,name:positionName});
				//设置类别
				$("#entrys_franceBnsPsnType").shrPromptBox("setValue",{id:typeId,name:typeName});
                 //协议类型
				$("#entrys_contractTemplet").shrPromptBox("setValue",{id:ContractTempletid,name:ContractTempletname});
				 //劳动关系主体
				 $("#entrys_labContractFirstParty").shrPromptBox("setValue",{id:LabContractFirstPartyid,name:LabContractFirstPartyname});
				 //设置成本部门
				$("#entrys_costCntr").shrPromptBox("setValue",{id:cstCenterId,name:cstCenterName});
				//管理岗新等级
				$("#entrys_mngJobGrade").shrPromptBox("setValue",{id:CmpStdLevelid,name:CmpStdLevelname});
				//内部职级
				$("#entrys_grpJobGrade").shrPromptBox("setValue",{id:JobGradeid,name:JobGradename});
				//职级
				$("#entrys_jobGrade").shrPromptBox("setValue",{id:JobGrade1id,name:JobGrade1name});
				//英语津贴
				$("#entrys_engBnsRank").shrPromptBox("setValue",{id:EngBnsRankid,name:EngBnsRankname});
				//法语津贴
				$("#entrys_frsBnsRank").shrPromptBox("setValue",{id:LangBnsRankid,name:LangBnsRankname});
				//学历
				$("#entrys_diploma").shrPromptBox("setValue",{id:Diplomaid,name:Diplomaname});
				//性别2021-09-17 hj
				$('#entrys_gender').shrSelect("setValue",gender);
				//增加入职日期2021-09-17 hj,判断入职日期不等于空再截取设置值在界面上
				if(enterDate!=null || enterDate!=""){
					var enterDateStr = enterDate.substring(0,10);
					$("#entrys_cur_joinGroupDate").shrDateTimePicker('setValue', enterDateStr);
				}
			}
		});
	}
	
});
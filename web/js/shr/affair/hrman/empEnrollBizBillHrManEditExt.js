  /**
 * 入职申请：专业用户
 * uipk:com.kingdee.eas.hr.affair.app.EmpEnrollBizBill.form
 */
shr.defineClass("shr.affair.hrman.EmpEnrollBizBillHrManEditExt",shr.affair.hrman.EmpEnrollBizBillHrManEdit, {
	initalizeDOM : function () {
		var that = this;
		shr.affair.hrman.EmpEnrollBizBillHrManEditExt.superClass.initalizeDOM.call(this);
		that.politicalFaceChangeEvent();//绑定政治面貌改变事件
		that.departmentChangeEvent();//绑定组织改变事件
		that.professionalDepChangeEvent();//绑定专业序列改变事件
	},
	//HR组织带出用工单位，如果当前变动操作是原用工单位，则不修改
	hrOrgUnitF7ChangeEvent:function(){
		var self = this;
		if(self.getOperateState() == 'VIEW'){
			return ;
		}
		var $hrOrgUnit = self.getField("hrOrgUnit");
		self.afterOnchangeClearFields = $hrOrgUnit.shrPromptBox("option").afterOnchangeClearFields;
		$hrOrgUnit.shrPromptBox("option", {
			onchange : function(e, value) {
				var currentObj = value.current;
				var hrOrgUnitObj = null;
				if( currentObj ) {
					 hrOrgUnitObj = {
						id:currentObj.id,
						name:currentObj.name
					};
					self._currentHROrgUnit = hrOrgUnitObj;
					self.getField("entrys.employerUnit").shrPromptBox("setValue",hrOrgUnitObj);
					if(self.isPersonF7FilterByHROrg()){
						self.updatePersonF7ValRange(hrOrgUnitObj);
					}
				}
				if(self.isClearPersonF7AndAssociateF7AfterChangeHRO()){
					self.clearPersonF7AndAssociateF7();
				}
				self.clearPositionAndAssociateF7();
				if(self.isPositionF7FilterByHROrg()){
					self.setPositionF7Filter(currentObj);//设置职位F7过滤条件
					self.setAdminOrgF7Filter(currentObj);//设置专业序列职位F7过滤条件
				}
				self.fixClearFields(currentObj);//筛选需要清除的字段
				
				if(self.isRelatePositionJobRange==false){//职层职等不强制关联职位时，按HR组织过滤
					self.setJobLevelGradeF7FilterByHrOrg(hrOrgUnitObj);
				}
				//2021-09-30hj,根据HR组织来动态展示字段信息
				self.filterOtherFieds(hrOrgUnitObj);
			}
		});
	},
	/**
	 * 设置专业序列职位F7过滤条件
	 */
	setAdminOrgF7Filter:function(bizFieldValue){
		var self = this;
		var filter = " hrOrgUnit.id = '"+bizFieldValue.id+"' and department.number like 'Z%'";
		//专业序列职位过滤
		self.getField("entrys.professionalPosition").shrPromptBox("setFilter",filter);
		//专业序列
		self.getField("entrys.professionalDep").shrPromptBox("setFilter",filter);
                //设置入职/项目加过滤
                
                var adminOrgFilter = " hrOrgUnit.id = '"+bizFieldValue.id+"' and number not  like 'Z%'";               
                self.getField("entrys.position.adminOrgUnit.department").shrPromptBox("setFilter",adminOrgFilter);
	},
	/**
	 * 专业序列改变事件
	 */
	professionalDepChangeEvent:function(){
		var self = this;
		if (self.getOperateState() != 'VIEW') {
			self.getField("entrys.professionalDep").shrPromptBox("option",{
				verifyBeforeOpenCallback: function(event){
					var $hrOrgUnit = self.getField("hrOrgUnit");
					var hrOrgUnitF7Value = $hrOrgUnit.shrPromptBox("getValue");
					if(!hrOrgUnitF7Value || hrOrgUnitF7Value.id == ""){
						shr.showError({
							message:jsBizMultLan.emp_shrAffairBillBaseHrManEdit_i18n_10
						});
						return false;
					}
				}
			});
			//绑定专业序列的change事件
			self.getField("entrys.professionalDep").shrPromptBox("option",{
				onchange : function(e, value) {
					if(value.current!="" || value.current!=null){
						var professionalDepId = value.current.id;//专业序列id
						//给专业序列职位设置过滤条件
						var filter = "department.id = '" + professionalDepId+ "'";
						var $hrOrgUnit = self.getField("hrOrgUnit");
						var hrOrgUnitF7Value = $hrOrgUnit.shrPromptBox("getValue");
						filter += " and hrOrgUnit.id = '"+hrOrgUnitF7Value.id+"'";
						self.getField("entrys.professionalPosition").shrPromptBox("setFilter",filter);
					}
				}
			});
		}
	},
	//根据HR组织id动态展示政治面貌和其他字段等信息
	filterOtherFieds:function(hrOrgUnitObj){
		var hrOrgUnitName = hrOrgUnitObj.name;
		if(hrOrgUnitName=="国内人力板块"){
			//"原累计在阿工作年限"字段隐藏或置灰
			$("#entrys_adjustCoValue").shrTextField("disable");
			$("#entrys_adjustCoValue").shrTextField({required:false});
			//2021-10-11处理其他的字段信息
			//政治面貌
			$('#entrys_politicalFace').shrPromptBox("enable");
			$("#entrys_politicalFace").shrPromptBox({required:true});
			$("#entrys_politicalFace").shrPromptBox('setValue',null);
			//入党日期
			$('#entrys_JionPtyDate').shrDateTimePicker("enable");
			$('#entrys_JionPtyDate').shrDateTimePicker({required:false});
			$("#entrys_JionPtyDate").shrDateTimePicker('setValue',null);

			//党费是否代缴
			$('input[id="entrys_withholdPtyFee"]').removeAttr("disabled");
			$('input[id="entrys_withholdPtyFee"]').attr("readonly",false);
			$("#entrys_withholdPtyFee").val(0);
			$("#entrys_withholdPtyFee").parent().removeClass("checked");
			
			$("#entrys_JionPtyDate").shrDateTimePicker('setValue',null);

			//法语津贴类别
			$('#entrys_FranceBnsPsnType').shrPromptBox("enable");
			$("#entrys_FranceBnsPsnType").shrPromptBox({required:false});
			$("#entrys_FranceBnsPsnType").shrPromptBox('setValue',null);
			
		}else if  (hrOrgUnitName=="属地化国际化管理板块"){
			//“政治面貌”“入党日期”“党费是否代缴”“法语津贴职员类别”"原累计在阿工作年限"字段隐藏或置灰
			//政治面貌
			$('#entrys_politicalFace').shrPromptBox("disable");
			$("#entrys_politicalFace").shrPromptBox({required:false});
			//入党日期
			$('#entrys_JionPtyDate').shrDateTimePicker("disable");
			$('#entrys_JionPtyDate').shrDateTimePicker({required:false});
			//党费是否代缴，不可编辑
			$('input[id="entrys_withholdPtyFee"]').attr("disabled","disabled");
			$('input[id="entrys_withholdPtyFee"]').attr("readonly",true);
			//每次都设置默认为0，不选中
			$("#entrys_withholdPtyFee").val(0);
			$("#entrys_withholdPtyFee").parent().removeClass("checked");
			//法语津贴类别
			$('#entrys_FranceBnsPsnType').shrPromptBox("disable");
			$("#entrys_FranceBnsPsnType").shrPromptBox({required:false});
			//"原累计在阿工作年限"字段隐藏或置灰
			$("#entrys_adjustCoValue").shrTextField("disable");
			$("#entrys_adjustCoValue").shrTextField({required:false});
			$("#entrys_adjustCoValue").shrTextField('setValue',null);

		} else{//其他HR组织的情况
			//政治面貌
			$('#entrys_politicalFace').shrPromptBox("enable");
			$('#entrys_politicalFace').shrPromptBox('setValue',null);
			$("#entrys_politicalFace").shrPromptBox({required:true});

			//入党日期
			$('#entrys_JionPtyDate').shrDateTimePicker("enable");
			$('#entrys_JionPtyDate').shrDateTimePicker('setValue',null);
			//职员类别
			$('#entrys_FranceBnsPsnType').shrPromptBox("enable");
			$('#entrys_FranceBnsPsnType').shrPromptBox('setValue',null);
			//"原累计在阿工作年限"字段隐藏或置灰
			$("#entrys_adjustCoValue").shrTextField("enable");
			$("#entrys_adjustCoValue").shrTextField('setValue',null);
			//党费是否代缴
			$('input[id="entrys_withholdPtyFee"]').removeAttr("disabled");
			$('input[id="entrys_withholdPtyFee"]').attr("readonly",false);
			$("#entrys_withholdPtyFee").val(0);
			$("#entrys_withholdPtyFee").parent().removeClass("checked");
			
		}
	},
	//2021-09-30 hj-政治面貌改变事件,“政治面貌”非“中共党员”或者“中共预备党员”的，“入党日期”“党费是否代缴”字段置灰，不可填；
	politicalFaceChangeEvent:function(){
		var self = this;
		if(self.getOperateState() == 'VIEW'){
			return ;
		}
		var $politicalFace = self.getField("entrys.politicalFace");
		self.afterOnchangeClearFields = $politicalFace.shrPromptBox("option").afterOnchangeClearFields;
		$politicalFace.shrPromptBox("option", {
			onchange : function(e, value) {
				var currentObj = value.current;
				if(currentObj!=null){
					var politicalFaceName = currentObj.name;
					//“政治面貌”非“中共党员”或者“中共预备党员”的，“入党日期”“党费是否代缴”字段置灰，不可填；
					if(!(politicalFaceName=="中共党员" || politicalFaceName=="中共预备党员")){
						//入党日期
						$('#entrys_JionPtyDate').shrDateTimePicker("disable");
						$('#entrys_JionPtyDate').shrDateTimePicker({required:false});
						//党费是否代缴
						$('input[id="entrys_withholdPtyFee"]').attr("disabled","disabled");
						$('input[id="entrys_withholdPtyFee"]').attr("readonly",true);
					} else{
						//入党日期
						$('#entrys_JionPtyDate').shrDateTimePicker("enable");
						$('#entrys_JionPtyDate').shrDateTimePicker({required:false});
						//党费是否代缴
						$('input[id="entrys_withholdPtyFee"]').removeAttr("disabled");
						$("#entrys_withholdPtyFee").val(0);
						$("#entrys_withholdPtyFee").parent().removeClass("checked");
					}
				} 
//					else{
//					//入党日期
//					$('#entrys_JionPtyDate').shrDateTimePicker("enable");
//					$('#entrys_JionPtyDate').shrDateTimePicker({required:false});
//					//党费是否代缴
//					$('input[id="entrys_withholdPtyFee"]').removeAttr("disabled");
//				}
			}
		});
	},
	/**
	 * 预入职下推初始化数据
	 */
	preEntryInit:function(){
		var _self = this;
		var selectId = shr.getUrlRequestParam("selectId");

		shr.remoteCall({
			url : shr.getContextPath() + "/dynamic.do?method=getPreEntryInitData&uipk="+jsBinder.uipk,
			type : "POST",
			param:{
				"billId" : selectId,
				"tempId" : _self.getTemporaryPersonId()//传入tempId以复制一个预入职头像都入职单
			},
			success : function(res){
				if(res.preEntryInfo != undefined){
					if(res.preEntryInfo.sourceBillType.value == 2){//offer来的预入职单
						shr.callService( {
							serviceName : 'getPreEntryInfoOSF',
							param : {preEntryId : res.preEntryInfo.id},
							async : false,
							success : function(data) {
								_self.initParameterByRecurit("entrys_probation",data.probationPeriod == undefined ? null : data.probationPeriod,false);//试用期
								_self.initParameterByRecurit("entrys_empType",data.employeeType == undefined ? null : data.employeeType,true);//用工关系状态
								_self.initParameterByRecurit("entrys_jobStartDate",data.jobStartDate == undefined ? null : data.jobStartDate.substring(0,10),false);//参加工作日期
							}
						});
					}
					
					if(res.preEntryInfo.hrOrgUnit != undefined){
						//基础资料在HR组织设置前先设置，可以接受HR组织可使用权限校验
						//_self.initParameterByRecurit("entrys_variationReason",res.preEntryInfo.enrollSource,true);
						//招聘来源反写变动原因，，handler中用招聘来源按编码匹配查出变动原因放到enrollSource中
						var hrOrgUnitObj = {id:res.preEntryInfo.hrOrgUnit.id,name:res.preEntryInfo.hrOrgUnit.name}
						_self.initParameterByRecurit("hrOrgUnit",hrOrgUnitObj,true);//人事业务组织
					}
					// 年龄
					if(res.preEntryInfo.age != undefined){
						_self.initParameterByRecurit('entrys_age',res.preEntryInfo.age,false);
					}
					/***
					 * 2021-03-15 hj增加二开带出的字段信息开始
					 */
					if(res.preEntryInfo.wed != undefined){
						_self.initParameterByRecurit('entrys_wed',res.preEntryInfo.wed,true);//婚姻状况
					}
					/**
					 * 政治面貌
					 */
					if(res.preEntryInfo.politicalFace != undefined){
						_self.initParameterByRecurit('entrys_politicalFace',res.preEntryInfo.politicalFace,true);
					}
					/**
					 * 入党时间
					 */
					if(res.preEntryInfo.JionPtyDate != undefined){
                        $("#entrys_JionPtyDate").shrDateTimePicker('setValue',res.preEntryInfo.JionPtyDate);
//						_self.initParameterByRecurit('entrys_JionPtyDate',res.preEntryInfo.JionPtyDate,false);
					}
					/**
					 * 学历entrys_highestDegree
					 */
					if(res.preEntryInfo.highestDegree != undefined){
						_self.initParameterByRecurit('entrys_highestDegree',res.preEntryInfo.highestDegree,true);
					}
					/**
					 * 毕业院校
					 */
					if(res.preEntryInfo.graduateSchool != undefined){
						_self.initParameterByRecurit('entrys_graduateSchool',res.preEntryInfo.graduateSchool,false);
					}					
					/**
					 * 专业
					 */
					if(res.preEntryInfo.specialty != undefined){
						_self.initParameterByRecurit('entrys_specialty',res.preEntryInfo.specialty,false);
					}						
					/**
					 * 入职来源
					 */
					if(res.preEntryInfo.enrollSource != undefined){
						_self.initParameterByRecurit('entrys_source',res.preEntryInfo.enrollSource,true);
					}	
					/**
					 * 岗位序列
					 */
					if(res.preEntryInfo.pstnClass != undefined){
						_self.initParameterByRecurit('entrys_professionalPosition_positionClass',res.preEntryInfo.pstnClass,true);
					}						
					/**
					 * 专业序列
					 */
					if(res.preEntryInfo.personDepP != undefined){
						_self.initParameterByRecurit('entrys_professionalDep',res.preEntryInfo.personDepP,true);
					}	
					/**
					 * 专业序列职位
					 */
					if(res.preEntryInfo.pPosition != undefined){
						_self.initParameterByRecurit('entrys_professionalPosition',res.preEntryInfo.pPosition,true);
					}						
					/**
					 * 参加工作日期
					 */
					if(res.preEntryInfo.jobStartDate != undefined){
						var jobStartDate = res.preEntryInfo.jobStartDate.substring(0,10);
						_self.initParameterByRecurit('entrys_jobStartDate',jobStartDate,false);
					}	

					// 内部职级
					if(res.preEntryInfo.grpJobGrade != undefined){
						_self.initParameterByRecurit('entrys_grpJobGrade',res.preEntryInfo.grpJobGrade,true);
					}
					// 职级
					if(res.preEntryInfo.JobGrade != undefined){
						_self.initParameterByRecurit('entrys_jobGrade',res.preEntryInfo.JobGrade,true);
					}
					// 管理岗薪等级
					if(res.preEntryInfo.mngJobGrade != undefined){
						_self.initParameterByRecurit('entrys_mngJobGrade',res.preEntryInfo.mngJobGrade,true);
					}	
					/** 
					 // 职级基薪
					if(res.preEntryInfo.wageRank != undefined){
						//_self.initParameterByRecurit('entrys_wageRank',parseFloat(res.preEntryInfo.wageRank),false);
						//$('#entrys_wageRank').val(res.preEntryInfo.wageRank);
						$('#entrys_wageRank').val(val(res.preEntryInfo.wageRank));
					}
					**/
					 //职级基薪
					if(res.preEntryInfo.wageRank  != undefined){
						//$("#entrys_wageRank").shrNumberField('setValue',res.preEntryInfo.wageRank);
						$("#entrys_wageRank").shrNumberField('setValue',res.preEntryInfo.wageRank);
					}
					
					// 海外津贴系数
					if(res.preEntryInfo.ovrseaAllwncRatio != undefined){
						$("#entrys_ovrseaAllwncRatio").shrNumberField('setValue',res.preEntryInfo.ovrseaAllwncRatio);
					}

					// 海外津贴
					if(res.preEntryInfo.ovrseaAllwnc != undefined){
						$("#entrys_ovrseaAllwnc").shrNumberField('setValue',res.preEntryInfo.ovrseaAllwnc);
					}
					
					// 管理岗薪
					if(res.preEntryInfo.adPstnWage != undefined){
						$("#entrys_adPstnWage").shrNumberField('setValue',res.preEntryInfo.adPstnWage);
					}
					// 月基薪
					if(res.preEntryInfo.mnthBasicWage != undefined){
						$("#entrys_mnthBasicWage").shrNumberField('setValue',res.preEntryInfo.mnthBasicWage);
					}
					// 现场津贴
					if(res.preEntryInfo.siteAllwnc != undefined){
						$("#entrys_siteAllwnc").shrNumberField('setValue',res.preEntryInfo.siteAllwnc);
					}
					// 英语津贴
					if(res.preEntryInfo.EnAllwnc != undefined){
						$("#entrys_EnAllwnc").shrNumberField('setValue',res.preEntryInfo.EnAllwnc);
					}
					// 法语津贴
					if(res.preEntryInfo.FrAllwnc != undefined){
						$("#entrys_FrAllwnc").shrNumberField('setValue',res.preEntryInfo.FrAllwnc);
					}
					// 专业序列津贴
					if(res.preEntryInfo.pAllwnc != undefined){
						$("#entrys_pAllwnc").shrNumberField('setValue',res.preEntryInfo.pAllwnc);
					}
					// 特殊专业津贴
					if(res.preEntryInfo.specialAllwnc != undefined){
						$("#entrys_specialAllwnc").shrNumberField('setValue',res.preEntryInfo.specialAllwnc);
					}
					// 交通医药电脑补助（国外）
					if(res.preEntryInfo.trvlMedComAllwnc != undefined){
						$("#entrys_trvlMedComAllwnc").shrNumberField('setValue',res.preEntryInfo.trvlMedComAllwnc);
					}
					// 月度福利费(国内)
					if(res.preEntryInfo.monthBnft != undefined){
						$("#entrys_monthBnft").shrNumberField('setValue',res.preEntryInfo.monthBnft);
					}
					
					// 月工资
					if(res.preEntryInfo.monthWage != undefined){
						$("#entrys_monthWage").shrNumberField('setValue',res.preEntryInfo.monthWage);
					}
					  
					/****************** 2021-03-15 hj增加二开带出的字段信息结束 ******************/
					 
					//_self.initParameterByRecurit('entrys_empName',res.preEntryInfo.name,false, 'shrMultiLangBox');//姓名
					$("#entrys_empName").shrMultiLangBox("setValue", res.preEntryInfo.name_l1, 'l1');
					$("#entrys_empName").shrMultiLangBox("setValue", res.preEntryInfo.name_l2, 'l2');
					$("#entrys_empName").shrMultiLangBox("setValue", res.preEntryInfo.name_l3, 'l3');
					
					if (null != res.preEntryInfo.firstname_l1 && "" != res.preEntryInfo.firstname_l1) {
						$("#entrys_firstName").shrMultiLangBox("setValue", res.preEntryInfo.firstname_l1, 'l1');
					}
					if (null != res.preEntryInfo.firstname_l2 && "" != res.preEntryInfo.firstname_l2) {
						$("#entrys_firstName").shrMultiLangBox("setValue", res.preEntryInfo.firstname_l2, 'l2');
					}
					if (null != res.preEntryInfo.firstname_l3 && "" != res.preEntryInfo.firstname_l3) {
						$("#entrys_firstName").shrMultiLangBox("setValue", res.preEntryInfo.firstname_l3, 'l3');
					}
					
					if (null != res.preEntryInfo.lastname_l1 && "" != res.preEntryInfo.lastname_l1) {
						$("#entrys_lastName").shrMultiLangBox("setValue", res.preEntryInfo.lastname_l1, 'l1');
					}
					if (null != res.preEntryInfo.lastname_l2 && "" != res.preEntryInfo.lastname_l2) {
						$("#entrys_lastName").shrMultiLangBox("setValue", res.preEntryInfo.lastname_l2, 'l2');
					}
					if (null != res.preEntryInfo.lastname_l3 && "" != res.preEntryInfo.lastname_l3) {
						$("#entrys_lastName").shrMultiLangBox("setValue", res.preEntryInfo.lastname_l3, 'l3');
					}
					
					if (null != res.preEntryInfo.middlename_l1 && "" != res.preEntryInfo.middlename_l1) {
						$("#entrys_middleName").shrMultiLangBox("setValue", res.preEntryInfo.middlename_l1, 'l1');
					}
					if (null != res.preEntryInfo.middlename_l2 && "" != res.preEntryInfo.middlename_l2) {
						$("#entrys_middleName").shrMultiLangBox("setValue", res.preEntryInfo.middlename_l2, 'l2');
					}
					if (null != res.preEntryInfo.middlename_l3 && "" != res.preEntryInfo.middlename_l3) {
						$("#entrys_middleName").shrMultiLangBox("setValue", res.preEntryInfo.middlename_l3, 'l3');
					}
					
					
					_self.initParameterByRecurit("entrys_bizDate",res.preEntryInfo.preEnterDate == undefined ? null : res.preEntryInfo.preEnterDate.substring(0,10), false, "datetimepicker");//入职日期
					_self.initParameterByRecurit('entrys_telNum',res.preEntryInfo.cellPhone,false);//手机号	
					_self.initParameterByRecurit('entrys_email',res.preEntryInfo.email,false);//电子邮件
					_self.initParameterByRecurit("entrys_description",res.preEntryInfo.remark,false);//备注
					_self.initParameterByRecurit("entrys_nCell",res.preEntryInfo.nCell,false);//手机号
					_self.initParameterByRecurit("entrys_globalRoaming_el",res.preEntryInfo.globalRoaming,false);//区号
					
					if(res.preEntryInfo.talent != undefined){
						_self.initParameterByRecurit('entrys_IDCardNo',res.preEntryInfo.talent.idCardNO,false);//身份证号码
						_self.initParameterByRecurit("entrys_passportNo",res.preEntryInfo.talent.passportNO,false);//护照号码
						_self.initParameterByRecurit("entrys_birthday",res.preEntryInfo.talent.birthday == undefined ? null : res.preEntryInfo.talent.birthday.substring(0,10), false, "datetimepicker");//出生日期
						_self.initParameterByRecurit("entrys_myFolk",res.preEntryInfo.talent.folk,true);//民族
						_self.initParameterByRecurit('entrys_nativePlace',res.preEntryInfo.talent.nativePlace,false);//籍贯
						_self.initParameterByRecurit("entrys_idCardStartDate",res.preEntryInfo.talent.idCardBeginDate == undefined ? null : res.preEntryInfo.talent.idCardBeginDate.substring(0,10), false, "datetimepicker");//身份证开始日期
						_self.initParameterByRecurit("entrys_idCardEndDate",res.preEntryInfo.talent.idCardEndDate == undefined ? null : res.preEntryInfo.talent.idCardEndDate.substring(0,10), false, "datetimepicker");//身份证结束日期
						_self.initParameterByRecurit("entrys_idCardAddress",res.preEntryInfo.talent.idCardAddress,false);//身份证地址
						_self.initParameterByRecurit("entrys_idCardIssued",res.preEntryInfo.talent.idCardIssueOrg,false);//身份证签发机关
						_self.initParameterByRecurit("entrys_idCardLongEffect",res.preEntryInfo.talent.idCardLongEffect,false,"checkbox");//身份证长期有效
						if(res.preEntryInfo.talent.gender != undefined){
							_self.initParameterByRecurit('entrys_gender',res.preEntryInfo.talent.gender.value,true);//性别
						}
						if (null != res.preEntryInfo.talent.zodiac) {
							_self.initParameterByRecurit('entrys_zodiac',res.preEntryInfo.talent.zodiac,true);//属相
						}
						if (null != res.preEntryInfo.talent.constellation) {
							_self.initParameterByRecurit('entrys_constellation',res.preEntryInfo.talent.constellation,true);//星座
						}
						
						_self.setHeadUpload();//在后台复制一份再入职的头像后，这里再初始化头像
					}
					_self.getField("entrys.preEntryID").val(selectId);//隐藏预入职ID字段发送至后端
					if (res.errorMsg){
						shr.showError({
            				message: data.errorMsg,
            				hideAfter: null
            			});
					}else if (res.personExist && res.personExist=="true") {
                    	$("#entrys_enrollAgain").val(1);
                        if(res.personOldInfo){
                			//编码,如果使用原编码，才设置原编码
                        	//osf取回来的 data.personOldInfo.useOldNumber 值为枚举值{0:使用原编码, 1：使用新编码}
                            if(!res.personOldInfo.useOldNumber && res.personOldInfo.empNumber){
                                $("#entrys_empNumber").val(res.personOldInfo.empNumber);
                        		$("#entrys_useOldNumber").val(1);
                        		_self.getField('entrys_empNumber').shrTextField('disable');
                            }
                            else if(res.personOldInfo.useOldNumber){
                            	$("#entrys_useOldNumber").val(0);
                            }
                            //参加工作日期
                            if (res.personOldInfo.jobStartDate) {
                                $("#entrys_jobStartDate").shrDateTimePicker('setValue', res.personOldInfo.jobStartDate);
                            }
                        }
                        _self.swapHrbizDefineInfo(_self._hrBizdefine_enrollAgain);
                        _self.getField("entrys.hrBizDefine").shrPromptBox("setFilter", _self._default_hrBizDefine_filter+" and bizDefineType = 16");//只能选择再入职变动操作
                        _self.showEnrollAgainMsg(res);
					}
					//职位放在加载再入职信息之后，避免再入职使用原编码时又按职位刷编码
					_self.initParameterByRecurit("entrys_position",res.preEntryInfo.position,true);//入职职位
					//根据入职日期填写入职时间
					var effdt=_self.getField('entrys.bizDate').shrDateTimePicker("getValue");
	 	 			_self.getField('entrys.bizTime').shrDateTimePicker('setValue', effdt);
				}
			}
		});
	},
	//初始化组织层级相关F7 状态，避免客户自定义配置可编辑,2021-10-18改造为可编辑
	initAdminOrgUnitLayerF7:function(){
		var self = this;
		self.getField("entrys.adminOrg").shrPromptBox("disable");
		self.getField("entrys.company").shrPromptBox("disable");
		self.getField("entrys.job").shrPromptBox("disable");
//		self.getField("entrys.position.adminOrgUnit.department").shrPromptBox("disable");
		self.getField("entrys.position.adminOrgUnit.office").shrPromptBox("disable");
		self.getField("entrys.position.adminOrgUnit.levelFourGroup").shrPromptBox("disable");
	},
	/**
	 * 2021-10-18hj 入职部门/项目绑定改变事件
	 */
	departmentChangeEvent:function(){
		var self = this;
		if (self.getOperateState() != 'VIEW') {
			self.getField("entrys.position.adminOrgUnit.department").shrPromptBox("option",{
				verifyBeforeOpenCallback: function(event){
					var $hrOrgUnit = self.getField("hrOrgUnit");
					var hrOrgUnitF7Value = $hrOrgUnit.shrPromptBox("getValue");
					if(!hrOrgUnitF7Value || hrOrgUnitF7Value.id == ""){
						shr.showError({
							message:jsBizMultLan.emp_shrAffairBillBaseHrManEdit_i18n_10
						});
						return false;
					}
				}
			});
			//绑定change事件
			self.getField("entrys.position.adminOrgUnit.department").shrPromptBox("option",{
				onchange : function(e, value) {
					//当前信息不等于空的时候再去设置过滤条件
					if(value.current!=undefined){
						var adminOrgId = value.current.id;
						//给职位设置过滤条件
						var filter = "adminOrgUnit.id = '" + adminOrgId+ "'";
						var $hrOrgUnit = self.getField("hrOrgUnit");
						var hrOrgUnitF7Value = $hrOrgUnit.shrPromptBox("getValue");
						filter += " and bizManageType.id='"+self._bizManageTypeId+"' and hrOrgUnit.id = '"+hrOrgUnitF7Value.id+"' and adminOrgBURelation.state = 1";
						self.getField("entrys.position").shrPromptBox("setFilter",filter);
					}
				}
			});
		}
	},
	//2021-10-19职位改变事件
	positionF7ChangeEvent:function(){
		var self = this;
		if (self.getOperateState() != 'VIEW') {
			
			self.getField("entrys.position").shrPromptBox("option",{
				verifyBeforeOpenCallback: function(event){
					var $hrOrgUnit = self.getField("hrOrgUnit");
					var hrOrgUnitF7Value = $hrOrgUnit.shrPromptBox("getValue");
					if(self.isPositionF7FilterByHROrg()){
						if(!hrOrgUnitF7Value || hrOrgUnitF7Value.id == ""){
							shr.showError({
								message:jsBizMultLan.emp_shrAffairBillBaseHrManEdit_i18n_10
							});
							return false;
						}
					}
					//如果只有 按 HR组织 过滤的职位才有左树右表，则需要把下面这段代码挪到上面的if块里面去，而且不按HR组织过滤职位的单据视图要去掉左树右表配置。
					//将人事业务组织id,权限项传给后台，过滤职位F7左树右表左边树
					var $position = self.getField("entrys.position");
					var hrOrgUnitIdVal = hrOrgUnitF7Value? hrOrgUnitF7Value.id : "";
					if(!self.isPositionF7FilterByHROrg()){
						hrOrgUnitIdVal = "";
					}
					var positionUipk = $position.shrPromptBox("option").subWidgetOptions.uipk;
					$position.shrPromptGrid();
					$position.shrPromptGrid('option',{
						leftTreeOtherParam:{
							hrOrgUnitIdVal:hrOrgUnitIdVal,
							currentPagePermItemId:shr.getCurrentPagePermItemId(),
							positionUipk:positionUipk
						}
					});
				}
			});
			
			self.getField("entrys.position").shrPromptBox("option",{
				onchange : function(e, value) {
				 	if(!value.current || !value.current.id){
				 		self.clearPositionAndAssociateF7();
				 		return false;
				 	}
				 	var positionId = value.current.id;
				 	var positionAndAdminOrgData = self.getPositionById(positionId);
 					var adminOrgUnitDetailData = self.getAdminOrgUnitDetailData(positionId);
					var isValidate = self.validatePositionF7(positionAndAdminOrgData,adminOrgUnitDetailData);
					if(isValidate){
						self.updateAdminF7Data(positionAndAdminOrgData,adminOrgUnitDetailData);
						//2021-10-19hj更新岗位序列信息
						self.setProfessionalPosition_positionClassValue(value);
						//职务体系信息
						if(self.isRelatePositionJobRange){
							shr.callService({
								serviceName : 'getJobInfosByPositionService',
								param : {
									"positionId" : positionId
								},
								async:true,//职层职等可以异步更新
								success: function(data) {
									if(self.isUpdateJobGradeAndJobLevelF7ByPosition()){
										self.updateJobGradeAndJobLevel(positionId,data);
									}
								}
							});
							self.getField("entrys.jobGrade").shrPromptBox("setValue", null);
							self.getField("entrys.jobLevel").shrPromptBox("setValue", null);
						}
						
					}else{
						self.clearPositionAndAssociateF7();
						return false;//需要在onchange中返回false,否则F7还是设置值
					}	
				}//end onchange
			});
		}
	},
	/**
	 * 通过职位信息设置岗位序列的值
	 */
	setProfessionalPosition_positionClassValue:function(value){
		var jobGradeModuleName = value.current["jobGradeModule.name"];
		var jobGradeModuleId = value.current["jobGradeModule.id"];
		$("#entrys_professionalPosition_positionClass").shrPromptBox('setValue',
			{id:jobGradeModuleId,name:jobGradeModuleName});
	}

});
/**
 * 调薪申请表
 *
 * @name: shr.custom.bizbill.DepCustomBillEditEx
 * @model: com.kingdee.shr.cmpdesign.app.CmpAdjstBill
 * @uipk: com.kingdee.shr.cmpdesign.app.CmpAdjstBill.form
 * @author: xy
 * @date: 2021/4/13 17:58
 */
shr.defineClass(
    "csce.shr.SalaryChangeApplication",
    shr.custom.bizbill.DepCustomBillEdit, {
        initalizeDOM: function () {
            csce.shr.SalaryChangeApplication.superClass.initalizeDOM.call(this);
            const _self = this;
            const formType = _self.getOperateState().toUpperCase();
            if ('VIEW' !== formType) {
                _self.gridChange();
            }
        },

        gridChange: function() {
            const _self = this;
            const grid = waf("#entrys");
            grid.wafGrid("option", {
                afterEditCell: function(afterSrowid, cellname, value, iRow, iColaveCell) {
                    if ("person" === cellname) {
                        _self.setPersonFilter(iRow);
                    }
                },
                afterSaveCell: function (
                    afterSrowid,
                    cellname,
                    value,
                    iRow,
                    iColaveCell
                ) {
                    if ("comAdjType" === cellname || 'person' === cellname) {
                        _self.updateOrCalculateSalaryInfo(afterSrowid);
                    } else {
                        _self.calcSalaryInfo(afterSrowid);
                    }
                },
            });
        },

        setPersonFilter: function (rowIndex) {
            let cellId = rowIndex + '_person';
            const currentCell = $('#' + cellId);
            const dept = currentCell.parent().parent().parent().parent().prev().html();
            let filter = ''
            if (dept !== '') {
                filter = "adminOrgUnit.name='" + dept + "'";
                currentCell.shrPromptBox('setFilter', filter);
            }
        },

        updateOrCalculateSalaryInfo: function (rowId) {
            const _self = this;
            const grid = waf("#entrys");
            const type = grid.jqGrid('getCell', rowId, "comAdjType").name;
            if (type === '调薪前') {
                _self.updateSalaryInfo(rowId);
            } else {
                _self.cleanSalaryInfo(rowId);
            }
        },

        cleanSalaryInfo: function(rowId) {
            const grid = waf("#entrys");
            grid.jqGrid('setCell', rowId, "anlWage", 0);
            grid.jqGrid('setCell', rowId, "floatAnlWage", 0)
            grid.jqGrid('setCell', rowId, "floatPercent", 0);
            grid.jqGrid('setCell', rowId, "BgAnlWage", 0);
            grid.jqGrid('setCell', rowId, "anlBnft", 0);
            grid.jqGrid('setCell', rowId, "AnlIncmBg", 0);
            grid.jqGrid('setCell', rowId, "anlAllwnc", 0);
            grid.jqGrid('setCell', rowId, "grpJobGrade", '');
            grid.jqGrid('setCell', rowId, "JobGrade", '')
            grid.jqGrid('setCell', rowId, "wageRank", 0);
            grid.jqGrid('setCell', rowId, "ovrseaAllwncRatio", 0);
            grid.jqGrid('setCell', rowId, "ovrseaAllwnc", 0)
            grid.jqGrid('setCell', rowId, "adPstnWage", 0);
            grid.jqGrid('setCell', rowId, "mnthBasicWage", 0);
            grid.jqGrid('setCell', rowId, "siteAllwnc", 0)
            grid.jqGrid('setCell', rowId, "EnAllwnc", 0);
            grid.jqGrid('setCell', rowId, "FrAllwnc", 0);
            grid.jqGrid('setCell', rowId, "pAllwnc", 0)
            grid.jqGrid('setCell', rowId, "trvlMedComAllwnc", 0);
            grid.jqGrid('setCell', rowId, "monthBnft", 0);
            grid.jqGrid('setCell', rowId, "monthWage", 0);
            grid.jqGrid('setCell', rowId, "effectDate", 0);
        },

        calcSalaryInfo: function(rowId) {
            const _self = this;
            const grid = waf("#entrys");
            const personId = grid.jqGrid('getCell', rowId, 'person').id;
            if (personId === '') {
                _self.cleanSalaryInfo(rowId)
            } else {
                // todo 年收入预算计算 AnlIncmBg
                const floatPercent = grid.jqGrid('getCell', rowId, "floatPercent");
                const anlBnft = grid.jqGrid('getCell', rowId, "anlBnft")
                const wageRank = grid.jqGrid('getCell', rowId, "wageRank");
                const ovrseaAllwncRatio = grid.jqGrid('getCell', rowId, "ovrseaAllwncRatio");
                const adPstnWage = grid.jqGrid('getCell', rowId, "adPstnWage");
                const siteAllwnc = grid.jqGrid('getCell', rowId, "siteAllwnc")
                const EnAllwnc = grid.jqGrid('getCell', rowId, "EnAllwnc");
                const FrAllwnc = grid.jqGrid('getCell', rowId, "FrAllwnc");
                const pAllwnc = grid.jqGrid('getCell', rowId, "pAllwnc")
                const trvlMedComAllwnc = grid.jqGrid('getCell', rowId, "trvlMedComAllwnc");
                const monthBnft = grid.jqGrid('getCell', rowId, "monthBnft");
                const param = {
                    'ratio': floatPercent,
                    'annualReward': anlBnft,
                    'baseRankSalary': wageRank,
                    'overseaAllowanceCoefficient': ovrseaAllwncRatio,
                    'managementPositionSalary': adPstnWage ,
                    'siteAllowance': siteAllwnc,
                    'englishAllowance': EnAllwnc,
                    'franceAllowance': FrAllwnc,
                    'specialProfessionalAllowance': pAllwnc,
                    'transportationAllowance': trvlMedComAllwnc,
                    'monthlyReward': monthBnft
                };
                _self.remoteCall({
                    type: "post",
                    method: "calculateSalaryInfo",
                    param: param,
                    async: false,
                    success: function (res) {
                        _self.setSalaryInfo(rowId, res);
                    }
                })

            }
        },

        updateSalaryInfo: function(rowId) {
            const _self = this;
            const grid = waf("#entrys");
            const personId = grid.jqGrid('getCell', rowId, 'person').id;
            if (personId === '') {
                _self.cleanSalaryInfo(rowId)
            } else {
                _self.remoteCall({
                    type: "post",
                    method: "getCurrentSalaryInfo",
                    param: {personId: personId},
                    async: false,
                    success: function (res) {
                        _self.setSalaryInfo(rowId, res);
                        grid.jqGrid('setCell', rowId, "JobGrade", res.positionRankEntity)
                        grid.jqGrid('setCell', rowId, "grpJobGrade", res.innerPositionRankEntity);
                        grid.jqGrid('setCell', rowId, "effectDate", res.effectDate);
                    },
                });
            }
        },

        setSalaryInfo: function (rowId, res) {
            const grid = waf("#entrys");
            grid.jqGrid('setCell', rowId, "anlWage", res.baseAnnualSalary);
            grid.jqGrid('setCell', rowId, "floatAnlWage", res.floatingAnnualSalary)
            grid.jqGrid('setCell', rowId, "floatPercent", res.ratio);
            grid.jqGrid('setCell', rowId, "BgAnlWage", res.annualSalaryBudget);
            grid.jqGrid('setCell', rowId, "anlBnft", res.annualReward)
            grid.jqGrid('setCell', rowId, "anlAllwnc", res.annualAllowance);
            grid.jqGrid('setCell', rowId, "wageRank", res.baseRankSalary);
            grid.jqGrid('setCell', rowId, "ovrseaAllwncRatio", res.overseaAllowanceCoefficient);
            grid.jqGrid('setCell', rowId, "ovrseaAllwnc", res.overseaAllowance)
            grid.jqGrid('setCell', rowId, "adPstnWage", res.managementPositionSalary);
            grid.jqGrid('setCell', rowId, "mnthBasicWage", res.baseMonthlySalary);
            grid.jqGrid('setCell', rowId, "siteAllwnc", res.specialProfessionalAllowance)
            grid.jqGrid('setCell', rowId, "EnAllwnc", res.englishAllowance);
            grid.jqGrid('setCell', rowId, "FrAllwnc", res.franceAllowance);
            grid.jqGrid('setCell', rowId, "pAllwnc", res.specialProfessionalAllowance)
            grid.jqGrid('setCell', rowId, "trvlMedComAllwnc", res.transportationAllowance);
            grid.jqGrid('setCell', rowId, "monthBnft", res.monthlyReward);
            grid.jqGrid('setCell', rowId, "monthWage", res.monthlySalary);
            grid.jqGrid('setCell', rowId, "AnlIncmBg", res.annualIncomeBudget);
        },

        getRowData: function(rowIndex) {
            const grid = waf("#entrys");
            const gridData = grid.jqGrid("getAllRowData");
            for (let i = 0; i < gridData.length && i < rowIndex; i++) {
                return gridData[i];
            }
            return null;
        }
    }
);

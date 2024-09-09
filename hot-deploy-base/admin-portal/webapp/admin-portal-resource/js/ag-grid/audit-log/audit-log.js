//$("#loader").show();
//$(function() {
//	loadAgGrid();
//});
//
//var columnDefs = [
//	{ 
//        "headerName":"Entity",
//        "field":"changedEntityName",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"Record ID",
//        "field":"auditHistorySeqId",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"Field",
//        "field":"changedFieldName",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"Old Value",
//        "field":"oldValueText",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"New Value",
//        "field":"newValueText",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"Modified On",
//        "field":"changedDate",
//        "editable":true,
//        "cellEditor":null
//     },
//     { 
//        "headerName":"Modified By",
//        "field":"changedByInfo",
//        "editable":true,
//        "cellEditor":null,
//        "cellEditorParams":null
//     }
//    ];
//
//function loadAgGrid(){
//	$("#grid1").empty();
//	var gridOptions = {
//		    defaultColDef: {
//		        filter: true,
//		        sortable: true,
//		        resizable: true,
//		        // allow every column to be aggregated
//		        //enableValue: true,
//		        // allow every column to be grouped
//		        //enableRowGroup: true,
//		        // allow every column to be pivoted
//		        //enablePivot: true,
//		    },
//		    columnDefs: columnDefs,
//		    rowData: getGridData(),
//		    floatingFilter: true,
//		    rowSelection: "multiple",
//		    editType: "fullRow",
//		    paginationPageSize: 10,
//		    domLayout:"autoHeight",
//		    pagination: true,
//		    onFirstDataRendered: onFirstDataRendered
//		};
//
//		//lookup the container we want the Grid to use
//		var eGridDiv = document.querySelector("#grid1");
//
//		// create the grid passing in the div to use together with the columns & data we want to use
//		new agGrid.Grid(eGridDiv, gridOptions);
//
//}
//function getGridData() {
//    var result = [];
//    var resultRes = null;
//    var params = {}
//    var paramStr = $("#searchForm").serialize();
//    /*$('#searchForm :input:hidden').each(function(){
//    	params[this.name] = this.value;
//    }); */
//    console.log("formData--->"+JSON.stringify(paramStr));
//    var fromData = JSON.stringify(paramStr);
//    $.ajax({
//        type: "POST",
//        url: "getAuditConfigurations",
//        async: false,
//        data: JSON.parse(fromData),
//        success: function(data) {
//            resultRes = data;
//            result.push(data);
//            console.log("--result-----" + result);
//        }
//    });
//    return resultRes;
//}
//$("#loader").hide();
////data binding while loading the page
//function onFirstDataRendered(params) {
//    params.api.sizeColumnsToFit();
//}
//function clearData() {
//    gridOptions.api.setRowData([]);
//}
//
//function onAddRow() {
//    var newItem = createNewRowData();
//    var res = gridOptions.api.updateRowData({
//        add: [newItem]
//    });
//    printResult(res);
//}
//
//function addItems() {
//    var newItems = [createNewRowData(), createNewRowData(), createNewRowData()];
//    var res = gridOptions.api.updateRowData({
//        add: newItems
//    });
//    printResult(res);
//}
//
//function addItemsAtIndex() {
//    var newItems = [createNewRowData(), createNewRowData(), createNewRowData()];
//    var res = gridOptions.api.updateRowData({
//        add: newItems,
//        addIndex: 2
//    });
//    printResult(res);
//}
//
//function updateItems() {
//    // update the first 5 items
//    var itemsToUpdate = [];
//    gridOptions.api.forEachNodeAfterFilterAndSort(function(rowNode, index) {
//        // only do first 5
//        if (index >= 5) {
//            return;
//        }
//
//        var data = rowNode.data;
//        data.price = Math.floor((Math.random() * 20000) + 20000);
//        itemsToUpdate.push(data);
//    });
//    var res = gridOptions.api.updateRowData({
//        update: itemsToUpdate
//    });
//    printResult(res);
//}
//
//function onInsertRowAt2() {
//    var newItem = createNewRowData();
//    var res = gridOptions.api.updateRowData({
//        add: [newItem],
//        addIndex: 2
//    });
//    printResult(res);
//}
//
//function onRemoveSelected() {
//    var selectedData = gridOptions.api.getSelectedRows();
//    console.log(selectedData.length);
//    for (i = 0; i <= selectedData.length; i++) {
//    }
//    var res = gridOptions.api.updateRowData({
//        remove: selectedData
//    });
//
//    printResult(res);
//}
//
//function removeSubmit() {
//    var selectedData = gridOptions.api.getSelectedRows();
//}
//
//function printResult(res) {
//    console.log('---------------------------------------')
//    if (res.add) {
//        res.add.forEach(function(rowNode) {
//            console.log('Added Row Node', rowNode);
//        });
//    }
//    if (res.remove) {
//        res.remove.forEach(function(rowNode) {
//            console.log('Removed Row Node', rowNode);
//        });
//    }
//}
//
//
//
//
//function sizeToFit() {
//    gridOptions.api.sizeColumnsToFit();
//}
//
//function getSelectedRows() {
//    const selectedNodes = gridOptions.api.getSelectedNodes()
//    const selectedData = selectedNodes.map(function(node) {
//        return node.data
//    })
//    const selectedDataStringPresentation = selectedData.map(function(node) {
//        return node.Owner + ' ' + node.Date_Due
//    }).join(', ')
//    alert('Selected nodes: ' + selectedDataStringPresentation);
//}
//
//function onBtExport() {
//    var params = {
//        skipHeader: false,
//        allColumns: true,
//        fileName: "Audit_Log_Entity",
//        exportMode: 'csv'
//    };
//
//
//    gridOptions.api.exportDataAsCsv(params);
//}


$(function() {
let auditLogInstanceId= "AUDIT_LOG";
let gridInstance  = "";
var externalLoginKey = $('#externalLoginKey').val();
var userId = $("#userId").val();

const formDataObject = {};
formDataObject.gridInstanceId = auditLogInstanceId;
formDataObject.externalLoginKey = externalLoginKey;
formDataObject.userId = userId;	

gridInstance = prepareGridInstance(formDataObject);

$('#audit-save-pref-btn').click(function(){
	saveGridPreference(gridInstance, auditLogInstanceId, userId);
});
$('#audit-clear-filter-btn').click(function(){
	clearGridPreference(gridInstance, auditLogInstanceId, userId);
	if (gridInstance) {
		gridInstance.destroy();
	}
	gridInstance = prepareGridInstance(formDataObject);
	if(gridInstance){
		getAuditLogGridData();
	}
});
$("#audit-export-btn").click(function () {
  gridInstance.exportDataAsCsv();
});
$("#main-search-btn").click(function () {
	getAuditLogGridData();
});
$("#searchForm").on("keypress", function (event) {
	var keyPressed = event.keyCode || event.which; 
	if (keyPressed === 13) { 
		getAuditLogGridData();
		event.preventDefault();
		return false; 
	} 
}); 
function getAuditLogGridData(){
	gridInstance.showLoadingOverlay();
	const callCtx = {};
	callCtx.ajaxUrl = "/admin-portal/control/getAuditConfigurations";
	callCtx.externalLoginKey = externalLoginKey;
	callCtx.formId = "#searchForm, #limitForm_AUDIT_LOG";
	callCtx.ajaxResponseKey = "data";

	setGridData(gridInstance, callCtx);
}
if(gridInstance){
	getAuditLogGridData();
}
});

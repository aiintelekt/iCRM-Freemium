$("#loader1").show();
$(function() {
	//loadActivityGrid();
});

var columnDefsActivity = [{ 
	"headerName":"Activity Number",
	"field":"activity",
	"sortable":false,
	"filter":false,
	"filter":"agTextColumnFilter",
	"checkboxSelection":false,
	cellRenderer:params => `<a href="viewActivity?workEffortId=${params.data.workEffortId}">${params.value}</a>`
},


{ 
	"headerName":"Activity Type",
	"field":"activityType",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"
},
{ 
	"headerName":"Activity Sub Type",
	"field":"activitySubType",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},
{ 
	"headerName":"Regarding Id",
	"field":"regardingId",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},

{  //rowData: getRowDataActivity(),
	"headerName":"Customer Name",
	"field":"customerName",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},

{ 
	"headerName":"Customer CIN",
	"field":"customerCIN",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},
{ 
	"headerName":"Prospect Name",
	"field":"prospectName",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},
{
	"headerName":"Prospect CIN",
	"field":"prospectCIN",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},
{
	"headerName":"Activity Status",
	"field":"status",
	"sortable":true,
	"filter":true,
	"filter":"agTextColumnFilter"

},
{
  	"headerName":"Owner",
  	"field":"owner",
  	"sortable":true,
  	"filter":true,
  	"filter":"agTextColumnFilter"

  },
{
    "headerName":"Created Date",
    "field":"createdDate",
    "sortable":true,
    "filter":true,
    "filter":"agTextColumnFilter"

 },
{
    "headerName":"Planned Date",
    "field":"plannedDate",
    "sortable":true,
    "filter":true,
    "filter":"agTextColumnFilter"

}
];

var gridOptionsActivity = null;
function loadActivityGrid(){
	$("#activityGrid").empty();
	gridOptionsActivity = {
			defaultColDef: {
				filter: false,
				sortable: false,
				resizable: true
			},
			columnDefs: columnDefsActivity,
			floatingFilter: true,
			rowSelection: "multiple",
			editType: "fullRow",
			paginationPageSize: 10,
			domLayout:"autoHeight",
			pagination: true,
			onGridReady: function() {
				getRowDataActivity();
			}
	}

	//lookup the container we want the Grid to use
	var eGridDiv = document.querySelector("#activityGrid");
	// create the grid passing in the div to use together with the columns & data we want to use
	new agGrid.Grid(eGridDiv, gridOptionsActivity);

}


function getAjaxResponseActivity(callback) {
	var data1;
	var result = [];
	var resultRes = null;
	const queryString = window.location.search;

	const urlParams = new URLSearchParams(queryString);
	var salesOpportunityId = urlParams.get("salesOpportunityId");


	var srNumber = urlParams.get('srNumber')
	var errorMessage = null;
	var resultData = null;
	$.ajax({
		type: "POST",
		url: "getActivityDataForOpportunity",
		async: false,
		data: { salesOpportunityId : salesOpportunityId },
		success: function(data) {
			var result1 = data[0];
			console.log(result1);
			if(data[0] != null || data[0] != undefined){
				errorMessage = data[0].errorMessage;
				resultData = data[0].errorResult;
			}
			if(errorMessage != null || errorMessage != undefined) {
				showAlert("error", errorMessage);
				callback(resultData);
			}else{
				callback(data);
			}

		},
		error: function() {
			console.log('Error occured');
			showAlert("error", "Error occured!");
		},
		complete: function() {
			//$('#loader').hide();
		}
	});
}

function getRowDataActivity() {
	var result;
	result = getAjaxResponseActivity(function(agdata) {
		gridOptionsActivity.api.setRowData(agdata);
	});
}

//data binding while loading the page
function onFirstDataRendered(params) {
	params.api.sizeColumnsToFit();
}
function clearData() {
	gridOptionsActivity.api.setRowData([]);
}



function onBtExport() {
	var params = {
			skipHeader: false,
			allColumns: true,
			fileName: "Find activity",
			exportMode: 'csv'
	};


	gridOptionsActivity.api.exportDataAsCsv(params);
}
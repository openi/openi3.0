/**
 * @author SUJEN
 */
var ComponentRenderer = {
	
	renderAllComponents : function(pivotID) {
		var components = {
			"table-container" : "TABLE",
			"navigator-container" : "NAVIGATOR",
			"chart-container" : "CHART",
			"slicer-value-container" : "SLICERVALUE",
			"chart-form-container" : "CHARTPROPERTIESFORM",
			"mdx-edit-form-container" : "MDXEDITFORM",
			"print-form-container" : "PRINTFORM",
			"sort-form-container" : "SORTFORM"
		};
		AjaxIndicator.showProcessing("Loading Analysis Components");
		ComponentRenderer.renderComponents(components, pivotID);
		AjaxIndicator.hideProcessing();
	},

	renderComponents : function(components, pivotID) {
		jQuery
				.each(
						components,
						function(componentContainer, componentType) {
							if (componentType == "CHART") {
								var actionPath = "wcfChartComp";
								var url = Rest.constructBaseURL() + Rest.WCF_COMPONENT_RESOURCE_PATH + actionPath;
								
								var chartWidth = jQuery("#table-container").width() - 20;
								var chartHeight = jQuery("#table-container").height() - 20;
								var type = jQuery("#chart-btn").text();
								var chartType =  1;
								chartType = jQuery("#chart-selection-menu").val();
								
								var dataParams = {
									"pivotID" : pivotID,
									"chartWidth" : chartWidth,
									"chartHeight" : chartHeight,
									"chartType" : chartType
								};
								var dataStr = "";
								jQuery
									.each(dataParams, function(paramName, paramValue) {
										dataStr += paramName + "=" + paramValue + "&";
									});
								url = url + "?" + dataStr + "timestamp=" + new Date().getTime();
								jQuery("#chart").removeAttr("src").attr("src", url);
								/*var requestType = "GET";
								var asyncType = false;
								var restResourcePath = Rest.WCF_COMPONENT_RESOURCE_PATH + actionPath;
								var result = Rest.sendRequest(restResourcePath, dataParams, requestType, asyncType);
								ComponentRenderer.renderResult(componentContainer, result);*/
								
							} 
							else if (componentType == "SLICERVALUE") {
								/*
								var requestType = "GET";
								var actionPath = "slicerValue";
								var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
								var asyncType = false;
								var dataParams = {
										"pivotID" : pivotID
									};
								var result = Rest.sendRequest(restResourcePath, dataParams, requestType, asyncType);
								ComponentRenderer.renderResult(componentContainer, result);
								*/
								
								var requestType = "GET";
								var actionPath = "slicerValueObjects";
								var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
								var asyncType = false;
								var dataParams = {
										"pivotID" : pivotID
									};
								var slicerValueObjects = Rest.sendRequest(restResourcePath, dataParams, requestType, asyncType);
								
								var slicerValueHTML = "<span class='slicerCnt' style='font-weight: bold;float: left;margin-left: 5px;margin-right: 5px;margin-top: 8px;'>Slicers : </span>";
								jQuery(slicerValueObjects)
										.each(
												function(index) {
													var selectedSlicerObj = slicerValueObjects[index];
													slicerValueHTML += "<div class='slicerValue slicerCnt'>"
															+ selectedSlicerObj.level
															+ ":"
															+ selectedSlicerObj.member
															+ "<a href='#' class='ui-dialog-titlebar-close ui-corner-all' role='button' onclick=\"OlapActions.removeSlicerSelection('"
															+ selectedSlicerObj.dimension
															+ "','"
															+ selectedSlicerObj.hierarchy
															+ "','"
															+ selectedSlicerObj.level
															+ "','"
															+ selectedSlicerObj.member
															+ "','"
															+ selectedSlicerObj.uniqueName
															+ "','"
															+ pivotID
															+ "'); return false;\"><span class='ui-icon ui-icon-closethick' style='float: right;margin-left: 12px;'>x</span></a></div>";
												});
								jQuery(".slicer-value-container").find(".slicerCnt").remove();
								jQuery(".slicer-value-container").append(slicerValueHTML);
								//ComponentRenderer.renderResult(componentContainer, slicerValueHTML);
							}
							else {
								var requestType = "GET";
								var actionPath = "wcfComp";
								var restResourcePath = Rest.WCF_COMPONENT_RESOURCE_PATH + actionPath;
								var asyncType = false;
								var dataParams = {
									"componentType" : componentType,
									"pivotID" : pivotID
								};
								
								var result = Rest.sendRequest(restResourcePath, dataParams, requestType, asyncType);
								ComponentRenderer.renderResult(componentContainer, result);
								
								jQuery("#" + componentContainer + " input[type='checkbox']").uniform();
								jQuery("#" + componentContainer + " input[type='radio']").uniform();
								jQuery("#" + componentContainer + " input[type='text']").uniform();
								jQuery("#" + componentContainer + " select").uniform();
								jQuery("#" + componentContainer + " textarea").uniform();
								
								if (componentType == "NAVIGATOR")
									OpenIAnalysis
											.registerNavigatorDNDHandlers(pivotID);
							}
						});
		OpenIAnalysis.adjustAxisCategorySize();
		OpenIAnalysis.adjustMemberNavigatorSize();
	},

	renderResult : function(resultContainer, resultData) {
		jQuery("#" + resultContainer).empty();
		jQuery("#" + resultContainer).html(resultData);
	}
}
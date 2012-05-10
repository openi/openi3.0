/**
 * Handles rest request/response for olap actions like swapaxes,
 * move category item, show hierarchy, drillthrough etc.
 * 
 * @author SUJEN
 */
var OlapActions = {
	
	swapAxes : function(pivotID, doSwap) {
		var requestType = "POST";
		var actionPath = "swapaxes";
		var queryParams = {
				"pivotID" : pivotID,
				"doSwap" : doSwap
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"navigator-container" : "NAVIGATOR",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	hideEmptyRowsCols : function(pivotID, doHide) {
		var requestType = "POST";
		var actionPath = "hideEmptyRowsCols";
		var queryParams = {
				"pivotID" : pivotID,
				"doHide" : doHide
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	setAxisStyle : function(pivotID, doSet) {
		var requestType = "POST";
		var actionPath = "setAxisStyle";
		var queryParams = {
				"pivotID" : pivotID,
				"doSet" : doSet
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	showHideHierarchy : function(pivotID, doShow) {
		var requestType = "POST";
		var actionPath = "showHideHierarchy";
		var queryParams = {
				"pivotID" : pivotID,
				"doShow" : doShow
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	enableDisableDrillthrough : function(pivotID, enableDrillThrough) {
		var requestType = "POST";
		var actionPath = "enableDisableDrillthrough";
		var queryParams = {
				"pivotID" : pivotID,
				"enableDrillThrough" : enableDrillThrough
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	enableDisableReplace : function(pivotID, enableReplace) {
		var requestType = "POST";
		var actionPath = "enableDisableReplace";
		var queryParams = {
				"pivotID" : pivotID,
				"enableReplace" : enableReplace
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	moveItem : function(pivotID, targetAxis, item, position) {
		var requestType = "POST";
		var actionPath = pivotID + "/targetAxis/" + targetAxis + "/hierarchyItem/" + item;
		var queryParams = {
				"position" : position
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"navigator-container" : "NAVIGATOR",
				"chart-container" : "CHART",
				"slicer-value-container" : "SLICERVALUE",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	drillExpandCollapse : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "drillExpandCollapse";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	drillThrough : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "drillThrough";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"drillthrough-table-container" : "DRILLTHROUGHTABLE"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	sort : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "sort";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	expandMemberTree : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "expandMemberTree";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"member-navigator-container" : "MEMBERNAVIGATOR",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	collapseMemberTree : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "collapseMemberTree";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"member-navigator-container" : "MEMBERNAVIGATOR",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	selectDeselectMember : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "selectDeselectMember";
		var elemID = element.id;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		/*
		var componentsToUpdate = {
				"member-navigator-container" : "MEMBERNAVIGATOR"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		*/
		return false;
	},
	
	applyMemberSelection : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "applyMemberSelection";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"chart-container" : "CHART",
				"table-container" : "TABLE",
				"slicer-value-container" : "SLICERVALUE",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		OpenIAnalysis.hideMemberNavigator();
		return false;
	},
	
	revertMemberSelection : function(element, pivotID) {
		var requestType = "POST";
		var actionPath = "revertMemberSelection";
		var elemID = element.name;
		var queryParams = {
				"pivotID" : pivotID,
				"elementID" : elemID
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"member-navigator-container" : "MEMBERNAVIGATOR",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		OpenIAnalysis.hideMemberNavigator();
		return false;
	},
	
	removeSlicerSelection : function(dimensionName, hierarchyName, levelName, memberName, slicerUniqueName, pivotID) {
		var requestType = "POST";
		var actionPath = "removeSlicerSelection";
		var queryParams = {
				"pivotID" : pivotID,
				"dimensionName" : encodeURIComponent(dimensionName),
				"hierarchyName" : encodeURIComponent(hierarchyName),
				"levelName" : encodeURIComponent(levelName),
				"memberName" : encodeURIComponent(memberName),
				"slicerUniqueName" : encodeURIComponent(slicerUniqueName)
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"slicer-value-container" : "SLICERVALUE",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
		return false;
	},
	
	applyMDX : function(pivotID, newMDXQuery) {
		var requestType = "POST";
		var actionPath = "applyMDX";
		var queryParams = {
				"pivotID" : pivotID,
				"newMDXQuery" : encodeURIComponent(newMDXQuery)
		};
		
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"navigator-container" : "NAVIGATOR",
				"chart-container" : "CHART",
				"slicer-value-container" : "SLICERVALUE"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	},
	
	applySortProperties : function(element, pivotID) {
		var sortMode = jQuery("#sortForm-sort-mode-select-menu").val();
		var rowsCount = jQuery("#sortForm-rows-count-input").val();
		var isChecked = jQuery("#sortForm-show-props-chk-box").attr("checked");
		var showProps = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			showProps = true;
		}
		
		var requestType = "POST";
		var actionPath = "applySortProperties";
		var queryParams = {
				"pivotID" : pivotID,
				"sortMode" : sortMode,
				"rowsCount" : rowsCount,
				"showProps" : showProps
		};
		var asyncType = false;
		var restResourcePath = Rest.QUERY_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, queryParams, requestType, asyncType);
		if(status == "Error")
			return false;
		var componentsToUpdate = {
				"table-container" : "TABLE",
				"chart-container" : "CHART",
				"slicer-value-container" : "SLICERVALUE",
				"mdx-edit-form-container" : "MDXEDITFORM"
		};
		ComponentRenderer.renderComponents(componentsToUpdate, pivotID);
	} 

}
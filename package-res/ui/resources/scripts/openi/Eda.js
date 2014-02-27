/**
 * @author SUJEN
 */
var Eda = {

	loadEDAWidgets : function(edaWidgets) {
		jQuery("#eda-main-container").empty();
		jQuery(edaWidgets)
				.each(
						function(index, edaWidget) {
							var edaWidgetTitle = edaWidget.widgetTitle;
							var datasourceType = edaWidget.datasourceType;
							var datasource = edaWidget.datasource;
							var cube = edaWidget.cube;
							var edaWidgetContentQuery = edaWidget.widgetContentQuery;
							var edaWidgetID = "eda-widget-" + index;
							var edaWidgetCntID = "eda-widget-" + index + "-cnt";
							var edaWidgetCntQueryID = "eda-widget-" + index
									+ "-cnt-query";
							var widgetHTML = "<div id='" + edaWidgetID
									+ "'  class='eda-widget'>";
							widgetHTML += "<input type='hidden' name='"
									+ edaWidgetCntQueryID + "' id='"
									+ edaWidgetCntQueryID + "' value='"
									+ edaWidgetContentQuery + "'/>";
							widgetHTML += "<div class='eda-widget-hdr'>";
							widgetHTML += edaWidgetTitle;
							widgetHTML += "</div>";
							widgetHTML += "<div id='" + edaWidgetCntID + "'>";
							widgetHTML += "</div>";
							widgetHTML += "</div>";
							jQuery("#eda-main-container").append(widgetHTML);
							jQuery("#" + edaWidgetCntID)
									.html(
											'<div style="margin-left: 180px; margin-top: 120px;"><img src="images/ajax-loader.gif" /></div><div style="text-align: center; padding: 10px;">Loading,  Please Wait.......</div>');
							Eda.loadEDAWidgetContent(edaWidgetID, edaWidgetCntID,
									datasourceType, datasource, cube,
									edaWidgetContentQuery, edaWidget);
						});
	},

	loadEDAWidgetContent : function(edaWidgetID, edaWidgetCntID,
			datasourceType, datasource, cube, edaWidgetContentQuery, edaWidget) {
		var requestType = "GET";
		var actionPath = "edaWidgetContent";

		/*
		 * var edaWidgetCntWidth = jQuery("#" + edaWidgetCntID).width(); var
		 * edaWidgetCntHeight = jQuery("#" + edaWidgetCntID).height();
		 */
		var edaWidgetCntWidth = 400;
		var edaWidgetCntHeight = 310;
		var dataParams = {
			"edaWidgetTitle" : edaWidget.widgetTitle,
			"datasourceType" : datasourceType,
			"datasourceName" : datasource,
			"cube" : cube,
			"edaWidgetContentQuery" : edaWidgetContentQuery,
			"edaWidgetCntWidth" : edaWidgetCntWidth,
			"edaWidgetCntHeight" : edaWidgetCntHeight
		};

		var asyncType = false;
		var restResourcePath = Rest.EDA_RESOURCE_PATH + actionPath;

		var dataStr = "";
		jQuery.each(dataParams, function(paramName, paramValue) {
			dataStr += paramName + "=" + paramValue + "&";
		});
		var url = Rest.constructPluginResourceBaseURL() + restResourcePath + "?" + dataStr;
		var img = new Image();
		jQuery(img).load(function() {
			jQuery("#" + edaWidgetCntID).empty().append(this);
		}).error(
				function() {
					jQuery("#" + edaWidgetCntID).empty().append(
							"Error in loading " + edaWidget.widgetTitle);
				}).click(
				function() {
					window.location.href = Rest.constructPluginContentBaseURL()
							+ "/openi/RenderOAnalysis?datasourceType="
							+ datasourceType + "&datasource=" + datasource
							+ "&cube=" + cube + "&mdx=" + encodeURIComponent(edaWidgetContentQuery)
							+ "&actionType=new";
				}).css('cursor', 'pointer').attr('src', url);
		/*
		 * jQuery .ajax({ type : requestType, url : url, data: dataStr, async:
		 * asyncType, success : function(successResult) { jQuery("#" +
		 * edaWidgetCntID).empty().html(successResult); }, error:
		 * function(errorResult) { jQuery("#" +
		 * edaWidgetCntID).empty().html(errorResult.responseText); } });
		 */
	}
}
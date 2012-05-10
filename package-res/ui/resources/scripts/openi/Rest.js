/**
 * @author SUJEN
 */
var Rest = {
		
	CONTEXT_PATH : "/pentaho",
	
	QUERY_RESOURCE_PATH : "/openianalytics/api/queryResource/",
	
	ANALYSIS_RESOURCE_PATH : "/openianalytics/api/analysisResource/",
	
	DATASOURCE_RESOURCE_PATH : "/openianalytics/api/datasourceResource/",
	
	OLAP_DISCOVER_RESOURCE_PATH : "/openianalytics/api/discoverResource/",
	
	WCF_COMPONENT_RESOURCE_PATH : "/openianalytics/api/wcfCompResource/",
	
	EDA_RESOURCE_PATH : "/openianalytics/api/exploreDataResource/",
	
	sendRequest: function(restResourcePath, resourceParams, restRequestType, restAsyncType) {
		var restURL = Rest.constructBaseURL() + restResourcePath;
	    return Ajax.sendRequest(restRequestType, restURL, resourceParams, restAsyncType);
	},
	
	constructBaseURL : function() {
		var protocol = window.location.protocol;
		var host = window.location.host;
		var baseURL = protocol + "//" + host + Rest.CONTEXT_PATH + "/content";
		return baseURL;
	}

}
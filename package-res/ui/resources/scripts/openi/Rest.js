/**
 * @author SUJEN
 */
var Rest = {
		
	CONTEXT_PATH : "/pentaho",
	
	QUERY_RESOURCE_PATH : "/openi/api/queryResource/",
	
	ANALYSIS_RESOURCE_PATH : "/openi/api/analysisResource/",
	
	DATASOURCE_RESOURCE_PATH : "/openi/api/datasourceResource/",
	
	OLAP_DISCOVER_RESOURCE_PATH : "/openi/api/discoverResource/",
	
	WCF_COMPONENT_RESOURCE_PATH : "/openi/api/wcfCompResource/",
	
	EDA_RESOURCE_PATH : "/openi/api/exploreDataResource/",
	
	REPO_RESOURCE_PATH : "/openi/api/solutionRepoResource/",
	
	sendRequest: function(restResourcePath, resourceParams, restRequestType, restAsyncType) {
		var restURL = Rest.constructPluginResourceBaseURL() + restResourcePath;
	    return Ajax.sendRequest(restRequestType, restURL, resourceParams, restAsyncType);
	},
	
	constructPluginContentBaseURL : function() {
		var protocol = window.location.protocol;
		var host = window.location.host;
		var baseURL = protocol + "//" + host + Rest.CONTEXT_PATH + "/content";
		return baseURL;
	},
	
	constructPluginResourceBaseURL : function() {
		var protocol = window.location.protocol;
		var host = window.location.host;
		var baseURL = protocol + "//" + host + Rest.CONTEXT_PATH + "/plugin";
		return baseURL;
	}

}
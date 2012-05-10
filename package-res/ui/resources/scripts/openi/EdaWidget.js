/**
 * @author SUJEN
 * 
 * @param widgetTitle
 * @param datasourceType
 * @param datasource
 * @param cube
 * @param widgetContentQuery
 * @returns
 */
var EdaWidget = function(widgetTitle, datasourceType, datasource, cube, widgetContentQuery) {
	this.widgetTitle = widgetTitle;
	this.datasourceType = datasourceType;
	this.datasource = datasource;
	this.cube = cube;
	this.widgetContentQuery = widgetContentQuery;
};
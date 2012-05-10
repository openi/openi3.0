/**
 * @author SUJEN
 */
var Ajax = {
	sendRequest: function(requestType, url, dataParams, async) {
		var dataStr = "";
		jQuery
			.each(dataParams, function(paramName, paramValue) {
				dataStr += paramName + "=" + paramValue + "&";
			});
		jQuery
			.ajax({
				type : requestType,
				url : url,
				data: dataStr,
				async: async,
				success : function(successResult) {
					result = successResult;
				},
				error: function(errorResult) {
					jQuery("#error-message-container").empty().html(errorResult.responseText);
					jQuery("#error-message-dialog").dialog('open');
					result = "Error";
				}
			});
		return result;
	}
}
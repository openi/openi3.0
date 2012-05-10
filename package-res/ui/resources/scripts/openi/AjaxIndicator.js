/**
 * @author SUJEN
 */
var AjaxIndicator = {
	showProcessing : function(msg) {
		jQuery.unblockUI();
		jQuery
				.blockUI({
					message : '<div class="processing"><div class="ajaxIndicator">&nbsp;</div><div>'
							+ msg + '</div></div>',
					overlayCSS : {
						backgroundColor : '#FFF',
						opacity : 0.5
					}
				});
	},
	
	hideProcessing : function(block_div) {
		jQuery.unblockUI();
	}
}
/**
 * initializes OpenIAnalysis, i.e. initializes UI components, registers olap
 * action handlers, navigator dnd handlers and so on....
 * 
 * @author SUJEN
 */

var OpenIAnalysis = {
	drillThroughElement : "",
	drillThroughPivotID : "",
	isNavigatorPanelCollapsed : false,

	initialize : function(analysisPivotID) {
		OpenIAnalysis.initUI(analysisPivotID);
		OpenIAnalysis.renderAllComponents(analysisPivotID);
		OpenIAnalysis.registerNavigatorDNDHandlers(analysisPivotID);
		OpenIAnalysis.registerToolbarBtnHandlers(analysisPivotID);
		OpenIAnalysis.registerAOP();
		OpenIAnalysis.adjustAxisCategorySize();
		PUC.allowSave(true);
	},

	initUI : function(analysisPivotID) {

		jQuery("input[type='checkbox'], input[type='radio'], input[type='text'], textarea, select")
				.uniform();

		jQuery("#configure-dialog").dialog({
			autoOpen : false,
			modal : true,
			width : 530,
			height : 430
		});

		jQuery("#slicer-value-dialog").dialog({
			autoOpen : false
		});

		jQuery("#error-message-dialog").dialog({
			autoOpen : false
		});
		jQuery("#success-message-dialog").dialog({
			autoOpen : false
		});

		jQuery("#drillthrough-table-dialog").dialog({
			autoOpen : false
		});

		jQuery("#drillthrough-option-dialog").dialog({
			autoOpen : false
		});
		
		jQuery("#member-navigator-dialog").dialog({
			autoOpen : false,
			modal : true,
			width : 400,
			height : 400,
			resize: function(event, ui) {
				OpenIAnalysis.adjustMemberNavigatorSize();
			}
		});
		
		jQuery(
			"#configure-dialog, #slicer-value-dialog, #success-message-dialog, #error-message-dialog, #drillthrough-option-dialog, #member-navigator-dialog").prev().addClass(
			"openi-popup-dialog-hdr");
		jQuery(
				"#configure-dialog, #slicer-value-dialog, #success-message-dialog, #error-message-dialog, #drillthrough-option-dialog, #member-navigator-dialog").parent().addClass(
				"openi-popup-dialog");

		/*
		 * jQuery("#configure-dialog").dialog(function(){ autoOpen: false,
		 * height: 300, modal: true });
		 * 
		 * jQuery("#configure-tabs").tabs();
		 */
		jQuery("#configure-tabs").tabs();

		jQuery("#table-chart-tabs").tabs();

		jQuery("#top-bar button").button();
		jQuery("#configure-btn").button({
			icons : {
				primary : "ui-icon-gear"
			}
		}).click(function() {
			jQuery("#configure-dialog").dialog('open');
		});

		jQuery("#refresh-btn").click(function() {
			location.reload();
		});

		jQuery("#print-btn").click(
				function() {
					var actionPath = "printAnalysisReport";
					var restResourcePath = Rest.constructBaseURL()
							+ Rest.ANALYSIS_RESOURCE_PATH + actionPath;
					var restURL = restResourcePath;
					restURL = restURL + "?pivotID=" + analysisPivotID;
					window.open(restURL);
				});

		jQuery("#export-btn").button({
			icons : {
				secondary : "ui-icon-triangle-1-s"
			}
		}).click(function() {
			var dropMenu = $(this).next().next();
			if (dropMenu.is(':hidden'))
				dropMenu.show("fast");
			else
				dropMenu.hide("fast");
		});

		/*
		 * jQuery("#chart-btn").button({ icons : { secondary :
		 * "ui-icon-triangle-1-s" } }).click(function() { var dropMenu =
		 * $(this).next(); if (dropMenu.is(':hidden')) dropMenu.show("fast");
		 * else dropMenu.hide("fast"); });
		 */

		/*
		 * jQuery("#apply-mdx-btn").button({ icons : {} }).click(function() {
		 * var newMDXQuery = jQuery.trim(jQuery("#mdx-input").val());
		 * OlapActions.applyMDX(analysisPivotID, newMDXQuery); return false; });
		 */

		jQuery("#export-to-pdf").click(
				function() {
					var actionPath = "exportAnalysisReport";
					var restResourcePath = Rest.constructBaseURL()
							+ Rest.ANALYSIS_RESOURCE_PATH + actionPath;
					var restURL = restResourcePath;
					restURL = restURL + "?pivotID=" + analysisPivotID
							+ "&exportType=0";
					window.open(restURL);
					var dropMenu = jQuery("#export-fmt-selection-menu");
					dropMenu.hide("fast");
					return false;
				});
		jQuery("#export-to-xls").click(
				function() {
					var actionPath = "exportAnalysisReport";
					var restResourcePath = Rest.constructBaseURL()
							+ Rest.ANALYSIS_RESOURCE_PATH + actionPath;
					var restURL = restResourcePath;
					restURL = restURL + "?pivotID=" + analysisPivotID
							+ "&exportType=1";
					window.open(restURL);
					var dropMenu = jQuery("#export-fmt-selection-menu");
					dropMenu.hide("fast");
					return false;
				});

		/*
		 * jQuery("#chart-selection-menu ul li").click( function() { var
		 * dropMenu = jQuery(this).parent().parent(); dropMenu.hide("fast");
		 * jQuery("#chart-btn").find('.ui-button-text').text(
		 * jQuery(this).text()); var components = { "chart-container" : "CHART" };
		 * AjaxIndicator.showProcessing("Loading Analysis Components");
		 * ComponentRenderer.renderComponents(components, analysisPivotID);
		 * AjaxIndicator.hideProcessing(); });
		 */

		jQuery("#chart-selection-menu").change(function() {
			var components = {
				"chart-container" : "CHART"
			};
			AjaxIndicator.showProcessing("Loading Analysis Components");
			ComponentRenderer.renderComponents(components, analysisPivotID);
			AjaxIndicator.hideProcessing();
		});

		jQuery(window).resize(jQuery.debounce(250, function() {
			OpenIAnalysis.adjustTabContainerSize(analysisPivotID);
			OpenIAnalysis.adjustChartSize(analysisPivotID);
		}));

		jQuery(".slicer-item a, .column-item a, .row-item a").click(
				function() {
					OpenIAnalysis.showMemberNavigator(jQuery(this).id,
							analysisPivotID);
					return false;
				});

		jQuery("#member-navigator-close-btn").click(function() {
			OpenIAnalysis.hideMemberNavigator();
		});
		
		jQuery("#member-navigator-popup").resizable({
			resize: function(event, ui) {
				OpenIAnalysis.adjustMemberNavigatorSize();
			}
		});

		jQuery("#download-drillthrough-btn").click(
				function() {
					OpenIAnalysis.drillThrough(
							OpenIAnalysis.drillThroughElement,
							OpenIAnalysis.drillThroughPivotID);
					jQuery("#drillthrough-option-dialog").dialog('close');
					return false;
				});
		jQuery("#send-drillthrough-btn")
				.click(
						function() {
							var webServiceURL = $("#drillthroughURL").val();

							jQuery("#drillthrough-option-dialog").dialog(
									'close');
							AjaxIndicator.showProcessing("Sending...");
							var requestType = "GET";
							var actionPath = "drillThroughSend";
							var dataParams = {
								'pivotID' : OpenIAnalysis.drillThroughPivotID,
								'elementID' : OpenIAnalysis.drillThroughElement.name,
								'webServiceURL' : webServiceURL
							};
							var asyncType = false;
							var restResourcePath = Rest.QUERY_RESOURCE_PATH
									+ actionPath;

							var result = Rest.sendRequest(restResourcePath,
									dataParams, requestType, asyncType);
							AjaxIndicator.hideProcessing();
							if (result == 'OK') {
								jQuery("#success-message-container")
										.empty()
										.html(
												"OpenI DrillThrough Report sent successfully.");
								jQuery("#success-message-dialog")
										.dialog('open');
								return false;
							} else {

								return "Sending Failed..";
							}
						});

		jQuery("#hide-sidebar-icon").click(function(){
			 jQuery("#sidebar").animate({
				 	marginLeft : "-225px"
				}, 300,function() {
					OpenIAnalysis.isNavigatorPanelCollapsed = true;
					OpenIAnalysis.adjustTabContainerSize(analysisPivotID);
					OpenIAnalysis.adjustChartSize(analysisPivotID);
			 });
			 jQuery("#show-sidebar").show("normal").animate({width:"19px", opacity:1}, 200);
			 jQuery("#show-sidebar").parent().css({width: "19px"});
		});
		
		jQuery("#show-sidebar-icon").click(function(){
			 $("#sidebar").animate({marginLeft:"0px"}, 300 );
			 $("#show-sidebar").animate({width:"0px", opacity:0}, 300).hide("slow");
			 jQuery("#show-sidebar").parent().css({width: "auto"});
			 OpenIAnalysis.isNavigatorPanelCollapsed = false;
			 OpenIAnalysis.adjustTabContainerSize(analysisPivotID);
			 OpenIAnalysis.adjustChartSize(analysisPivotID);
		});
		
		OpenIAnalysis.adjustTabContainerSize(analysisPivotID);

		var isChecked = jQuery("#showChart-chk-box").attr("checked");
		var doShow = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			doShow = true;
		}
		OpenIAnalysis.showHideChart(analysisPivotID, doShow);

		isChecked = jQuery("#showTable-chk-box").attr("checked");
		doShow = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			doShow = true;
		}
		OpenIAnalysis.showHideTable(analysisPivotID, doShow);
	},

	adjustTabContainerSize : function(analysisPivotID) {
		jQuery("#chart-container").height(jQuery(window).height() - 185);
		jQuery("#table-container").height(jQuery(window).height() - 185);
		if(OpenIAnalysis.isNavigatorPanelCollapsed) {
			jQuery("#table-container").width(jQuery(window).width() - 70);
			jQuery("#chart-container").width(jQuery(window).width() - 70);
		}
		else {
			jQuery("#table-container").width(jQuery(window).width() - 270);
			jQuery("#chart-container").width(jQuery(window).width() - 270);
		}
	},
	
	adjustAxisCategorySize : function() {
		var filterCatHeight = jQuery("#navigator-container").height() - 180 - jQuery(".Rows-category").height() - jQuery(".Columns-category").height();
		jQuery(".Filter-category").css({
			"max-height" : filterCatHeight + "px"
		});
	},
	
	adjustMemberNavigatorSize : function() {
		/*var memNavHeight = jQuery("#member-navigator-popup").height() - 30;
		jQuery("#mem-tree-container").height(memNavHeight);*/
		
		var memNavHeight = jQuery("#member-navigator-dialog").height() - 20;
		jQuery("#mem-tree-container").height(memNavHeight - 35);
		jQuery("#member-navigator-container").height(memNavHeight);
	},

	adjustChartSize : function(analysisPivotID) {
		AjaxIndicator.showProcessing("Loading Analysis Components");
		var components = {
			"chart-container" : "CHART"
		};
		ComponentRenderer.renderComponents(components, analysisPivotID);
		AjaxIndicator.hideProcessing();
	},

	renderAllComponents : function(analysisPivotID) {
		ComponentRenderer.renderAllComponents(analysisPivotID);
	},

	registerNavigatorDNDHandlers : function(analysisPivotID) {

		jQuery(".sortable").sortable({
			revert : true,
			placeholder : "ui-state-highlight"
		});

		OpenIAnalysis.applyDNDConstraints();
		jQuery("#Rows-category, #Columns-category, #Filter-category")
				.sortable(
						{
							cancel : ".drag-false",
							placeholder : "ui-state-highlight",
							scroll : true,
							dropOnEmpty : true,
							scrollSensitivity : 40,
							cursor : 'move',
							start : function(event, ui) {
								item = ui.item;
								newCategory = oldCategory = ui.item.parent();
								oldIndex = item.index();
							},
							stop : function(event, ui) {
								newIndex = item.index();
								if (newCategory == oldCategory) {
									if (newIndex == oldIndex)
										return;
								} else {
									if (newCategory.attr('id') == "Rows-category") {
										item.addClass("row-item");
										item.removeClass("column-item");
										item.removeClass("ui-state-default");
									} else if (newCategory.attr('id') == "Columns-category") {
										item.addClass("column-item");
										item.removeClass("ui-state-default");
										item.removeClass("row-item");
									} else if (newCategory.attr('id') == "Filter-category") {
										item.addClass("ui-state-default");
										item.removeClass("column-item");
										item.removeClass("row-item");
									}
								}
								OlapActions.moveItem(analysisPivotID,
										newCategory.attr('id'), item.text(),
										newIndex);
								OpenIAnalysis.applyDNDConstraints();
							},
							over: function(event, ui) {
								OpenIAnalysis.adjustAxisCategorySize();
							},
							change : function(event, ui) {
								if (ui.sender)
									newCategory = ui.placeholder.parent();
							},
							connectWith : ".connectedSortable"
						}).disableSelection();
	},

	applyDNDConstraints : function() {
		var rowItemsCount = jQuery("#Rows-category").children().length;
		if (rowItemsCount < 2)
			jQuery("#Rows-category li:first").addClass("drag-false");
		else {
			jQuery("#Rows-category li").each(function() {
				jQuery(this).removeClass("drag-false");
			});
		}

		var colItemsCount = jQuery("#Columns-category").children().length;
		if (colItemsCount < 2)
			jQuery("#Columns-category li:first").addClass("drag-false");
		else {
			jQuery("#Columns-category li").each(function() {
				jQuery(this).removeClass("drag-false");
			});
		}
	},

	registerToolbarBtnHandlers : function(analysisPivotID) {
		// swapAxes
		jQuery("#swapAxes-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doSwap = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doSwap = true;
			}
			OlapActions.swapAxes(analysisPivotID, doSwap);
		});

		// hideEmptyRowsColumns
		jQuery("#hideEmptyRowsCols-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doHide = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doHide = true;
			}
			OlapActions.hideEmptyRowsCols(analysisPivotID, doHide);
		});

		// showHideHierarchy
		jQuery("#setAxisStyle-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doSet = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doSet = true;
			}
			OlapActions.setAxisStyle(analysisPivotID, doSet);
		});

		// showHideHierarchy
		jQuery("#showHideHierarchy-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doShow = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doShow = true;
			}
			OlapActions.showHideHierarchy(analysisPivotID, doShow);
		});

		// showHideTable
		jQuery("#showTable-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doShow = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doShow = true;
			}
			OpenIAnalysis.showHideTable(analysisPivotID, doShow);
		});

		// showHideChart
		jQuery("#showChart-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var doShow = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				doShow = true;
			}
			OpenIAnalysis.showHideChart(analysisPivotID, doShow);
		});

		// enableDisableDrillthrouh
		jQuery("#dataReport-chk-box")
				.click(
						function() {
							var isChecked = jQuery(this).attr("checked");
							var enableDrillThrough = false;
							if (typeof isChecked !== 'undefined'
									&& isChecked !== false) {
								enableDrillThrough = true;
							}
							OlapActions.enableDisableDrillthrough(
									analysisPivotID, enableDrillThrough);
						});

		// enableDisableReplace
		jQuery("#replace-chk-box").click(function() {
			var isChecked = jQuery(this).attr("checked");
			var enableReplace = false;
			if (typeof isChecked !== 'undefined' && isChecked !== false) {
				enableReplace = true;
			}
			OlapActions.enableDisableReplace(analysisPivotID, enableReplace);
		});
	},

	registerAOP : function() {
		/*
		 * jQuery.aop.before({ target : OlapActions, method : '[a-zA-Z]+' },
		 * function() { AjaxIndicator.showProcessing("Loading Analysis Components"); });
		 * jQuery.aop.after({ target : OlapActions, method : '[a-zA-Z]+' },
		 * function() { AjaxIndicator.hideProcessing(); });
		 */

		jQuery.aop.before({
			target : OlapActions,
			method : 'swapAxes'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'hideEmptyRowsCols'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'setAxisStyle'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'showHideHierarchy'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'enableDisableDrillthrough'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'enableDisableReplace'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'moveItem'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'drillExpandCollapse'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'applyMemberSelection'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'revertMemberSelection'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});
		jQuery.aop.before({
			target : OlapActions,
			method : 'removeSlicerSelection'
		}, function() {
			AjaxIndicator.showProcessing("Loading Analysis Components");
		});


		jQuery.aop.after({
			target : OlapActions,
			method : 'swapAxes'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'hideEmptyRowsCols'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'setAxisStyle'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'showHideHierarchy'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'enableDisableDrillthrough'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'enableDisableReplace'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'moveItem'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'drillExpandCollapse'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'applyMemberSelection'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'revertMemberSelection'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
		jQuery.aop.after({
			target : OlapActions,
			method : 'removeSlicerSelection'
		}, function() {
			AjaxIndicator.hideProcessing("Loading Analysis Components");
		});
	},

	exportMe : function(analysisPivotID) {
		var requestType = "GET";
		var actionPath = "exportAnalysisReport";
		var dataParams = {
			"pivotID" : analysisPivotID,
			"exportType" : 0
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var result = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);
		OpenIAnalysis.doNothing();
	},

	showMemberNavigator : function(elementID, analysisPivotID, event) {
		/*
		var navContainerHeight = jQuery("#member-navigator-popup").height();
		var navContainerWidth = jQuery("#member-navigator-popup").width();
		var topVal;
		if (event.pageY < ((navContainerHeight / 2) + 40))
			topVal = event.pageY - 35 + "px";
		else if ((jQuery(document).height() - event.pageY) < ((navContainerHeight / 2) + 40))
			topVal = event.pageY - navContainerHeight + 35 + "px";
		else
			topVal = event.pageY - (navContainerHeight / 2) + "px";
		var leftVal = event.pageX + 20 + "px";
		jQuery("#member-navigator-popup").css({
			left : leftVal,
			top : topVal
		}).show();
		jQuery("#member-navigator-popup").height(260);
		jQuery("#member-navigator-popup").width(230);
		*/
		jQuery("#member-navigator-container")
				.html(
						'<div style="margin-left: 100px; margin-top: 110px;"><img src="images/ajax-loader.gif" /></div>');

		var requestType = "GET";
		var actionPath = "wcfMemNavComp";
		var dataParams = {
			'pivotID' : analysisPivotID,
			'hierItemID' : elementID
		};
		var asyncType = false;
		var restResourcePath = Rest.WCF_COMPONENT_RESOURCE_PATH + actionPath;
		var result = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);

		ComponentRenderer.renderResult('member-navigator-container', result);
		jQuery("#member-navigator-container input[type='checkbox']").uniform();
		jQuery("#member-navigator-container input[type='radio']").uniform();
		OpenIAnalysis.adjustMemberNavigatorSize();
		jQuery("#member-navigator-dialog").dialog('open');
		return false;
	},

	hideMemberNavigator : function() {
		/*jQuery("#member-navigator-popup").fadeOut();
		jQuery("#member-navigator-container").empty();*/
		
		jQuery("#member-navigator-dialog").dialog("close");
	},

	showHideTable : function(analysisPivotID, doShow) {
		AjaxIndicator.showProcessing("Loading Analysis Components");
		var requestType = "POST";
		var actionPath = "showHideTable";
		var dataParams = {
			'pivotID' : analysisPivotID,
			'doShow' : doShow
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var result = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);

		if (doShow) {
			// jQuery("#configure-tabs").tabs("add", "#table-chart-tabs-1",
			// "Table", 0);
			jQuery("#table-chart-tabs-1").css("display", "block");
			jQuery("a[href='#table-chart-tabs-1']").css("display", "block");
			// jQuery("#configure-tabs").tabs("select", 0);
			jQuery("a[href='#table-chart-tabs-1']").click();
		} else {
			// jQuery("#configure-tabs").tabs("remove", 0);
			jQuery("#table-chart-tabs-1").css("display", "none");
			jQuery("a[href='#table-chart-tabs-1']").css("display", "none");
			// jQuery("#configure-tabs").tabs("select", 1);
			jQuery("a[href='#table-chart-tabs-2']").click();
		}
		AjaxIndicator.hideProcessing();
	},

	showHideChart : function(analysisPivotID, doShow) {
		AjaxIndicator.showProcessing("Loading Analysis Components");

		var requestType = "POST";
		var actionPath = "showHideChart";
		var dataParams = {
			'pivotID' : analysisPivotID,
			'doShow' : doShow
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var result = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);

		if (doShow) {
			// jQuery("#configure-tabs").tabs("add", "#table-chart-tabs-2",
			// "Chart", 1);
			jQuery("#table-chart-tabs-2").css("display", "block");
			jQuery("a[href='#table-chart-tabs-2']").css("display", "block");
			// jQuery("#configure-tabs").tabs("selected", 1);
			jQuery("a[href='#table-chart-tabs-2']").click();
		} else {
			// jQuery("#configure-tabs").tabs("remove", 1);
			jQuery("#table-chart-tabs-2").css("display", "none");
			jQuery("a[href='#table-chart-tabs-2']").css("display", "none");
			// jQuery("#configure-tabs").tabs("selected", 0);
			jQuery("a[href='#table-chart-tabs-1']").click();
		}
		AjaxIndicator.hideProcessing();
	},

	showSlicerValueDialog : function() {

		jQuery("#slicer-value-dialog").dialog('open');
	},

	showDrillThroughOptionDialog : function(element, pivotID) {
		OpenIAnalysis.drillThroughPivotID = pivotID;
		OpenIAnalysis.drillThroughElement = element;
		jQuery("#drillthrough-option-dialog").dialog('open');

		return false;

		/*
		 * jQuery("#drillthrough-option-dialog").prev().addClass("openi-popup-dialog-hdr");
		 * jQuery("#drillthrough-option-dialog").parent().addClass("openi-popup-dialog");
		 * 
		 * jQuery("input[type='checkbox'], input[type='text'], textarea,
		 * select").uniform();
		 * 
		 * jQuery("button").addClass("openi-btn");
		 */

		// drillThrough(this, pivotID)
	},

	drillThrough : function(element, pivotID) {
		/*
		 * OlapActions.drillThrough(element, pivotID);
		 * jQuery("#drillthrough-table-dialog").dialog('open'); oTable =
		 * jQuery("#drillthrough-table").dataTable({ "bJQueryUI" : true,
		 * "sPaginationType" : "full_numbers" });
		 */

		var actionPath = "drillThrough";
		var restResourcePath = Rest.constructBaseURL()
				+ Rest.QUERY_RESOURCE_PATH + actionPath;

		var restURL = restResourcePath;
		restURL = restURL + "?pivotID=" + pivotID + "&elementID="
				+ element.name;
		window.open(restURL);
		return false;
	},

	applyPrintSettings : function(element, pivotID) {
		var reportTitle = jQuery("#printForm-report-title-input").val();
		var pageOrientation = jQuery("#printForm-page-orientation-select-menu")
				.val();
		var pageSize = jQuery("#printForm-page-size-select-menu").val();
		var pageHeight = jQuery("#printForm-page-height-input").val();
		var pageWidth = jQuery("#printForm-page-width-input").val();

		var isChecked = jQuery("#printForm-table-width-chk-box")
				.attr("checked");
		var applyTableWidth = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			applyTableWidth = true;
		}
		var tableWidth = jQuery("#printForm-table-width-input").val();

		isChecked = jQuery("#printForm-chart-page-break-chk-box").attr(
				"checked");
		var chartOnSeparatePage = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			chartOnSeparatePage = true;
		}

		var requestType = "POST";
		var actionPath = "applyPrintSettings";
		var dataParams = {
			"pivotID" : pivotID,
			"reportTitle" : reportTitle,
			"pageOrientation" : pageOrientation,
			"pageSize" : pageSize,
			"pageHeight" : pageHeight,
			"pageWidth" : pageWidth,
			"applyTableWidth" : applyTableWidth,
			"tableWidth" : tableWidth,
			"chartOnSeparatePage" : chartOnSeparatePage
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);

		return false;
	},

	save : function(pivotID, filename, solution, path, type, overwrite) {
		var requestType = "POST";
		var actionPath = "saveAnalysisReport";
		var dataParams = {
			"filename" : filename,
			"solution" : solution,
			"path" : path,
			"type" : type,
			"overwrite" : overwrite,
			"pivotID" : pivotID
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var status = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);
		if (status == "Error")
			return false;
		PUC.refreshRepo();
		jQuery("#success-message-container").empty().html(
				"OpenI Analysis Report saved successfully.");
		jQuery("#success-message-dialog").dialog('open');
	},

	applyChartProperties : function(element, analysisPivotID) {
		var isChecked = jQuery("#configure-chart-show-legend").attr("checked");
		var showLegend = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			showLegend = true;
		}

		var legendPosition = jQuery("#configure-chart-select-legend-position")
				.val();
		var legendFontFamily = jQuery(
				"#configure-chart-select-legend-fontFamily").val();
		var legendFontStyle = jQuery(
				"#configure-chart-select-legend-fontWeight").val();
		var legendFontSize = jQuery("#configure-chart-select-legend-fontSize")
				.val();

		isChecked = jQuery("#configure-chart-show-slicer").attr("checked");
		var showSlicer = false;
		if (typeof isChecked !== 'undefined' && isChecked !== false) {
			showSlicer = true;
		}
		var slicerPosition = jQuery("#configure-chart-select-slicer-position")
				.val();
		var slicerFontFamily = jQuery(
				"#configure-chart-select-slicer-fontFamily").val();
		var slicerFontStyle = jQuery(
				"#configure-chart-select-slicer-fontWeight").val();
		var slicerFontSize = jQuery("#configure-chart-select-slicer-fontSize")
				.val();

		var subTitle = jQuery("#configure-chart-subtitle-input").val();
		var chartTitleFontFamily = jQuery(
				"#configure-chart-select-chartTitle-fontFamily").val();
		var chartTitleFontStyle = jQuery(
				"#configure-chart-select-chartTitle-fontWeight").val();
		var chartTitleFontSize = jQuery(
				"#configure-chart-select-chartTitle-fontSize").val();

		var horizAxisLabel = jQuery("#configure-chart-horiz-axislabel-input")
				.val();
		var vertAxisLabel = jQuery("#configure-chart-vert-axislabel-input")
				.val();

		var axisLabelFontFamily = jQuery(
				"#configure-chart-select-axisLabel-fontFamily").val();
		var axisLabelFontStyle = jQuery(
				"#configure-chart-select-axisLabel-fontWeight").val();
		var axisLabelFontSize = jQuery(
				"#configure-chart-select-axisLabel-fontSize").val();

		var axisTickLabelFontFamily = jQuery(
				"#configure-chart-select-axisTickLabel-fontFamily").val();
		var axisTickLabelFontStyle = jQuery(
				"#configure-chart-select-axisTickLabel-fontWeight").val();
		var axisTickLabelFontSize = jQuery(
				"#configure-chart-select-axisTickLabel-fontSize").val();

		var requestType = "POST";
		var actionPath = "applyChartProperties";
		var dataParams = {
			"pivotID" : analysisPivotID,
			"showLegend" : showLegend,
			"legendPosition" : legendPosition,
			"legendFontFamily" : legendFontFamily,
			"legendFontStyle" : legendFontStyle,
			"legendFontSize" : legendFontSize,
			"showSlicer" : showSlicer,
			"slicerPosition" : slicerPosition,
			"slicerFontFamily" : slicerFontFamily,
			"slicerFontStyle" : slicerFontStyle,
			"slicerFontSize" : slicerFontSize,
			"subTitle" : subTitle,
			"chartTitleFontFamily" : chartTitleFontFamily,
			"chartTitleFontStyle" : chartTitleFontStyle,
			"chartTitleFontSize" : chartTitleFontSize,
			"horizAxisLabel" : horizAxisLabel,
			"vertAxisLabel" : vertAxisLabel,
			"axisLabelFontFamily" : axisLabelFontFamily,
			"axisLabelFontStyle" : axisLabelFontStyle,
			"axisLabelFontSize" : axisLabelFontSize,
			"axisTickLabelFontFamily" : axisTickLabelFontFamily,
			"axisTickLabelFontStyle" : axisTickLabelFontStyle,
			"axisTickLabelFontSize" : axisTickLabelFontSize
		};
		var asyncType = false;
		var restResourcePath = Rest.ANALYSIS_RESOURCE_PATH + actionPath;
		var result = Rest.sendRequest(restResourcePath, dataParams,
				requestType, asyncType);

		var components = {
			"chart-container" : "CHART"
		};
		AjaxIndicator.showProcessing("Loading Analysis Components");
		ComponentRenderer.renderComponents(components, analysisPivotID);
		AjaxIndicator.hideProcessing();
	}

}
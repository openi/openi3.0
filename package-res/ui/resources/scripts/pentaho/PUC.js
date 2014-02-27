var PUC_enabled = (window.parent != null && window.parent.mantle_initialized == true);

// The following code is for Save/Save As functionality
var gCtrlr;

// this is a required object
function Wiz() {
    currPgNum = 0;
}

// this is a required object
function RepositoryBrowserControllerProxy() {

    // This function is called after the Save dialog has been used
    this.remoteSave = function( myFilename, mySolution, myPath, myType, myOverwrite ) {
		
    }
}

function handle_puc_save(path, filename, overwrite) {
		OpenIAnalysis.save(pivotID, filename, "", path, "", overwrite);
}
// this is a required object
function WaqrProxy() {
    this.wiz = new Wiz();
    this.repositoryBrowserController = new RepositoryBrowserControllerProxy();
}

function PentahoPlugin(filetype) {
	this.filetype = filetype;
	this.editing  = false;
}

PentahoPlugin.prototype.init = function(command) {
	var that = this;
	this.editing = (command == 'edit' || command == 'new');
	
	if (PUC_enabled) { //PUC enabled
		gCtrlr = new WaqrProxy(); // this is a required variable
		//subscribe to the save event and allow instances of plugin to override the method
		$(gCtrlr.repositoryBrowserController).bind("save",
			function(event, data) {
				$(that).trigger("saveComplete", data);
			}
		);
		
	    //console.log("enabling save: " + window.parent.enableAdhocSave);
	    if (PUC_enabled && window.parent.enableSave ) {
	        window.parent.enableSave( true );
	    }
		
	} //end if PUC_enabled
	
	
	$(this).trigger("PluginInitComplete");

} 
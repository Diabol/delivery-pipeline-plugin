var BuildPipeline = function(viewProxy, buildCardTemplate, projectCardTemplate, refreshFrequency){
	this.buildCardTemplate = buildCardTemplate;
	this.projectCardTemplate = projectCardTemplate;
	this.buildProxies = {};
    this.projectProxies = {};
	this.viewProxy = viewProxy;
	this.refreshFrequency = refreshFrequency;
};

BuildPipeline.prototype = {
	showProgress : function(id, dependencies) {
		var buildPipeline = this;
		var intervalId = setInterval(function(){
            if (isPageVisible()) {
			    buildPipeline.buildProxies[id].asJSON(function(data){
                    var buildData = jQuery.parseJSON(data.responseObject());
                    if (buildData.build.progress > 0) {
                        buildPipeline.updateBuildCardFromJSON(buildData, false);
                    } else {
                        buildPipeline.updateBuildCardFromJSON(buildData, true);
                        if (!jQuery.isEmptyObject(buildPipeline.projectProxies)) {
                            buildPipeline.updateProjectCard(buildData.project.id);
                        }
                        clearInterval(intervalId);
                        //refresh all build cards since some statuses will be invalid for older builds
                        buildPipeline.updateAllBuildCards(dependencies);
                        // trigger all dependency tracking
                        jQuery.each(dependencies, function(){
                            jQuery("#pipelines").trigger("show-status-" + this);
                        });
                    }
                });
            }
		}, buildPipeline.refreshFrequency);
	},
	updateBuildCard : function(id) {
		var buildPipeline = this;
		buildPipeline.buildProxies[id].asJSON(function(data){
			buildPipeline.updateBuildCardFromJSON(jQuery.parseJSON(data.responseObject()), true);
		});
	},
    updateAllBuildCards : function(dependenciesNotToUpdate) {
        var buildPipeline = this;
        jQuery.each(buildPipeline.buildProxies, function(key, value){
            if (jQuery.inArray(parseInt(key), dependenciesNotToUpdate) < 0) {
                buildPipeline.updateBuildCard(key);
            }
        });
    },
	updateProjectCard : function(id) {
		var buildPipeline = this;
		buildPipeline.projectProxies[id].asJSON(function(data){
			buildPipeline.updateProjectCardFromJSON(jQuery.parseJSON(data.responseObject()), true);
		});
	},
	updateBuildCardFromJSON : function(buildAsJSON, fadeIn) {
		var buildPipeline = this;
		jQuery("#build-" + buildAsJSON.id).empty();
		jQuery(buildPipeline.buildCardTemplate(buildAsJSON)).hide().appendTo("#build-" + buildAsJSON.id).fadeIn(fadeIn ? 1000 : 0);
	},
	updateProjectCardFromJSON : function(projectAsJSON, fadeIn) {
		var buildPipeline = this;
		jQuery("#project-" + projectAsJSON.id).empty();
		jQuery(buildPipeline.projectCardTemplate(projectAsJSON)).hide().appendTo("#project-" + projectAsJSON.id).fadeIn(fadeIn ? 1000 : 0);
	},
	updateNextBuildAndShowProgress : function(id, nextBuildNumber, dependencies) {
		var buildPipeline = this;
		//try to get the updated build, that's not pending
		var intervalId = setInterval(function(){
			buildPipeline.buildProxies[id].updatePipelineBuild(nextBuildNumber, function(updated){
				if (updated.responseObject()) {
					buildPipeline.showProgress(id, dependencies);
					clearInterval(intervalId);
				}
			});
		}, buildPipeline.refreshFrequency);
	},
	triggerBuild : function(id, upstreamProjectName, upstreamBuildNumber, triggerProjectName, dependencyIds) {
		var buildPipeline = this;
		buildPipeline.viewProxy.triggerManualBuild(upstreamBuildNumber, triggerProjectName, upstreamProjectName, function(data){
			buildPipeline.updateNextBuildAndShowProgress(id, data.responseObject(), dependencyIds);
		});
	},
	retryBuild : function(id, triggerProjectName, dependencyIds) {
		var buildPipeline = this;
		buildPipeline.viewProxy.retryBuild(triggerProjectName, function(data){
			buildPipeline.updateNextBuildAndShowProgress(id, data.responseObject(), dependencyIds);
		});
	},
	rerunBuild : function(id, buildExternalizableId, dependencyIds) {
		var buildPipeline = this;
		buildPipeline.viewProxy.rerunBuild(buildExternalizableId, function(data){
			buildPipeline.updateNextBuildAndShowProgress(id, data.responseObject(), dependencyIds);
		});
	},
	showSpinner : function(id){
		jQuery("#status-bar-" + id).html('<table class="progress-bar" align="center"><tbody><tr class="unknown"><td></td></tr></tbody></table>');
		jQuery("#icons-" + id).empty();
	},
	fillDialog : function(href, title) {
		jQuery.fancybox({
			type: 'iframe',
			title: title,
			titlePosition: 'outside',
			href: '/' + href,
			transitionIn : 'elastic',
			transitionOut : 'elastic',
			width: '90%',
			height: '80%'
		});
	},
	closeDialog : function() {
		jQuery.fancybox.close();
	},
	showModalSpinner : function() {
		jQuery.fancybox.showActivity();
	},
	hideModalSpinner : function() {
		jQuery.fancybox.hideActivity();
	}

}

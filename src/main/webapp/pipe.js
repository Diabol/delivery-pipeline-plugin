function updatePipelines(divNames, errorDiv, view, showAvatars, showChanges, timeout) {
    Q.ajax({
        url: 'api/json',
        dataType: 'json',
        async: true,
        cache: false,
        timeout: 20000,
        success: function (data) {
            refreshPipelines(data, divNames, errorDiv, view, showAvatars, showChanges);
            setTimeout(function () {
                updatePipelines(divNames, errorDiv, view, showAvatars, showChanges, timeout)
            }, timeout);
        },
        error: function (xhr, status, error) {
            Q("#" + errorDiv).html('Error communicating to server! ' + htmlEncode(error));
            Q("#" + errorDiv).show();
            plumb.repaintEverything();
            setTimeout(function () {
                updatePipelines(divNames, errorDiv, view, showAvatars, showChanges, timeout)
            }, timeout);
        }
    });


}


function refreshPipelines(data, divNames, errorDiv, view, showAvatars, showChanges) {
    var lastUpdate = data.lastUpdated;

    if (data.error) {
        Q("#" + errorDiv).html('Error: ' + data.error);
        Q("#" + errorDiv).show();
    } else {
        Q("#" + errorDiv).html('');
        Q("#" + errorDiv).hide();
    }

    if (lastResponse == null || JSON.stringify(data.pipelines) != JSON.stringify(lastResponse.pipelines)) {

        for (var z = 0; z < divNames.length; z++) {
            Q("#" + divNames[z]).html('');
        }

        var tasks = [];

        if (!data.pipelines || data.pipelines.length == 0) {
            Q("#pipeline-message").html('No pipelines configured or found. Please review the <a href="configure">configuration</a>')
        }
        plumb.reset();
        for (var c = 0; c < data.pipelines.length; c++) {
            var component = data.pipelines[c];
            var html = "<section class='pipeline-component'>";
            html = html + "<h1>" + htmlEncode(component.name) + "</h1>";
            if (component.pipelines.length == 0) {
                html = html + "No builds done yet.";
            }
            for (var i = 0; i < component.pipelines.length; i++) {
                var pipeline = component.pipelines[i];



                var triggered = "";
                if (pipeline.triggeredBy && pipeline.triggeredBy.length > 0) {
                    for (var y = 0; y < pipeline.triggeredBy.length; y++) {
                        var trigger = pipeline.triggeredBy[y];
                        triggered = triggered + ' <span class="' + trigger.type + '">' + htmlEncode(trigger.description) + '</span>';
                    }
                    if (y < pipeline.triggeredBy.length - 1) {
                        triggered = triggered + ", ";
                    }
                }

                var contributors = [];
                if (pipeline.contributors) {
                    Q.each(pipeline.contributors, function (index, contributor) {
                        contributors.push(htmlEncode(contributor.name));
                    });
                }
		
		        if (contributors.length > 0) {
		            triggered = triggered + " changes by " + contributors.join(", ");
		        }

                if (pipeline.aggregated) {
                    html = html + '<h2>Aggregated view</h2>'
                } else {
                    html = html + '<h2>' + htmlEncode(pipeline.version);
                    if (triggered != "") {
                        html = html + " triggered by " + triggered;
                    }
                    html = html + ' started <span id="' + pipeline.id + '\">' + formatDate(pipeline.timestamp, lastUpdate) + '</span></h2>';

                    if (showChanges && pipeline.changes && pipeline.changes.length > 0) {
                        html = html + generateChangeLog(pipeline.changes);
                    }
                }
                html = html + '<section class="pipeline">';

                var row = 0;
                var column = 0;

                html = html + '<div class="pipeline-row">';

                for (var j = 0; j < pipeline.stages.length; j++) {
                    var stage = pipeline.stages[j];
                    if (stage.row > row) {

                        html = html + '</div><div class="pipeline-row">';
                        column = 0;
                        row++;
                    }

                    if (stage.column > column) {
                        for (var as = column; as < stage.column; as++) {
                            html = html + '<div class="pipeline-cell"><div class="stage hide"></div></div>';
                            column++;
                        }

                    }

                    html = html + '<div class="pipeline-cell">';
                    html = html + '<div id="' + getStageId(stage.id + "", i) + '" class="stage ' + getStageClassName(stage.name) + '">';
                    html = html + '<div class="stage-header"><div class="stage-name">' + htmlEncode(stage.name) + '</div>';
                    if (!pipeline.aggregated) {
                        html = html + '</div>'
                    } else {
                        var stageversion = stage.version;
                        if (!stageversion) {
                            stageversion = "N/A"
                        }
                        html = html + ' <div class="stage-version">' + htmlEncode(stageversion) + '</div></div>'
                    }
                    for (var k = 0; k < stage.tasks.length; k++) {
                        var task = stage.tasks[k];

                        var id = getTaskId(task.id, i);

                        var timestamp = formatDate(task.status.timestamp, lastUpdate);

                        tasks.push({id: id, taskId: task.id, buildId: task.buildId});

                        var progress = 0;

                        if (task.status.percentage) {
                            progress = task.status.percentage;
                        }

                        html = html + "<div id=\"" + id + "\" class=\"stage-task " + task.status.type +
                            "\"><div class=\"task-progress\" style=\"width: " + progress + "%;\"><div class=\"task-content\">" +
                            "<div class=\"taskname\"><a href=\"" + task.link + "\">" + htmlEncode(task.name) + "</a></div>";

                        if (timestamp != "") {
                            html = html + "<div id=\"" + id + ".timestamp\" class='timestamp'>" + timestamp + "</div>"
                        }

                        if (task.status.duration >= 0)
                            html = html + "<div class='duration'>" + formatDuration(task.status.duration) + "</div>";

                        html = html + "</div></div></div>"

                    }
                    html = html + "</div>";
                    html = html + '</div>';
                    column++;
                }
                html = html + '</div>';

                html = html + "</section>";

            }
            html = html + "</section>";
            Q("#" + divNames[c % divNames.length]).append(html);
            Q("#pipeline-message").html('');
        }
        var index = 0;
        lastResponse = data;
        equalheight(".pipeline-row .stage");

        Q.each(data.pipelines, function (i, component) {
            Q.each(component.pipelines, function (j, pipeline) {
                var index = j;
                Q.each(pipeline.stages, function (k, stage) {
                    if (stage.downstreamStages) {
                        Q.each(stage.downstreamStageIds, function (l, value) {
                            var source = getStageId(stage.id + "", index);
                            var target = getStageId(value + "", index);

                            plumb.connect({
                                source: source,
                                target: target,
                                anchors: ["RightMiddle", "LeftMiddle"],
                                overlays: [
                                    [ "Arrow", { location: 1}]
                                ],
                                cssClass: "relation",
                                connector: ["Flowchart", { stub: 25, gap: 2, midpoint: 1, alwaysRespectStubs: true } ],
                                paintStyle: { lineWidth: 2, strokeStyle: "rgba(0,0,0,0.5)" },
                                drawEndpoints: false
                            });


                        });
                    }
                });

            });
        });

    } else {
        for (var p = 0; p < data.pipelines.length; p++) {
            var comp = data.pipelines[p];
            for (var d = 0; d < comp.pipelines.length; d++) {
                var pipe = comp.pipelines[d];
                var head = document.getElementById(pipe.id);
                if (head) {
                    head.innerHTML = formatDate(pipe.timestamp, lastUpdate)
                }

                for (var l = 0; l < pipe.stages.length; l++) {
                    var st = pipe.stages[l];
                    for (var m = 0; m < st.tasks.length; m++) {
                        var ta = st.tasks[m];
                        var time = document.getElementById(getTaskId(ta.id, d) + ".timestamp");
                        if (time) {
                            time.innerHTML = formatDate(ta.status.timestamp, lastUpdate);
                        }
                    }
                }
            }
        }
    }
    plumb.repaintEverything();
}

function generateChangeLog(changes) {
    var html = '<div class="changes">';
    html = html + '<h1>Changes:</h1>';
    for (var i = 0; i < changes.length; i++) {
        html = html + '<div class="change">';
        var change = changes[i];
        html = html + '<div class="change-author">' + htmlEncode(change.author.name) + '</div>';
        if (change.changeLink) {
            html = html + '<div class="change-message"><a href="' + change.changeLink + '">' + htmlEncode(change.message) + '</a></div>';
        } else {
            html = html + '<div class="change-message">' + htmlEncode(change.message) + '</div>';
        }
        html = html + '</div>';
    }
    html = html + '</div>';
    return html;
}

function getStageClassName(stagename) {
    return "stage_" + replace(stagename, " ", "_");
}

function getTaskId(taskname, count) {
    var re = new RegExp(' ', 'g');
    return "task-" + taskname.replace(re, '_') + "_" + count;
}

function replace(string, replace, replaceWith) {
    var re = new RegExp(replace, 'g');
    return string.replace(re, replaceWith);
}


function formatDate(date, currentTime) {
    if (date != null) {
        return moment(date, "YYYY-MM-DDTHH:mm:ss").from(moment(currentTime, "YYYY-MM-DDTHH:mm:ss"))
    } else {
        return "";
    }
}

function formatDuration(millis) {
    if (millis > 0) {
        var seconds = Math.floor(millis / 1000);
        var minutes = Math.floor(seconds / 60);
        seconds = seconds % 60;

        var minstr;
        if (minutes == 0)
            minstr = "";
        else
            minstr = minutes + " min ";

        var secstr = "" + seconds + " sec";

        return minstr + secstr;
    }
    return "0 sec";
}

function htmlEncode(html) {
    html = document.createElement('a').appendChild(
        document.createTextNode(html)).parentNode.innerHTML;
    return html.replace(/\n/g, '<br/>');
}
function getStageId(name, count) {
    var re = new RegExp(' ', 'g');
    return name.replace(re, '_') + "_" + count;
}

function equalheight(container) {

    var currentTallest = 0,
        currentRowStart = 0,
        rowDivs = new Array(),
        $el,
        topPosition = 0;
    Q(container).each(function () {

        $el = Q(this);
        Q($el).height('auto');
        topPostion = $el.position().top;

        if (currentRowStart != topPostion) {
            for (currentDiv = 0; currentDiv < rowDivs.length; currentDiv++) {
                rowDivs[currentDiv].height(currentTallest);
            }
            rowDivs.length = 0; // empty the array
            currentRowStart = topPostion;
            currentTallest = $el.height();
            rowDivs.push($el);
        } else {
            rowDivs.push($el);
            currentTallest = (currentTallest < $el.height()) ? ($el.height()) : (currentTallest);
        }
        for (currentDiv = 0; currentDiv < rowDivs.length; currentDiv++) {
            rowDivs[currentDiv].height(currentTallest);
        }
    });
}

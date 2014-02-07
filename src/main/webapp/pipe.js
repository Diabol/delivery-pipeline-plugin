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
            Q("#" + errorDiv).html('Error communicating to server! ' + error);
            Q("#" + errorDiv).show();
            plumb.repaintEverything();
            setTimeout(function () {
                updatePipelines(divNames, errorDiv, view, showAvatars, showChanges, timeout)
            }, timeout);
        }
    });


}


function refreshPipelines(data, divNames, errorDiv, view, showAvatars, showChanges) {
    Q("#" + errorDiv).html('');
    Q("#" + errorDiv).hide();
    var lastUpdate = data.lastUpdated;
    if (lastResponse == null || JSON.stringify(data.pipelines) != JSON.stringify(lastResponse.pipelines)) {

        for (var z = 0; z < divNames.length; z++) {
            Q("#" + divNames[z]).html('');
        }

        var tasks = [];

        if (data.pipelines.length == 0) {
            Q("#pipeline-message").html('No pipelines configured or found. Please review the <a href="configure">configuration</a>')
        }

        for (var c = 0; c < data.pipelines.length; c++) {
            var component = data.pipelines[c];
            var html = "<section class='component'>";
            html = html + "<h1>" + component.name + "</h1>";
            if (component.pipelines.length == 0) {
                html = html + "No builds done yet.";
            }
            for (var i = 0; i < component.pipelines.length; i++) {
                var pipeline = component.pipelines[i];
                html = html + '<section class="pipeline">';

                var triggered = "";
                if (pipeline.triggeredBy && pipeline.triggeredBy.length > 0) {
                    for (var y = 0; y < pipeline.triggeredBy.length; y++) {
                        var trigger = pipeline.triggeredBy[y];
                        triggered = triggered + ' <span class="' + trigger.type + '">' + trigger.description + '</span>';
                    }
                    if (y < pipeline.triggeredBy.length - 1) {
                        triggered = triggered + ", ";
                    }
                }

                if (pipeline.aggregated) {
                    html = html + '<h1>Aggregated view</h1>'
                } else {
                    html = html + '<h1>' + pipeline.version;
                    if (triggered != "") {
                        html = html + " triggered by " + triggered;
                    }
                    html = html + ' started <span id="' + pipeline.id + '\">' + formatDate(pipeline.timestamp, lastUpdate) + '</span></h1>';

                    if (showChanges && pipeline.changes && pipeline.changes.length > 0) {
                        html = html + '<div class="changes">';
                        html = html + '<h1>Changes:</h1>';
                        for (var o = 0; o < pipeline.changes.length; o++) {
                            html = html + '<div class="change">';
                            var change = pipeline.changes[o];
                            html = html + '<div class="change-author">' + change.author.name + '</div>';
                            if (change.changeLink) {
                                html = html + '<div class="change-message"><a href="' + change.changeLink + '">' + change.message + '</a></div>';
                            } else {
                                html = html + '<div class="change-message">' + change.message + '</div>';
                            }
                            html = html + '</div>';
                        }
                        html = html + '</div>';


                    }


                }


                var row = 0;
                var column = 0;

                html = html + '<div class="pipeline-row">';

                for (var j = 0; j < pipeline.stages.length; j++) {
                    var stage = pipeline.stages[j];
                    if (stage.row > row) {

                        html = html + '</div><div class="pipeline-row">';
                        column = 0;
                    }

                    if (stage.column > column) {
                        for (var as = column; as < stage.column; as++) {
                            html = html + '<section class="stage hide"></section>';
                        }

                    }

                    html = html + '<section id="' + getStageId(stage.name, i) + '" class="stage ' + getStageClassName(stage.name) + '">';
                    html = html + '<h1><span class="stage-name">' + stage.name + '</span>';
                    if (!pipeline.aggregated) {
                        html = html + '</h1>'
                    } else {
                        var stageversion = stage.version;
                        if (!stageversion) {
                            stageversion = "N/A"
                        }
                        html = html + ' <span class="stage-version">' + stageversion + '</span></h1>'
                    }
                    for (var k = 0; k < stage.tasks.length; k++) {
                        var task = stage.tasks[k];

                        var id = getTaskId(task.id, i);

                        var timestamp = formatDate(task.status.timestamp, lastUpdate);

                        tasks.push({id: id, taskId: task.id, buildId: task.buildId});

                        html = html + "<div id=\"" + id + "\" class=\"task " + task.status.type +
                            "\"><div class=\"taskname\"><a href=\"" + task.link + "\">" + task.name + "</a></div>";

                        if (timestamp != "") {
                            html = html + "<span id=\"" + id + ".timestamp\" class='timestamp'>" + timestamp + "</span>"
                        }

                        if (task.status.duration >= 0)
                            html = html + "<span class='duration'>" + formatDuration(task.status.duration) + "</span>";

                        html = html + "</div>"

                    }
                    html = html + "</section>";
                    column++;
                    if (stage.row > row) {
                        html = html + '</div>';
                        row++;
                        column = 0;
                    }

                }
                html = html + '</div>';

                html = html + "</section>";

            }
            html = html + "</section>";
            var index = 0;
            Q("#" + divNames[c % divNames.length]).append(html);
            Q("#pipeline-message").html('');
            lastResponse = data;
            equalheight(".stage");

            Q.each(data.pipelines, function (i, component) {
                Q.each(component.pipelines, function (j, pipeline) {
                    var index = j;
                    Q.each(pipeline.stages, function (k, stage) {
                        if (stage.downstreamStages) {
                            Q.each(stage.downstreamStages, function (l, value) {
                                var source = getStageId(stage.name, index);
                                var target = getStageId(value, index);

                                plumb.connect({
                                    source: source,
                                    target: target,
                                    anchors: ["RightMiddle", "LeftMiddle"],
                                    overlays: [
                                        [ "Arrow", { location: 1}]
                                    ],
                                    cssClass: "relation",
                                    connector: ["Flowchart", { stub: 25, gap: 2, midpoint: 1, alwaysRespectStubs:true } ],
                                    paintStyle: { lineWidth: 2, strokeStyle: "rgba(0,0,0,0.5)" },
                                    drawEndpoints: false
                                });


                            });
                        }
                    });

                });
            });


        }
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

function getStageId(name, count) {
    var re = new RegExp(' ', 'g');
    return name.replace(re, '_') + "_" + count;
}

function equalheight(container) {

    var currentTallest = 0,
        currentRowStart = 0,
        rowDivs = [],
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

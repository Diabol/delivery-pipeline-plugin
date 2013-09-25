function renderPipelines(divNames, errorDiv, view) {
    //Simple feature switch for task details
    var popover = false;
    Q("#" + errorDiv).html('');
    Q.ajax({
        url: 'api/json',
        dataType: 'json',
        async: false,
        cache: false,
        success: function (data) {
                if (JSON.stringify(data) != JSON.stringify(lastResponse)) {

            for (var z = 0; z < divNames.length; z++) {
                Q("#" + divNames[z]).html('');
            }

            var tasks = [];

            for (var c = 0; c < data.pipelines.length; c++) {
                var component = data.pipelines[c];
                var html = "<section class='component'>";
                html = html + "<h1>" + component.name + "</h1>";
                for (var i = 0; i < component.pipelines.length; i++) {
                    var pipeline = component.pipelines[i];
                    html = html + "<section class=\"pipe\">";

                            var triggered = "";
                            if (pipeline.triggeredBy) {
                                for (var t = 0; t < pipeline.triggeredBy.length; t++) {
                                    var user = pipeline.triggeredBy[t];
                                    if (user.avatarUrl) {
                                        triggered = triggered + "<img src=\"" + user.avatarUrl + "\" alt=\"" + user.name + "\" title=\"" + user.name + "\"/>"
                    } else {
                                        triggered = triggered + user.name;
                    }
                            }
                        }

                            if (pipeline.aggregated) {
                                html = html + '<h1>Aggregated view</h1>'
                            } else {
                                html = html + '<h1>' + pipeline.version + ' by ' + triggered + ', started ' + formatDate(pipeline.timestamp) + '</h1>'
                            }

                            for (var j = 0; j < pipeline.stages.length; j++) {
                                var stage = pipeline.stages[j];
                                html = html + "<section class=\"stage\">";
                                if (!pipeline.aggregated) {
                                    html = html + '<h1>' + stage.name + '</h1>'
                                } else {
                                    if (stage.version) {
                                        html = html + '<h1>' + stage.name + ' - ' + stage.version + '</h1>'
                                    } else {
                                        html = html + '<h1>' + stage.name + ' - N/A</h1>'
                                    }
                                }
                                for (var k = 0; k < stage.tasks.length; k++) {
                                    var task = stage.tasks[k];
                                    var re = new RegExp(' ', 'g');

                                    var id = "task-" + task.id.replace(re, '_') + "_" + task.buildId;
                                    if (pipeline.aggregated) {
                                        id = "aggregated-task-" + task.id.replace(re, '_') + "_" + task.buildId;
                                    }

                                    var timestamp = formatDate(task.status.timestamp);

                                    tasks.push({id: id, taskId: task.id, buildId: task.buildId});

                                    html = html + "<div id=\"" + id + "\" class=\"task " + task.status.type +
                                        "\"><div class=\"taskname\"><a href=\"" + task.link + "\">" + task.name + "</a></div>";

                                    if (timestamp != "") {
                                html = html + "<span class='timestamp'>" + timestamp + "</span>"
                                    }

                            if (task.status.duration >= 0)
                                html = html + "<span class='duration'>"+ formatDuration(task.status.duration) + "</span>";

                                    html = html + "</div>"

                                }

                                html = html + "</section>";
                            }


                            html = html + "</section>";

                        }
                        html = html + "</section>";

                        Q("#" + divNames[c % divNames.length]).append(html);
                    if (popover) {
                        for (var x = 0; x < tasks.length; x++) {
                            var taskId = tasks[x].taskId;
                            var buildId = tasks[x].buildId;
                            Q('#' + tasks[x].id).on("mouseenter mouseleave", {taskId: taskId, buildId: buildId}, function (e) {
                                if (e.type == "mouseenter") {
                                    Q('#taskDetails').html('');
                                    view.getTask(e.data.taskId, e.data.buildId, function (call) {
                                        var stage = call.responseObject();

                                        Q('#taskDetails').html(stage.id);
                                    });


                                    Q('#taskDetails').show().css('top', e.pageY)
                                        .css('left', e.pageX);
                                } else {
                                    Q('#taskDetails').hide();
                                }
                            });
                        }
                    }
                }
                lastResponse = data;
                equalheight(".stage");
            }
        },
        error
:
    function (xhr, status, error) {
        Q("#" + errorDiv).html('Error communicating to server! ' + error);
    }
}
)
;

}

function formatDate(date) {
    if (date != null) {
        return moment(date, "YYYY-MM-DDTHH:mm:ss").fromNow()
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
        if(minutes == 0)
            minstr = "";
        else
            minstr = minutes + " min ";

        var secstr = "" + seconds + " sec";

        return minstr + secstr;
    }
}

function equalheight(container) {

    var currentTallest = 0,
        currentRowStart = 0,
        rowDivs = new Array(),
        $el,
        topPosition = 0;
    Q(container).each(function () {

        $el = Q(this);
        Q($el).height('auto')
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

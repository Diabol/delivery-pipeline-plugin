function renderPipelines(divNames, errorDiv, view) {
    var popover = false;
    Q("#" + errorDiv).html('');
    Q.ajax({
        url: 'api/json',
        dataType: 'json',
        async: false,
        cache: false,
        success: function (data) {

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

                    if (!pipeline.aggregated) {
                        html = html + '<h1>' + pipeline.version + ' (' + pipeline.triggeredBy + ')</h1>'
                    } else {
                        html = html + '<h1>Aggregated view</h1>'
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

                            tasks.push({id: id, taskId: task.id, buildId: task.buildId});

                            html = html + "<div id=\"" + id + "\"  class=\"task " + task.status.type +
                                "\"><div class=\"taskname\"><a href=\"" + task.link + "\">" + task.name + "</a></div>" +
                                "<div class=\"timestamp\">" + formatDate(task.status.timestamp) + "</div></div>"

                            //tasks.push([task.id +"-" + task.buildId]);
                        }

                        html = html + "</section>";
                    }


                    html = html + "</section>";

                }
                html = html + "</section>";
                Q("#" + divNames[c % divNames.length]).append(html);
                if (popover) {
                    for (var x = 0; x < tasks.length; x++) {
                        //console.log(tasks[x].id);
                        var taskId = tasks[x].taskId;
                        var buildId = tasks[x].buildId;
                        Q('#' + tasks[x].id).on("mouseenter mouseleave", {taskId: taskId, buildId: buildId}, function (e) {
                            if (e.type == "mouseenter") {
                                Q('#taskDetails').html('');
                                view.getTask(e.data.taskId, e.data.buildId, function(call) {
                                    //console.log(stage.responseObject().id);
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
        },
        error: function (xhr, status, error) {
            //window.alert("Error!")
            Q("#" + errorDiv).html('Error communicating to server!' + error);
        }
    });

}

function formatDate(date) {
    if (date != null) {
        return moment(date, "YYYY-MM-DDTHH:mm:ss").fromNow()
    } else {
        return "";
    }
}
function renderPipelines(divNames, errorDiv) {
    Q("#" + errorDiv).html('');
    Q.ajax({
        url: 'api/json',
        dataType: 'json',
        async: false,
        cache: false,
        success: function (data)
        {
            for (var z = 0; z < divNames.length ;z++) {
                Q("#" + divNames[z]).html('');
            }

            for (var i = 0; i < data.pipelines.length ;i++) {
                var pipeline = data.pipelines[i];
                var html = "<section class=\"pipe\">";

                if (!pipeline.aggregated) {
                    html = html + '<h1>' + pipeline.name + ' - ' + pipeline.version + ' (' + pipeline.triggeredBy + ')</h1>'
                } else {
                    html = html + '<h1>' + pipeline.name + ' (aggregated)</h1>'
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
                            html = html + "<span class=\"" + task.status.type  +"\"><a href=\"" + task.link +"\">"+ task.name + "</a></span>"
                        }

                        html = html + "</section>";
                    }


                html = html + "</section>";

                Q("#" + divNames[i%divNames.length]).append(html);

            }
        },
        error: function (xhr, status, error) {
            //window.alert("Error!")
            Q("#" + errorDiv).html('Error communicating to server!');
        }
    });

}
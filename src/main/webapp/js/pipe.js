function renderPipelines(divNames, aggregated) {

    Q.ajax({
        url: 'api/json',
        dataType: 'json',
        success: function (data)
        {
            for (var i = 0; i < divNames.length ;i++) {
                Q("#" + divNames[i]).html('');
            }

            for (var i = 0; i < data.pipelines.length ;i++) {
                var pipeline = data.pipelines[i];
                var html = "<section class=\"pipe\">";

                if (!aggregated) {
                    html = html + '<h1>' + pipeline.name + ' - ' + pipeline.version + ' (' + pipeline.triggeredBy + ')</h1>'
                } else {
                    html = html + '<h1>' + pipeline.name + '</h1>'
                }

                for (var j = 0; j < pipeline.stages.length; j++) {
                    var stage = pipeline.stages[j];
                        html = html + "<section class=\"stage\">";
                        if (!aggregated) {
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
        }
    });

}
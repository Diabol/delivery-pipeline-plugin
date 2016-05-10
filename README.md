Delivery Pipeline Plugin
========================

[![Build Status](https://travis-ci.org/Diabol/delivery-pipeline-plugin.png)](https://travis-ci.org/Diabol/delivery-pipeline-plugin)
[![Coverage Status](https://coveralls.io/repos/Diabol/delivery-pipeline-plugin/badge.png?branch=master)](https://coveralls.io/r/Diabol/delivery-pipeline-plugin?branch=master)

For info see the [Delivery Pipeline Plugin - Wiki](https://wiki.jenkins-ci.org/display/JENKINS/Delivery+Pipeline+Plugin)

[Issue tracker](https://issues.jenkins-ci.org/secure/IssueNavigator.jspa?mode=hide&reset=true&jqlQuery=project+%3D+JENKINS+AND+status+in+%28Open%2C+%22In+Progress%22%2C+Reopened%29+AND+component+%3D+%27delivery-pipeline-plugin%27) for bugs, improvements and new features, please report any issues on component [delivery-pipeline-plugin](https://issues.jenkins-ci.org/browse/JENKINS/component/18134)

Contributed by [Diabol AB](http://www.diabol.se)

How to contribute
---
Read GitHub's general contribution guidelines: https://guides.github.com/activities/contributing-to-open-source/#contributing

It basically comes down to the following guidelines:
 1. If applicable, create a [Jira issue](https://issues.jenkins-ci.org/browse/JENKINS/component/18134)
    + Make sure a similar issue doesn't already exist
 2. Fork the repo
 3. Contribute and have fun!
 4. Add as much unit testing as possible to any new code changes
    + This will make the code much more easy to maintain and to understand its intent
 5. Make sure your code is well formatted and aligns with the projects code style conventions
 6. Make sure to prefix the commit message with the associated Jira issue number together with a descriptive commit message
 7. If you have multiple commits, please make sure to squash them before creating a pull request
    + It's hard to follow contributions when they are scattered across several commits
 8. Create a pull request to get feedback from the maintainers
    + Add a link to the pull request to the associated Jira issue

Build
---

    mvn install

Run locally
---
    mvn hpi:run

Run function tests
---
    mvn integration-test

Build and run the Delivery Pipeline plugin in a Docker container
----
    mvn install
    docker build -t dpp .
    docker run -p 8080:8080 dpp

Run Jenkins with the latest released Delivery Pipeline plugin in a Docker container
---
	docker run -dt -p 8080:8080 diabol/delivery-pipeline-plugin:0.9.9

If you run on Mac and use boot2docker, enable port forwarding between your host and boot2docker VM:

    VBoxManage controlvm boot2docker-vm natpf1 8080,tcp,,8080,,8080

Configuring manually triggered jobs
----
**Note:** This requires the Build Pipeline plugin to be installed.

To be able to configure a certain job in the pipeline as a manual step, you have to configure the upstream job that triggers the job which is to be performed manually to be marked as a manual step.

In the Jenkins UI this shows up as a Post-Build Action: Build other projects (manual step), where you configure the name of the job to be manually triggered in the "Downstream Project Names".

If you're creating your jobs with JobDSL, use the following syntax in the publishers section (parameters is optional):

    publishers {
        buildPipelineTrigger('name-of-the-manually-triggered-job') {
            parameters {
                propertiesFile('env.${BUILD_NUMBER}.properties')
            }
        }
    }

In your pipeline configuration, make sure to enable manual triggers. The manual triggers (a play button) will not be shown in the UI for aggregate pipelines, only for pipeline instances. If you want to access manual triggers from the UI, make sure to show at least one pipeline instance.

Here is an example of a corresponding JobDSL pipeline view configuration: 

    deliveryPipelineView("my-pipeline") {
        name("my-pipeline")
        description("Delivery pipeline with a manual trigger")
        pipelineInstances(1)
        showAggregatedPipeline(false)
        columns(1)
        updateInterval(2)
        enableManualTriggers(true)
        showAvatars(false)
        showChangeLog(true)
        pipelines {
            component("My pipeline", "the-name-of-the-first-job-in-the-pipeline")
        }
    }

Using a custom CSS
----
Here is an example of how to specify a custom CSS for the Delivery Pipeline Plugin using a JobDSL pipeline view configuration:

    deliveryPipelineView("my-pipeline") {
        name("my-pipeline")
        description("Delivery pipeline with custom full screen CSS")
        pipelineInstances(1)
        showAggregatedPipeline(false)
        columns(1)
        updateInterval(2)
        enableManualTriggers(true)
        showAvatars(false)
        showChangeLog(true)
        configure { node ->
            node << {
                fullScreenCss('https://my-jenkins-instance/userContent/my-pipeline-fullscreen.css')
            }
        }
        pipelines {
            component("My pipeline", "the-name-of-the-first-job-in-the-pipeline")
        }
    }

For Jenkins Job Builder job configuration examples, see: [demo.yaml](https://github.com/Diabol/delivery-pipeline-plugin/blob/master/examples/demo.yaml)

For JobDSL job configuration examples, see: [demo.groovy](https://github.com/Diabol/delivery-pipeline-plugin/blob/master/examples/demo.groovy)

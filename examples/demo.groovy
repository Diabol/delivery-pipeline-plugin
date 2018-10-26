folder('Demo')

deliveryPipelineView('Demo/Pipeline') {
    pipelineInstances(5)
    allowPipelineStart()
    enableManualTriggers()
    showChangeLog()
    pipelines {
        component('Component', 'Demo/Build')
    }
}

job('Demo/Build') {
    deliveryPipelineConfiguration("Build", "Build")
    scm {
        git {
            remote {
                url('https://github.com/Diabol/delivery-pipeline-plugin')
            }
        }
    }
    wrappers {
        deliveryPipelineVersion('1.0.0.\$BUILD_NUMBER', true)
    }
    publishers {
        downstreamParameterized {
            trigger('Demo/Sonar') {
                parameters {
                    currentBuild()
                }
            }
            trigger('Demo/DeployCI') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

job('Demo/Sonar') {
    deliveryPipelineConfiguration("Build", "Static code analysis")
    scm {
        git {
            remote {
                url('https://github.com/Diabol/delivery-pipeline-plugin')
            }
        }
    }

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 10'
    }
}

job('Demo/DeployCI') {
    deliveryPipelineConfiguration("CI", "Deploy")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 5'
    }

    publishers {
        downstreamParameterized {
            trigger('Demo/TestCI') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

job('Demo/TestCI') {
    deliveryPipelineConfiguration("CI", "Test")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 10'
    }


    publishers {
        buildPipelineTrigger('Demo/DeployQA') {
        }
    }
}

job('Demo/DeployQA') {
    deliveryPipelineConfiguration("QA", "Deploy")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 5'
    }

    publishers {
        downstreamParameterized {
            trigger('Demo/TestQA') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

job('Demo/TestQA') {
    deliveryPipelineConfiguration("QA", "Test")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 10'
    }

    publishers {
        buildPipelineTrigger('Demo/DeployProd') {
        }
    }
}

job('Demo/DeployProd') {
    deliveryPipelineConfiguration("Prod", "Deploy")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 5'
    }

    publishers {
        downstreamParameterized {
            trigger('Demo/TestProd') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                }
            }
        }
    }
}

job('Demo/TestProd') {
    deliveryPipelineConfiguration("Prod", "Test")

    wrappers {
        buildName('\$PIPELINE_VERSION')
    }

    steps {
        shell 'sleep 5'
    }
}


Delivery Pipeline Plugin
========================

In Continuous Delivery visualisation is one of the most important areas.
When using Jenkins as a build server it is now possible with the Delivery Pipeline Plugin to visualise one or more
Delivery Pipelines in the same view even in full screen.
![Screenshot](http://beta.diabol.se/delivery-pipeline-plugin/screenshot1.png)   

Jenkins jobs is tagged with a stage and a taskname.
In the screenshot above the pipeline consists of four stages called Build, CI, QA and Production. 
The second stage is called CI and consists of two tasks called Deploy and Function Tests.

Installation
------------
* Manage Jenkins -> Manage Plugins -> Available
* Search for Delivery Pipeline Plugin


Configuration
-------------
* Create jobs with downstream/upstream relationships.
* Tag your Jenkins jobs with which stage it belongs to and the task name.
![Configuration](http://beta.diabol.se/delivery-pipeline-plugin/config.png)   
* Create a view by clicking the +  
![New View](http://beta.diabol.se/delivery-pipeline-plugin/newview.png)   
* Choose Delivery Pipeline View and give it a name  
![New View](http://beta.diabol.se/delivery-pipeline-plugin/configview1.png)
* Configure the view by choosing the number of pipeline instances, component  
name and choose the first Jenkins job for each pipeline.
![New View](http://beta.diabol.se/delivery-pipeline-plugin/configview2.png)   
* Done

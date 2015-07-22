/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline.functionaltest;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class DeliveryPipelinePage {

    private WebDriver webDriver;
    private String baseUrl;
    private String viewUri;


    public DeliveryPipelinePage(WebDriver webDriver, String baseUrl, String viewUri) {
        this.webDriver = webDriver;
        this.baseUrl = baseUrl;
        this.viewUri = viewUri;
    }


    public void open() {
        webDriver.get(baseUrl + viewUri);
    }


    public void triggerManual(String build) {
        String id = "manual-task-" + build;

        webDriver.findElement(By.id(id)).click();
    }

    public void triggerRebuild(String build) {
        String id = "rebuild-task-" + build;

        webDriver.findElement(By.id(id)).click();
    }

    public void triggerNewPipelineBuild(String no){
        webDriver.findElement(By.id("startpipeline-" + no)).click();
   }

    public void triggerNewParameterizedPipelineBuild(String no){
        webDriver.findElement(By.id("startpipeline-" + no)).click();
        /* this build requires parameter */
        webDriver.findElement(By.id("yui-gen1-button")).click();
   }

}

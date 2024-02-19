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
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

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

        SeleniumUtil.waitForElement(webDriver,"manual-task-" + build).click();

    }

    public void triggerRebuild(String build) {
        String id = "rebuild-task-" + build;
        SeleniumUtil.waitForElement(webDriver,"rebuild-task-" + build).click();
    }

    public void triggerNewPipelineBuild(String no) {
        SeleniumUtil.waitForElement(webDriver,"startpipeline-" + no).sendKeys(Keys.RETURN);
        SeleniumUtil.waitForElement(webDriver,"task-Start0.timestamp");
    }

    public void triggerNewParameterizedPipelineBuild(String no) {
        SeleniumUtil.waitForElement(webDriver,"startpipeline-" + no).sendKeys(Keys.RETURN);
        /* this build requires parameter */
        webDriver.findElement(By.xpath("//button[@class=\"jenkins-button jenkins-button--primary jenkins-!-build-color\"]")).click();
    }

    public String getJsPlumbUtilityVariable() {
        JavascriptExecutor jse = (JavascriptExecutor) webDriver;
        return jse.executeScript("return jsPlumbUtilityVariable.length").toString();
    }
}

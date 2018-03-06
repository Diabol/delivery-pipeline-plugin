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

public class NewJobPage {

    private WebDriver webDriver;
    private String baseUrl;


    public NewJobPage(WebDriver webDriver, String baseUrl) {
        this.webDriver = webDriver;
        this.baseUrl = baseUrl;
    }

    public NewJobPage open() {
        webDriver.get(baseUrl + "/newJob");
        SeleniumUtil.waitForElement(webDriver, "ok-button");
        return this;
    }

    public void setJobName(String name) {
        SeleniumUtil.waitForElement(webDriver, "ok-button");
        webDriver.findElement(By.xpath("//*[@id=\"name\"]")).sendKeys(name);
    }

    public void setFreeStyle() {
        webDriver.findElement(By.xpath("//*[@id=\"j-add-item-type-standalone-projects\"]/ul/li[1]")).click();
    }

    public ConfigureJobPage submit() {
        webDriver.findElement(By.id("ok-button")).click();
        return new ConfigureJobPage(webDriver);
    }

}

package businessLogics;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

public class ZoomUtil {

    public static void zoomOut(WebDriver driver, String percentage) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.body.style.zoom='" + percentage + "'");
    }
    
    public static void zoomOutUsingKeys(WebDriver driver) {
        Actions actions = new Actions(driver);
        actions.keyDown(Keys.CONTROL)
               .sendKeys(Keys.SUBTRACT)
               .keyUp(Keys.CONTROL)
               .perform();
    }
    
    public static void scalePage(WebDriver driver, double scale) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "document.body.style.transform='scale(" + scale + ")';" +
            "document.body.style.transformOrigin='0 0';"
        );
    }
    
    public static void reduceTextSize(WebDriver driver, String fontSize) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "document.body.style.fontSize='" + fontSize + "';"
        );
    }
    
    public static void zoomOutHard(WebDriver driver, String percentage) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "document.documentElement.style.zoom='" + percentage + "';" +
            "document.body.style.zoom='" + percentage + "';"
        );
    }

    public static void scaleMainContainer(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript(
            "var el = document.querySelector('body');" +
            "el.style.transform='scale(0.8)';" +
            "el.style.transformOrigin='0 0';" +
            "document.body.style.width='125%';"
        );
    }

    public static void chromeZoomOut(WebDriver driver, int level) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("chrome.settingsPrivate.setDefaultZoom(" + level + ");");
    }



}

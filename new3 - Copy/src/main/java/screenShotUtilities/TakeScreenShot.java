package screenShotUtilities;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;


public class TakeScreenShot {
	 public static void captureElementScreenshot(WebElement element, String filePath) {
	        try {
	            File src = element.getScreenshotAs(OutputType.FILE);
	            File dest = new File(filePath);
	            FileUtils.copyFile(src, dest);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	 
	 public static void capturePageScreenshot(WebDriver driver, String filePath) {
	        try {
	            TakesScreenshot ts = (TakesScreenshot) driver;
	            File src = ts.getScreenshotAs(OutputType.FILE);
	            File dest = new File(filePath);
	            FileUtils.copyFile(src, dest);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	 
	 public static void captureFullPageScreenshot(WebDriver driver, String filePath) {
	        try {
	            Screenshot screenshot = new AShot()
	                    .shootingStrategy(ShootingStrategies.viewportPasting(1000))
	                    .takeScreenshot(driver);

	            ImageIO.write(screenshot.getImage(), "PNG", new File(filePath));
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

}

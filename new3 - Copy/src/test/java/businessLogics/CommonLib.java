package businessLogics;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CommonLib {
	
	public static void selectDropdownValue(WebDriver driver, By by, String value) {
        Select dropdown = new Select(driver.findElement(by));
        dropdown.selectByValue(value);
    }

	
	public static WebElement waitForElement(WebDriver driver, String locator) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
		return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locator)));
	}
}

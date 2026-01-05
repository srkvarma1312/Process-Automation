package script;

import static fileUtilities.GamesAdditionExcelUtility1.getGameDataFromExcel;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import businessLogics.BusinessUtilities1BackUP;
import businessLogics.ExcelSummaryWriter;
import businessLogics.ZoomUtil;
import fileUtilities.GamesAdditionExcelUtility1.GameData;
import io.github.bonigarcia.wdm.WebDriverManager;

public class AddGamesToLobbies1 {

	static Logger log = LogManager.getLogger(AddGamesToLobbies1.class);

	public static String getTimeStamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		return LocalDateTime.now().format(formatter);
	}

	@Test
	public void addGamesToLobbies() {

		List<GameData> gameDataFromExcel = getGameDataFromExcel(".\\src\\test\\resources\\TemplatesFile.xlsx");

		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		options.addArguments("--force-device-scale-factor=0.85");
        options.addArguments("--high-dpi-support=0.85");
		options.addArguments("--disable-notifications");
		options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });

		WebDriver driver = new ChromeDriver(options);

		log.info("Browser launched");
		log.info("Excel rows count => " + gameDataFromExcel.size());

		try {
			driver.manage().window().maximize();
			driver.get("https://backoffice.partygaming.com.e7new.com/home.action");
			log.warn("URL loaded");

			BusinessUtilities1BackUP bu = new BusinessUtilities1BackUP();

			// ✅ Correct login (matches method signature)
			bu.login(driver, "guest", "123123");

//    			Thread.sleep(20000);

			log.info("========== LOGIN SUCCESS ==========");

			driver.switchTo().newWindow(WindowType.TAB);
			driver.get("https://backoffice.partygaming.com.e7new.com/home.action");

			bu.navigateToLobbyManager(driver);

			int j = 0;
			for (GameData data : gameDataFromExcel) {

				// Skip rows where variants are empty / null
				if (data.getGameVariants() == null || data.getGameVariants().isEmpty()) {
					log.warn("Skipping brand (no variants): " + data.getLabel());
					j++;
					continue;
				}

				bu.configureLobbies(driver, new String[] { data.getLabel() }, j, gameDataFromExcel);
				j++;
			}

			log.info("Configured all applicable lobbies successfully");
			
			  ExcelSummaryWriter.writeSummary( "Game_Addition_Summary_" +getTimeStamp() +
			 ".xlsx", BusinessUtilities1BackUP.summaryMap );
			
		} catch (Exception e) {
			log.error("Execution failed", e);
		} finally {
			driver.quit();

		}
	}
}

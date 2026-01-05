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

import businessLogics.BusinessUtilitiesParallel;
import businessLogics.ExcelSummaryWriter;
import fileUtilities.GamesAdditionExcelUtility1.GameData;
import io.github.bonigarcia.wdm.WebDriverManager;

public class Desktop {

	private static ThreadLocal<List<GameData>> threadLocalGameData = new ThreadLocal<>();
	private static ThreadLocal<WebDriver> threadLocalDriver = new ThreadLocal<>();
	 private static final String CONFIG_PATH = ".\\src\\test\\resources\\DesktopTemplates.json";
    static Logger log = LogManager.getLogger(Desktop.class);
    String className = this.getClass().getSimpleName();

    
    public static String getTimeStamp() {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        return LocalDateTime.now().format(formatter);
    }

    public List<GameData> getGameDataFromExcelTest1() 
    {
    	
    	if (threadLocalGameData.get() == null) {
    		threadLocalGameData.set(getGameDataFromExcel(".\\src\\test\\resources\\Desktop.xlsx"));  // Load data from Excel
    	}
    	return threadLocalGameData.get();
    }
    @Test
    public void addGamesToLobbies() throws InterruptedException {

//    			getGameDataFromExcel(".\\src\\test\\resources\\TemplatesFile.xlsx");
    	List<GameData> gameDataFromExcel =  getGameDataFromExcelTest1();


    	 WebDriverManager.chromedriver().setup();
    	 Thread.sleep(5000);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--force-device-scale-factor=0.85");
        options.addArguments("--high-dpi-support=0.85");
        options.addArguments("--disable-notifications");
        options.setExperimentalOption(
                "excludeSwitches", new String[]{"enable-automation"});

        threadLocalDriver.set(new ChromeDriver(options));  // Initialize WebDriver
        WebDriver driver = threadLocalDriver.get();
        BusinessUtilitiesParallel bu = new BusinessUtilitiesParallel(driver,CONFIG_PATH);
        System.out.println("Test 1 - Thread: " + Thread.currentThread().getId());

        log.info("Browser launched");
        log.info("Excel rows count => " + gameDataFromExcel.size());

        try {
            driver.manage().window().maximize();
            driver.get("https://backoffice.partygaming.com.e7new.com/home.action");
            log.warn("URL loaded");

            // ✅ Correct login (matches method signature)
            bu.login( "guest","123123");
            
//    			Thread.sleep(20000);
    		
            log.info("========== LOGIN SUCCESS ==========");
            
            driver.switchTo().newWindow(WindowType.TAB);
            driver.get("https://backoffice.partygaming.com.e7new.com/home.action");

            bu.navigateToLobbyManager();

            int j = 0;
            for (GameData data : gameDataFromExcel) {

                // Skip rows where variants are empty / null
                if (data.getGameVariants() == null || data.getGameVariants().isEmpty()) {
                    log.warn("Skipping brand (no variants): " + data.getLabel());
                    j++;
                    continue;
                }

                bu.configureLobbies(
                      
                        new String[]{data.getLabel()},
                        j,
                        gameDataFromExcel,
                        className
                );
                j++;
            }

            log.info("Configured all applicable lobbies successfully");

            ExcelSummaryWriter.writeSummary(
            		"Game_Addition_Summary_Desktop" +getTimeStamp() + ".xlsx",
            		BusinessUtilitiesParallel.summaryMap
            		);
        } catch (Exception e) {
            log.error("Execution failed", e);
        } finally {
            driver.quit();

        }
    }
}

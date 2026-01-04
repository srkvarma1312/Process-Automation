package businessLogics;

import static businessLogics.BrandConfigUtil.loadConfig;
import static businessLogics.CommonLib.selectDropdownValue;

import java.awt.AWTException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import fileUtilities.GamesAdditionExcelUtility1.GameData;
import screenShotUtilities.TakeScreenShot;

public class BusinessUtilitiesParallel {

    private WebDriver driver;
    private static JavascriptExecutor js;
    private String CONFIG_PATH;

    public BusinessUtilitiesParallel(WebDriver driver,String configPath) {
        this.driver = driver;
        this.js = (JavascriptExecutor) driver;
        this.CONFIG_PATH= configPath;
        PageFactory.initElements(driver, this);
    }
    
    public static String getTimeStamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		return LocalDateTime.now().format(formatter);
	}

    /* ===================== LOGGER ===================== */

    static Logger log = LogManager.getLogger(BusinessUtilitiesParallel.class);

    /* ===================== SUMMARY ===================== */

    public static Map<String, GameSummary> summaryMap = new HashMap<>();

    private static GameSummary getSummary(String game) {
        return summaryMap.computeIfAbsent(game, GameSummary::new);
    }

    /* ===================== LOGIN ===================== */

    @FindBy(name = "j_username")
    private WebElement username;

    @FindBy(name = "j_password")
    private WebElement password;

    @FindBy(name = "submit")
    private WebElement submitBtn;

    public void login(String user, String pass) {
        username.sendKeys(user);
        password.sendKeys(pass);
        submitBtn.sendKeys(Keys.ENTER);

        try {
            driver.switchTo().alert().accept();
        } catch (Exception ignored) {}

        log.info("LOGIN SUCCESS");
    }

    /* ===================== NAVIGATION ===================== */

    @FindBy(xpath = "//a[contains(text(),'Casino')]//parent::span[contains(@class,'subnav')]")
    private WebElement casinoMenu;

    @FindBy(xpath = "//a[contains(text(),'Casino Admin')]")
    private WebElement casinoAdmin;

    @FindBy(xpath = "//*[@class='nav-icon-ADT']")
    private WebElement navIcon;

    @FindBy(id = "rcasino_adminCasinoLobbyManager")
    private WebElement lobbyManager;

    public void navigateToLobbyManager() throws InterruptedException {

        js.executeScript("arguments[0].click();", navIcon);
        Thread.sleep(2000);

        js.executeScript("arguments[0].click();", casinoMenu);
        Thread.sleep(2000);

        js.executeScript("arguments[0].click();", casinoAdmin);
        Thread.sleep(2000);

        js.executeScript("arguments[0].click();",
                lobbyManager.findElement(By.tagName("a")));
        Thread.sleep(3000);

        log.info("Navigated to Casino Lobby Manager");
    }

    /* ===================== LOBBY CONFIG ===================== */

    @FindBy(id = "templateBrandId")
    private WebElement templateBrand;

    @FindBy(id = "channelSelect")
    private WebElement channelSelect;

    @FindBy(id = "lobbyTypeSelect")
    private WebElement lobbyTypeSelect;

    @FindBy(id = "templateNameInput")
    private WebElement templateName;

    @FindBy(name = "listOfTemplates")
    private List<WebElement> templates;

    @FindBy(id = "proceedToLoad")
    private WebElement proceedBtn;

    public void configureLobbies(String[] brands,
                                 int rowIndex,
                                 List<GameData> excelData,String className)
            throws InterruptedException, AWTException {

        Map<String, String> expectedTemplates = loadConfig(CONFIG_PATH);

        for (String brand : brands) {

        	WebElement configureLobby = driver
					.findElement(By.xpath("//*[@id='rcasino_adminCasinoLobbyManagerConfigureLobby']/a"));
			js.executeScript("arguments[0].click();", configureLobby);
			Thread.sleep(2000);
			GameData row = excelData.get(rowIndex);

			log.info("========== BRAND START : " + brand + " ==========");
            
           

            if (row.getGameVariants() == null || row.getGameVariants().isEmpty()) {
                log.warn("No variants for brand: " + brand);
                continue;
            }

            if (!expectedTemplates.containsKey(brand.toUpperCase())) {
                log.warn("Template not configured for brand: " + brand);
                continue;
            }

            if (!expectedTemplates.get(brand.toUpperCase())
                    .equalsIgnoreCase(row.getTemplate())) {
                log.error("Template mismatch for brand: " + brand);
                continue;
            }

            templateBrand.clear();
            templateBrand.sendKeys(brand);
            Thread.sleep(2000);

            selectDropdownValue(driver, By.id("channelSelect"), row.getChannel());
            Thread.sleep(2000);
            selectDropdownValue(driver, By.id("lobbyTypeSelect"), row.getLobbyType());
            Thread.sleep(2000);

            templateName.sendKeys(row.getTemplate());
            Thread.sleep(2000);

            for (WebElement radio : templates) {
                if (row.getTemplate()
                        .equalsIgnoreCase(radio.getAttribute("tname"))) {
                    radio.click();
                    break;
                }
            }

            proceedBtn.click();
            Thread.sleep(5000);

			// Category & Subcategory
			try {
				driver.findElement(By.xpath("//input[@value='" + row.getCategories() + "']")).click();
				Thread.sleep(2000);

				if (row.getSubCategory() != null) {
					driver.findElement(By.xpath("//input[@value='" + row.getSubCategory() + "']")).click();
				}
			} catch (Exception e) {
				log.warn("Category/Subcategory not found");
			}

			Thread.sleep(5000);

            addGames(row.getGameVariants().toArray(new String[0]), brand,className);

            log.info("========== BRAND END : " + brand + " ==========");
        }
    }

    /* ===================== GAME ADDITION ===================== */
    
    private static String getClusterMessage(WebDriver driver) {

		try {
			WebElement lcgMsg = driver.findElement(By.id("LCG_CLUSTER_MESSAGE"));
			js.executeScript("arguments[0].scrollIntoView(true);", lcgMsg);

			if (lcgMsg.isDisplayed()) {
				log.info("LCG message found: " + lcgMsg.getText());
				return lcgMsg.getText();
			}
		} catch (Exception ignored) {
		}

		try {
			WebElement gvcMsg = driver.findElement(By.id("GVC_CLUSTER_MESSAGE"));
			js.executeScript("arguments[0].scrollIntoView(true);", gvcMsg);
			if (gvcMsg.isDisplayed()) {
				log.info("GVC message found: " + gvcMsg.getText());
				return gvcMsg.getText();
			}
		} catch (Exception ignored) {
		}

		String errorMsg = " Cluster message not found! Neither LCG nor GVC message is visible.";
		log.error(errorMsg);

		throw new ClusterMessageNotFoundException(errorMsg);
	}

	private static String extractClusterFromMessage(String message) {
		Pattern pattern = Pattern.compile("^(LCG_CLUSTER|GVC_CLUSTER)");
		Matcher matcher = pattern.matcher(message);

		if (matcher.find()) {
			return matcher.group(1);
		}

		throw new RuntimeException("Cluster not found in message: " + message);
	}

//	private static boolean isGameOnRightSide(WebDriver driver, String game) {
//		return driver
//				.findElements(By
//						.xpath("//div[@id='div2Main2SubPanel2']//*[contains(normalize-space(text()),'" + game + "')]"))
//				.size() > 0;
//	}
    
	private boolean isGameOnRightSide(String game) {
		return driver.findElements(By.xpath(
				"//div[@id='div2Main2SubPanel2']//*[contains(text(),'" + game + "')]"))
				.size() > 0;
	}
//================================================================
    @FindBy(id = "typeGameVariantName")
    private WebElement gameSearchInput;

    @FindBy(id = "searchGameVariant")
    private WebElement searchGameBtn;

    @FindBy(id = "gamePosition")
    private WebElement gamePosition;

    @FindBy(id = "moveGames")
    private WebElement moveGamesBtn;


    public void addGames(String[] games, String brand,String className)
            throws InterruptedException {

        for (String game : games) {

            log.info("Processing game variant: " + game);
            GameSummary summary = getSummary(game);

            gameSearchInput.clear();
            gameSearchInput.sendKeys(game);
            searchGameBtn.click();
            Thread.sleep(4000);

            List<WebElement> left =
                    driver.findElements(By.xpath(
                            "//div[@id='div2Main1SubPanel2']//input[@type='checkbox']"));

            if (!left.isEmpty()) {
                left.get(0).click();
                gamePosition.clear();
                gamePosition.sendKeys("1");
                moveGamesBtn.click();
                summary.addNewlyAdded(brand);

            } else if (isGameOnRightSide(game)) {
                summary.addAlreadyAdded(brand);
                log.info("ALREADY ADDED (Right Side): " + game);

            } else {
                summary.addNotConfigured(brand);
                log.warn("NOT CONFIGURED: " + game);
            }
        }
        
     // Save & Publish
     		driver.findElement(By.id("savePublishTemplate")).click();
     		Thread.sleep(5000);

     		String msg = getClusterMessage(driver);

     		String cluster_Name = extractClusterFromMessage(msg);

     		String refreshId = msg.replaceAll("\\D", "");
     		
     		
     		String path = "ScreenShots/"+className+"Save&Publish_" +getTimeStamp() + ".png";
     		

     		TakeScreenShot.capturePageScreenshot(driver,path);

     		log.info("RefreshId ==> " + refreshId);

     		waitForRefreshCompletion(refreshId, cluster_Name,className);
    }

    /* ===================== STATUS UPDATE ===================== */
    private void openStatusUpdate(WebDriver driver) throws InterruptedException {


		WebElement techSpan = driver.findElement(By.id("rcasino_adminTechnology"));
		WebElement techLink = techSpan.findElement(By.tagName("a"));
		js.executeScript("arguments[0].click();", techLink);

		Thread.sleep(3000);

		js.executeScript("handleUrls('rcasino_adminTechnologyRefreshDetails'," + "'Status Update',"
				+ "'/casino/CDSUpdateStatus.action',event);");

		Thread.sleep(5000);

		log.info("Status Update page opened");
	}
    //============================================================================
    @FindBy(id = "activeClusterName")
	private WebElement clusterSelect;

	
	private void selectClusterBasedOnBrand(WebDriver driver, String cluster) {

		log.info("Selecting Cluster: " + cluster);

		selectDropdownValue(driver, By.id("activeClusterName"), cluster);
	}
	
    @FindBy(id = "rcasino_adminTechnology")
    private WebElement techMenu;

    public void waitForRefreshCompletion(String refreshId,String cluster,String className)
            throws InterruptedException {
    	
    	log.info("Waiting for refreshId: " + refreshId);
    	
		// Open status update page
		openStatusUpdate(driver);

		// Select cluster
		selectClusterBasedOnBrand(driver, cluster);
		
		int maxRetries = 50;
	    int waitTime = 5000;

        js.executeScript("arguments[0].click();",
                techMenu.findElement(By.tagName("a")));
        Thread.sleep(3000);

        js.executeScript(
                "handleUrls('rcasino_adminTechnologyRefreshDetails'," +
                        "'Status Update','/casino/CDSUpdateStatus.action',event);");

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
//	    	ZoomUtil.zoomOut(driver, "50%");
	    	List<WebElement> status = driver.findElements(By.xpath("//td[normalize-space()='" + refreshId
	    			+ "']//following-sibling::td//tr[@class='statusSucces']//td[3]"));
	    	log.info("Waiting for status size : " + status.size());

	        log.info(" Checking status attempt: " + attempt);

	        List<WebElement> statusList = status;

	        boolean allCompleted = true;

	        int row = 1;
	        for (WebElement statusCell : statusList) {

	            String statusText = statusCell.getText().trim();
	            log.info("Row " + row + " Status: " + statusText);

	            if (!"Completed".equalsIgnoreCase(statusText)) {
	                allCompleted = false;
	                log.warn(" Row " + row + " not completed yet.");
	                break;
	            }

	            row++;
	        }
	        
	       
	        
	        if (allCompleted && !statusList.isEmpty()) {
	            log.info(" ALL SERVICES COMPLETED for Refresh ID: " + refreshId);
	            String path = "ScreenShots/"+className+"table_" +getTimeStamp() + ".png";
		        WebElement table = driver.findElement(By.xpath("//td[text()='"+refreshId+"']/parent::tr"));
		        js.executeScript("arguments[0].scrollIntoView(true);", table);
//		        TakeScreenShot.captureElementScreenshot(table,".\\ScreenShots");
		        TakeScreenShot.captureElementScreenshot(table,path);
	            return;
	        }

	        // Refresh the table
	        log.info(" Refreshing status table...");
	        js.executeScript(
	                "handleUrls('rcasino_adminTechnologyRefreshDetails'," +
	                "'Status Update','/casino/CDSUpdateStatus.action',event);");

	        Thread.sleep(waitTime);
	    }

        throw new RuntimeException(" Status NOT completed for all services. RefreshId: " + refreshId);

    }
    //===========================================================================================================================
}

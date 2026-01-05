package businessLogics;

import static businessLogics.BrandConfigUtil.loadConfig;
import static businessLogics.CommonLib.selectDropdownValue;

import java.awt.AWTException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import fileUtilities.GamesAdditionExcelUtility1.GameData;
import screenShotUtilities.TakeScreenShot;

public class BusinessUtilities1BackUP {
	
	public static String getTimeStamp() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
		return LocalDateTime.now().format(formatter);
	}
	
	static Logger log = LogManager.getLogger(BusinessUtilities1BackUP.class);
	private static final String CONFIG_PATH = ".\\src\\test\\resources\\brand_template_config.json";
	
	/* ===================== SUMMARY ===================== */

	public static Map<String, GameSummary> summaryMap = new HashMap<>();

	private static GameSummary getSummary(String game) {
		return summaryMap.computeIfAbsent(game, GameSummary::new);
	}

	// ======================== LOGIN ========================

	public void login(WebDriver driver, String username, String password) {

		driver.findElement(By.name("j_username")).sendKeys(username);

		driver.findElement(By.name("j_password")).sendKeys(password);

		driver.findElement(By.name("submit")).sendKeys(Keys.ENTER);

		try {
			driver.switchTo().alert().accept();
		} catch (Exception ignored) {
		}

		log.info("LOGIN SUCCESS");
	}

	// ======================== NAVIGATION ========================

	public void clickCasinoAdmin(WebDriver driver) throws InterruptedException {

		JavascriptExecutor js = (JavascriptExecutor) driver;

		WebElement casinoMenu = driver
				.findElement(By.xpath("//a[contains(text(),'Casino')]//parent::span[contains(@class,'subnav')]"));
		js.executeScript("arguments[0].click();", casinoMenu);
		Thread.sleep(3000);

		WebElement casinoAdmin = driver.findElement(By.xpath("//a[contains(text(),'Casino Admin')]"));
		js.executeScript("arguments[0].click();", casinoAdmin);
		Thread.sleep(2000);
	}

	public void navigateToLobbyManager(WebDriver driver) throws InterruptedException {

		JavascriptExecutor js = (JavascriptExecutor) driver;

		WebElement navIcon = driver.findElement(By.xpath("//*[@class='nav-icon-ADT']"));
		js.executeScript("arguments[0].click();", navIcon);
		Thread.sleep(2000);

		clickCasinoAdmin(driver);

		WebElement lobbyManager = driver.findElement(By.xpath("//*[@id='rcasino_adminCasinoLobbyManager']/a"));
		js.executeScript("arguments[0].click();", lobbyManager);
		Thread.sleep(3000);

		log.info("Navigated to Casino Lobby Manager");
	}

	// ======================== MAIN CONFIG FLOW ========================

	public void configureLobbies(WebDriver driver, String[] brands, int j, List<GameData> gameDataFromExcel)
			throws InterruptedException, AWTException {

		Map<String, String> expectedTemplates = loadConfig(CONFIG_PATH);

		for (String brand : brands) {

			WebElement configureLobby = driver
					.findElement(By.xpath("//*[@id='rcasino_adminCasinoLobbyManagerConfigureLobby']/a"));
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", configureLobby);
			Thread.sleep(2000);

			log.info("========== BRAND START : " + brand + " ==========");

			GameData row = gameDataFromExcel.get(j);

			if (row.getGameVariants() == null || row.getGameVariants().isEmpty()) {
				log.warn("No variants for brand: " + brand);
				continue;
			}

			if (!expectedTemplates.containsKey(brand.toUpperCase())) {
				log.warn("Template not configured for brand: " + brand);
				continue;
			}

			if (!expectedTemplates.get(brand.toUpperCase()).equalsIgnoreCase(row.getTemplate())) {
				log.error("Template mismatch for brand: " + brand);
				continue;
			}

			driver.findElement(By.id("templateBrandId")).clear();
			driver.findElement(By.id("templateBrandId")).sendKeys(brand);
			Thread.sleep(2000);

			selectDropdownValue(driver, By.id("channelSelect"), row.getChannel());
			Thread.sleep(2000);

			selectDropdownValue(driver, By.id("lobbyTypeSelect"), row.getLobbyType());
			Thread.sleep(2000);

			driver.findElement(By.id("templateNameInput")).sendKeys(row.getTemplate());
			Thread.sleep(2000);

			for (WebElement radio : driver.findElements(By.name("listOfTemplates"))) {
				if (row.getTemplate().equalsIgnoreCase(radio.getAttribute("tname"))) {
					radio.click();
					break;
				}
			}

			driver.findElement(By.id("proceedToLoad")).click();
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

			addGames(driver, row.getGameVariants().toArray(new String[0]));

			log.info("========== BRAND END : " + brand + " ==========");
		}
	}

	// ======================== GAME ADDITION ========================

	private static String getClusterMessage(WebDriver driver) {

		try {
			WebElement lcgMsg = driver.findElement(By.id("LCG_CLUSTER_MESSAGE"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", lcgMsg);

			if (lcgMsg.isDisplayed()) {
				log.info("LCG message found: " + lcgMsg.getText());
				return lcgMsg.getText();
			}
		} catch (Exception ignored) {
		}

		try {
			WebElement gvcMsg = driver.findElement(By.id("GVC_CLUSTER_MESSAGE"));
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", gvcMsg);
			if (gvcMsg.isDisplayed()) {
				log.info("GVC message found: " + gvcMsg.getText());
				return gvcMsg.getText();
			}
		} catch (Exception ignored) {
		}

		// ❌ If neither found → throw exception
		String errorMsg = "❌ Cluster message not found! Neither LCG nor GVC message is visible.";
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

	private static boolean isGameOnRightSide(WebDriver driver, String game) {
		return driver
				.findElements(By
						.xpath("//div[@id='div2Main2SubPanel2']//*[contains(normalize-space(text()),'" + game + "')]"))
				.size() > 0;
	}

	public void addGames(WebDriver driver, String[] games) throws InterruptedException {

		for (String game : games) {

			log.info("Processing game variant: " + game);
			GameSummary summary = getSummary(game);

			WebElement input = driver.findElement(By.id("typeGameVariantName"));
			input.clear();
			input.sendKeys(game);
			Thread.sleep(2000);

			driver.findElement(By.id("searchGameVariant")).click();
			Thread.sleep(4000);

			List<WebElement> left = driver
					.findElements(By.xpath("//div[@id='div2Main1SubPanel2']//input[@type='checkbox']"));

			if (!left.isEmpty()) {

				left.get(0).click();
				Thread.sleep(2000);

				driver.findElement(By.id("gamePosition")).clear();
				driver.findElement(By.id("gamePosition")).sendKeys("1");
				Thread.sleep(2000);

				driver.findElement(By.id("moveGames")).click();
				Thread.sleep(3000);

				log.info("NEWLY ADDED: " + game);

			} else if (isGameOnRightSide(driver, game)) {

				log.info("ALREADY ADDED (Right Side): " + game);

			} else {

				log.warn("NOT CONFIGURED: " + game);
			}
		}

		// Save & Publish
		driver.findElement(By.id("savePublishTemplate")).click();
		Thread.sleep(5000);

		String msg = getClusterMessage(driver);

		String cluster_Name = extractClusterFromMessage(msg);

		String refreshId = msg.replaceAll("\\D", "");
		
		
		String path = "ScreenShots/Save&Publish_" +getTimeStamp() + ".png";
		
//		TakeScreenShot.captureFullPageScreenshot(driver,".\\ScreenShots");
		TakeScreenShot.capturePageScreenshot(driver,path);

		log.info("RefreshId ==> " + refreshId);

		waitForRefreshCompletion(driver, refreshId, cluster_Name);
	}

	// ======================== STATUS UPDATE (FIXED) ========================

	private void openStatusUpdate(WebDriver driver) throws InterruptedException {

		JavascriptExecutor js = (JavascriptExecutor) driver;

		WebElement techSpan = driver.findElement(By.id("rcasino_adminTechnology"));
		WebElement techLink = techSpan.findElement(By.tagName("a"));
		js.executeScript("arguments[0].click();", techLink);

		Thread.sleep(3000);

		js.executeScript("handleUrls('rcasino_adminTechnologyRefreshDetails'," + "'Status Update',"
				+ "'/casino/CDSUpdateStatus.action',event);");

		Thread.sleep(5000);

		log.info("Status Update page opened");
	}

	@FindBy(id = "activeClusterName")
	private WebElement clusterSelect;

	private static final Set<String> LCG_BRANDS = Set.of("LADBROKEUK", "CORAL", "BWINDE", "LADBROKEDE", "PPDE", "PSDE",
			"PREMIUMDE", "SCDE", "SBDE", "GBDE");

	private void selectClusterBasedOnBrand(WebDriver driver, String cluster) {

		log.info("Selecting Cluster: " + cluster);

		selectDropdownValue(driver, By.id("activeClusterName"), cluster);
	}

	public void waitForRefreshCompletion(WebDriver driver, String refreshId, String cluster)
			throws InterruptedException {

		log.info("Waiting for refreshId: " + refreshId);

		// Open status update page
		openStatusUpdate(driver);

		// Select cluster
		selectClusterBasedOnBrand(driver, cluster);

		int maxRetries = 50;
	    int waitTime = 5000;

	    for (int attempt = 1; attempt <= maxRetries; attempt++) {

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
	            String path = "ScreenShots/table_" +getTimeStamp() + ".png";
		        WebElement table = driver.findElement(By.xpath("//td[text()='"+refreshId+"']/parent::tr"));
		        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", table);
//		        TakeScreenShot.captureElementScreenshot(table,".\\ScreenShots");
		        TakeScreenShot.captureElementScreenshot(table,path);
	            return;
	        }

	        // Refresh the table
	        log.info(" Refreshing status table...");
	        ((JavascriptExecutor) driver).executeScript(
	                "handleUrls('rcasino_adminTechnologyRefreshDetails'," +
	                "'Status Update','/casino/CDSUpdateStatus.action',event);");

	        Thread.sleep(waitTime);
	    }

	    throw new RuntimeException(" Status NOT completed for all services. RefreshId: " + refreshId);

		}

	}

	// ============================================================================================



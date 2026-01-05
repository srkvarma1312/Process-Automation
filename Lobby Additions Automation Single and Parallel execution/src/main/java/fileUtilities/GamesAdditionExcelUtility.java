package fileUtilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class GamesAdditionExcelUtility {
	static Logger log = LogManager.getLogger(GamesAdditionExcelUtility.class);

    public static class GameData {
        private String label;
        private String template;
        private List<String> gameVariants;

        public GameData(String label, String template, List<String> gameVariants) {
            this.label = label;
            this.template = template;
            this.gameVariants = gameVariants;
        }

        public String getLabel() {
            return label;
        }

        public String getTemplate() {
            return template;
        }

        public List<String> getGameVariants() {
            return gameVariants;
        }

        @Override
        public String toString() {
            return "GameData{" +
                    "label='" + label + '\'' +
                    ", template='" + template + '\'' +
                    ", gameVariants=" + gameVariants +
                    '}';
        }
    }

    public static List<GameData> getGameDataFromExcel(String filePath) {
    	List<GameData> list = new ArrayList<>();
      
      try (FileInputStream fis = new FileInputStream(filePath);
    		     Workbook workbook = new XSSFWorkbook(fis);
    		     ) 
      {
    	  log.info(" excel data reader  ");
    	  Sheet sheet = workbook.getSheet("QA2Templates");
      
    		    // Start from row index 1 to skip header
    	  log.info(" sheet.getLastRowNum()  " +sheet.getLastRowNum());
    	  
          for (int i = 1; i <= sheet.getLastRowNum(); i++) 
    		    {
//    		    	log.info(" excel data reader  ");
    		        Row row = sheet.getRow(i);
    		        
    		        if (row == null)
    		        {
    		        	log.info(" list is null of row varable or not fetched the data properly ");
    		        	return list;
                        
    		        }

            Cell labelCell = row.getCell(1);       // Label column (B)
            Cell templateCell = row.getCell(2);    // Template column (C)
            Cell gameVariantsCell = row.getCell(3); // Game Variants column (D)

            String label = labelCell != null ? labelCell.getStringCellValue() : null;
            String template = templateCell != null ? templateCell.getStringCellValue() : null;
            String gameVariantsStr = gameVariantsCell != null ? gameVariantsCell.getStringCellValue() : null;

            List<String> gameVariants = null;
            
            if (gameVariantsStr != null && !gameVariantsStr.isEmpty())
            {
                gameVariants = Arrays.stream(gameVariantsStr.split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }
            
            list.add(new GameData(label, template, gameVariants));
            
            log.info(" adding values to list  ");
            
        	}
    		    log.info(" excel data reading done   ");
            return  list;

        } catch (IOException e) {
        	System.out.println(e.getMessage());
            e.printStackTrace();
            return list;
        }
    }

   
}


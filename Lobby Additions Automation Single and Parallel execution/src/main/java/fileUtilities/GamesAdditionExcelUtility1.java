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

public class GamesAdditionExcelUtility1 {
    static Logger log = LogManager.getLogger(GamesAdditionExcelUtility1.class);

    public static class GameData {
        private String label;
        private String template;
        private String channel;
        private String lobbyType;
        private String categories;
        private String subCategory;
        private List<String> gameVariants;

        public GameData(String label, String template, String channel, String lobbyType,
                        String categories, String subCategory, List<String> gameVariants) {
            this.label = label;
            this.template = template;
            this.channel = channel;
            this.lobbyType = lobbyType;
            this.categories = categories;
            this.subCategory = subCategory;
            this.gameVariants = gameVariants;
        }

        public String getLabel() { return label; }
        public String getTemplate() { return template; }
        public String getChannel() { return channel; }
        public String getLobbyType() { return lobbyType; }
        public String getCategories() { return categories; }
        public String getSubCategory() { return subCategory; }
        public List<String> getGameVariants() { return gameVariants; }

        @Override
        public String toString() {
            return "GameData{" +
                    "label='" + label + '\'' +
                    ", template='" + template + '\'' +
                    ", channel='" + channel + '\'' +
                    ", lobbyType='" + lobbyType + '\'' +
                    ", categories='" + categories + '\'' +
                    ", subCategory='" + subCategory + '\'' +
                    ", gameVariants=" + gameVariants +
                    '}';
        }
    }

    public static List<GameData> getGameDataFromExcel(String filePath) {
        List<GameData> list = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            log.info("Reading Excel data...");
            Sheet sheet = workbook.getSheet("QA2Templates"); // change if sheet name differs

            log.info("Total rows: " + sheet.getLastRowNum());

            for (int i = 1; i <=sheet.getLastRowNum(); i++) { // Skip header row
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String label = getCellValue(row.getCell(1));
                String template = getCellValue(row.getCell(2));
                String channel = getCellValue(row.getCell(3));
                String lobbyType = getCellValue(row.getCell(4));
                String categories = getCellValue(row.getCell(5));
                String subCategory = getCellValue(row.getCell(6));
                String gameVariantsStr = getCellValue(row.getCell(7));

                List<String> gameVariants = null;
                if (gameVariantsStr != null && !gameVariantsStr.isEmpty()) {
                    gameVariants = Arrays.stream(gameVariantsStr.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());
                }

                list.add(new GameData(label, template, channel, lobbyType, categories, subCategory, gameVariants));
            }

            log.info("Excel data reading completed.");
            return list;

        } catch (IOException e) {
            log.error("Error reading Excel file: " + e.getMessage(), e);
            return list;
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return null;
        }
    }
}

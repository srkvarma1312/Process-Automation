package businessLogics;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelSummaryWriter {

    public static void writeSummary(String filePath,
                                    Map<String, GameSummary> summaryMap)
            throws IOException {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Summary");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Game Variant");
        header.createCell(1).setCellValue("NEWLY_ADDED Brands");
        header.createCell(2).setCellValue("ALREADY_ADDED Brands");
        header.createCell(3).setCellValue("NOT_CONFIGURED Brands");

        int rowNum = 1;
        for (GameSummary gs : summaryMap.values()) {

            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(gs.getGameVariant());
            row.createCell(1).setCellValue(gs.getNewlyAdded());
            row.createCell(2).setCellValue(gs.getAlreadyAdded());
            row.createCell(3).setCellValue(gs.getNotConfigured());
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            wb.write(fos);
        }

        wb.close();
    }
}


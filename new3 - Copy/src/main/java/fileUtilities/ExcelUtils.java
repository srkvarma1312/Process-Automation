package fileUtilities;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

public class ExcelUtils {

    private String excelFilePath;
    private FileInputStream fis;
    private FileOutputStream fos;
    private Workbook workbook;

    public ExcelUtils(String excelFilePath) {
        this.excelFilePath = excelFilePath;
        try {
            fis = new FileInputStream(excelFilePath);
            workbook = new XSSFWorkbook(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not load Excel file: " + excelFilePath, e);
        }
    }

    public String getCellData(String sheetName, int rowNum, int colNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        if (row == null) return "";
        Cell cell = row.getCell(colNum);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    public void setCellData(String sheetName, int rowNum, int colNum, String value) {
        try {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) sheet = workbook.createSheet(sheetName);

            Row row = sheet.getRow(rowNum);
            if (row == null) row = sheet.createRow(rowNum);

            Cell cell = row.getCell(colNum);
            if (cell == null) cell = row.createCell(colNum);

            cell.setCellValue(value);

            fos = new FileOutputStream(excelFilePath);
            workbook.write(fos);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException("Error writing data to Excel", e);
        }
    }

    public int getRowCount(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        return (sheet == null) ? 0 : sheet.getLastRowNum() + 1;
    }

    public int getColumnCount(String sheetName, int rowNum) {
        Sheet sheet = workbook.getSheet(sheetName);
        Row row = sheet.getRow(rowNum);
        return (row == null) ? 0 : row.getLastCellNum();
    }

    public List<Map<String, String>> getDataAsMap(String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();
        Sheet sheet = workbook.getSheet(sheetName);

        if (sheet == null || sheet.getPhysicalNumberOfRows() < 3) {
            return data;
        }

        Row headerRow = sheet.getRow(0);
        int rowCount = getRowCount(sheetName);

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Map<String, String> rowData = new LinkedHashMap<>();
            for (int j = 0; j < headerRow.getLastCellNum(); j++) {
                String key = getCellData(sheetName, 0, j);
                String value = getCellData(sheetName, i, j);
                rowData.put(key, value);
            }
            data.add(rowData);
        }

        return data;
    }

    public void close() {
        try {
            if (fis != null) fis.close();
            if (workbook != null) workbook.close();
        } catch (IOException e) {
            // Do nothing
        }
    }
}


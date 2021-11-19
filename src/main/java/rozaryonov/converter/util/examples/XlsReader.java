package rozaryonov.converter.util.examples;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

public class XlsReader {
    public static void main(String [] args) throws IOException {
        File inputFile = Paths.get(System.getProperty("user.dir"),"/util-files/tab.xls").toFile();
        FileInputStream inputStream = new FileInputStream(inputFile);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        HSSFSheet hssfSheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = hssfSheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch(cell.getCellType()) {
                    case NUMERIC:
                        System.out.print(cell.getNumericCellValue() + "\t\t");
                        break;
                    case STRING:
                        System.out.print(cell.getStringCellValue() + "\t\t");
                        break;
                    case FORMULA:
                        System.out.print(cell.getNumericCellValue() + "\t\t");
                        break;
                    case BLANK:
                        System.out.print("" + "\t\t");
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + cell.getCellType());
                }
            }
            System.out.println();
        }
        inputStream.close();
    }
}

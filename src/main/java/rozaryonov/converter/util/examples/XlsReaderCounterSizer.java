package rozaryonov.converter.util.examples;

import com.itextpdf.text.DocumentException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

public class XlsReaderCounterSizer {
    public static void main(String [] args) throws IOException, DocumentException {
        File inputFile = Paths.get(System.getProperty("user.dir"),"/util-files/tab.xls").toFile();
        FileInputStream inputStream = new FileInputStream(inputFile);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        HSSFSheet hssfSheet = workbook.getSheetAt(0);
        Iterator<Row> rowIterator = hssfSheet.iterator();

        for(int i=0; i<10; i++) {
            System.out.println("Column: " +i + " width: " + hssfSheet.getColumnWidth(i));
            /*
            Column width
            hssfSheet.getColumnWidth(i) - i starts from 0
            1cm = 1308 units
            5,48 см = 7169 (1308)*
            2,25 см = 2948 (1310)

            */
        }

        int lastColumnInTable=-1;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            System.out.println("Row height = " + row.getHeight());
            /*
            Row height
            row.getHeight()
            1cm = 566 units
             0 746 - 1,32cm = 565
             1 293 - 0,53cm = 552
             7 1174 - 2,07cm = 567
             */

            Iterator<Cell> cellIterator = row.cellIterator();
            int lastColumnInRow = -1;
            while(cellIterator.hasNext()) {
                lastColumnInRow++;
                Cell cell = cellIterator.next();
            }
            lastColumnInTable = Math.max(lastColumnInRow, lastColumnInTable);
            //next row
        }
        System.out.println("Total number of columns: " + lastColumnInTable+1);


        inputStream.close();
    }
}

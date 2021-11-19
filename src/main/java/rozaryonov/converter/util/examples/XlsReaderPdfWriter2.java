package rozaryonov.converter.util.examples;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import rozaryonov.converter.util.CoPageType;
import rozaryonov.converter.util.CoWorksheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;

public class XlsReaderPdfWriter2 {
    public static void main(String [] args) throws IOException, DocumentException {
        Document outDocument = new Document();
        FontSelector fontSelector = getFontSelector();
        File outputFile = Paths.get(System.getProperty("user.dir"),"/util-files/tab.pdf").toFile();
        outputFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PdfWriter.getInstance(outDocument, outputStream);
        outDocument.open();

        File inputFile = Paths.get(System.getProperty("user.dir"),"/util-files/tab.xls").toFile();
        FileInputStream inputStream = new FileInputStream(inputFile);
        HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
        HSSFSheet hssfSheet = workbook.getSheetAt(0);
        //start
        CoWorksheet coWorksheet = new CoWorksheet();
        buidCoWorksheet(hssfSheet, coWorksheet);

        List<Integer> widthsListInt = coWorksheet.getClumnWidth();
        float[] widthArray = new float[widthsListInt.size()];
        for (int i=0; i<widthsListInt.size(); i++) {
            widthArray[i] = (widthsListInt.get(i) / CoPageType.APACHE_WIDTH_RATIO) / 2.54F;
        }

        PdfPTable pdfTable = new PdfPTable(widthArray);
        PdfPCell pdfCell;


        Iterator<Row> rowIterator = hssfSheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while(cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                switch(cell.getCellType()) {
                    case STRING:
                        Phrase phrase = new Phrase(fontSelector.process(cell.getStringCellValue()));
                        pdfCell = new PdfPCell(phrase);
                        break;
                    case NUMERIC:
                        pdfCell = new PdfPCell(new Phrase(fontSelector.process(String.valueOf((float) cell.getNumericCellValue()))));
                        break;
                    case FORMULA:
                        pdfCell = new PdfPCell(new Phrase(fontSelector.process(String.valueOf((float) cell.getNumericCellValue()))));
                        break;
                    case BLANK:
                        pdfCell = new PdfPCell(); //(new Phrase(cell.getStringCellValue()))
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + cell.getCellType());
                }
                pdfTable.addCell(pdfCell);
            }
            //next row
        }
        outDocument.add(pdfTable);
        outDocument.close();
        outputStream.close();
        inputStream.close();
    }

    private static FontSelector getFontSelector() throws DocumentException, IOException {
        File narrowRegularFontFile = Paths.get(System.getProperty("user.dir"),"/util-files/LiberationSansNarrow-Regular.ttf").toFile();
        File narrowItalicFontFile = Paths.get(System.getProperty("user.dir"),"/util-files/LiberationSansNarrow-Italic.ttf").toFile();
        File narrowBoldFontFile = Paths.get(System.getProperty("user.dir"),"/util-files/LiberationSansNarrow-Bold.ttf").toFile();
        File narrowBoldItalicFontFile = Paths.get(System.getProperty("user.dir"),"/util-files/LiberationSansNarrow-BoldItalic.ttf").toFile();
        BaseFont narrowRegular = BaseFont.createFont(narrowRegularFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowItallic = BaseFont.createFont(narrowBoldItalicFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowBold = BaseFont.createFont(narrowBoldFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowBoldItalic = BaseFont.createFont(narrowBoldItalicFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        FontSelector fontSelector = new FontSelector();
        fontSelector.addFont(new Font(narrowRegular));
        fontSelector.addFont(new Font(narrowBold));
        fontSelector.addFont(new Font(narrowItallic));
        fontSelector.addFont(new Font(narrowBoldItalic));
        return fontSelector;
    }
    public static void buidCoWorksheet(HSSFSheet hssfSheet, CoWorksheet coWorksheet) {
        Iterator<Row> rowIterator = hssfSheet.iterator();
        int lastColumnInTable=-1;
        int numberOfRows = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            numberOfRows++;
            //System.out.println("Row height = " + row.getHeight());
            coWorksheet.getRowHeight().add((int) row.getHeight());
            Iterator<Cell> cellIterator = row.cellIterator();
            int lastColumnInRow = -1;
            int cellsInRow = 0;
            while(cellIterator.hasNext()) {
                lastColumnInRow++;
                cellIterator.next();
                cellsInRow++;
            }
            lastColumnInTable = Math.max(lastColumnInRow, lastColumnInTable);
            //next row
            coWorksheet.getCellsInRow().add(cellsInRow);
        }
        //System.out.println("Total number of columns: " + lastColumnInTable+1);
        coWorksheet.setNumberOfColumns(lastColumnInTable+1);
        coWorksheet.setNumberOfRows(numberOfRows);
        //Set widths of columns
        for(int i=0; i<coWorksheet.getNumberOfColumns(); i++) {
            System.out.println("Column: " +i + " width: " + hssfSheet.getColumnWidth(i));
            coWorksheet.getClumnWidth().add(i, hssfSheet.getColumnWidth(i));
        }
        //Creates a grid of pages of worksheet
        //creationGridOfPages(coWorksheet, coDocument);


    }

}

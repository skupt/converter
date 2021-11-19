package rozaryonov.converter.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

public class XlsReaderPdfWriter {
    public static void main(String [] args) throws IOException, DocumentException {
        Document outDocument = new Document();
        FontSelector fontSelector = getFontSelector();
        File outputFile = Paths.get(System.getProperty("user.dir"),"/util-files/tab.pdf").toFile();
        outputFile.createNewFile();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PdfWriter.getInstance(outDocument, outputStream);
        outDocument.open();
        PdfPTable pdfTable = new PdfPTable(5);
        PdfPCell pdfCell;

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
                        pdfCell = new PdfPCell(new Phrase(cell.getStringCellValue()));
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
}

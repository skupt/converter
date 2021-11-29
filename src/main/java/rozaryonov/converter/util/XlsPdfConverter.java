package rozaryonov.converter.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import rozaryonov.converter.exception.AdapterConstructorException;
import rozaryonov.converter.exception.AdapterIOStreamsClosingException;

import java.io.*;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
public class XlsPdfConverter {
    public static enum SupportedTypes {
        XLS;
        public static boolean checkSupport (String extension) {
            try {
                SupportedTypes.valueOf(extension.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Supported extension haven't been found. Do nothing
                return false;
            }
            return true;
        }
    }

    private InputStream inputStream;
    private OutputStream outputStream;
    private HSSFWorkbook apacheHssfWorkbook;
    private Map<CoPage.CoPageCoords, CoPage> pagesOnGreedMap = new TreeMap<>();

    public XlsPdfConverter() {
    }

    public static void main(String[] args) throws IOException {
        File inputFile = Paths.get(System.getProperty("user.dir"), "/util-files/tab.xls").toFile();
        InputStream inputStream = new FileInputStream(inputFile);
        File outputFile = Paths.get(System.getProperty("user.dir"), "/util-files/tab.pdf").toFile();
        outputFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(outputFile);

        XlsPdfConverter translator = new XlsPdfConverter();
        translator.writeXlsToPdf(inputStream, outputStream);
    }

    private static FontSelector getFontSelector() throws DocumentException, IOException {
        File narrowRegularFontFile = Paths.get(System.getProperty("user.dir"), "/util-files/LiberationSansNarrow-Regular.ttf").toFile();
        File narrowItalicFontFile = Paths.get(System.getProperty("user.dir"), "/util-files/LiberationSansNarrow-Italic.ttf").toFile();
        File narrowBoldFontFile = Paths.get(System.getProperty("user.dir"), "/util-files/LiberationSansNarrow-Bold.ttf").toFile();
        File narrowBoldItalicFontFile = Paths.get(System.getProperty("user.dir"), "/util-files/LiberationSansNarrow-BoldItalic.ttf").toFile();
        BaseFont narrowRegular = BaseFont.createFont(narrowRegularFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowItallic = BaseFont.createFont(narrowItalicFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowBold = BaseFont.createFont(narrowBoldFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont narrowBoldItalic = BaseFont.createFont(narrowBoldItalicFontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        FontSelector fontSelector = new FontSelector();
        fontSelector.addFont(new Font(narrowRegular));
        fontSelector.addFont(new Font(narrowBold));
        fontSelector.addFont(new Font(narrowItallic));
        fontSelector.addFont(new Font(narrowBoldItalic));
        return fontSelector;
    }

    public void init(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        try {
            this.apacheHssfWorkbook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new AdapterConstructorException("IOException in init()", e);
        }
        pagesOnGreedMap = new TreeMap<>();
    }

    public void writeXlsToPdf(InputStream inputStream, OutputStream outputStream) {
        init(inputStream, outputStream);
        Document outDocument = new Document();
        try {
            PdfWriter.getInstance(outDocument, outputStream);
            outDocument.open();
            createItextPdf(outDocument);
            outDocument.close();
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new AdapterIOStreamsClosingException("Exception while closing IO streams of Adapter", e);
            }
        }
    }

    public void createItextPdf(Document openedDocument) throws IOException, DocumentException {
        createPagesOnGreedMap();
        for (Map.Entry<CoPage.CoPageCoords, CoPage> entry : pagesOnGreedMap.entrySet()) {
            CoPage coPage = entry.getValue();
            Cell[][] cellArray = coPage.getCellArray();
            int numberOfColumns = cellArray.length; // todo cellArray[0].length
            int numberOfRows = cellArray.length;
            PdfPTable pdfTable = new PdfPTable(numberOfColumns);
            pdfTable.setWidths(coPage.calculateRelativeWidths());
            for (int i = 0; i < cellArray[0].length; i++) { //columns
                for (int j = 0; j < cellArray.length; j++) { //rows
                    Cell cell = cellArray[j][i];
                    PdfPCell pdfCell = createPdfCell(cell);
                    pdfTable.addCell(pdfCell);

                }
            }
            openedDocument.add(pdfTable);
        }
    }

    private PdfPCell createPdfCell(Cell cell) throws DocumentException, IOException {
        FontSelector fontSelector = getFontSelector();
        PdfPCell pdfCell;
        if (cell == null) {
            pdfCell = new PdfPCell();
            pdfCell.setPhrase(new Phrase(""));
            return pdfCell;
        }
        switch (cell.getCellType()) {
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
        return pdfCell;
    }

    private void createPagesOnGreedMap() {
        CoDocument coDocument = buildStructureOfCoDocument();
        for (int i = 0; i < coDocument.getSheets().size(); i++) {
            HSSFSheet hssfSheet = apacheHssfWorkbook.getSheetAt(i);
            Iterator<Row> rowIterator = hssfSheet.iterator();
            int currentRow = 0;
            int currentColumn = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                currentRow++;
                Iterator<Cell> cellIterator = row.cellIterator();
                currentColumn = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    currentColumn++;
                    CoWorksheet currentCoSheet = coDocument.getSheets().get(i);
                    CoPage.CoPageCoords coPageCoords = calculatePageCoords(currentRow, currentColumn, currentCoSheet);
                    int columnNumBegin = currentCoSheet.getGrid().getLastColumnNumberForGrid().get(coPageCoords.getX()); //todo
                    int columnNumEnd = currentCoSheet.getGrid().getLastColumnNumberForGrid().get(coPageCoords.getX() + 1);
                    int rowNumBegin = currentCoSheet.getGrid().getLastRowNumberForGrid().get(coPageCoords.getY());
                    int rowNumEnd = currentCoSheet.getGrid().getLastRowNumberForGrid().get(coPageCoords.getY() + 1);
                    int[] columnRelativeWidthArray = new int[columnNumEnd - columnNumBegin];
                    for (int a = columnNumBegin, b = 0; a < columnNumEnd; a++, b++) {
                        columnRelativeWidthArray[b] = currentCoSheet.getClumnWidth().get(a);
                    }
                    CoPage coPageForCurrentCell = pagesOnGreedMap.computeIfAbsent(coPageCoords,
                            k -> new CoPage(coPageCoords, columnNumBegin, columnNumEnd, rowNumBegin, rowNumEnd,
                                    columnRelativeWidthArray));
                    coPageForCurrentCell.setCellToArray(cell, currentColumn, currentRow);

                }
                //next row
            }
        }
    }

    private CoPage.CoPageCoords calculatePageCoords(int currentRow, int currentColumn, CoWorksheet currentCoSheet) {
        int x = 0;
        for (int j = 1; j < currentCoSheet.getGrid().getLastColumnNumberForGrid().size(); j++) {
            if ((currentCoSheet.getGrid().getLastColumnNumberForGrid().get(j - 1) <= currentColumn)
                    && (currentColumn < currentCoSheet.getGrid().getLastColumnNumberForGrid().get(j))) {
                x = j - 1;
            }
        }
        int y = 0;
        for (int j = 1; j < currentCoSheet.getGrid().getLastRowNumberForGrid().size(); j++) {
            if ((currentCoSheet.getGrid().getLastRowNumberForGrid().get(j - 1) <= currentRow)
                    && (currentRow < currentCoSheet.getGrid().getLastRowNumberForGrid().get(j))) {
                y = j - 1;
            }
        }
        return new CoPage.CoPageCoords(x, y);
    }

    private CoDocument buildStructureOfCoDocument() {
        CoDocument coDocument = new CoDocument();
        int numberOfSheets = getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            CoWorksheet coWorksheet = new CoWorksheet();
            coDocument.getSheets().add(coWorksheet);
            HSSFSheet hssfSheet = apacheHssfWorkbook.getSheetAt(i);
            buidCoWorksheet(hssfSheet, coWorksheet, coDocument);
        }
        return coDocument;
    }

    /**
     * Current implementation provide only one sheet in Workbook
     */
    private int getNumberOfSheets() {
        //@todo some calculation with hssfWorkbook here to calculate number of sheets
        return 1;
    }

    /**
     * Reads HssfSheet and sets next data in CoWorksheet :
     * adds widths  of Worksheet rows to List<Integer> clumnWidth of CoWorksheet
     * adds heights of Worksheet columns to List<Integer> clumnHeight of CoWorksheet
     * sets number of used columns to CoWorksheet.numberOfColumns
     *
     * @param hssfSheet
     * @param coWorksheet
     */
    public void buidCoWorksheet(HSSFSheet hssfSheet, CoWorksheet coWorksheet, CoDocument coDocument) {
        Iterator<Row> rowIterator = hssfSheet.iterator();
        int lastColumnInTable = -1;
        int numberOfRows = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            numberOfRows++;
            //System.out.println("Row height = " + row.getHeight());
            coWorksheet.getRowHeight().add((int) row.getHeight());
            Iterator<Cell> cellIterator = row.cellIterator();
            int lastColumnInRow = -1;
            int cellsInRow = 0;
            while (cellIterator.hasNext()) {
                lastColumnInRow++;
                cellIterator.next();
                cellsInRow++;
            }
            lastColumnInTable = Math.max(lastColumnInRow, lastColumnInTable);
            //next row
            coWorksheet.getCellsInRow().add(cellsInRow);
        }
        //System.out.println("Total number of columns: " + lastColumnInTable+1);
        coWorksheet.setNumberOfColumns(lastColumnInTable + 1);
        coWorksheet.setNumberOfRows(numberOfRows);
        //Set widths of columns
        for (int i = 0; i < coWorksheet.getNumberOfColumns(); i++) {
            System.out.println("Column: " + i + " width: " + hssfSheet.getColumnWidth(i));
            coWorksheet.getClumnWidth().add(i, hssfSheet.getColumnWidth(i));
        }
        //Creates a grid of pages of worksheet
        creationGridOfPages(coWorksheet, coDocument);


    }

    private void creationGridOfPages(CoWorksheet coWorksheet, CoDocument coDocument) {
        int currentSumWidths = 0;
        boolean wasSetlastColumnNUmber = false;
        for (int i = 0; i < coWorksheet.getNumberOfColumns(); i++) {
            currentSumWidths += coWorksheet.getClumnWidth().get(i);
            if (currentSumWidths > coDocument.getApacheClearWidthForCoPagesOfCoWorksheet(coWorksheet)) {
                coWorksheet.getGrid().getLastColumnNumberForGrid().add(i);
                currentSumWidths = coWorksheet.getClumnWidth().get(i);
                wasSetlastColumnNUmber = true;
            }
        }
        if (!wasSetlastColumnNUmber)
            coWorksheet.getGrid().getLastColumnNumberForGrid().add(coWorksheet.getNumberOfColumns());

        boolean wasSetlastRowNUmber = false;
        int currentSumHeights = 0;
        for (int i = 0; i < coWorksheet.getNumberOfRows(); i++) {
            currentSumHeights += coWorksheet.getRowHeight().get(i);
            if (currentSumHeights > coDocument.getApacheClearHeightForCoPagesOfCoWorksheet(coWorksheet)) {
                coWorksheet.getGrid().getLastRowNumberForGrid().add(i);
                currentSumHeights = coWorksheet.getRowHeight().get(i);
                wasSetlastRowNUmber = true;
            }
        }
        if (!wasSetlastRowNUmber) coWorksheet.getGrid().getLastRowNumberForGrid().add(coWorksheet.getNumberOfRows());
    }
}

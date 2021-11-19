package rozaryonov.converter.util;

import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;

@Data
public class CoPage {
    private final CoPageCoords coPageCoords;
    private int columnNumberBegin = 0;
    private int columnNumberEnd = 0;
    private int rowNumberBegin = 0;
    private int rowNumberEnd = 0;
    private Cell[][] cellArray;
    int [] columnRelativeWidthArray;

    public CoPage(CoPageCoords coPageCoords, int columnStart, int columnEnd, int rowStart, int rowEnd, int[] columnRelativeWidthArray) {
        this.coPageCoords = coPageCoords;
        columnNumberBegin = columnStart;
        columnNumberEnd = columnEnd;
        rowNumberBegin = rowStart;
        rowNumberEnd = rowEnd;
        cellArray = new Cell[columnEnd - columnStart][rowEnd - rowStart];
        this.columnRelativeWidthArray = columnRelativeWidthArray;

    }

    void setCellToArray(Cell cell, int absColumnNum, int absRowNum) {
        cellArray[absColumnNum-columnNumberBegin] [absRowNum-rowNumberBegin] = cell;
    }

    float[] calculateRelativeWidths() {
        float ratio = 2.54F;
        float [] widthArray = new float[columnRelativeWidthArray.length];
        for (int i=0; i<columnRelativeWidthArray.length; i++) {
            widthArray[i] = (columnRelativeWidthArray[i] / CoPageType.APACHE_WIDTH_RATIO) / ratio;
        }
        return widthArray;
    }

    @Data
    /**
     * The place in the grid
     */
    public final static class CoPageCoords implements Comparable {
        private final int x;
        private final int y;

        @Override
        public int compareTo(Object other) {
            CoPage.CoPageCoords o2 = (CoPage.CoPageCoords) other;
            int res1 = o2.x-this.x;
            if ( res1!=0 ) return res1;
            int res2 = o2.y-this.y;
            return res2;
        }
    }

}

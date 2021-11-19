package rozaryonov.converter.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CoWorksheet {
    private List<Integer> clumnWidth = new ArrayList<>();
    private List<Integer> rowHeight = new ArrayList<>();
    private List<Integer> cellsInRow = new ArrayList<>();
    private int marginTopMM = 13;
    private int marginBottomMM = 13;
    private int marginLeftMM = 13;
    private int marginRight = 13;
    private int numberOfColumns;
    private int numberOfRows;
    private Grid grid = new Grid();
    //private List<List<CoPage>> coPageList = new ArrayList<>();

    int getApacheMarginTop() {
        return this.marginTopMM/10*CoPageType.APACHE_HEIGT_RATIO;
    }

    int getApacheMarginBottom() {
        return this.marginBottomMM/10*CoPageType.APACHE_HEIGT_RATIO;
    }

    int getApacheMarginLeft() {
        return this.marginLeftMM/10*CoPageType.APACHE_WIDTH_RATIO;
    }
    int getApacheMarginRight() {
        return this.marginRight/10*CoPageType.APACHE_WIDTH_RATIO;
    }

    @Data
    public static class Grid {
        List<Integer> lastColumnNumberForGrid = new ArrayList<>();
        List<Integer> lastRowNumberForGrid = new ArrayList<>();

        public Grid() {
            lastColumnNumberForGrid.add(0);
            lastRowNumberForGrid.add(0);
        }
    }


}

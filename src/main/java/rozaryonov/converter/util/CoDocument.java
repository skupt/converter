package rozaryonov.converter.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CoDocument {

    List<CoWorksheet> sheets = new ArrayList<>();
    CoPageType pageType = CoPageType.A4;

    int getApacheClearWidthForCoPagesOfCoWorksheet(CoWorksheet coWorksheet) {
        return pageType.getApacheWidth()-(coWorksheet.getApacheMarginLeft() + coWorksheet.getApacheMarginRight());
    }

    int getApacheClearHeightForCoPagesOfCoWorksheet(CoWorksheet coWorksheet) {
        return pageType.getApacheHeight()-(coWorksheet.getApacheMarginTop() + coWorksheet.getApacheMarginBottom());
    }

}

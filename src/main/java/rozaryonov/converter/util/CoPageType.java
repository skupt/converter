package rozaryonov.converter.util;

public enum CoPageType {
    A4(210, 297), A5(148, 210);
    public static final int APACHE_HEIGT_RATIO = 566;
    public static final int APACHE_WIDTH_RATIO = 1308;
    int width;
    int height;

    CoPageType(int width, int heigth) {
        this.width =width;
        this.height =heigth;
    }

    int getApacheWidth() {
        return width * APACHE_WIDTH_RATIO;
    }
    int getApacheHeight() {
        return height * APACHE_HEIGT_RATIO;
    }
}

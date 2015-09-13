import java.awt.*;

public class PointsBoard {

    public final int width = 75;
    public final int height = 75;

    public Color[][] map;

    PointsBoard() {
        map = new Color[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                map[i][j] = new Color(230, 230, 230);
            }
        }
        map[0][0] = new Color(30, 40, 100);
        map[74][0] = new Color(30, 40, 100);
        map[0][74] = new Color(30, 40, 100);
        map[74][74] = new Color(30, 40, 100);
    }

    @Override
    public String toString() {
        StringBuilder a = new StringBuilder(3 * height * width);
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                a.append((char) map[i][j].getRed());
                a.append((char)map[i][j].getGreen());
                a.append((char)map[i][j].getBlue());
            }
        }
        return a.toString();
    }

}
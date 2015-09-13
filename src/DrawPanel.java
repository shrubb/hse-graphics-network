import javax.swing.*;
import java.awt.*;
import java.lang.Math.*;

public class DrawPanel extends JPanel {

    PointsBoard data;

    DrawPanel() {
        data = new PointsBoard();
    }

    @Override
    public void paintComponent(Graphics g) {
        //super.paintComponent(g);

        for (int i = 0; i < data.height; ++i) {
            for (int j = 0; j < data.width; ++j) {
                g.setColor(data.map[i][j]);
                g.fillRect(j * 6, i * 6, 6, 6);
            }
        }
    }
}
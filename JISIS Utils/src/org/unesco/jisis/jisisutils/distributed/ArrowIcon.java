package org.unesco.jisis.jisisutils.distributed;

// ArrowIcon.java
// A simple implementation of the Icon interface that can make
// Up and Down arrows.
//
import javax.swing.Icon;
import java.awt.*;

public class ArrowIcon implements Icon {

    public static final int UP = 0;
    public static final int DOWN = 1;

    private final int direction;
    private final Polygon pagePolygon = new Polygon(new int[]{2, 4, 4, 10, 10, 2},
            new int[]{4, 4, 2, 2, 12, 12},
            6);
    private final int[] arrowX = {4, 9, 6};
    private final Polygon arrowUpPolygon
            = new Polygon(arrowX, new int[]{10, 10, 4}, 3);
    private final Polygon arrowDownPolygon
            = new Polygon(arrowX, new int[]{6, 6, 11}, 3);

    public ArrowIcon(int which) {
        direction = which;
    }

    @Override
    public int getIconWidth() {
        return 14;
    }

    @Override
    public int getIconHeight() {
        return 14;
    }

    /**
     *
     * @param c
     * @param g
     * @param x
     * @param y
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.setColor(Color.black);
        pagePolygon.translate(x, y);
        g.drawPolygon(pagePolygon);
        pagePolygon.translate(-x, -y);
        if (direction == UP) {
            arrowUpPolygon.translate(x, y);
            g.fillPolygon(arrowUpPolygon);
            arrowUpPolygon.translate(-x, -y);
        } else {
            arrowDownPolygon.translate(x, y);
            g.fillPolygon(arrowDownPolygon);
            arrowDownPolygon.translate(-x, -y);
        }
    }
}

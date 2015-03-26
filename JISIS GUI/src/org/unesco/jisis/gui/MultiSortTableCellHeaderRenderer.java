/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package org.unesco.jisis.gui;

//~--- JDK imports ------------------------------------------------------------



import java.awt.*;

import java.util.List;

import javax.swing.*;
import javax.swing.RowSorter.SortKey;
import javax.swing.border.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

/**
 *
 * @author jc_dauphin
 */
public class MultiSortTableCellHeaderRenderer extends DefaultTableCellRenderer {
    protected SortIcon sortIcon = new SortIcon(8);

    public MultiSortTableCellHeaderRenderer() {
        setHorizontalAlignment(0);
        setHorizontalTextPosition(10);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        JTableHeader tableHeader = table.getTableHeader();
        Color        fg          = null;
        Color        bg          = null;
        Border       border      = null;
        Icon         icon        = null;
        if (hasFocus) {
            fg     = UIManager.getColor("TableHeader.focusCellForeground");
            bg     = UIManager.getColor("TableHeader.focusCellBackground");
            border = UIManager.getBorder("TableHeader.focusCellBorder");
        }
        if (fg == null) {
            fg = tableHeader.getForeground();
        }
        if (bg == null) {
            bg = tableHeader.getBackground();
        }
        if (border == null) {
            border = UIManager.getBorder("TableHeader.cellBorder");
        }
        if (!tableHeader.isPaintingForPrint() && (table.getRowSorter() != null)) {
            icon = getSortIcon(table, table.convertColumnIndexToModel(column));
        }
        setFont(tableHeader.getFont());
        setText(((value != null) && (value != ""))
                ? value.toString()
                : " ");
        setBorder(border);
        setIcon(icon);
        return this;
    }

     @SuppressWarnings("unchecked")
    protected Icon getSortIcon(JTable table, int column) {
        List<SortKey> sortKeys = (List<SortKey>) table.getRowSorter().getSortKeys();
        if ((sortKeys == null) || (sortKeys.size() == 0)) {
            return null;
        }
        int priority = 0;
        for (SortKey sortKey : sortKeys) {
            if (sortKey.getColumn() == column) {
                sortIcon.setPriority(priority);
                sortIcon.setSortOrder(sortKey.getSortOrder());
                return sortIcon;
            }
            priority++;
        }
        return null;
    }
}

class SortIcon implements Icon, SwingConstants {
    private double[]         sizePercentages = {
        1.0, .85, .70, .55, .40, .25, .10
    };
    private int              baseSize;
    private int              direction;
    private BasicArrowButton iconRenderer;
    private int              size;

    public SortIcon(int size) {
        this.baseSize = this.size = size;
        iconRenderer  = new BasicArrowButton(direction);
    }

    public void setPriority(int priority) {
        size = (int) (baseSize * sizePercentages[priority]);
    }

    public void setSortOrder(SortOrder sortOrder) {
        direction = (sortOrder == SortOrder.ASCENDING)
                    ? NORTH
                    : SOUTH;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
        iconRenderer.paintTriangle(g, x, y, size, direction, true);
    }

    public int getIconWidth() {
        return size;
    }

    public int getIconHeight() {
        return size / 2;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.jisisutils.gui;

/**
 * Code from Christian Kaufhold (swing@chka.de)
 * http://www.chka.de/swing/table/scrolling.html
 *
 */
import org.unesco.jisis.jisisutils.gui.Scrolling;
import java.awt.Rectangle;
import java.awt.Insets;
import javax.swing.JTable;


public abstract class JTableScrolling
{
    private JTableScrolling()
    {
    }


    public static Rectangle getRowBounds(JTable table, int row)
    {
        checkRow(table, row);

        Rectangle result = table.getCellRect(row, -1, true);
        Insets i = table.getInsets();

        result.x = i.left;
        result.width = table.getWidth() - i.left - i.right;

        return result;
    }

    public static Rectangle getRowBounds(JTable table, int first, int last)
    {
        checkRows(table, first, last);

        Rectangle result = table.getCellRect(first, -1, true);
        result = result.union(table.getCellRect(last, -1, true));
        Insets i = table.getInsets();

        result.x = i.left;
        result.width = table.getWidth() - i.left - i.right;

        return result;
    }


    public static Rectangle getColumnBounds(JTable table, int column)
    {
        checkColumn(table, column);

        Rectangle result = table.getCellRect(-1, column, true);
        Insets i = table.getInsets();

        result.y = i.top;
        result.height = table.getHeight() - i.top - i.bottom;

        return result;
    }

    public static Rectangle getColumnBounds(JTable table, int first, int last)
    {
        checkColumns(table, first, last);

        Rectangle result = table.getCellRect(-1, first, true);
        result = result.union(table.getCellRect(-1, last, true));
        Insets i = table.getInsets();

        result.y = i.top;
        result.height = table.getHeight() - i.top - i.bottom;

        return result;
    }


    /** For completeness. Only allows valid rows/columns. */
    public static Rectangle getCellBounds(JTable table, int row, int column)
    {
        checkCell(table, row, column);

        return table.getCellRect(row, column, true);
    }

    public static Rectangle getCellBounds(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn)
    {
        checkCells(table, firstRow, lastRow, firstColumn, lastColumn);

        Rectangle result = table.getCellRect(firstRow, firstColumn, true);
        return result.union(table.getCellRect(lastRow, lastColumn, true));
    }


    public static void makeRowVisible(JTable table, int row)
    {
        Scrolling.scrollVertically(table, getRowBounds(table, row));
    }


    public static void makeColumnVisible(JTable table, int column)
    {
        Scrolling.scrollHorizontally(table, getColumnBounds(table, column));
    }


    public static void makeRowsVisible(JTable table, int first, int last)
    {
        Scrolling.scrollVertically(table, getRowBounds(table, first, last));
    }


    public static void makeRowsVisible(JTable table, int first, int last, int bias)
    {
        Scrolling.scrollVertically(table, getRowBounds(table, first, last), bias);
    }

    public static void makeColumnsVisible(JTable table, int first, int last)
    {
        Scrolling.scrollHorizontally(table, getColumnBounds(table, first, last));
    }

    public static void makeColumnsVisible(JTable table, int first, int last, int bias)
    {
        Scrolling.scrollHorizontally(table, getColumnBounds(table, first, last), bias);
    }

    public static void makeCellsVisible(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn)
    {
        table.scrollRectToVisible(getCellBounds(table, firstRow, lastRow, firstColumn, lastColumn));
    }

    public static void makeCellsVisible(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn, int bias)
    {
        Scrolling.scroll(table, getCellBounds(table, firstRow, lastRow, firstColumn, lastColumn), bias);
    }


    public static void makeCellsVisible(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn, int rowBias, int columnBias)
    {
        Scrolling.scroll(table, getCellBounds(table, firstRow, lastRow, firstColumn, lastColumn), rowBias, columnBias);
    }




    public static void centerRow(JTable table, int row)
    {
        Scrolling.centerVertically(table, getRowBounds(table, row), false);
    }


    public static void centerColumn(JTable table, int column)
    {
        Scrolling.centerHorizontally(table, getColumnBounds(table, column), false);
    }


    public static void centerRows(JTable table, int first, int last)
    {
        Scrolling.centerVertically(table, getRowBounds(table, first, last), false);
    }

    public static void centerColumns(JTable table, int first, int last)
    {
        Scrolling.centerHorizontally(table, getColumnBounds(table, first, last), false);
    }

    public static void centerCell(JTable table, int row, int column)
    {
        Scrolling.center(table, getCellBounds(table, row, column), false);
    }

    public static void centerCells(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn)
    {
        Scrolling.center(table, getCellBounds(table, firstRow, lastRow, firstColumn, lastColumn), false);
    }






    public static boolean isRowVisible(JTable table, int row)
    {
        return Scrolling.isVerticallyVisible(table, getRowBounds(table, row));
    }

    public static boolean isColumnVisible(JTable table, int column)
    {
        return Scrolling.isHorizontallyVisible(table, getColumnBounds(table, column));
    }

    public static boolean isCellVisible(JTable table, int row, int column)
    {
        return Scrolling.isVisible(table, getCellBounds(table, row, column));
    }

    public static boolean areColumnsVisible(JTable table, int first, int last)
    {
        return Scrolling.isHorizontallyVisible(table, getColumnBounds(table, first, last));
    }

    public static boolean areRowsVisible(JTable table, int first, int last)
    {
        return Scrolling.isVerticallyVisible(table, getRowBounds(table, first, last));
    }

    public static boolean areCellsVisible(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn)
    {
        checkCells(table, firstRow, lastRow, firstColumn, lastColumn);

        return Scrolling.isVisible(table, getCellBounds(table, firstRow, lastRow, firstColumn, lastColumn));
    }



    private static void checkRow(JTable table, int row)
    {
        if (row < 0)
            throw new IndexOutOfBoundsException(row+" < 0");
        if (row >= table.getRowCount())
            throw new IndexOutOfBoundsException(row+" >= "+table.getRowCount());
    }

    private static void checkColumn(JTable table, int column)
    {
        if (column < 0)
            throw new IndexOutOfBoundsException(column+" < 0");
        if (column >= table.getColumnCount())
            throw new IndexOutOfBoundsException(column+" >= "+table.getColumnCount());
    }

    private static void checkCell(JTable table, int row, int column)
    {
        checkRow(table, row);
        checkColumn(table, column);
    }


    private static void checkRows(JTable table, int first, int last)
    {
        if (first < 0)
            throw new IndexOutOfBoundsException(first+" < 0");
        if (first > last)
            throw new IndexOutOfBoundsException(first+" > "+last);
        if (last >= table.getRowCount())
            throw new IndexOutOfBoundsException(last+" >= "+table.getRowCount());
    }


    private static void checkColumns(JTable table, int first, int last)
    {
        if (first < 0)
            throw new IndexOutOfBoundsException(first+" < 0");
        if (first > last)
            throw new IndexOutOfBoundsException(first+" > "+last);
        if (last >= table.getColumnCount())
            throw new IndexOutOfBoundsException(last+" >= "+table.getColumnCount());
    }


    private static void checkCells(JTable table, int firstRow, int lastRow, int firstColumn, int lastColumn)
    {
        checkRows(table, firstRow, lastRow);
        checkColumns(table, firstColumn, lastColumn);
    }
}
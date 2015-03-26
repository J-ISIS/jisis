/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.gui;

import java.awt.Color;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JWindow;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Utilities;


/**
 *
 * @author jc_dauphin
 */
public class FilterOnlyTableRowSorter<M extends TableModel> extends TableRowSorter<M> {
    JTable table;
    JWindow sortProgressWin;

    static final String ICON_PATH = "org/unesco/jisis/jisisutils/gui/ajax-loader.gif";
    public FilterOnlyTableRowSorter() {
        super();
        createSortProgressWindow();
    }

    public FilterOnlyTableRowSorter(JTable table) {
        super((M) table.getModel());
        //super( table.getModel() );

        this.table = table;
        createSortProgressWindow();
    }

    public void createSortProgressWindow() {
        JLabel progressIndicator = new JLabel("Filtering, please wait ...",
               new ImageIcon(Utilities.loadImage(ICON_PATH)), JLabel.LEFT );
            progressIndicator.setBackground(Color.LIGHT_GRAY);
            progressIndicator.setOpaque(true);
            progressIndicator.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );

        sortProgressWin = new JWindow();
        sortProgressWin.getContentPane().add(progressIndicator);
        sortProgressWin.pack();
        sortProgressWin.setLocationRelativeTo(table);
    }


    public void sort() {
        sortProgressWin.setVisible(true);
        Runnable runnable =  new Runnable() {
            public void run() {
                try {
                    FilterOnlyTableRowSorter.super.sort();
                    sortProgressWin.setVisible(false);
                } catch(Exception e) {}
            }
        };
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(runnable);
    }


    public FilterOnlyTableRowSorter(M model) {
        super(model);
    }

    @Override
    public void toggleSortOrder(int col) {
    } // no sorting when user selects column header
}

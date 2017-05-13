/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.jisisutils.distributed;

import org.unesco.jisis.jisisutils.gui.SmallButton;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import org.openide.util.ImageUtilities;

/**
 *
 * @author jcdau
 */
public class PagingToolBar extends JToolBar {

    protected javax.swing.JButton btnFirst_;

    protected javax.swing.JButton btnLast_;
    protected javax.swing.JButton btnNext_;
    protected javax.swing.JButton btnPrevious_;
    protected javax.swing.JButton btnPrint_;
    protected javax.swing.JLabel lblPageNo_;

    static final String FIRST_ICON_PATH = "org/unesco/jisis/jisisutils/distributed/images/first.GIF";
    static final String LAST_ICON_PATH = "org/unesco/jisis/jisisutils/distributed/images/last.GIF";
    static final String NEXT_ICON_PATH = "org/unesco/jisis/jisisutils/distributed/images/next.GIF";
    static final String PREV_ICON_PATH = "org/unesco/jisis/jisisutils/distributed/images/previous.GIF";

    static final String BUNDLE_PATH = "org/unesco/jisis/jisisutils/distributed/Bundle";

    protected javax.swing.JTextField txtGoTo_;

    protected KeyListener keyNavigationListener_;

    protected PagingModel pagingModel_;
    protected JScrollPane scrollPane_;
    protected JTable jtable_;

    /**
     *
     * @param pagingModel
     * @param scrollPane
     * @param jtable
     */
    public PagingToolBar(PagingModel pagingModel, JScrollPane scrollPane, JTable jtable) {
        pagingModel_ = pagingModel;
        scrollPane_ = scrollPane;
        jtable_ = jtable;
        this.keyNavigationListener_ = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent evt) {
            }

            @Override
            public void keyPressed(KeyEvent evt) {
                keyNavigate(evt);
            }

            @Override
            public void keyReleased(KeyEvent evt) {
            }
        };

        initComponents();

        pagingModel_.addTableModelListener((TableModelEvent e) -> {
            PagingModel source = (PagingModel) e.getSource();
            int first = e.getFirstRow(), last = e.getLastRow();
            if (first == 0 && last == Integer.MAX_VALUE) {
                int page = source.getPageOffset() + 1;
                int pageCount = source.getPageCount();
                lblPageNo_.setText("Page " + page + "/" + pageCount);

            }
        });
    }

    /**
     *
     */
    private void initComponents() {

        this.setFloatable(false);
        this.setRollover(true);

        btnFirst_ = new javax.swing.JButton();
        btnPrevious_ = new javax.swing.JButton();
        btnNext_ = new javax.swing.JButton();
        btnLast_ = new javax.swing.JButton();
        txtGoTo_ = new javax.swing.JTextField();
        lblPageNo_ = new javax.swing.JLabel();

        ImageIcon iconFirst = new javax.swing.ImageIcon(ImageUtilities.loadImage(FIRST_ICON_PATH, true));
        String firstToolTip = java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("FIRST_PAGE");
        Action firstAction = new AbstractAction(firstToolTip, iconFirst) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnFirstActionPerformed(evt);
            }

        };
        ImageIcon iconNext = new javax.swing.ImageIcon(ImageUtilities.loadImage(NEXT_ICON_PATH, true));
        String nextToolTip = java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("NEXT_PAGE");
        Action nextAction = new AbstractAction(nextToolTip, iconNext) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnNextActionPerformed(evt);
            }

        };
        ImageIcon iconPrev = new javax.swing.ImageIcon(ImageUtilities.loadImage(PREV_ICON_PATH, true));
        String prevToolTip = java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("PREVIOUS_PAGE");
        Action prevAction = new AbstractAction(prevToolTip, iconPrev) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnPreviousActionPerformed(evt);
            }

        };
        ImageIcon iconLast = new javax.swing.ImageIcon(ImageUtilities.loadImage(LAST_ICON_PATH, true));
        String lastToolTip = java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("LAST_PAGE");
        Action lastAction = new AbstractAction(lastToolTip, iconLast) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnLastActionPerformed(evt);
            }

        };

        btnFirst_ = new SmallButton(firstAction, firstToolTip);

        btnFirst_.addKeyListener(keyNavigationListener_);
        this.add(btnFirst_);

        btnPrevious_ = new SmallButton(prevAction, prevToolTip);

        btnPrevious_.addKeyListener(keyNavigationListener_);
        this.add(btnPrevious_);

        btnNext_ = new SmallButton(nextAction, nextToolTip);

        btnNext_.addKeyListener(keyNavigationListener_);
        this.add(btnNext_);

        btnLast_ = new SmallButton(lastAction, lastToolTip);
        btnLast_.addKeyListener(keyNavigationListener_);
        this.add(btnLast_);

        this.addSeparator();

        txtGoTo_.setToolTipText(java.util.ResourceBundle.getBundle(BUNDLE_PATH).getString("GO_TO_PAGE"));
        txtGoTo_.setMaximumSize(new java.awt.Dimension(80, 23));
        txtGoTo_.setMinimumSize(new java.awt.Dimension(80, 23));
        txtGoTo_.setPreferredSize(new java.awt.Dimension(80, 23));
        txtGoTo_.addActionListener((java.awt.event.ActionEvent evt) -> {
            txtGoToActionPerformed(evt);
        });
        txtGoTo_.addKeyListener(keyNavigationListener_);
        this.add(txtGoTo_);

        this.addSeparator();

        int page = pagingModel_.getPageOffset() + 1;
        int pageCount = pagingModel_.getPageCount();
        lblPageNo_.setText(" Page " + page + " / " + pageCount);

        this.add(lblPageNo_);

    }

    /**
     *
     * @param evt
     */
    void txtGoToActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            int pageNumber = Integer.parseInt(txtGoTo_.getText());
            pagingModel_.setPageOffset(pageNumber - 1);

        } catch (NumberFormatException e) {
        }
    }

    void btnLastActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        int pageNumber = pagingModel_.getPageCount();
        pagingModel_.setPageOffset(pageNumber - 1);
    }

    void btnNextActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        pagingModel_.pageDown();

    }

    void btnPreviousActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        pagingModel_.pageUp();

    }

    void btnFirstActionPerformed(java.awt.event.ActionEvent evt) {
        // Add your handling code here:
        pagingModel_.setPageOffset(0);

    }

    protected void keyNavigate(KeyEvent evt) {

        switch (evt.getKeyCode()) {
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_PAGE_DOWN:
                dnNavigate(evt);
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_PAGE_UP:
                upNavigate(evt);
                break;
            case KeyEvent.VK_HOME:
                homeEndNavigate(0);
                break;
            case KeyEvent.VK_END:
                homeEndNavigate(pagingModel_.getPageCount() - 1);
                break;
            default:
            // Do nothing
        }

    }

    private void dnNavigate(KeyEvent evt) {
        int bottomPosition = scrollPane_.getVerticalScrollBar().getValue();
        scrollPane_.dispatchEvent(evt);
        if ((scrollPane_.getViewport().getHeight() > jtable_.getHeight()
                || scrollPane_.getVerticalScrollBar().getValue() == bottomPosition)
                && pagingModel_.getPageOffset() < pagingModel_.getPageCount() - 1) {
            pagingModel_.pageDown();
            if (scrollPane_.isEnabled()) {
                scrollPane_.getVerticalScrollBar().setValue(0);
            }
        }
    }

    private void upNavigate(KeyEvent evt) {
        if ((scrollPane_.getViewport().getHeight() > jtable_.getHeight()
                || scrollPane_.getVerticalScrollBar().getValue() == 0)
                && pagingModel_.getPageOffset() > 0) {
            pagingModel_.pageUp();
            if (scrollPane_.isEnabled()) {
                scrollPane_.getVerticalScrollBar().setValue(scrollPane_.getVerticalScrollBar().getMaximum());
            }
        } else {
            scrollPane_.dispatchEvent(evt);
        }
    }

    private void homeEndNavigate(int pageNumber) {
        pagingModel_.setPageOffset(pageNumber);
        if (scrollPane_.isEnabled()) {
            scrollPane_.getVerticalScrollBar().setValue(0);
        }
    }
}

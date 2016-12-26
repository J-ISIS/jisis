/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.unesco.jisis.dataentryex;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXList;
import org.unesco.jisis.corelib.common.Global;
import org.unesco.jisis.corelib.util.StringUtils;
import org.unesco.jisis.dataentryex.xml.FixedFieldDescription;
import org.unesco.jisis.dataentryex.xml.FixedFieldSubfield;
import org.unesco.jisis.dataentryex.xml.FixedFieldsContainer;
import org.unesco.jisis.dataentryex.xml.SubfieldValue;
import org.unesco.jisis.dataentryex.xml.XmlFixedFieldsReaderMediator;
import org.unesco.jisis.gui.FixedSizeDocument;

/**
 *
 * @author jcd
 */
public class Marc21FixedFieldsEditor extends JDialog {


    /*
     All of the dashboard components are created here as instance fields
     to keep the buildDashboard() method streamlined for display in the article.
     */
    JLabel settingsLabel = new JLabel("Dashboard Settings");
    JLabel recStatLabel = new JLabel("Rec stat:");
    JLabel typeLabel = new JLabel("Type:");
    JLabel BLvlLabel = new JLabel("BLvl:");
    JLabel CtrlLabel = new JLabel("Ctrl:");
    JLabel ELvlLabel = new JLabel("ELvl:");
    JLabel avatarLabel = new JLabel("Avatar Image:");

   

    boolean fieldRightClicked_ = false;
    
    JTextField recStatField = new JTextField(1);
    JTextField typeField = new JTextField(1);
    JTextField BLvlField = new JTextField(1);
    JTextField CtrlField = new JTextField(1);
    JTextField ELvlField = new JTextField(1);

    JTextField zipField = new JTextField(5);
    JTextField emailField = new JTextField(20);
    JTextField avatarField = new JTextField(30);

    JButton okBttn = new JButton("Ok");
    JButton cancelBttn = new JButton("Cancel");
    JButton helpBttn = new JButton("Help");

    JLabel headerLabel;
    JLabel footerLabel;
    JLabel widget1;
    JLabel widget2;
    JLabel widget3;

    JLabel avatarImage;
    protected JPanel contentPane;
    
     LabelField[] labelFields_;

    /**
     * Construct the dialog.
     * @param title
     */
    public Marc21FixedFieldsEditor(String title) {
        
         super((Dialog) null, "Test Marc21FixedFieldEditor", true);
         
        /**
         * Fixed Field 8
         */
        String fullFilepath = Global.getClientWorkPath() + File.separator + "fixedFields.xml";

        FixedFieldsContainer fixedFieldContainer = new FixedFieldsContainer();

        File file = new File(fullFilepath);
        if (file.exists()) {
            XmlFixedFieldsReaderMediator med = new XmlFixedFieldsReaderMediator(fixedFieldContainer, fullFilepath, fullFilepath);
        }

    
         initializeBooksTemplate(fixedFieldContainer);

         initializeDockingComponents();
         contentPane = buildMiGDashboard();
         setContentPane(contentPane);
  
    }
    
       /** Initialize our JXList; this is standard stuff, just as with JTable */
   private JXList initList(String mnemonic) {
      // boilerplate table-setup; this would be the same for a JTable
      JXList list = new JXList();
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setVisibleRowCount(5);
      SimpleListModel model = new SimpleListModel();
      model.loadData(mnemonic);

      list.setModel(model);
      return list;
   }
   
       /** Initialize our JXList; this is standard stuff, just as with JTable */
   private JXList initList(String mnemonic, List<SubfieldValue> subfieldValues) {
      // boilerplate table-setup; this would be the same for a JTable
      JXList list = new JXList();
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setVisibleRowCount(5);
      SimpleListModel model = new SimpleListModel();
      model.loadData(subfieldValues);

      list.setModel(model);
      return list;
   }
   

     /**
     * =============================================================
     * Helper Classes
     * =============================================================
     */
    /**
     * Class used to control the JPopupMenu visibility.
     * 
     * The JPopupMenu is visible when the mouse cursor is over the JTextField and becomes invisible when
     * the cursor leaves the JTextField
     * If the mouse is right-clicked when the cursor is over the JTextField, the associated JPopupMenu
     * remains visible for JList item selection. Double clicking a JList item or pressing ESCAPE key to cancel
     * the selection
     */
    class JTextFieldMouseRightClick extends MouseAdapter {

       JTextFieldJPopupMenuMediator med_;
 
       
        public JTextFieldMouseRightClick(JTextFieldJPopupMenuMediator med) {
            med_ = med;
        }
        
       
        @Override
        public void mouseEntered(MouseEvent me) {
            if (!med_.getJPopupMenu().isVisible()) {
                med_.getJPopupMenu().show(med_.getJTextField(), me.getX(), me.getY());
            }
        }

        @Override
        public void mouseExited(MouseEvent me) {
            if (med_.getJPopupMenu().isVisible() && !med_.isPopupMenuVisible()) {
                med_.getJPopupMenu().setVisible(false);
            }
        }

        @Override
        public void mouseClicked(MouseEvent event) {

            if ((event.getModifiers() & InputEvent.BUTTON3_MASK) != 0) {
                if (!med_.getJPopupMenu().isVisible()) {
                    med_.getJPopupMenu().show(med_.getJTextField(), event.getX(), event.getY());
                }
                med_.getJTextField().requestFocus();
                med_.setPopupMenuVisible(true);

                System.out.println("Right click detected" + (event.getPoint()));

            }
        }
    }
    /**
     * Class used to intercept the JXList item double click - 
     */
    class JListMouseSelector extends MouseAdapter {

         JTextFieldJPopupMenuMediator med_;

        public JListMouseSelector(  JTextFieldJPopupMenuMediator med) {
            med_ = med;
        }

        /**
         * Note that double clicking generates in fact 2 events, one to select the list item (single click)
         * and the action event (double click)
         *
         * @param evt
         */
        @Override
        public void mouseClicked(MouseEvent evt) {
            JList list = (JList) evt.getSource();
            if (evt.getClickCount() == 2) {

                // Double-click detected
                int index = list.locationToIndex(evt.getPoint());
                String s = (String) list.getModel().getElementAt(index);
                String code = s.substring(9, 10);
                med_.getJTextField().setText(code);
                System.out.println("Double Click s=" + s + " code=" + code);
                med_.getJPopupMenu().setVisible(false);
                med_.setPopupMenuVisible(false);

            } else if (evt.getClickCount() == 3) {

                // Triple-click detected
                int index = list.locationToIndex(evt.getPoint());
            } else if (evt.getClickCount() == 1) {

                // Triple-click detected
                int index = list.locationToIndex(evt.getPoint());
            }
        }
    }
    
    class JTextFieldJPopupMenuMediator {
       private final JTextField jtextField_;
       private final JPopupMenu jpopupMenu_;
       private boolean popupMenuVisible_;
       
       public JTextFieldJPopupMenuMediator(JTextField jtextField, JPopupMenu jpopupMenu) {
          jtextField_ = jtextField;
          jpopupMenu_ = jpopupMenu;
          popupMenuVisible_ = false;
       }
       
       public void setPopupMenuVisible( boolean visible) {
          popupMenuVisible_ = visible;
       }
       
       public boolean isPopupMenuVisible() {
          return popupMenuVisible_;
       }
       
       public JTextField getJTextField() {
          return jtextField_;
       }
       
       public JPopupMenu getJPopupMenu() {
          return jpopupMenu_;
       }
    }

    /**
     * Create a JPopupMenu component accessible through a right click on the JTextField. A JXList component
     * is populated from the values referenced by "mnemonic" and is added the JPopupMenu for selection by
     * the user.
     * 
     * @param mnemonic
     * @param jtextField 
     */
    private void initFixedField(String mnemonic,JTextField jtextField) {
        /**
         * Create the JXList with the "mnemonic" items
         */
        final JXList listCodes       = initList(mnemonic);
        /**
         * Create a JScrollPane to provide a scrollable view of the JXList
         */
        JScrollPane scrollPane       = new JScrollPane(listCodes);
        final JPopupMenu popMenu     = new JPopupMenu();
        /**
         * Create a mediator to control the JPopupMenu visibility over the JTextField
         */
        final JTextFieldJPopupMenuMediator med = 
                new JTextFieldJPopupMenuMediator(jtextField, popMenu);
        
        /**
         * Add a mouse listener to the JXList to catch item double click - The Listener will
         * also set the JTextField value with the double clicked item code
         */
         listCodes.addMouseListener(new JListMouseSelector(med)); 
        /**
         * Add a Mouse Listener to the JXList to catch ESCAPE key. The ESCAPE key hides the JPopupMenu
         */
        popMenu.addMenuKeyListener(new MenuKeyListener() {

            @Override
            public void menuKeyTyped(MenuKeyEvent e) {
                
            }
            @Override
            public void menuKeyPressed(MenuKeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (popMenu.isVisible()) {
                        popMenu.setVisible(false);
                        e.consume();
                        med.setPopupMenuVisible(false);
                    }
                }
            }

            @Override
            public void menuKeyReleased(MenuKeyEvent e) {
            }

        });
     
        /**
         * Add the scrollable JXList component to the JPopupMenu
         */
        popMenu.add(scrollPane);
                
        /**
         * Add a Mouse Listener to the JTextField to catch the Mouse Right Click
         */
        jtextField.addMouseListener(new JTextFieldMouseRightClick(med));
    }
    
      private void initFixedField(String mnemonic,JTextField jtextField,  List<SubfieldValue> subfieldValues) {
        /**
         * Create the JXList with the "mnemonic" items
         */
        final JXList listCodes       = initList(mnemonic, subfieldValues);
        /**
         * Create a JScrollPane to provide a scrollable view of the JXList
         */
        JScrollPane scrollPane       = new JScrollPane(listCodes);
        final JPopupMenu popMenu     = new JPopupMenu();
        /**
         * Create a mediator to control the JPopupMenu visibility over the JTextField
         */
        final JTextFieldJPopupMenuMediator med = 
                new JTextFieldJPopupMenuMediator(jtextField, popMenu);
        
        /**
         * Add a mouse listener to the JXList to catch item double click - The Listener will
         * also set the JTextField value with the double clicked item code
         */
         listCodes.addMouseListener(new JListMouseSelector(med)); 
        /**
         * Add a Mouse Listener to the JXList to catch ESCAPE key. The ESCAPE key hides the JPopupMenu
         */
        popMenu.addMenuKeyListener(new MenuKeyListener() {

            @Override
            public void menuKeyTyped(MenuKeyEvent e) {
                
            }
            @Override
            public void menuKeyPressed(MenuKeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (popMenu.isVisible()) {
                        popMenu.setVisible(false);
                        e.consume();
                        med.setPopupMenuVisible(false);
                    }
                }
            }

            @Override
            public void menuKeyReleased(MenuKeyEvent e) {
            }

        });
     
        /**
         * Add the scrollable JXList component to the JPopupMenu
         */
        popMenu.add(scrollPane);
                
        /**
         * Add a Mouse Listener to the JTextField to catch the Mouse Right Click
         */
        jtextField.addMouseListener(new JTextFieldMouseRightClick(med));
    }
    
    class LabelField {
       private final JLabel jlabel_;
       private final JTextField jtextfield_;
       
       public LabelField(String text, String description) {
          jlabel_ = new JLabel(text);
          description = "<html>"+
                  StringUtils.fastReplaceAll(description, ".", ".<br>")+"</html>";
          jlabel_.setToolTipText(description);
          int length = 1;
          String[] pos = text.split("-");
          if (pos.length > 2) {
              int beginning = Integer.parseInt(pos[0].trim());
              int end = Integer.parseInt(pos[1].trim());
              length = end - beginning + 1;
          }
          jtextfield_ = new JTextField("", length);
          jtextfield_.setDocument(new FixedSizeDocument(length));
       }
    }
    
    
    private void initializeBooksTemplate(FixedFieldsContainer fixedFieldsContainer) {

        initFixedField("Rec stat", recStatField);
        initFixedField("BLvl", BLvlField);
        initFixedField("Type", typeField);
        initFixedField("Ctrl", CtrlField);
        initFixedField("ELvl", ELvlField);
        
        FixedFieldDescription fixedFieldDescription = fixedFieldsContainer.getFieldByIndex(0);
        
        List<FixedFieldSubfield> fixedFieldSubfields = fixedFieldDescription.getFixedFieldSubfields();
        labelFields_ = new LabelField[fixedFieldSubfields.size()];
        for (int i=0; i<fixedFieldSubfields.size(); i++) {
           FixedFieldSubfield fixedFieldSubfield = fixedFieldSubfields.get(i);
           String text = fixedFieldSubfield.positionsLabel_;
           labelFields_[i] = new LabelField(text, fixedFieldSubfield.positionsDescription_);
          
           
           List<SubfieldValue> subfieldValues = fixedFieldSubfield.subfieldValues_;
           initFixedField("", labelFields_[i].jtextfield_, subfieldValues);
        }

        
    }

    /**
     * Build the main panel of the dashboard using
     * the MiGLayout layout manager.
     */
    private JPanel buildMiGDashboard() {
        JPanel panel = new JPanel();
	panel.setLayout(new MigLayout());
        buildMiGForm(panel);

        //add docked components
        panel.add(widget3, "east, gapleft 5, w 100");
        panel.add(headerLabel, "north, gapbottom 15, h 40, id headerLabel");    //can specify height and not width
        panel.add(footerLabel, "south, gaptop 15, h 40");
        panel.add(widget1, "west, gapright 5, w 80");
        panel.add(widget2, "west, gapright 10, w 80");

        //add absolutely positioned component
        panel.add(avatarImage, "pos (headerLabel.x2 - 28) (headerLabel.y2 + 5)");

        return panel;
    }
    private void buildMiGForm(JPanel panel) {
        panel.add(settingsLabel, "span, center, gapbottom 15");
        panel.add(recStatLabel, "align label");
        panel.add(recStatField);
        panel.add(typeLabel,"align label");
        panel.add(typeField);
        panel.add(BLvlLabel, "align label");
        panel.add(BLvlField, "wrap");
        panel.add(CtrlLabel, "align label");
        panel.add(CtrlField);
        panel.add(ELvlLabel, "align label");
        panel.add(ELvlField, "wrap");
        
        for (int i = 0; i < labelFields_.length; i++) {
            panel.add(labelFields_[i].jlabel_, "align label");
            if ((i / 3) * 3 == i) {
                panel.add(labelFields_[i].jtextfield_, "wrap");
            } else {
                panel.add(labelFields_[i].jtextfield_);
            }
        }

       
        panel.add(okBttn, "tag ok, span, split 3, sizegroup bttn");
        panel.add(cancelBttn, "tag cancel, sizegroup bttn");
        panel.add(helpBttn, "tag help, sizegroup bttn");
    }

   

    /**
     * This example demonstrates using MiGLayout as a flow layout.
     */
//    private JPanel buildMiGFlowLayout() {
//        JPanel panel = new JPanel();
//	panel.setLayout(new MigLayout("nogrid, flowy, debug"));    //make vertical flowing, also running in debug mode
//
//        //add flowing components
//        panel.add(widget3, "w 100");
//        panel.add(headerLabel, "h 40");
//        panel.add(footerLabel, "h 40");
//        panel.add(widget1, "w 80, wrap");
//        panel.add(widget2, "w 80");
//        panel.add(avatarImage);
//
//        return panel;
//    }


    /**
     * Everything below here is just setup code that is the same for
     * all examples. It is not part of the comparison.
     */
    private void initializeDockingComponents() {
        Font labelFont = new Font("Arial",Font.BOLD, 14);

        headerLabel = new JLabel("Player Dashboard", SwingConstants.CENTER);
        setUpLabel(headerLabel, Color.BLUE, Color.YELLOW, labelFont.deriveFont((float)24));

        widget1 = new JLabel("Widget 1", SwingConstants.CENTER);
        setUpLabel(widget1, new Color(209,234,249), Color.BLACK, labelFont);

        widget2 = new JLabel("Widget 2", SwingConstants.CENTER);
        setUpLabel(widget2, new Color(209,234,249), Color.BLACK, labelFont);

        widget3 = new JLabel("Widget 3", SwingConstants.CENTER);
        setUpLabel(widget3, new Color(191,139,158), Color.BLACK, labelFont);

        footerLabel = new JLabel("Have Fun!", SwingConstants.CENTER);
        setUpLabel(footerLabel, Color.BLUE, Color.YELLOW, labelFont.deriveFont((float)18));

        ImageIcon avatarIcon =
                new ImageIcon(getClass().getClassLoader().getResource("/org/unesco/jisis/dataentryex/mr-kucing-welcome.png"));
        avatarImage = new JLabel(avatarIcon);
    }
    private void setUpLabel(JLabel label, Color bg, Color fg, Font f) {
        label.setOpaque(true);
        label.setBackground(bg);
        label.setForeground(fg);
        label.setFont(f);    
    }

    class SimpleListModel extends DefaultListModel implements ListModel {

        /**
         *
         * @param Mnemonic
         */
        void loadData(String mnemonic) {
            switch (mnemonic) {
                case "Rec stat":
                    addElement("<html><b>n</b> - New</html>");
                    addElement("<html><b>d</b> - Deleted</html>");
                    addElement("<html><b>a</b> - Increase in encoding level</html>");
                    addElement("<html><b>c</b> - Corrected or revised</html>");
                    addElement("<html><b>p</b> - Increase in encoding level from prepublication</html>");
                    break;
                case "Type":
                    addElement("<html><b>a</b> - Language material</html>");
                    addElement("<html><b>c</b> - Notated music</html>");
                    addElement("<html><b>d</b> - Manuscript notated music");
                    addElement("<html><b>e</b> - Cartographic material</html>");
                    addElement("<html><b>f</b> - Manuscript cartographic material</html>");
                    addElement("<html><b>g</b> - Projected medium</html>");
                    addElement("<html><b>i</b> - Nonmusical sound recording<html>");
                    addElement("<html><b>j</b> - Musical sound recording</html>");
                    addElement("<html><b>k</b> - Two-dimensional nonprojectable graphic</html>");
                    addElement("<html><b>m</b> - Computer file</html>");
                    addElement("<html><b>o</b> - Kit</html>");
                    addElement("<html><b>p</b> - Mixed materials</html>");
                    addElement("<html><b>r</b> - Three-dimensional artifact or naturally occurring object</html>");
                    addElement("<html><b>t</b> - Manuscript language material</html>");
                    break;
                case "BLvl":
                    addElement("<html><b>a</b> - Monographic component part</html>");
                    addElement("<html><b>b</b> - Serial component part</html>");
                    addElement("<html><b>c</b> - Collection</html>");
                    addElement("<html><b>d</b> - Subunit</html>");
                    addElement("<html><b>i</b> - Integrating resource</html>");
                    addElement("<html><b>m</b> - Monograph/Item</html>");
                    addElement("<html><b>s</b> - Serial</html>");
                    break;
                case "Ctrl":
                    addElement("<html><b>#</b> - Default: blanc</html>");
                    addElement("<html><b>a</b> - Archival</html>");
                    break;
                case "ELvl":
                    addElement("<html><b>#</b> - Full level</html>");
                    addElement("<html><b>1</b> - Full level, material not examined</html>");
                    addElement("<html><b>2</b> - Less-than-full level, material not examined</html>");
                    addElement("<html><b>3</b> - Abbreviated level</html>");
                    addElement("<html><b>4</b> - Core level</html>");
                    addElement("<html><b>5</b> - Partial (preliminary) level</html>");
                    addElement("<html><b>7</b> - Minimal level</html>");
                    addElement("<html><b>8</b> - Prepublication level</html>");
                    addElement("<html><b>u</b> - Unknown</html>");
                    addElement("<html><b>z</b> - Not applicable</html>");
                    break;
            }
        }

        void loadData(List<SubfieldValue> subfieldValues) {
            for (SubfieldValue subfieldValue : subfieldValues) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><b>")
                    .append(subfieldValue.getValueLabel().substring(0,1))
                    .append("</b>")
                    .append(subfieldValue.getValueLabel().substring(1))
                    .append("</html>");
                addElement(sb.toString());
            }
        }
    }

}

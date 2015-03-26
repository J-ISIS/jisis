/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.unesco.jisis.gui;

//~--- JDK imports ------------------------------------------------------------

import org.unesco.jisis.jisisutil.history.OperatingSystem;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author jc_dauphin
 */
public class GuiUtils {
   public static void TweakJTable(JTable table) {
      // ------------------------------------------------
      // Default row height is 16 pixels, increase to 20
      // ------------------------------------------------
      table.setRowHeight(20);

      // ------------------------------------------
      // Increase the gap between rows and columns
      // ------------------------------------------
      // Get defaults
      Dimension d = table.getIntercellSpacing();
      // d.width == 1, d.height == 1
      // Add 5 spaces to the left and right sides of a cell.
      // Add 2 spaces to the top and bottom sides of a cell.
      int gapWidth  = 10;
      int gapHeight = 4;

      table.setIntercellSpacing(new Dimension(gapWidth, gapHeight));
      // Increase the row height
      table.setRowHeight(table.getRowHeight() + gapHeight);
   }

   public static void adjustColumnWidthHeader(JTable table) {
      TableColumnModel tcm = table.getColumnModel();

      for (int i = 0; i < table.getColumnCount(); i++) {
         int               maxwidth       = 0;
         TableColumn       tc             = tcm.getColumn(i);
         TableCellRenderer headerRenderer = tc.getHeaderRenderer();

         if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
         }

         Object    headerValue = tc.getHeaderValue();
         Component headerComp  = headerRenderer.getTableCellRendererComponent(table, headerValue,
                                    false, false, 0, i);

         maxwidth = Math.max(maxwidth, headerComp.getPreferredSize().width);

         tc.setMinWidth(maxwidth);
      }
   }
   
   
   /**
    * Change the column header so that the text is in bold
    * @param table
    */
     public static void makeColumnHeaderBold(JTable table) {    
       JTableHeader header = table.getTableHeader();
       final Font boldFont = header.getFont().deriveFont(Font.BOLD);
       final TableCellRenderer headerRenderer = header.getDefaultRenderer();
       header.setDefaultRenderer( new DefaultTableCellRenderer() {
         @Override
	 public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
	   Component comp = 
                   headerRenderer.getTableCellRendererComponent( table, value, 
                   isSelected, hasFocus, row, column );
				
	   comp.setFont( boldFont );
           setHorizontalAlignment(JLabel.CENTER);
	   return comp;
          }
	});
        
     }
     
    //{{{ getScreenBounds() method
    /**
     * Returns the screen bounds, taking into account multi-screen environments.
     *
     * @return
     * @since jEdit 4.3pre18
     */
    public static Rectangle getScreenBounds() {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().
            getMaximumWindowBounds();
        GraphicsDevice[] devices = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getScreenDevices();
        if (devices.length > 1) {
            for (GraphicsDevice device : devices) {
                for (GraphicsConfiguration config : device.getConfigurations()) {
                    bounds = bounds.union(config.getBounds());
                }
            }
        }
        return bounds;
    }

    //{{{ showPopupMenu() method
    /**
     * Shows the specified popup menu, ensuring it is displayed within the bounds of the screen.
     *
     * @param popup The popup menu
     * @param comp The component to show it for
     * @param x The x co-ordinate
     * @param y The y co-ordinate
     * @since jEdit 4.0pre1
     * @see javax.swing.JComponent#setComponentPopupMenu(javax.swing.JPopupMenu) setComponentPopupMenu which
     * works better and is simpler to use: you don't have to write the code to show/hide popups in response to
     * mouse events anymore.
     */
    public static void showPopupMenu(JPopupMenu popup, Component comp,
        int x, int y) {
        showPopupMenu(popup, comp, x, y, true);
    } //}}}

    //{{{ showPopupMenu() method
    /**
     * Shows the specified popup menu, ensuring it is displayed within the bounds of the screen.
     *
     * @param popup The popup menu
     * @param comp The component to show it for
     * @param x The x co-ordinate
     * @param y The y co-ordinate
     * @param point If true, then the popup originates from a single point; otherwise it will originate from
     * the component itself. This affects positioning in the case where the popup does not fit onscreen.
     *
     * @since jEdit 4.1pre1
     */
    public static void showPopupMenu(JPopupMenu popup, Component comp,
        int x, int y, boolean point) {
        int offsetX = 0;
        int offsetY = 0;

        int extraOffset = point ? 1 : 0;

        Component win = comp;
        while (!(win instanceof Window || win == null)) {
            offsetX += win.getX();
            offsetY += win.getY();
            win = win.getParent();
        }

        if (win != null) {
            Dimension size = popup.getPreferredSize();

            Rectangle screenSize = getScreenBounds();

            if (x + offsetX + size.width + win.getX() > screenSize.width
                && x + offsetX + win.getX() >= size.width) {
                //System.err.println("x overflow");
                if (point) {
                    x -= size.width + extraOffset;
                } else {
                    x = win.getWidth() - size.width - offsetX + extraOffset;
                }
            } else {
                x += extraOffset;
            }

			//System.err.println("y=" + y + ",offsetY=" + offsetY
            //	+ ",size.height=" + size.height
            //	+ ",win.height=" + win.getHeight());
            if (y + offsetY + size.height + win.getY() > screenSize.height
                && y + offsetY + win.getY() >= size.height) {
                if (point) {
                    y = win.getHeight() - size.height - offsetY + extraOffset;
                } else {
                    y = -size.height - 1;
                }
            } else {
                y += extraOffset;
            }

            popup.show(comp, x, y);
        } else {
            popup.show(comp, x + extraOffset, y + extraOffset);
        }

    } //}}}
    //{{{ isPopupTrigger() method
	/**
	 * Returns if the specified event is the popup trigger event.
	 * This implements precisely defined behavior, as opposed to
	 * MouseEvent.isPopupTrigger().
	 * @param evt The event
	 * @since jEdit 3.2pre8
	 */
	public static boolean isPopupTrigger(MouseEvent evt)
	{
		return isRightButton(evt.getModifiers());
	} //}}}

	
        
        //{{{ isMiddleButton() method
	/**
	 * @param modifiers The modifiers flag from a mouse event
	 * @return true if the modifier match the middle button
	 * @since jEdit 4.3pre7
	 */
	  public static boolean isMiddleButton(int modifiers) {
      if (OperatingSystem.isMacOS()) {
         if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            return (modifiers & InputEvent.ALT_MASK) != 0;
         } else {
            return (modifiers & InputEvent.BUTTON2_MASK) != 0;
         }
      } else {
         return (modifiers & InputEvent.BUTTON2_MASK) != 0;
      }
   } //}}}

   //{{{ isRightButton() method
   /**
    * @param modifiers The modifiers flag from a mouse event
    * @return true if the modifier match the right button
    * @since jEdit 4.3pre7
    */
   public static boolean isRightButton(int modifiers) {
      if (OperatingSystem.isMacOS()) {
         if ((modifiers & InputEvent.BUTTON1_MASK) != 0) {
            return (modifiers & InputEvent.CTRL_MASK) != 0;
         } else {
            return (modifiers & InputEvent.BUTTON3_MASK) != 0;
         }
      } else {
         return (modifiers & InputEvent.BUTTON3_MASK) != 0;
      }
   } //}}}
    


}

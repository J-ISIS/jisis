/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.unesco.jisis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 *
 * @author jcd
 */
public class ImagePreview extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 0;
	private static final Dimension PREVIEW_DIMENSIONS = new Dimension(200, 200);
	private static final Color BACKGROUND = Color.GRAY;
	private JFileChooser chooser;
	private BufferedImage image;
	private boolean showDimensions = true;

    /**
     * Adds an image preview to a file chooser.
     * @param chooser     chooser to be augmented
     * @return added image previewer
     */
	public static ImagePreview addPreview(JFileChooser chooser) {
        ImagePreview preview = new ImagePreview(chooser);
        chooser.addPropertyChangeListener(preview);
        chooser.setAccessory(preview);
        return preview;
	}

    /**
     * Strips all instances of the image preview from a file chooser.
     * @param chooser     chooser to be altered
     */
	public static void removePreview(JFileChooser chooser) {
		for (PropertyChangeListener listener : chooser.getPropertyChangeListeners()) {
			if (listener instanceof ImagePreview) {
				chooser.removePropertyChangeListener(listener);
				chooser.setAccessory(null);
			}
		}
	}

    /**
     * Constructs an image preview that tracks a file chooser.
     * @param chooser     source of files to display
     */
	public ImagePreview(JFileChooser chooser) {
		this.chooser = chooser;
		setPreferredSize(PREVIEW_DIMENSIONS);
	}

    /**
     * Sets if the display should note previewed image's dimensions.
     * @param display     if true dimensions are shown, otherwise they
     * are hidden
     */
	public void showDimensions(boolean display) {
		this.showDimensions = display;
	}

	public void propertyChange(PropertyChangeEvent event) {
		try {
			File file = this.chooser.getSelectedFile();
			updateImage(file);
		} catch (IOException exc) {
			System.out.println(exc.getMessage());
			exc.printStackTrace();
		}
	}

   @Override
	public void paintComponent(Graphics g) {
		// Fills background
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (this.image != null) {
			// Finds scaling that will fit within display
			double widthScale = PREVIEW_DIMENSIONS.getWidth() / this.image.getWidth();
			double heightScale = PREVIEW_DIMENSIONS.getHeight() / this.image.getHeight();
			double scale = Math.min(widthScale, heightScale);

			int imageWidth = (int) (scale * this.image.getWidth());
			int imageHeight = (int) (scale * this.image.getHeight());
			g.drawImage(this.image, 0, 0, imageWidth, imageHeight, null);

			// Draws lable for dimensions
			if (this.showDimensions) {
				String dimensions = imageWidth + " x " + imageHeight;
				g.setColor(Color.BLACK);
				g.drawString(dimensions, 31, PREVIEW_DIMENSIONS.height - 4);
				g.setColor(Color.WHITE);
				g.drawString(dimensions, 30, PREVIEW_DIMENSIONS.height - 5);
			}
		} else {
			g.setColor(Color.BLACK);
			g.drawString("No image available", 30, PREVIEW_DIMENSIONS.height / 2);
		}
	}

	// Sets the dispaly to a given file
	private void updateImage(File file) throws IOException {
		if (file == null) return;
		this.image = ImageIO.read(file);
		repaint();
	}
}


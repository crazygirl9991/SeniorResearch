package downloadCenter;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;

/**
 * 
 * @author vtielebein, lmachen
 *
 */
@SuppressWarnings("serial")
public class SpectrumPlotter extends JComponent {
	
	private TableElement[] _elements;

	private static final int SIZE_X = 800; // width of the plots in pixels
	private static final int SIZE_Y = 300; // height of the plots in pixels
	private static final float DIVISION_X = 10; // number of divisions along the x axis
	private static final float DIVISION_Y = 10; // number of divisions along the y axis
	private float minX; // minimum x of view window
	private float maxX; // maximum x of view window
	private float minY; // minimum y of view window
	private float maxY; // maximum y of view window
	private BufferedImage bitmap; // bitmap used for double buffering

	public SpectrumPlotter(TableElement[] elements) throws Exception {
		_elements = elements;
		
		// Spectrum plotter has control over coloring each table element for now, but the
		// elements themselves keep track of their color for more organized persistence
		// and the potential of adding auto-coloring to the TableElement class later.
		if(_elements.length == 1)
			elements[0].setColor(Color.red);
		else if(_elements.length == 2) {
			elements[0].setColor(Color.green);
			elements[1].setColor(Color.blue);
		} else
			throw new Exception("ERROR: Attempting to plot invalid number of spectra.");

		
		// set the size of the plots
		setPreferredSize( new Dimension(SIZE_X, SIZE_Y) );
		
		// find the min and max of all of the data
		minX = _elements[0].getSpectrumDataX()[0];
		maxX = minX;
		minY = _elements[0].getSpectrumDataY()[0];
		maxY = minY;
		
		for (int i = 0; i < _elements.length; i++) {
			for (int j = 0; j < _elements[i].getSpectrumDataX().length; j++) {
				float x = _elements[i].getSpectrumDataX()[j];
				float y = _elements[i].getSpectrumDataY()[j];
				if(x < minX)
					minX = x;
				if(x > maxX)
					maxX = x;
				if(y < minY)
					minY = y;
				if(y > maxY)
					maxY = y;
			}
		}
		
		// Create the bitmap and draw the plot
		bitmap = new BufferedImage(SIZE_X, SIZE_Y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bitmap.getGraphics();
		// draw the background white
		g2.setColor(Color.white);
		g2.fillRect(0, 0, SIZE_X, SIZE_Y);
		
		// draw a line for each data point
		for (int i = 0; i < _elements.length; i++) {
			g2.setColor( _elements[i].getColor() );
			for (int j = 0; j < _elements[i].getSpectrumDataX().length - 1; j++) {
				g2.draw(new Line2D.Float( pixelX( _elements[i].getSpectrumDataX()[j] ), 
										  pixelY(_elements[i].getSpectrumDataY()[j] ), 
										  pixelX(_elements[i].getSpectrumDataX()[j + 1] ), 
										  pixelY(_elements[i].getSpectrumDataY()[j + 1] ) ) );
			}
		}
		
		// draw the axes
		g2.setColor(Color.black);
		g2.drawLine(0, SIZE_Y - 1, SIZE_X - 1, SIZE_Y - 1);
		g2.drawLine(0, 0, 0, SIZE_Y - 1);
		for(int i = 1; i < DIVISION_X; i++) {
			// draw the x axis tick marks
			g2.draw(new Line2D.Float(i * SIZE_X / DIVISION_X, SIZE_Y - 1, i * SIZE_X / DIVISION_X, SIZE_Y - 6));
			// and the labels for each tick mark (centered)
			String xlbl = String.format("%01.3f", ( (maxX - minX) / DIVISION_X * i) );
			float strwidth = (float) g2.getFontMetrics().getStringBounds(xlbl, g2).getWidth();
			g2.drawString(xlbl, i * SIZE_X / DIVISION_X - strwidth / 2, SIZE_Y - 11);
			// draw the y axis tick marks
			g2.draw(new Line2D.Float(0, i * SIZE_Y / DIVISION_Y, 5, i * SIZE_Y / DIVISION_Y) );
			// and the labels for each tick mark (centered)
			String ylbl = String.format("%01.3f", ( (maxY - minY) / DIVISION_Y * i) );
			float strheight = (float) g2.getFontMetrics().getStringBounds(ylbl, g2).getHeight();
			g2.drawString(ylbl, 10, SIZE_Y - i * SIZE_Y / DIVISION_Y + strheight / 2);
		}
	}

	/**
	 * Convert graph x coordinate into pixel value.
	 */
	private float pixelX(float x) {
		return SIZE_X * (x - minX) / (maxX - minX);
	}

	/**
	 * Convert graph y coordinate into pixel value
	 */
	private float pixelY(float y) {
		return SIZE_Y * (1 - (y - minY) / (maxY - minY) );
	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		// Paint the plot
		graphics.drawImage(bitmap, 0, 0, null);
	}
}

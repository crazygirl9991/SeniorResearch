package downloadCenter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JComponent;

/**
 * 
 * @author vtielebein, lmachen
 * 
 */
@SuppressWarnings("serial")
public class SpectrumPlotter extends JComponent implements ComponentListener, MouseListener, MouseMotionListener {

	private static final Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 14);

	private TableElement[] _elements;

	private static int SIZE_X = 800; // width of the plots in pixels
	private static int SIZE_Y = 300; // height of the plots in pixels
	private int LEFT_OFFSET;
	private int RIGHT_OFFSET;
	private int TOP_OFFSET;
	private int BOTTOM_OFFSET;
	private static final float DIVISION_X = 10; // number of divisions along the x axis
	private static final float DIVISION_Y = 10; // number of divisions along the y axis

	private static final int SMOOTH_RADIUS = 1;

	private static final int GAP = 5;

	private static final float DOT_RADIUS = 3;

	private final float MINX;
	private final float MAXX;

	private Float minX; // minimum x of view window
	private Float maxX; // maximum x of view window
	private Float minY; // minimum y of view window
	private Float maxY; // maximum y of view window
	private static float zoom = 2;
	private static float center;
	private BufferedImage bitmap; // bitmap used for double buffering

	private static float currenttrace = 0;

	private static boolean smoothdata;

	private ArrayList<ArrayList<Float>> xdata = new ArrayList<ArrayList<Float>>();

	private ArrayList<ArrayList<Float>> ydata = new ArrayList<ArrayList<Float>>();

	public SpectrumPlotter(TableElement[] elements) throws Exception {
		_elements = elements;

		// Spectrum plotter has control over coloring each table element for now, but the
		// elements themselves keep track of their color for more organized persistence
		// and the potential of adding auto-coloring to the TableElement class later.
		if (_elements.length == 1)
			elements[0].setColor(Color.red);
		else if (_elements.length == 2) {
			elements[0].setColor(Color.green.darker());
			elements[1].setColor(Color.blue);
		} else
			throw new Exception("ERROR: Attempting to plot invalid number of spectra.");

		// set the size of the plots
		setPreferredSize(new Dimension(SIZE_X, SIZE_Y));

		addComponentListener(this);

		addMouseListener(this);
		addMouseMotionListener(this);

		// find the min and max of all of the data
		float min_X = _elements[0].getSpectrumDataX()[0];
		float max_X = min_X;
		for (int i = 0; i < _elements.length; i++) {
			float x;
			for (int j = 0; j < _elements[i].getSpectrumDataX().length; j++) {
				x = _elements[i].getSpectrumDataX()[j];
				if (x < min_X)
					min_X = x;
				if (x > max_X)
					max_X = x;
			}
		}
		MINX = min_X;
		MAXX = max_X;
		center = (MAXX + MINX) / 2;
		redraw();
	}

	/**
	 * Convert graph x coordinate into pixel value.
	 */
	private float pixelX(float x) {
		return LEFT_OFFSET + (SIZE_X - LEFT_OFFSET - RIGHT_OFFSET) * (x - minX) / (maxX - minX);
	}

	/**
	 * Convert graph y coordinate into pixel value
	 */
	private float pixelY(float y) {
		return TOP_OFFSET + (SIZE_Y - BOTTOM_OFFSET - TOP_OFFSET) * (1 - (y - minY) / (maxY - minY));
	}

	private float divX(int i) {
		return pixelX(minX + i * (maxX - minX) / DIVISION_X);
	}

	private float divY(int i) {
		return pixelY(minY + i * (maxY - minY) / DIVISION_Y);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		// Paint the plot
		graphics.drawImage(bitmap, 0, 0, null);
		Graphics2D g2 = (Graphics2D) graphics;
		FontMetrics fontMetrics = g2.getFontMetrics();
		int strheight = fontMetrics.getAscent() - fontMetrics.getDescent();
		for (int i = 0; i < xdata.size(); i++) {
			g2.setColor(_elements[i].getColor());
			int index = Collections.binarySearch(xdata.get(i), currenttrace);
			if (index < 0)
				index = -index - 1;
			if (index >= xdata.get(i).size())
				index = xdata.get(i).size() - 1;
			g2.fill(new Ellipse2D.Float(pixelX(xdata.get(i).get(index)) - DOT_RADIUS, pixelY(ydata.get(i).get(index)) - DOT_RADIUS, 2 * DOT_RADIUS,
					2 * DOT_RADIUS));
			String xcoord = "X: " + String.format("%01.3f", xdata.get(i).get(index));
			g2.drawString(xcoord, SIZE_X - fontMetrics.stringWidth(xcoord), (2 * i + 1) * (strheight + GAP));
			String ycoord = "Y: " + String.format("%01.3f", ydata.get(i).get(index));
			g2.drawString(ycoord, SIZE_X - fontMetrics.stringWidth(ycoord), (2 * i + 2) * (strheight + GAP));
		}

	}

	public void redraw() {
		float minbound = center - (MAXX - MINX) / zoom;
		float maxbound = center + (MAXX - MINX) / zoom;

		xdata.clear();
		ydata.clear();
		minX = null;
		maxX = null;
		minY = null;
		maxY = null;

		for (int i = 0; i < _elements.length; i++) {
			int len = _elements[i].getSpectrumDataX().length;
			if (smoothdata)
				len -= 2 * SMOOTH_RADIUS;
			xdata.add(new ArrayList<Float>());
			ydata.add(new ArrayList<Float>());
			for (int j = 0; j < len; j++) {
				float x = 0, y = 0;
				if (smoothdata) {
					x = _elements[i].getSpectrumDataX()[j + SMOOTH_RADIUS];
					if (x < minbound || x > maxbound)
						continue;
					for (int l = 0; l <= SMOOTH_RADIUS * 2; l++) {
						y += _elements[i].getSpectrumDataY()[j + l] / (2 * SMOOTH_RADIUS + 1);
					}
				} else {
					x = _elements[i].getSpectrumDataX()[j];
					if (x < minbound || x > maxbound)
						continue;
					y = _elements[i].getSpectrumDataY()[j];
				}
				// find the min and max for the current window
				if (minX == null)
					minX = x;
				if (maxX == null)
					maxX = x;
				if (minY == null)
					minY = y;
				if (maxY == null)
					maxY = y;
				if (x < minX)
					minX = x;
				if (x > maxX)
					maxX = x;
				if (y < minY)
					minY = y;
				if (y > maxY)
					maxY = y;
				xdata.get(i).add(x);
				ydata.get(i).add(y);
			}
		}

		// Create the bitmap and draw the plot
		bitmap = new BufferedImage(SIZE_X, SIZE_Y, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bitmap.getGraphics();
		// draw the background white
		g2.setColor(Color.white);
		g2.fillRect(0, 0, SIZE_X, SIZE_Y);
		g2.setFont(font);
		FontMetrics fontMetrics = g2.getFontMetrics();
		int strheight = fontMetrics.getAscent() - fontMetrics.getDescent();
		int maxWidth = 0;
		for (int i = 0; i <= DIVISION_Y; i++) {
			int width = fontMetrics.stringWidth(String.format("%01.3f", minY + ((maxY - minY) / DIVISION_Y * i)));
			if (width > maxWidth)
				maxWidth = width;
		}
		LEFT_OFFSET = strheight + maxWidth + 4 * GAP;
		RIGHT_OFFSET = fontMetrics.stringWidth(String.format("%01.3f", maxX)) / 2 + GAP;
		TOP_OFFSET = strheight / 2 + GAP;
		BOTTOM_OFFSET = 2 * strheight + 4 * GAP;

		// draw a line for each data point
		for (int i = 0; i < _elements.length; i++) {
			g2.setColor(_elements[i].getColor());
			for (int j = 0; j < xdata.get(i).size() - 1; j++) {
				g2.draw(new Line2D.Float(pixelX(xdata.get(i).get(j)), pixelY(ydata.get(i).get(j)), pixelX(xdata.get(i).get(j + 1)), pixelY(ydata.get(i).get(
						j + 1))));
			}
		}

		// draw the axes
		g2.setColor(Color.black);
		g2.draw(new Line2D.Float(pixelX(minX), pixelY(minY), pixelX(maxX), pixelY(minY)));
		g2.draw(new Line2D.Float(pixelX(minX), pixelY(minY), pixelX(minX), pixelY(maxY)));
		String xaxislabel = "Wavelength (\u00C5)";
		g2.drawString(xaxislabel, pixelX((maxX + minX) / 2) - fontMetrics.stringWidth(xaxislabel) / 2, pixelY(minY) + 3 * GAP + 2 * strheight);

		if (_elements.length == 2) {
			AffineTransform identity = g2.getTransform();
			g2.transform(AffineTransform.getRotateInstance(-Math.PI / 2, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2, pixelY((maxY + minY) / 2)));
			AttributedString yaxislabel = new AttributedString("Flux (10-17 erg/(cm*s2*\u00C5))");
			yaxislabel.addAttribute(TextAttribute.FONT, font);
			yaxislabel.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 8, 11);
			yaxislabel.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 21, 22);
			AttributedCharacterIterator iterator = yaxislabel.getIterator();
			float yaxislabelwidth = (float) fontMetrics.getStringBounds(iterator, iterator.getBeginIndex(), iterator.getEndIndex(), g2).getWidth();
			g2.drawString(iterator, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2 - yaxislabelwidth / 2, pixelY((maxY + minY) / 2) + strheight / 2);
			g2.setTransform(identity);
		} else {
			g2.setStroke(new BasicStroke(0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] { 5 }, 0));
			g2.draw(new Line2D.Float(pixelX(minX), pixelY(1), pixelX(maxX), pixelY(1)));
			AffineTransform identity = g2.getTransform();
			g2.transform(AffineTransform.getRotateInstance(-Math.PI / 2, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2, pixelY((maxY + minY) / 2)));
			String yaxislabel = "Ratio Between Flux";
			float yaxislabelwidth = fontMetrics.stringWidth(yaxislabel);
			g2.drawString(yaxislabel, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2 - yaxislabelwidth / 2, pixelY((maxY + minY) / 2) + strheight / 2);
			g2.setTransform(identity);
		}
		for (int i = 0; i <= DIVISION_X; i++) {
			// draw the x axis tick marks
			g2.draw(new Line2D.Float(divX(i), pixelY(minY), divX(i), pixelY(minY) + GAP));
			// and the labels for each tick mark (centered)
			String xlbl = String.format("%01.3f", minX + ((maxX - minX) / DIVISION_X * i));
			g2.drawString(xlbl, divX(i) - fontMetrics.stringWidth(xlbl) / 2, pixelY(minY) + 2 * GAP + strheight);
		}
		for (int i = 0; i <= DIVISION_Y; i++) {
			// draw the y axis tick marks
			g2.draw(new Line2D.Float(pixelX(minX), divY(i), pixelX(minX) - GAP, divY(i)));
			// and the labels for each tick mark (centered)
			String ylbl = String.format("%01.3f", minY + ((maxY - minY) / DIVISION_Y * i));
			g2.drawString(ylbl, pixelX(minX) - 2 * GAP - fontMetrics.stringWidth(ylbl), divY(i) + strheight / 2);
		}
		repaint();
	}

	public static void setSmoothed(boolean selected) {
		smoothdata = selected;
	}

	@Override
	public void componentHidden(ComponentEvent ce) {
	}

	@Override
	public void componentMoved(ComponentEvent ce) {
	}

	@Override
	public void componentResized(ComponentEvent ce) {
		SIZE_X = getWidth();
		SIZE_Y = getHeight();
		redraw();
	}

	@Override
	public void componentShown(ComponentEvent ce) {
	}

	@Override
	public void mouseClicked(MouseEvent me) {
		center = (me.getX() - LEFT_OFFSET) / ((float) SIZE_X - LEFT_OFFSET - RIGHT_OFFSET) * (maxX - minX) + minX;
		switch (me.getButton()) {
		case MouseEvent.BUTTON1:
			zoom *= 2;
			break;
		case MouseEvent.BUTTON3:
			if (zoom > 2)
				zoom /= 2;
			break;
		}
		center = Math.max((MAXX - MINX) / zoom + MINX, Math.min(MAXX - (MAXX - MINX) / zoom, center));
		DisplayFrame.redrawBothPlots();
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
	}

	@Override
	public void mouseReleased(MouseEvent me) {
	}

	@Override
	public void mouseDragged(MouseEvent me) {
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		currenttrace = (me.getX() - LEFT_OFFSET) / ((float) SIZE_X - LEFT_OFFSET - RIGHT_OFFSET) * (maxX - minX) + minX;
		DisplayFrame.repaintBothPlots();
	}
}

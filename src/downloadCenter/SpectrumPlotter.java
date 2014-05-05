package downloadCenter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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

	private TableElement[] _elements;

	private int SIZE_X = 800; // width of the plots in pixels
	private int SIZE_Y = 300; // height of the plots in pixels
	
	private final float DIVISION_X = 10; // number of divisions along the x axis
	private final float DIVISION_Y = 10; // number of divisions along the y axis
	
	// margins around the graph
	private int _leftMargin;
	private int _rightMargin;
	private int _topMargin;
	private int _bottomMargin;

	// how many pixels on either side to average together
	private final int SMOOTH_RADIUS = 1;

	// controls the spacing between the text
	private final int GAP = 5;

	// determines the size of the trace marker in pixels
	private final float DOT_RADIUS = 3;

	// 
	private final float MIN_X;
	private final float MAX_X;

	private Float minX; // minimum x of view window
	private Float maxX; // maximum x of view window
	private Float minY; // minimum y of view window
	private Float maxY; // maximum y of view window
	private float zoom = 2;
	private float center;
	private BufferedImage bitmap; // bitmap used for double buffering

	private float currenttrace = 0;

	private boolean smoothdata;

	private ArrayList<ArrayList<Float>> xdata;

	private ArrayList<ArrayList<Float>> ydata;
	
	private PlottingInterface _parent;
	
	private boolean _ratio;

	public SpectrumPlotter(TableElement[] elements, PlottingInterface parent, boolean ratio) throws Exception {
		_elements = elements;
		_parent = parent;
		_ratio = ratio;
		
		// set the size of the plots
		setPreferredSize( new Dimension(SIZE_X, SIZE_Y) );

		addComponentListener(this);

		addMouseListener(this);
		addMouseMotionListener(this);

		// find the min and max of all of the data
		float min_X = _elements[0].getSpectrumDataX()[0];
		float max_X = min_X;
		for (int i = 0; i < _elements.length; i++) {
			for (int j = 0; j < _elements[i].getSpectrumDataX().length; j++) {
				float x = _elements[i].getSpectrumDataX()[j];
				if (x < min_X)
					min_X = x;
				if (x > max_X)
					max_X = x;
			}
		}
		MIN_X = min_X;
		MAX_X = max_X;
		center = (MAX_X + MIN_X) / 2;
		redraw();
	}

	/**
	 * Convert graph x coordinate into pixel value.
	 */
	private float pixelX(float x) {
		return _leftMargin + (SIZE_X - _leftMargin - _rightMargin) * (x - minX) / (maxX - minX);
	}

	/**
	 * Convert graph y coordinate into pixel value
	 */
	private float pixelY(float y) {
		return _topMargin + (SIZE_Y - _bottomMargin - _topMargin) * (1 - (y - minY) / (maxY - minY));
	}

	private float divX(int i) {
		return pixelX(minX + i * (maxX - minX) / DIVISION_X);
	}

	private float divY(int i) {
		return pixelY(minY + i * (maxY - minY) / DIVISION_Y);
	}

	/**
	 * Create trace circle and labels.
	 */
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
			// create the circle at the y coordinate on the plot which corresponds with the x of the curser
			g2.fill(new Ellipse2D.Float(pixelX(xdata.get(i).get(index)) - DOT_RADIUS, pixelY(ydata.get(i).get(index)) - DOT_RADIUS, 2 * DOT_RADIUS,
					2 * DOT_RADIUS));
			
			// create the label in the corner to display current curser location
			String xcoord = "X: " + String.format("%01.3f", xdata.get(i).get(index));
			g2.drawString(xcoord, SIZE_X - fontMetrics.stringWidth(xcoord), (2 * i + 1) * (strheight + GAP));
			String ycoord = "Y: " + String.format("%01.3f", ydata.get(i).get(index));
			g2.drawString(ycoord, SIZE_X - fontMetrics.stringWidth(ycoord), (2 * i + 2) * (strheight + GAP));
		}

	}

	public void redraw() {
		float minbound = center - (MAX_X - MIN_X) / zoom;
		float maxbound = center + (MAX_X - MIN_X) / zoom;

		xdata = new ArrayList<ArrayList<Float>>();
		ydata = new ArrayList<ArrayList<Float>>();
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
		g2.setFont(Main.FONT);
		FontMetrics fontMetrics = g2.getFontMetrics();
		int strheight = fontMetrics.getAscent() - fontMetrics.getDescent();
		int maxWidth = 0;
		for (int i = 0; i < DIVISION_Y; i++) {
			int width = fontMetrics.stringWidth(String.format("%01.3f", minY + ((maxY - minY) / DIVISION_Y * i)));
			if (width > maxWidth)
				maxWidth = width;
		}
		_leftMargin = strheight + maxWidth + 4 * GAP;
		_rightMargin = fontMetrics.stringWidth(String.format("%01.3f", maxX)) / 2 + GAP;
		_topMargin = strheight / 2 + GAP;
		_bottomMargin = 2 * strheight + 4 * GAP;

		// draw a line for each data point
		for (int i = 0; i < _elements.length; i++) {
			g2.setColor(_elements[i].getColor());
			for (int j = 0; j < xdata.get(i).size() - 1; j++) {
				g2.draw(new Line2D.Float( pixelX( xdata.get(i).get(j) ),
										  pixelY( ydata.get(i).get(j) ),
										  pixelX( xdata.get(i).get(j+1) ),
										  pixelY( ydata.get(i).get(j+1) ) ) );
			}
		}

		// draw the axes
		g2.setColor(Color.black);
		g2.draw( new Line2D.Float( pixelX(minX), pixelY(minY), pixelX(maxX), pixelY(minY) ) );
		g2.draw( new Line2D.Float( pixelX(minX), pixelY(minY), pixelX(minX), pixelY(maxY) ) );
		String xaxislabel = "Wavelength (\u00C5)";
		g2.drawString(xaxislabel, pixelX((maxX + minX) / 2) - fontMetrics.stringWidth(xaxislabel) / 2, pixelY(minY) + 3 * GAP + 2 * strheight);

		if (_ratio) {
			g2.setStroke( new BasicStroke(0, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 1f, new float[] { 5 }, 0) );
			g2.draw( new Line2D.Float(pixelX(minX), pixelY(1), pixelX(maxX), pixelY(1) ) );
			AffineTransform identity = g2.getTransform();
			g2.transform(AffineTransform.getRotateInstance(-Math.PI / 2, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2, pixelY((maxY + minY) / 2)));
			String yaxislabel = "Ratio Between Flux";
			float yaxislabelwidth = fontMetrics.stringWidth(yaxislabel);
			g2.drawString(yaxislabel, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2 - yaxislabelwidth / 2, pixelY((maxY + minY) / 2) + strheight / 2);
			g2.setTransform(identity);
		} else {
			// label access with units of flux (rotated and positioned at center height and margin-left)
			AffineTransform identity = g2.getTransform();
			g2.transform(AffineTransform.getRotateInstance(-Math.PI / 2, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2, pixelY((maxY + minY) / 2)));
			AttributedString yaxislabel = new AttributedString("Flux (10-17 erg/(cm*s2*\u00C5))");
			
			yaxislabel.addAttribute(TextAttribute.FONT, Main.FONT);
			yaxislabel.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 8, 11);
			yaxislabel.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUPER, 21, 22);
			
			AttributedCharacterIterator iterator = yaxislabel.getIterator();
			float yaxislabelwidth = (float) fontMetrics.getStringBounds(iterator, iterator.getBeginIndex(), iterator.getEndIndex(), g2).getWidth();
			g2.drawString(iterator, pixelX(minX) - 3 * GAP - maxWidth - strheight / 2 - yaxislabelwidth / 2, pixelY((maxY + minY) / 2) + strheight / 2);
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

	public void setSmoothed(boolean selected) {
		smoothdata = selected;
	}

	@Override
	public void componentHidden(ComponentEvent ce) {
	}

	@Override
	public void componentMoved(ComponentEvent ce) {
	}

	/**
	 * This function is used when everything is resized (such as when a window is maximized). 
	 */
	@Override
	public void componentResized(ComponentEvent ce) {
		SIZE_X = getWidth();
		SIZE_Y = getHeight();
		redraw();
	}

	@Override
	public void componentShown(ComponentEvent ce) {
	}

	/**
	 * This is the zoom in and out functionality.
	 */
	@Override
	public void mouseClicked(MouseEvent me) {
		center = (me.getX() - _leftMargin) / ((float) SIZE_X - _leftMargin - _rightMargin) * (maxX - minX) + minX;
		switch (me.getButton()) {
		case MouseEvent.BUTTON1:
			zoom *= 2;
			break;
		case MouseEvent.BUTTON3:
			if (zoom > 2)
				zoom /= 2;
			break;
		}
		center = Math.max((MAX_X - MIN_X) / zoom + MIN_X, Math.min(MAX_X - (MAX_X - MIN_X) / zoom, center));
		_parent.updateWindow(center,zoom);
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
		currenttrace = (me.getX() - _leftMargin) / ((float) SIZE_X - _leftMargin - _rightMargin) * (maxX - minX) + minX;
		_parent.updateTrace(currenttrace);
	}

	public void updateWindow(float newcenter, float newzoom) {
		center = newcenter;
		zoom = newzoom;
	}

	public void updateTrace(float newcurrenttrace) {
		currenttrace = newcurrenttrace;
	}
}

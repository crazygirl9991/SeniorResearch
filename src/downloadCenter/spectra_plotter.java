package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

@SuppressWarnings("serial")
public class spectra_plotter extends JComponent implements ActionListener {

	private static final int YSIZE = 400;
	private static final int XSIZE = 800;
	private static final float XDIV = 10;
	private static final float YDIV = 10;
	private float minx;
	private float maxx;
	private float miny;
	private float maxy;
	private BufferedImage bitmap;

	public static void main(String[] args) throws Exception {
		float[][] flux = readFitFile("python_code", "spSpec-53847-2235-179.fit");
		float[][] flux2 = readFitFile("python_code", "spSpec-53729-2236-303.fit");
		int n = Math.min(flux[0].length, flux2[0].length);
		float[][] ratios = new float[2][n];
		for (int i = 0; i < n; i++) {
			ratios[0][i] = flux[0][i];
			ratios[1][i] = flux[1][i] / flux2[1][i];
		}
		JFrame frame = new JFrame("Spectra Plotter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		spectra_plotter plot = new spectra_plotter(new float[][][] { flux, flux2 }, new Color[] { Color.red, Color.blue });
		panel.add(plot);
		spectra_plotter plot2 = new spectra_plotter(new float[][][] { ratios }, new Color[] { Color.green });
		panel.add(plot2);
		content.add(panel, BorderLayout.CENTER);
		JPanel bottom = new JPanel();
		JButton previous = new JButton("Previous");
		previous.addActionListener(plot);
		JButton next = new JButton("Next");
		next.addActionListener(plot);
		bottom.add(previous);
		bottom.add(next);
		content.add(bottom, BorderLayout.SOUTH);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static float[][] readFitFile(String workingDirectory, String file) throws FitsException, IOException {
		Fits fitFileImport = new Fits(new File(workingDirectory, file));

		BasicHDU spectra = fitFileImport.getHDU(0);

		double c0 = spectra.getHeader().getDoubleValue("COEFF0");
		double c1 = spectra.getHeader().getDoubleValue("COEFF1");

		float[] flux = ((float[][]) spectra.getData().getData())[0];

		float[] wavelength = new float[flux.length];

		for (int i = 0; i < wavelength.length; i++)
			wavelength[i] = (float) Math.pow(10, c0 + c1 * i);

		float[] extrema = new float[] { wavelength[0], wavelength[0], flux[0], flux[0] };

		for (int i = 0; i < wavelength.length; i++) {
			if (wavelength[i] < extrema[0])
				extrema[0] = wavelength[i];
			if (wavelength[i] > extrema[1])
				extrema[1] = wavelength[i];
			if (flux[i] < extrema[2])
				extrema[2] = flux[i];
			if (flux[i] > extrema[3])
				extrema[3] = flux[i];
		}
		return new float[][] { wavelength, flux, extrema };
	}

	public spectra_plotter(float[][][] data, Color[] colors) {
		setPreferredSize(new Dimension(XSIZE, YSIZE));
		minx = data[0][0][0];
		maxx = data[0][0][0];
		miny = data[0][1][0];
		maxy = data[0][1][0];
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i][0].length; j++) {
				float x = data[i][0][j];
				float y = data[i][1][j];
				if (x < minx)
					minx = x;
				if (x > maxx)
					maxx = x;
				if (y < miny)
					miny = y;
				if (y > maxy)
					maxy = y;
			}
		}

		bitmap = new BufferedImage(XSIZE, YSIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bitmap.getGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, XSIZE, YSIZE);
		for (int i = 0; i < data.length; i++) {
			g2.setColor(colors[i]);
			for (int j = 0; j < data[i][0].length - 1; j++) {
				g2.draw(new Line2D.Float(xpix(data[i][0][j]), ypix(data[i][1][j]), xpix(data[i][0][j + 1]), ypix(data[i][1][j + 1])));
			}
		}
		g2.setColor(Color.black);
		g2.drawLine(0, YSIZE - 1, XSIZE - 1, YSIZE - 1);
		g2.drawLine(0, 0, 0, YSIZE - 1);
		for (int i = 1; i < XDIV; i++) {
			g2.draw(new Line2D.Float(i * XSIZE / XDIV, YSIZE - 1, i * XSIZE / XDIV, YSIZE - 6));
			String xlbl = String.format("%01.3f", ((maxx - minx) / XDIV * i));
			float strwidth = (float) g2.getFontMetrics().getStringBounds(xlbl, g2).getWidth();
			g2.drawString(xlbl, i * XSIZE / XDIV - strwidth / 2, YSIZE - 11);
			g2.draw(new Line2D.Float(0, i * YSIZE / YDIV, 5, i * YSIZE / YDIV));
			String ylbl = String.format("%01.3f", ((maxy - miny) / YDIV * i));
			float strheight = (float) g2.getFontMetrics().getStringBounds(ylbl, g2).getHeight();
			g2.drawString(ylbl, 10, YSIZE - i * YSIZE / YDIV + strheight / 2);
		}
	}

	private float xpix(float x) {
		return XSIZE * (x - minx) / (maxx - minx);
	}

	private float ypix(float y) {
		return YSIZE * (1 - (y - miny) / (maxy - miny));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bitmap, 0, 0, null);
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		switch (ae.getActionCommand()) {
		case "Next":
			//TODO stuff...
			System.out.println("Next");
			break;
		case "Previous":
			//TODO stuff...
			System.out.println("Previous");
			break;
		}
	}
}

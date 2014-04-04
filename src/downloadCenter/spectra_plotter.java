package downloadCenter;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;

@SuppressWarnings("serial")
public class spectra_plotter extends JComponent {

	private static final int YSIZE = 800;
	private static final int XSIZE = 800;
	private float minx;
	private float maxx;
	private float miny;
	private float maxy;
	private float minx2;
	private float maxx2;
	private float miny2;
	private float maxy2;
	private float[] xdata1;
	private float[] ydata1;
	private float[] xdata2;
	private float[] ydata2;
	private float[] ratios;
	private BufferedImage bitmap;
	private float ratiosmin;
	private float ratiosmax;
	public static void main(String[] args) throws Exception {
		float[][] flux = readFitFile("python_code", "spSpec-53847-2235-179.fit");
		float[][] flux2 = readFitFile("python_code", "spSpec-53729-2236-303.fit");

		JFrame frame = new JFrame("Spectra Plotter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		spectra_plotter plot = new spectra_plotter(flux, flux2);
		content.add(plot);
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

	public spectra_plotter(float[][] flux, float[][] flux2) {
		setPreferredSize(new Dimension(XSIZE, YSIZE));
		minx = flux[2][0];
		maxx = flux[2][1];
		minx2 = flux2[2][0];
		maxx2 = flux2[2][1];
		miny = flux[2][2];
		maxy = flux[2][3];
		miny2 = flux2[2][2];
		maxy2 = flux2[2][3];
		xdata1 = flux[0];
		ydata1 = flux[1];
		xdata2 = flux2[0];
		ydata2 = flux2[1];

		int n = Math.min(ydata1.length, ydata2.length);
		ratios = new float[n];
		ratiosmin = ydata1[0]/ydata2[0];
		ratiosmax = ydata1[0]/ydata2[0];
		for (int i = 0; i < n; i++) {
			ratios[i] = ydata1[i] / ydata2[i];
			if(ratios[i] < ratiosmin)
				ratiosmin = ratios[i];
			if(ratios[i] > ratiosmax)
				ratiosmax = ratios[i];
		}

		bitmap = new BufferedImage(800, 800, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) bitmap.getGraphics();
		g2.setColor(Color.white);
		g2.fillRect(0, 0, XSIZE, YSIZE);
		g2.setColor(Color.black);
		int m = Math.max(ydata1.length, ydata2.length);
		for (int i = 0; i < m - 1; i++) {
			g2.setColor(Color.red);
			if (i + 1 < xdata1.length)
				g2.draw(new Line2D.Float(xpix(xdata1[i],minx,maxx), ypix(ydata1[i],miny,maxy), xpix(xdata1[i + 1],minx,maxx), ypix(ydata1[i + 1],miny,maxy)));
			g2.setColor(Color.blue);
			if (i + 1 < xdata2.length)
				g2.draw(new Line2D.Float(xpix(xdata2[i],minx2,maxx2), ypix(ydata2[i],miny2,maxy2), xpix(xdata2[i + 1],minx2,maxx2), ypix(ydata2[i + 1],miny2,maxy2)));
			g2.setColor(Color.green);
			if (i + 1 < ratios.length)
				g2.draw(new Line2D.Float(xpix(xdata1[i],minx,maxx), ypix(ratios[i],ratiosmin,ratiosmax), xpix(xdata1[i + 1],minx,maxx), ypix(ratios[i + 1],ratiosmin,ratiosmax)));
		}
	}

	private float xpix(float x, float min_x, float max_x) {
		return XSIZE * (x - min_x) / (max_x - min_x);
	}

	private float ypix(float y, float min_y, float max_y) {
		return YSIZE * (1 - (y - min_y) / (max_y - min_y));
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bitmap, 0, 0, null);
	}
}

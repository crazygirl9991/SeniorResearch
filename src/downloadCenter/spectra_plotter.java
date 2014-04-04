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
import nom.tam.fits.Header;

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
		spectra_data flux = readFitFile("python_code", "spSpec-53847-2235-179.fit");
		flux.color = Color.red;
		spectra_data flux2 = readFitFile("python_code", "spSpec-53729-2236-303.fit");
		flux2.color = Color.blue;
		int n = Math.min(flux.xdata.length, flux2.xdata.length);
		spectra_data ratios = new spectra_data();
		ratios.color = Color.green;
		ratios.xdata = new float[n];
		ratios.ydata = new float[n];
		for (int i = 0; i < n; i++) {
			ratios.xdata[i] = flux.xdata[i];
			ratios.ydata[i] = flux.ydata[i] / flux2.ydata[i];
		}
		JFrame frame = new JFrame("Spectra Plotter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		spectra_plotter plot = new spectra_plotter(new spectra_data[] { flux, flux2 });
		panel.add(plot);
		spectra_plotter plot2 = new spectra_plotter(new spectra_data[] { ratios });
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
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right,BoxLayout.Y_AXIS));
		right.add(flux.getPanel());
		right.add(flux2.getPanel());
		frame.add(right,BorderLayout.EAST);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static spectra_data readFitFile(String workingDirectory, String file) throws FitsException, IOException {
		Fits fitFileImport = new Fits(new File(workingDirectory, file));

		BasicHDU spectra = fitFileImport.getHDU(0);

		Header header = spectra.getHeader();

		double c0 = header.getDoubleValue("COEFF0");
		double c1 = header.getDoubleValue("COEFF1");

		spectra_data ret_data = new spectra_data();
		ret_data.ra = header.getDoubleValue("RAOBJ");
		ret_data.dec = header.getDoubleValue("DECOBJ");
		ret_data.mjd = header.getIntValue("MJD");
		ret_data.plateid = header.getIntValue("PLATEID");
		ret_data.fiberid = header.getIntValue("FIBERID");

		ret_data.ydata = ((float[][]) spectra.getData().getData())[0];

		ret_data.xdata = new float[ret_data.ydata.length];

		for (int i = 0; i < ret_data.xdata.length; i++)
			ret_data.xdata[i] = (float) Math.pow(10, c0 + c1 * i);

		return ret_data;
	}

	public spectra_plotter(spectra_data[] spectra_datas) {
		setPreferredSize(new Dimension(XSIZE, YSIZE));
		minx = spectra_datas[0].xdata[0];
		maxx = spectra_datas[0].xdata[0];
		miny = spectra_datas[0].ydata[0];
		maxy = spectra_datas[0].ydata[0];
		for (int i = 0; i < spectra_datas.length; i++) {
			for (int j = 0; j < spectra_datas[i].xdata.length; j++) {
				float x = spectra_datas[i].xdata[j];
				float y = spectra_datas[i].ydata[j];
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
		for (int i = 0; i < spectra_datas.length; i++) {
			g2.setColor(spectra_datas[i].color);
			for (int j = 0; j < spectra_datas[i].xdata.length - 1; j++) {
				g2.draw(new Line2D.Float(xpix(spectra_datas[i].xdata[j]), ypix(spectra_datas[i].ydata[j]), xpix(spectra_datas[i].xdata[j + 1]), ypix(spectra_datas[i].ydata[j + 1])));
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

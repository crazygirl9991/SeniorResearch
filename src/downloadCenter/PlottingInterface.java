package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * TODO
 * 
 * @author vtielebein, lmachen
 * 
 */
public class PlottingInterface implements ActionListener, ChangeListener {
	FitFileStore fileStore = new FitFileStore();
	private static SpectrumPlotter plot1;
	private static SpectrumPlotter plot2;

	/**
	 * TODO
	 * 
	 * @throws Exception
	 */
	public void display(String file1, String file2) throws Exception {
		// Read in the data from each fit file
		TableElement te1 = FitFileStore.ParseFitFile(file1);
		TableElement te2 = FitFileStore.ParseFitFile(file2);

		// Create and setup a JFrame
		JFrame frame = new JFrame("Plotting Interface");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = frame.getContentPane();

		// Create a panel to hold both of the plots
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		plot1 = new SpectrumPlotter(new TableElement[] { te1, te2 });
		panel.add(plot1);

		plot2 = new SpectrumPlotter(new TableElement[] { calculateRatio(te1, te2) });
		panel.add(plot2);
		content.add(panel, BorderLayout.CENTER);

		// Create the bottom panel with the next and previous buttons on it
		JPanel bottom = new JPanel();
		JButton previous = new JButton("Previous");
		previous.addActionListener(this);
		
		JCheckBox smoothed = new JCheckBox("Smoothed");
		smoothed.addChangeListener(this);
		
		JButton next = new JButton("Next");
		next.addActionListener(this);
		
		bottom.add(previous);
		bottom.add(smoothed);
		bottom.add(next);
		content.add(bottom, BorderLayout.SOUTH);

		// Create the right panel with the legend for each of the files
		JPanel right = new JPanel();
		right.setLayout( new BoxLayout(right, BoxLayout.Y_AXIS) );
		right.add(createLegend(te1));
		right.add(createLegend(te2));
		frame.add(right, BorderLayout.EAST);

		// Show the window
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	private TableElement calculateRatio(TableElement element1, TableElement element2) {
		TableElement ratio = new TableElement();

		int offset = Arrays.binarySearch(element2.getSpectrumDataX(),element1.getSpectrumDataX()[0]);
		
		// If the data are different lengths then use the minimum for computing
		// the ratios		
		int n = Math.min(element1.getSpectrumDataX().length, element2.getSpectrumDataX().length-offset);
		
		float[] ratioX = new float[n];
		float[] ratioY = new float[n];
		for (int i = 0; i < n; i++) {
			ratioX[i] = element1.getSpectrumDataX()[i];

			// Divide each y value by the other corresponding one
			ratioY[i] = element1.getSpectrumDataY()[i] / element2.getSpectrumDataY()[i+offset];
		}

		ratio.setSpectrumData(ratioX, ratioY);

		return ratio;
	}

	/**
	 * TODO
	 * 
	 * @param element
	 * @return
	 */
	public JPanel createLegend(TableElement element) {
		Color color = element.getColor();

		// Create the legend with a label and field for each attribute and color them
		JPanel panel = new JPanel(new GridLayout(5, 2));
		JLabel lblRA = new JLabel("RA:");
		lblRA.setForeground(color);
		panel.add(lblRA);

		// Create coordinate labels and retrieve these values
		JLabel txtRA = new JLabel(Double.toString(element.getCoords()[0]));
		txtRA.setForeground(color);
		panel.add(txtRA);

		JLabel lblDEC = new JLabel("DEC:");
		lblDEC.setForeground(color);
		panel.add(lblDEC);

		JLabel lbldec = new JLabel(Double.toString(element.getCoords()[1]));
		lbldec.setForeground(color);
		panel.add(lbldec);

		// Create plate information labels and retrieve these values
		JLabel lblMJD = new JLabel("MJD:");
		lblMJD.setForeground(color);
		panel.add(lblMJD);

		JLabel lblmjd = new JLabel(Integer.toString(element.getPlateInfo()[0]));
		lblmjd.setForeground(color);
		panel.add(lblmjd);

		JLabel lblPLATEID = new JLabel("PLATE:");
		lblPLATEID.setForeground(color);
		panel.add(lblPLATEID);

		JLabel lblplateid = new JLabel(Integer.toString(element.getPlateInfo()[1]));
		lblplateid.setForeground(color);
		panel.add(lblplateid);

		JLabel lblFIBERID = new JLabel("FIBER:");
		lblFIBERID.setForeground(color);
		panel.add(lblFIBERID);

		JLabel lblfiberid = new JLabel(Integer.toString(element.getPlateInfo()[2]));
		lblfiberid.setForeground(color);
		panel.add(lblfiberid);

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
		case "Next":
			// TODO stuff...
			System.out.println("Next");
			break;
		case "Previous":
			// TODO stuff...
			System.out.println("Previous");
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		JCheckBox smoothed = (JCheckBox) ce.getSource();
		SpectrumPlotter.setSmoothed(smoothed.isSelected());
		redrawBothPlots();
	}
	
	public static void redrawBothPlots() {
		plot1.redraw();
		plot2.redraw();
	}
	
	public static void repaintBothPlots() {
		// this is a built in function which calls upon paintComponent() from SpectrumPlotter,
		// which is overridden for this purpose
		plot1.repaint();
		plot2.repaint();
	}

}

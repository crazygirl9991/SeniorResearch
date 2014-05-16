package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * TODO
 * 
 * @author vtielebein, lmachen
 * 
 */
@SuppressWarnings("serial")
public class PlottingInterface extends JFrame implements ActionListener, ItemListener {
	FitFileStore _fileStore = new FitFileStore();
	private ArrayList<SpectrumPlotter> _plots = new ArrayList<SpectrumPlotter>();
	private ArrayList<TableElement> _data;
	private TableElement _current;
	Float _centerX = null;
	Float _centerY = null;
	Float _zoom = null;

	public PlottingInterface(ArrayList<TableElement> data, TableElement current) {
		super("Plotting Interface");
		_data = data;
		_current = current;
	}

	public PlottingInterface(ArrayList<TableElement> data, TableElement current, float centerX, float centerY, float zoom) {
		this(data, current);
		_centerX = centerX;
		_centerY = centerY;
		_zoom = zoom;
	}

	/**
	 * TODO
	 * 
	 * @throws Exception
	 */
	public void display(ArrayList<TableElement> files) throws Exception {
		// Read in the data from each fit file

		TableElement[] elements = new TableElement[files.size()];
		for (int i = 0; i < files.size(); i++) {
			elements[i] = FitFileStore.getSpectrum(files.get(i));
			elements[i].setColor(Color.getHSBColor((float) (i + 1) / (elements.length + 1), 1, (float) 0.7));
		}

		// Create and setup a JFrame
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container content = getContentPane();

		// Create a panel to hold both of the plots
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if (_centerX != null && _centerY != null && _zoom != null)
			_plots.add(new SpectrumPlotter(elements, this, false, _centerX, _centerY, _zoom));
		else
			_plots.add(new SpectrumPlotter(elements, this, false));
		panel.add(_plots.get(0));

		if (elements.length == 2) {
			TableElement ratio = calculateRatio(elements[0], elements[1]);
			ratio.setColor(Color.red);
			if (_centerX != null && _centerY != null && _zoom != null)
				_plots.add(new SpectrumPlotter(new TableElement[] { ratio }, this, true, _centerX, _centerY, _zoom));
			else
				_plots.add(new SpectrumPlotter(new TableElement[] { ratio }, this, true));
			panel.add(_plots.get(1));
		}
		content.add(panel, BorderLayout.CENTER);

		// Create the bottom panel with the next and previous buttons on it
		JPanel bottom = new JPanel();
		JButton previous = new JButton("Previous");
		previous.addActionListener(this);

		JCheckBox smoothed = new JCheckBox("Smoothed");
		smoothed.addItemListener(this);
		
		JCheckBox capped = new JCheckBox("Capped");
		capped.addItemListener(this);

		JButton next = new JButton("Next");
		next.addActionListener(this);

		bottom.add(previous);
		bottom.add(smoothed);
		bottom.add(capped);
		bottom.add(next);
		content.add(bottom, BorderLayout.SOUTH);

		// Create the right panel with the legend for each of the files
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		for (TableElement curr : elements)
			right.add(createLegend(curr));
		content.add(right, BorderLayout.EAST);

		// Show the window
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private TableElement calculateRatio(TableElement element1, TableElement element2) {
		TableElement ratio = new TableElement();

		TableElement second;
		TableElement first;
		if (element2.getSpectrumDataX()[0] <= element1.getSpectrumDataX()[0]) {
			second = element1;
			first = element2;
		} else {
			second = element2;
			first = element1;
		}

		int offset = Arrays.binarySearch(first.getSpectrumDataX(), second.getSpectrumDataX()[0]);

		// If the data are different lengths then use the minimum for computing the ratios		
		int n = Math.min(second.getSpectrumDataX().length, first.getSpectrumDataX().length - offset);

		float[] ratioX = new float[n];
		float[] ratioY = new float[n];
		for (int i = 0; i < n; i++) {
			ratioX[i] = second.getSpectrumDataX()[i];

			// Divide each y value by the other corresponding one
			ratioY[i] = second.getSpectrumDataY()[i] / first.getSpectrumDataY()[i + offset];
		}

		ratio.setSpectrumData(ratioX, ratioY);

		return ratio;
	}
	
	//TODO add this
	private void plotOptionsMenu(JFrame frame, TableElement element, ArrayList<TableElement> data) {
		String windowTitle = "Plot Options";

		try {
			ArrayList<String> plotTheseFiles = new ArrayList<String>();

			if (!element.hasMatch()) {
				plotTheseFiles.add( element.getFilename() );
			} else {
				ArrayList<Integer> plottableFiles = element.getMatches();
				plottableFiles.add( element.getUniqueID() );
			
				JPanel boxLayout = new JPanel(), panel0 = new JPanel(), panel1 = new JPanel();
				boxLayout.setLayout(new BoxLayout(boxLayout, BoxLayout.PAGE_AXIS));
				panel0.setLayout(new BoxLayout(panel0, BoxLayout.PAGE_AXIS));
				panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));

				boxLayout.add(new Label("Please select at least 1 spectrum to plot. "
						+ "The ratio between any 2 spectra will be calculated only if exactly 2 are selected.", Main.FONT));

				// Sets the label names
				panel0.add(new Label("SDSS I & II", Main.FONT_BOLD));
				panel1.add(new Label("SDSS III", Main.FONT_BOLD));
			
				// Sorts the checkboxes based on Data release and adds them to the proper panel TODO no. just no. not maintainable.
				for (int index : plottableFiles) {
					Boolean initialSelect = (index == element.getUniqueID());
					TableElement temp = data.get(index);

					if (temp.getPlateInfo()[0] < FitFileStore.DATA_RELEASE)
						panel0.add( new Label("   " + temp.toString().replace(TableManager.COLUMN_DELIMITER, "  |  "), Main.FONT), initialSelect );
					else
						panel1.add( new Label("   " + temp.toString().replace(TableManager.COLUMN_DELIMITER, "  |  "), Main.FONT), initialSelect );
				}

				boxLayout.add(panel0);
				boxLayout.add(panel1);

				Object[] buttonOptions = { "Plot", "Cancel" };
				int n = JOptionPane.showOptionDialog(frame, boxLayout, windowTitle, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, //do not use a custom Icon 
						buttonOptions, // use this array to title the buttons
						buttonOptions[1]); // and set the default button

				if (n == 0) {
					for (int i = 1; i < panel0.getComponentCount(); i++) {
						JCheckBox current = (JCheckBox) panel0.getComponent(i);
						if (current.isSelected())
							plotTheseFiles.add(current.getText());
					}

					for (int i = 1; i < panel1.getComponentCount(); i++) {
						JCheckBox current = (JCheckBox) panel1.getComponent(i);
						if (current.isSelected())
							plotTheseFiles.add(current.getText());
					}
				}
			}

			if (plotTheseFiles.size() > 0) {
				//display(plotTheseFiles); TODO how can I pass a TableElement from here? 
			} else
				ErrorLogger.DIALOGUE(frame, "Please select at least one spectrum to plot.");
		} catch (Exception e) {
			e.printStackTrace();
		}
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
		int endIndex;

		switch (event.getActionCommand()) {
		case "Next":
			endIndex = findNextMatch();

			if (endIndex < 0)
				ErrorLogger.DIALOGUE(this, "No more matches found!");
			else
				Main.Plot(_data.get(endIndex) );

			break;
		case "Previous":
			endIndex = findPreviousMatch();

			if (endIndex < 0)
				ErrorLogger.DIALOGUE(this, "No more matches found!");
			else
				Main.Plot(_data.get(endIndex) );

			break;
		}
	}

	private int findNextMatch() {
		int startIndex = _current.getUniqueID();
		
		for(int i = 1; i <= _data.size(); i++)
			if(_data.get((startIndex + i) % _data.size()).hasMatch())
				return (startIndex + i) % _data.size();
		return -1;
		
//		int endIndex = -1;
//		if (startIndex < _data.size()) {
//
//			// check second half of list for a match
//			for (int i = startIndex + 1; i < _data.size(); i++)
//				if (_data.get(i).hasMatch())
//					endIndex = i;
//
//			// if none was found, then search the first half for a match
//			if (endIndex < 0) {
//				for (int i = 0; i < startIndex; i++)
//					if (_data.get(i).hasMatch())
//						endIndex = i;
//			}
//		}
//		return endIndex;
	}

	private int findPreviousMatch() {
		int startIndex = _current.getUniqueID();
		
		for(int i = _data.size() - 1; i >= 0; i--)
			if(_data.get((startIndex + i) % _data.size()).hasMatch())
				return (startIndex + i) % _data.size();
		return -1;
		
//		int endIndex = -1;
//
//		if (startIndex < _data.size()) {
//			// search first half of list in reverse for a match
//			for (int i = startIndex - 1; i >= 0; i--)
//				if (_data.get(i).hasMatch())
//					endIndex = i;
//
//			// if none was found, then search the second half
//			if (endIndex < 0) {
//				for (int i = _data.size()-1; i > startIndex; i--)
//					if (_data.get(i).hasMatch())
//						endIndex = i;
//			}
//		}
//
//		return endIndex;
	}

	@Override
	public void itemStateChanged(ItemEvent ce) {
		JCheckBox source = (JCheckBox) ce.getSource();
		switch(source.getText())
		{
		case "Smoothed":			
			for (SpectrumPlotter curr : _plots)
				curr.setSmoothed(source.isSelected());
			break;
		case "Capped":
			for (SpectrumPlotter curr : _plots)
				curr.setCapped(source.isSelected());
			break;
		}
		autosizeAllPlots();
	}

	public void autosizeAllPlots() {
		for (SpectrumPlotter curr : _plots)
			curr.autosize();
	}

	public void redrawAllPlots() {
		for (SpectrumPlotter curr : _plots)
			curr.redraw();
	}

	public void updateWindow(float centerX, float centerY, float zoom) {
		_centerX = centerX;
		_centerY = centerY;
		_zoom = zoom;
		for (SpectrumPlotter curr : _plots)
			curr.updateWindow(centerX, centerY, zoom);
		autosizeAllPlots();
	}

	public void repaintAllPlots() {
		// this is a built in function which calls upon paintComponent() from SpectrumPlotter,
		// which is overridden for this purpose
		for (SpectrumPlotter curr : _plots)
			curr.repaint();
	}

	public void updateTrace(float currenttrace) {
		for (SpectrumPlotter curr : _plots)
			curr.updateTrace(currenttrace);
		repaintAllPlots();
	}

	public float getCenterX() {
		return _centerX;
	}

	public float getCenterY() {
		return _centerY;
	}

	public float getZoom() {
		return _zoom;
	}

}

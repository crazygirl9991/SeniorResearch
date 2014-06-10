package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Sets up the framework for displaying one or two plots. Two will be displayed
 * if only two spectra are selected, the second plot being the calculated ratio.
 * If there are any other number of spectra, all of them will plot by default,
 * but the linked plot options UI will allow for more specific selection.
 * 
 * @author vtielebein, lmachen
 * 
 */
@SuppressWarnings("serial")
public class PlotUI extends JFrame implements ActionListener {
	private ArrayList<SpectrumPlotter> _plots = new ArrayList<SpectrumPlotter>();
	public TableElementModel MODEL;
	private TableElement _current;
	public PlotOptionsUI _options = new PlotOptionsUI();
	private JPanel _panel;
	private JPanel right = new JPanel();

	/**
	 * Sets up the display and imports the model. This class keeps track of the
	 * model because it is used more frequently in this class.
	 */
	public PlotUI() {
		super("Plotting Interface");
		FileManager manager = new FileManager();
		try {
			MODEL = new TableElementModel(manager.importTable());
		} catch (Exception e) {
			try {
				manager.updateTable();
				MODEL = new TableElementModel(manager.importTable());
			} catch (Exception e1) {
				ErrorLogger.update("Could not update or import table.", e1);
				ErrorLogger.LOG();
				ErrorLogger
						.DIALOGUE(
								this,
								"Could not update or import table. Catastrophic system failure, see \"errors/mostRecentLog.txt\" for more information. Goodbye.");
				System.exit(1);
			}
		}
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container content = getContentPane();
		_panel = new JPanel();
		_panel.setLayout(new BoxLayout(_panel, BoxLayout.Y_AXIS));
		content.add(_panel, BorderLayout.CENTER);
		JButton previous = new JButton("Previous");
		previous.addActionListener(this);
		JButton plotOptions = new JButton("Plot Options");
		plotOptions.addActionListener(this);
		JButton next = new JButton("Next");
		next.addActionListener(this);
		JPanel bottom = new JPanel();
		bottom.add(previous);
		bottom.add(plotOptions);
		bottom.add(next);
		content.add(bottom, BorderLayout.SOUTH);
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		content.add(right, BorderLayout.EAST);
	}

	/**
	 * Main function for organizing display of spectra. This method calls
	 * TableElement functions to retrieve spectral data, assigns colors to
	 * each spectrum, and creates a SpectrumPlotter for
	 * each plot (if there is more than one, then the
	 * second is a ratio).
	 * 
	 * @throws Exception
	 */
	public void display(TableElement element, ArrayList<Boolean> selected) {
		// Retrieves the matches for selected spectrum and finds them in the
		// model.
		ArrayList<TableElement> elements = new ArrayList<TableElement>();
		_current = element;
		if(selected == null)
			_options.setCurrentElement(element);
		ArrayList<Integer> matchIndices = element.getMatches();
		if(selected == null || selected.get(0))
			elements.add(element);
		for (int i = 0; i < matchIndices.size(); i++) {
			if (selected == null || selected.get(i+1))
				elements.add(MODEL.get(matchIndices.get(i)));
		}

		for (int i = 0; i < elements.size(); i++) {
			// Initialize spectrum retrieves the necessary data to plot from
			// each fit file
			elements.get(i).initializeSpectrum();

			// The colors are assigned by dividing up the color wheel according
			// to the number of spectra, excluding red (for ratio).
			elements.get(i).setColor(
					Color.getHSBColor((float) ( i + 1 )
							/ ( elements.size() + 1 ), 1, (float) 0.7));
		}

		// Removes what is already there (if anything), before reassigning.
		_plots.clear();
		_panel.removeAll();

		_plots.add(new SpectrumPlotter(elements, this, false));
		_panel.add(_plots.get(0));

		// This second bit initializes the ratio plot only if there are two
		// matching spectra.
		if (elements.size() == 2) {
			TableElement ratio = calculateRatio(elements.get(0),
					elements.get(1));
			ratio.setColor(Color.red);
			ArrayList<TableElement> tmp = new ArrayList<TableElement>();
			tmp.add(ratio);

			_plots.add(new SpectrumPlotter(tmp, this, true));
			_panel.add(_plots.get(1));
		}

		// Reinitialize and populate the legend.
		right.removeAll();
		for (TableElement current : elements)
			right.add(createLegend(current));

		// Show the window.
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	/**
	 * Divides one spectrum by the other to create another
	 * plottable "spectrum" representing the ratio between the two.
	 * 
	 * @return
	 */
	private TableElement calculateRatio(TableElement element1,
			TableElement element2) {
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

		int offset = Arrays.binarySearch(first.getSpectrumDataX(),
				second.getSpectrumDataX()[0]);

		// If the data are different lengths then use the minimum for computing
		// the ratios
		int n = Math.min(second.getSpectrumDataX().length,
				first.getSpectrumDataX().length - offset);

		float[] ratioX = new float[n];
		float[] ratioY = new float[n];
		for (int i = 0; i < n; i++) {
			ratioX[i] = second.getSpectrumDataX()[i];

			// Divide each y value by the other corresponding one
			ratioY[i] = second.getSpectrumDataY()[i]
					/ first.getSpectrumDataY()[i + offset];
		}

		ratio.setSpectrumData(ratioX, ratioY);

		return ratio;
	}

	/**
	 * Creates the legend for plotted spectra.
	 * 
	 * @param element
	 * @return
	 */
	public JPanel createLegend(TableElement element) {
		Color color = element.getColor();

		// Create the legend with a label and field for each attribute and color
		// them
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

		JLabel lblplateid = new JLabel(
				Integer.toString(element.getPlateInfo()[1]));
		lblplateid.setForeground(color);
		panel.add(lblplateid);

		JLabel lblFIBERID = new JLabel("FIBER:");
		lblFIBERID.setForeground(color);
		panel.add(lblFIBERID);

		JLabel lblfiberid = new JLabel(
				Integer.toString(element.getPlateInfo()[2]));
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
			{
				_options.setCurrentElement(_current);
				display(MODEL.get(endIndex), null);
			}

			break;
		case "Plot Options":
			_options.plotOptionsUI(this);
			break;
		case "Previous":
			endIndex = findPreviousMatch();

			if (endIndex < 0)
				ErrorLogger.DIALOGUE(this, "No more matches found!");
			else
			{
				_options.setCurrentElement(_current);
				display(MODEL.get(endIndex), null);
			}

			break;
		}
	}

	private int findNextMatch() {
		int startIndex = _options.getCurrentElement().getUniqueID();

		for (int i = 1; i <= MODEL.size(); i++) {
			int index = ( startIndex + i ) % MODEL.size();

			if (MODEL.get(index).hasMatch()
					&& MODEL.get(index).getUniqueID() < Collections.min(MODEL
							.get(index).getMatches()))
				return index;
		}

		return -1;
	}

	private int findPreviousMatch() {
		int startIndex = _options.getCurrentElement().getUniqueID();

		for (int i = MODEL.size() - 1; i >= 0; i--)
			if (MODEL.get(( startIndex + i ) % MODEL.size()).hasMatch())
				return ( startIndex + i ) % MODEL.size();
		return -1;
	}

	public PlotOptionsUI getOptions() {
		return _options;
	}

	public void autosizeAllPlots() {
		for (SpectrumPlotter curr : _plots)
			curr.autosize();
	}
}

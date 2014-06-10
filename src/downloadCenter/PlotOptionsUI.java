package downloadCenter;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Keeps track of all of the plot options that get passed from
 * comparison to comparison when the next button is hit. Contains
 * the UI for setting these values as well. 
 * 
 * @author victoria
 *
 */
public class PlotOptionsUI {
	private TableElement _currentElement;
	private ArrayList<Boolean> _selected = new ArrayList<Boolean>();
	private int _smoothRadius;
	private float _zoom;
	private float _centerX;
	private boolean _capData;
	private float _currenttrace;

	public PlotOptionsUI() {
		setSmoothRadius(0);
		setZoom(2);
		setCenterX(0);
		setCapData(false);
		setCurrentTrace(0);			
	}
	
	public void plotOptionsUI(PlotUI plotUI) {
		String windowTitle = "Plot Options";

		try {
			ArrayList<Integer> plottableFiles = _currentElement.getMatches();
			plottableFiles.add(0,_currentElement.getUniqueID());
			
			JPanel boxLayout = new JPanel(), panel0 = new JPanel(), panel1 = new JPanel();
			boxLayout.setLayout(new BoxLayout(boxLayout,
					BoxLayout.PAGE_AXIS));
			panel0.setLayout(new BoxLayout(panel0, BoxLayout.PAGE_AXIS));
			panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));

			boxLayout.add(new Label(
							"Please select at least 1 spectrum to plot. "
									+ "The ratio between any 2 spectra will be calculated only if exactly 2 are selected.",
							MainAndDownloadUI.FONT));

			// Sets the label names
			panel0.add(new Label("SDSS I & II", MainAndDownloadUI.FONT_BOLD));
			panel1.add(new Label("SDSS III", MainAndDownloadUI.FONT_BOLD));

			// Sorts the checkboxes based on Data release and adds them to			
			ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
			for(int i = 0; i < plottableFiles.size(); i++)
			{
				TableElement temp = plotUI.MODEL.get(plottableFiles.get(i));
				
				String prettierDelimiter = "  |  ";
				String reformatted = temp.toString().replace(Configurations.TABLE_COLUMN_DELIMITER, prettierDelimiter);
				
				JCheckBox checkbox = new JCheckBox(reformatted, _selected.get(i));
				checkboxes.add(checkbox);
				if (temp.getRelease() == SDSS.one_two)
					panel0.add( checkbox);
				else
					panel1.add(checkbox);
			}
			boxLayout.add(panel0);
			boxLayout.add(panel1);
			
			boxLayout.add(new Label("Other options:", MainAndDownloadUI.FONT_BOLD));
			JCheckBox capped = new JCheckBox("Toggle Outliers", _capData);
			boxLayout.add(capped);
			
			JComboBox<Integer> smoothed = new JComboBox<Integer>(new Integer[]{0,1,2,3,4});
			smoothed.setSelectedIndex(_smoothRadius);
			smoothed.setEditable(false);
			boxLayout.add(smoothed);

			Object[] buttonOptions = { "Save", "Cancel" };
			int n = JOptionPane.showOptionDialog(plotUI, boxLayout,
					windowTitle, JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, // do not use a
														// custom Icon
					buttonOptions, // use this array to title the buttons
					buttonOptions[1]); // and set the default button
				if (n == 0) {
					ArrayList<Boolean> tmp = new ArrayList<Boolean>();
					
					for(JCheckBox curr : checkboxes)
						tmp.add(curr.isSelected());
					if(tmp.contains(true)) {
						_capData = capped.isSelected();
						_smoothRadius = smoothed.getSelectedIndex();
						_selected = tmp;
						plotUI.display(_currentElement,_selected);
					} else
						ErrorLogger.DIALOGUE(plotUI, "At least one spectrum must be selected.");
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getSmoothRadius() { return _smoothRadius; }
	public void setSmoothRadius(int smoothRadius) { _smoothRadius = smoothRadius; }

	public float getZoom() { return _zoom; }
	public void setZoom(float zoom) { _zoom = zoom; }

	public float getCenterX() { return _centerX; }
	public void setCenterX(float centerX) { _centerX = centerX; }

	public boolean isCapData() { return _capData; }
	public void setCapData(boolean capData) { _capData = capData; }

	public float getCurrentTrace() {
		return _currenttrace;
	}

	public void setCurrentTrace(float currenttrace) {
		_currenttrace = currenttrace;
	}
	public TableElement getCurrentElement() {
		return _currentElement;
	}

	public void setCurrentElement(TableElement currentElement) {
		_currentElement = currentElement;
		_selected.clear();
		for(int i = 0; i < _currentElement.getMatches().size()+1;i++)
			_selected.add(true);
	}
}

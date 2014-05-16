package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Main implements ActionListener, DocumentListener {

	public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	public static final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	private static JFrame _frame = new JFrame("Spectrum Analysis Tool");

	private static TableElementModel _model;
	private JTable _list0;
	static JCheckBox _filter = new JCheckBox("Only display spectra with matches", false);

	private JTextField _status;
	private JTextField _file;
	private static PlottingInterface plotUI;

	public void Main_Menu() {
		// Create and setup a JFrame
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = _frame.getContentPane();

		try {
			// Create the bottom panel with the next and previous buttons on it
			JPanel boxLayout = new JPanel();
			boxLayout.setLayout(new BoxLayout(boxLayout, BoxLayout.PAGE_AXIS));

			JPanel panel0 = new JPanel(), panel1 = new JPanel(), panel3 = new JPanel(), panel4 = new JPanel();
			Label label0 = new Label("Coordinates:\t", Main.FONT), label1 = new Label("Plate Information:\t", Main.FONT);

			// Create textfields for input
			panel0.add(label0);
			panel0.add(TextField.RA.getTextField());
			panel0.add(TextField.DEC.getTextField());

			panel1.add(label1);
			panel1.add(TextField.MJD.getTextField());
			panel1.add(TextField.PLATE.getTextField());
			panel1.add(TextField.FIBER.getTextField());

			_filter.addActionListener(this);
			_filter.setActionCommand("toggle");

			for (TextField curr : TextField.values())
				curr.getTextField().getDocument().addDocumentListener(this);

			//			try {
			_model = new TableElementModel( CommandExecutor.importFile( TableManager.TABLE_NAME, new TableElement() ) );
			//			} catch ( Exception e ) {
			//				TableManager.updateTable();
			//				_model = new TableElementModel( TableManager.importTable() );
			//			}
			_list0 = new JTable(_model);
			JScrollPane scroll = new JScrollPane(_list0);
			panel3.add(scroll);

			// Create buttons
			JButton button0 = new JButton("Download Files");
			button0.addActionListener(this);

			JButton button1 = new JButton("Review Spectra");
			button1.addActionListener(this);

			panel4.add(button0);
			panel4.add(button1);

			JPanel panel5 = new JPanel(new GridLayout(1, 2));
			_status = new JTextField(STATUS.IDLE.toString());
			_file = new JTextField();
			panel5.add(_status);
			panel5.add(_file);

			// Add everything to the main box layout
			boxLayout.add(panel0);
			boxLayout.add(panel1);
			boxLayout.add(_filter);
			boxLayout.add(panel3);
			boxLayout.add(panel4);
			boxLayout.add(panel5);

			content.add(boxLayout, BorderLayout.SOUTH);

			// Show the window
			_frame.pack();
			_frame.setLocationRelativeTo(null);
			_frame.setVisible(true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void Plot(TableElement current) {
		try {
			//TODO I want auto-multi select to be enabled? or if multiple are selected plot all? Check if match? what do?
			ArrayList<TableElement> plotTheseElements = new ArrayList<TableElement>();
			ArrayList<Integer> matchIndices = current.getMatches();
			plotTheseElements.add(current);
			
			for(int i : matchIndices)
				plotTheseElements.add( _model.getData().get(i) );
			
			if (plotUI != null) {
				float centerX = plotUI.getCenterX();
				float centerY = plotUI.getCenterY();
				float zoom = plotUI.getZoom();
				plotUI.setVisible(false);
				plotUI.dispose();
				plotUI = new PlottingInterface(_model.getData(), current, centerX, centerY, zoom);
			} else
				plotUI = new PlottingInterface(_model.getData(), current);
			
			plotUI.display(plotTheseElements);
		} catch(Exception e) {
			e.printStackTrace();//TODO wut?
		}
	}

	

	public String retrieveValidCoords(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";

		// If both coordinates are valid, update the table element
		if (TextField.RA.hasValidTextEntry() && TextField.RA.hasValidTextEntry()) {
			String coords = TextField.RA.getText() + "," + TextField.DEC.getText();
			element.setCoords(coords);

			invalidEntries = "none";
		} else {
			// Handle invalid entries for error alert
			if (!TextField.RA.hasValidTextEntry())
				invalidEntries += TextField.RA + invalidMessage + TextField.RA.getText() + "\n";
			if (!TextField.DEC.hasValidTextEntry())
				invalidEntries += TextField.DEC + invalidMessage + TextField.DEC.getText() + "\n";
		}

		return invalidEntries;
	}

	public String retrieveValidPlateInfo(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";

		// If everything is valid, then update the table element
		if (TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.hasValidTextEntry()) {
			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + "," + TextField.FIBER.getText();
			element.setPlateInfo(plateInfo);

			invalidEntries = "none";
		} else if (TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.getText().equals("")) {
			//TODO is this messy?

			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + ",0";
			element.setPlateInfo(plateInfo);

			invalidEntries = "none";
		} else {
			// Handle invalid entries for error alert
			if (!TextField.MJD.hasValidTextEntry())
				invalidEntries += TextField.MJD + invalidMessage + TextField.MJD.getText() + "\n";
			if (!TextField.PLATE.hasValidTextEntry())
				invalidEntries += TextField.PLATE + invalidMessage + TextField.PLATE.getText() + "\n";
			if (!TextField.FIBER.hasValidTextEntry())
				invalidEntries += TextField.FIBER + invalidMessage + TextField.FIBER.getText() + "\n";
		}

		return invalidEntries;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		TableElement element;

		switch (event.getActionCommand()) {
		case "Download Files":
			element = new TableElement();
			//TODO this is not functional! It won't update the table afterwards and just crashes the GUI
			// String invalidCoords = retrieveValidCoords(element); //TODO invalid not working
			String invalidPlateInfo = retrieveValidPlateInfo(element);
			int[] plateInfo = element.getPlateInfo();
			try {
				//TableManager.updateTable();
				if (invalidPlateInfo.equals("none")) {
					final BackgroundDownload worker = new BackgroundDownload(plateInfo);
					worker.addPropertyChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(PropertyChangeEvent e) {
							_status.setText(worker.getStatus().toString());
							_file.setText(worker.getFile());
							_frame.repaint();
							//							switch(e.getPropertyName())
							//							{
							//							case "status":
							//								System.out.println("Status change: "+e.getOldValue()+"->"+e.getNewValue());
							//								break;
							//							case "file":
							//								System.out.println("File change: "+e.getOldValue()+"->"+e.getNewValue());
							//								break;
							//							}
						}

					});
				} else
					ErrorLogger.DIALOGUE(_frame, invalidPlateInfo);
			} catch (Exception e) {
				e.printStackTrace();
				//ErrorLogger.DIALOGUE(_frame, invalidPlateInfo);
			}
			break;
		case "Review Spectra":
			int[] selected = _list0.getSelectedRows();
			element = _model.getRowFiltered(selected[0]);

			if (selected.length == 0) {
				ErrorLogger.DIALOGUE(_frame, "Please select a spectrum.");
			} else {			
				Plot(element);
			}
			break;
		case "toggle":
			update();
			break;
		}
	}

	@Override
	public void changedUpdate(DocumentEvent de) {
		update();
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		update();
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		update();
	}

	public static void setData(ArrayList<TableElement> table) {
		_model.setData(table);
		update();
	}

	public static void update() {
		_model.filter(TextField.RA.getText(), TextField.DEC.getText(), TextField.MJD.getText(), TextField.PLATE.getText(), TextField.FIBER.getText(),
				_filter.isSelected());
	}
}

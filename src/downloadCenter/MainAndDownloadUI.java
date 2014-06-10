package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Loads the main UI and contains the logic for initializing a download.
 * 
 * @author victoria
 *
 */
public class MainAndDownloadUI implements ActionListener, DocumentListener {

	public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	public static final Font FONT_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 14);

	private static JFrame _frame = new JFrame("Spectrum Analysis Tool");

	private JTable _list0;
	static JCheckBox _filter = new JCheckBox("Only display spectra with matches", false);

	private JTextField _status;
	private JTextField _file;
	private static PlotUI plotUI = new PlotUI();
	final FileManager _fileManager = new FileManager();
	
	private final String DOWNLOAD_BUTTON = "Download Inputted";
	private final String REVIEW_SPECTRA_BUTTON = "Review Spectra";
	private final String DOWNLOAD_FROM_FILE_BUTTON = "Browse for Download";

	public void Main_Menu() {
		// Create and setup a JFrame
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = _frame.getContentPane();
		
		_fileManager.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				_status.setText(_fileManager.getStatus());
				_file.setText(_fileManager.getFile());
				_frame.repaint();
			}

		});

		try {
			// Create the bottom panel with the next and previous buttons on it
			JPanel boxLayout = new JPanel();
			boxLayout.setLayout(new BoxLayout(boxLayout, BoxLayout.PAGE_AXIS));

			JPanel panel0 = new JPanel(), panel1 = new JPanel(), panel3 = new JPanel(), panel4 = new JPanel();
			Label label0 = new Label("Coordinates:\t", MainAndDownloadUI.FONT), label1 = new Label("Plate Information:\t", MainAndDownloadUI.FONT);

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

			for (TextField current : TextField.values())
				current.getTextField().getDocument().addDocumentListener(this);
			
			_list0 = new JTable(plotUI.MODEL);
			_list0.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			JScrollPane scroll = new JScrollPane(_list0);
			panel3.add(scroll);

			// Create buttons
			JButton button0 = new JButton(DOWNLOAD_BUTTON);
			button0.addActionListener(this);

			JButton button1 = new JButton(REVIEW_SPECTRA_BUTTON);
			button1.addActionListener(this);
			
			JButton button2 = new JButton(DOWNLOAD_FROM_FILE_BUTTON);
			button2.addActionListener(this);

			panel4.add(button0);
			panel4.add(button1);
			panel4.add(button2);

			JPanel panel5 = new JPanel(new GridLayout(1, 2));
			_status = new JTextField();
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

	/**
	 * Updates plateInfo to given element if the input is valid, and constructs
	 * a string of invalid input to display as an error message otherwise. TODO:
	 * Needs refactoring.
	 */
	public String retrieveValidPlateInfo(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";

		// If everything is valid, then update the table element
		if (TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.hasValidTextEntry()) {
			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + "," + TextField.FIBER.getText();
			element.setPlateInfo(plateInfo);

			invalidEntries = "none";
		} else if (TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.getText().equals("")) {
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
		ArrayList<int[]> plateInfo;
		
		switch (event.getActionCommand()) {
		case DOWNLOAD_BUTTON:
			element = new TableElement();
			
			String invalidPlateInfo = retrieveValidPlateInfo(element);
			plateInfo = new ArrayList<int[]>();
			plateInfo.add(element.getPlateInfo());
			
			try {
				if (invalidPlateInfo.equals("none")) {
					_fileManager.downloadInBackground(plateInfo);
				} else
					ErrorLogger.DIALOGUE(_frame, invalidPlateInfo);
			} catch (Exception e) {
				e.printStackTrace();
				//ErrorLogger.DIALOGUE(_frame, invalidPlateInfo);
			}
			break;
		case REVIEW_SPECTRA_BUTTON:
			int selected = _list0.getSelectedRow();
			if (selected == -1) {
				ErrorLogger.DIALOGUE(_frame, "Please select a spectrum.");
			} else {			
				plotUI.display(plotUI.MODEL.getRowFiltered(selected),null);
			}
			break;
		case DOWNLOAD_FROM_FILE_BUTTON:
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory( new File(".") );
			if(fc.showOpenDialog(_frame) == JFileChooser.APPROVE_OPTION) {
				File chosen = fc.getSelectedFile();
				
				try {
					Scanner scanner = new Scanner(new FileReader(chosen));
					plateInfo = new ArrayList<int[]>();
					
					while(scanner.hasNext()) {
						String nextLine = scanner.nextLine();
					
						String[] strArry = nextLine.split(Configurations.LIST_DELIMITER);
						int[] intArry = new int[3];
					
						intArry[0] = Integer.parseInt(strArry[0]);
						intArry[1] = Integer.parseInt(strArry[1]);
						if(strArry.length == 3)
							intArry[2] = Integer.parseInt(strArry[2]);
						else
							intArry[2] = 0;
						
						plateInfo.add(intArry);
					}	
					scanner.close();
					
					_fileManager.downloadInBackground(plateInfo);
				} catch (FileNotFoundException e) {
					ErrorLogger.update("File not formatted correctly.", e);
					ErrorLogger.DIALOGUE(_frame, "Could not locate or read file. File should \n" +
						"be comma delimited (ex. 54676,5143,23), with one set of plate information \n" +
						"(MJD,Plate,Fiber) per line. If the fiber is absent or 0, then the entire plate will\n" +
						"be downloaded.");
				}
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
		plotUI.MODEL.setData(table);
		update();
	}

	public static void update() {
		plotUI.MODEL.filter(TextField.RA.getText(), TextField.DEC.getText(), TextField.MJD.getText(), TextField.PLATE.getText(), TextField.FIBER.getText(),
				_filter.isSelected());
	}
}

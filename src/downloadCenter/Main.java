package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Main implements ActionListener, DocumentListener {
	
	public static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	
	JFrame _frame = new JFrame("Spectrum Analysis Tool");

	private TableElementModel model;

	private JTable list0;
	
	public void Main_Menu() {
		// Create and setup a JFrame
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container content = _frame.getContentPane();
		
		try {
			// Create the bottom panel with the next and previous buttons on it
			JPanel boxLayout = new JPanel();
			boxLayout.setLayout( new BoxLayout(boxLayout, BoxLayout.PAGE_AXIS) );
			
			JPanel panel0 = new JPanel(), panel1 = new JPanel(), panel3 = new JPanel(), panel4 = new JPanel();
			JLabel label0 = new JLabel("Coordinates:\t"),  label1 = new JLabel("Plate Information:\t") ;
			
			// Create textfields for input
			panel0.add(label0);
			panel0.add( TextField.RA.getTextField() );
			panel0.add( TextField.DEC.getTextField() );
			
			panel1.add(label1);
			panel1.add( TextField.MJD.getTextField() );
			panel1.add( TextField.PLATE.getTextField() );
			panel1.add( TextField.FIBER.getTextField() );
			
			for(TextField curr : TextField.values())
				curr.getTextField().getDocument().addDocumentListener(this);
			
			model = new TableElementModel(TableManager.importTable());
			list0 = new JTable( model );
			JScrollPane scroll = new JScrollPane(list0);
			panel3.add(scroll);
			
			// Create buttons
			JButton button0 = new JButton("Download Files");
			button0.addActionListener(this);
			
			JButton button1 = new JButton("Review Spectra");
			button1.addActionListener(this);
			
			panel4.add(button0);
			panel4.add(button1);
			
			// Add everything to the main box layout
			boxLayout.add(panel0);
			boxLayout.add(panel1);
			boxLayout.add(panel3);
			boxLayout.add(panel4);
			
			content.add(boxLayout, BorderLayout.SOUTH);
			
			// Show the window
			_frame.pack();
			_frame.setLocationRelativeTo(null);
			_frame.setVisible(true);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO make it so this needs to be acknowledged before main menu can be accessed again
	 * @param details
	 */
	public void Error_Menu(String details) {
		JOptionPane.showMessageDialog(_frame, "Requested operation is invalid. " + details);
	}
	
	public String retrieveValidCoords(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";
		
		// If both coordinates are valid, update the table element
		if( TextField.RA.hasValidTextEntry() && TextField.RA.hasValidTextEntry() ) {
			String coords = TextField.RA.getText() + "," + TextField.DEC.getText();
			element.setCoords(coords);
			
			invalidEntries = "none";
		} else {
			// Handle invalid entries for error alert
			if( !TextField.RA.hasValidTextEntry() )
				invalidEntries += TextField.RA + invalidMessage + TextField.RA.getText() + "\n";
			if( !TextField.DEC.hasValidTextEntry() )
				invalidEntries += TextField.DEC + invalidMessage + TextField.DEC.getText() + "\n";
		}
		
		return invalidEntries;
	}
	
	public String retrieveValidPlateInfo(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";
		
		// If everything is valid, then update the table element
		if( TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.hasValidTextEntry() ) {
			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + "," + TextField.FIBER.getText();
			element.setPlateInfo(plateInfo);
			
			invalidEntries = "none";
		} else if( TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.getText().equals("Fiber") ) {
			//TODO this is messy
			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + ",0";		
			element.setPlateInfo(plateInfo);
			
			invalidEntries = "none";
		} else {		
			// Handle invalid entries for error alert
			if( !TextField.MJD.hasValidTextEntry() )
				invalidEntries += TextField.MJD + invalidMessage + TextField.MJD.getText() + "\n";
			if( !TextField.PLATE.hasValidTextEntry() )
				invalidEntries += TextField.PLATE + invalidMessage + TextField.PLATE.getText() + "\n";
			if( !TextField.FIBER.hasValidTextEntry() )
				invalidEntries += TextField.FIBER + invalidMessage + TextField.FIBER.getText() + "\n";
		}
		
		return invalidEntries;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
//		TableElement element = new TableElement();
		
//		String invalidCoords = retrieveValidCoords(element); //TODO invalid not working
//		String invalidPlateInfo = retrieveValidPlateInfo(element);
		
		switch (event.getActionCommand()) {
//		case "Download Files":
//			if( invalidPlateInfo.equals("none") ) {	
//				try {
//					FitFileStore store = new FitFileStore( element.getPlateInfo() );
//					if( !store.Download() ) {
//						Error_Menu("Could not locate file for download. See log for details.");
//						
//						//TODO make a log
//					}
//					//store.UpdateTable();
//					
//					//Main_Menu();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} else
//				Error_Menu(invalidPlateInfo);
//			break;
		case "Review Spectra":
			if ( list0.getSelectedRows().length != 2 )
				JOptionPane.showMessageDialog( _frame, "Please select 2 spectra to plot them", "Error! Unable to plot", JOptionPane.ERROR_MESSAGE );
			else {
//			if( invalidCoords.equals("none") ) {
//				
//			} else if( invalidPlateInfo.equals("none") ) {
//				
//			}
				try {
					PlottingInterface plotUI = new PlottingInterface();
					plotUI.display( model.getRow(list0.getSelectedRows()[0]),model.getRow(list0.getSelectedRows()[1]));//"spSpec-53847-2235-179.fit", "spSpec-53729-2236-303.fit" );
				} catch ( Exception e ) {
					e.printStackTrace();
				}
			}
			break;
		}
	}

	@Override
	public void changedUpdate(DocumentEvent de) {
		model.filter(TextField.RA.getText(),TextField.DEC.getText(),TextField.MJD.getText(),TextField.PLATE.getText(),TextField.FIBER.getText());
	}

	@Override
	public void insertUpdate(DocumentEvent de) {
		model.filter(TextField.RA.getText(),TextField.DEC.getText(),TextField.MJD.getText(),TextField.PLATE.getText(),TextField.FIBER.getText());
	}

	@Override
	public void removeUpdate(DocumentEvent de) {
		model.filter(TextField.RA.getText(),TextField.DEC.getText(),TextField.MJD.getText(),TextField.PLATE.getText(),TextField.FIBER.getText());
	}
}

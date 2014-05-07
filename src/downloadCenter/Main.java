package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Main implements ActionListener, DocumentListener {

	public static final Font FONT = new Font( Font.SANS_SERIF, Font.PLAIN, 14 );
	public static final Font FONT_BOLD = new Font( Font.SANS_SERIF, Font.BOLD, 14 );

	private static JFrame _frame = new JFrame( "Spectrum Analysis Tool" );

	private TableElementModel _model;
	private JTable _list0;
	JCheckBox _filter = new JCheckBox( "Only display spectra with matches", false );

	public void Main_Menu() {
		// Create and setup a JFrame
		_frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		Container content = _frame.getContentPane();

		try {
			// Create the bottom panel with the next and previous buttons on it
			JPanel boxLayout = new JPanel();
			boxLayout.setLayout( new BoxLayout( boxLayout, BoxLayout.PAGE_AXIS ) );

			JPanel panel0 = new JPanel(), panel1 = new JPanel(), panel3 = new JPanel(), panel4 = new JPanel();
			Label label0 = new Label( "Coordinates:\t", Main.FONT ), label1 = new Label( "Plate Information:\t", Main.FONT );

			// Create textfields for input
			panel0.add( label0 );
			panel0.add( TextField.RA.getTextField() );
			panel0.add( TextField.DEC.getTextField() );

			panel1.add( label1 );
			panel1.add( TextField.MJD.getTextField() );
			panel1.add( TextField.PLATE.getTextField() );
			panel1.add( TextField.FIBER.getTextField() );

			_filter.addActionListener( this );
			_filter.setActionCommand( "toggle" );

			for ( TextField curr : TextField.values() )
				curr.getTextField().getDocument().addDocumentListener( this );

			try {
				_model = new TableElementModel( TableManager.importTable() );
			} catch ( Exception e ) {
				TableManager.updateTable();
				_model = new TableElementModel( TableManager.importTable() );
			}
			_list0 = new JTable( _model );
			JScrollPane scroll = new JScrollPane( _list0 );
			panel3.add( scroll );

			// Create buttons
			JButton button0 = new JButton( "Download Files" );
			button0.addActionListener( this );

			JButton button1 = new JButton( "Review Spectra" );
			button1.addActionListener( this );

			panel4.add( button0 );
			panel4.add( button1 );

			// Add everything to the main box layout
			boxLayout.add( panel0 );
			boxLayout.add( panel1 );
			boxLayout.add( _filter );
			boxLayout.add( panel3 );
			boxLayout.add( panel4 );

			content.add( boxLayout, BorderLayout.SOUTH );

			// Show the window
			_frame.pack();
			_frame.setLocationRelativeTo( null );
			_frame.setVisible( true );

		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public static void Plot_Options_Menu(JFrame frame, TableElement element, ArrayList<TableElement> data) {
		String windowTitle = "Plot Options";

		try {
			PlottingInterface plotUI = new PlottingInterface( data, element );
			ArrayList<String> plotTheseFiles = new ArrayList<String>();

			if ( !element.hasMatch() ) {
				plotTheseFiles.add( element.getFilename() );
			} else {
				ArrayList<Integer> matchIndexList = element.getMatches();
				matchIndexList.add( element.getUniqueID() );

				JPanel boxLayout = new JPanel(), panel0 = new JPanel(), panel1 = new JPanel();
				boxLayout.setLayout( new BoxLayout( boxLayout, BoxLayout.PAGE_AXIS ) );
				panel0.setLayout( new BoxLayout( panel0, BoxLayout.PAGE_AXIS ) );
				panel1.setLayout( new BoxLayout( panel1, BoxLayout.PAGE_AXIS ) );

				boxLayout.add( new Label( "Please select at least 1 spectrum to plot. "
						+ "The ratio between any 2 spectra will be calculated only if exactly 2 are selected.", Main.FONT ) );

				boxLayout.add( new Label( "Spectra: ", Main.FONT_BOLD ) );
				boxLayout.add( new Label( "   " + element.toString().replace( TableManager.COLUMN_DELIMITER, "  |  " ), Main.FONT ) );

				panel0.add( new Label( "SDSS I & II", Main.FONT_BOLD ) );
				panel1.add( new Label( "SDSS III", Main.FONT_BOLD ) );

				for ( int index : matchIndexList ) {
					Boolean initialSelect = ( index == element.getUniqueID() );
					TableElement temp = data.get( index );

					if ( temp.getPlateInfo()[0] < FitFileStore.DATA_RELEASE )
						panel0.add( new JCheckBox( temp.getFilename(), initialSelect ) );
					else
						panel1.add( new JCheckBox( temp.getFilename(), initialSelect ) );
				}

				boxLayout.add( panel0 );
				boxLayout.add( panel1 );

				Object[] buttonOptions = { "Plot", "Cancel" };
				int n = JOptionPane.showOptionDialog( frame, boxLayout, windowTitle, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, //do not use a custom Icon 
						buttonOptions, // use this array to title the buttons
						buttonOptions[1] ); // and set the default button

				if ( n == 0 ) {
					for ( int i = 1; i < panel0.getComponentCount(); i++ ) {
						JCheckBox current = (JCheckBox) panel0.getComponent( i );
						if ( current.isSelected() )
							plotTheseFiles.add( current.getText() );
					}

					for ( int i = 1; i < panel1.getComponentCount(); i++ ) {
						JCheckBox current = (JCheckBox) panel1.getComponent( i );
						if ( current.isSelected() )
							plotTheseFiles.add( current.getText() );
					}
				}
			}

			if ( plotTheseFiles.size() > 0 )
				plotUI.display( plotTheseFiles );
			else
				ErrorLogger.DIALOGUE( _frame, "Please select at least one spectrum to plot." );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	public String retrieveValidCoords(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";

		// If both coordinates are valid, update the table element
		if ( TextField.RA.hasValidTextEntry() && TextField.RA.hasValidTextEntry() ) {
			String coords = TextField.RA.getText() + "," + TextField.DEC.getText();
			element.setCoords( coords );

			invalidEntries = "none";
		} else {
			// Handle invalid entries for error alert
			if ( !TextField.RA.hasValidTextEntry() )
				invalidEntries += TextField.RA + invalidMessage + TextField.RA.getText() + "\n";
			if ( !TextField.DEC.hasValidTextEntry() )
				invalidEntries += TextField.DEC + invalidMessage + TextField.DEC.getText() + "\n";
		}

		return invalidEntries;
	}

	public String retrieveValidPlateInfo(TableElement element) {
		String invalidEntries = "\n\n";
		String invalidMessage = " contains invalid entry: ";

		// If everything is valid, then update the table element
		if ( TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.hasValidTextEntry() ) {
			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + "," + TextField.FIBER.getText();
			element.setPlateInfo( plateInfo );

			invalidEntries = "none";
		} else if ( TextField.MJD.hasValidTextEntry() && TextField.PLATE.hasValidTextEntry() && TextField.FIBER.getText().equals( "" ) ) {
			//TODO is this messy?

			String plateInfo = TextField.MJD.getText() + "," + TextField.PLATE.getText() + ",0";
			element.setPlateInfo( plateInfo );

			invalidEntries = "none";
		} else {
			// Handle invalid entries for error alert
			if ( !TextField.MJD.hasValidTextEntry() )
				invalidEntries += TextField.MJD + invalidMessage + TextField.MJD.getText() + "\n";
			if ( !TextField.PLATE.hasValidTextEntry() )
				invalidEntries += TextField.PLATE + invalidMessage + TextField.PLATE.getText() + "\n";
			if ( !TextField.FIBER.hasValidTextEntry() )
				invalidEntries += TextField.FIBER + invalidMessage + TextField.FIBER.getText() + "\n";
		}

		return invalidEntries;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		TableElement element;

		switch ( event.getActionCommand() ) {
		case "Download Files":
			element = new TableElement();
			//TODO this is not functional! It won't update the table afterwards and just crashes the GUI
			// String invalidCoords = retrieveValidCoords(element); //TODO invalid not working
			String invalidPlateInfo = retrieveValidPlateInfo( element );
			final int[] plateInfo = element.getPlateInfo();
			try {
				//TableManager.updateTable();
				if ( invalidPlateInfo.equals( "none" ) ) {
					new SwingWorker<Void, Integer>() {
						@Override
						protected Void doInBackground() throws Exception {
							FitFileStore store = new FitFileStore( plateInfo );
							store.Download();
							TableManager.updateTable();
							_model.setData( TableManager.importTable() );
							return null;
						}

						@Override
						protected void done() {
							update();
						}
					}.execute();
				} else
					ErrorLogger.DIALOGUE( _frame, invalidPlateInfo );
			} catch ( Exception e ) {
				e.printStackTrace();
				//ErrorLogger.DIALOGUE(_frame, invalidPlateInfo);
			}
			break;
		case "Review Spectra":
			int[] selected = _list0.getSelectedRows();
			element = _model.getRowFiltered( selected[0] );

			if ( selected.length == 0 ) {
				ErrorLogger.DIALOGUE( _frame, "Please select a spectrum." );
			} else {
				Plot_Options_Menu( _frame, element, _model.getData() );
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

	public void update() {
		_model.filter( TextField.RA.getText(), TextField.DEC.getText(), TextField.MJD.getText(), TextField.PLATE.getText(), TextField.FIBER.getText(),
				_filter.isSelected() );
	}
}

package downloadCenter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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
	private ArrayList<SpectrumPlotter> plots = new ArrayList<SpectrumPlotter>();

	/**
	 * TODO
	 * 
	 * @throws Exception
	 */
	public void display(ArrayList<String> files) throws Exception {
		// Read in the data from each fit file
		//		TableElement te1 = FitFileStore.ParseFitFile(file1);
		//		TableElement te2 = FitFileStore.ParseFitFile(file2);

		TableElement[] elements = new TableElement[files.size()];
		for ( int i = 0; i < files.size(); i++ ) {
			elements[i] = FitFileStore.ParseFitFile( files.get( i ) );
			elements[i].setColor( Color.getHSBColor( (float) ( i + 1 ) / ( elements.length + 1 ), 1, 1 ) );
		}

		// Create and setup a JFrame
		JFrame frame = new JFrame( "Plotting Interface" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		Container content = frame.getContentPane();

		// Create a panel to hold both of the plots
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );

		plots.add( new SpectrumPlotter( elements, this, false ) );
		panel.add( plots.get( 0 ) );

		if ( elements.length == 2 ) {
			TableElement ratio = calculateRatio( elements[0], elements[1] );
			ratio.setColor( Color.red );
			plots.add( new SpectrumPlotter( new TableElement[] { ratio }, this, true ) );
			panel.add( plots.get( 1 ) );
		}
		content.add( panel, BorderLayout.CENTER );

		// Create the bottom panel with the next and previous buttons on it
		JPanel bottom = new JPanel();
		JButton previous = new JButton( "Previous" );
		previous.addActionListener( this );

		JCheckBox smoothed = new JCheckBox( "Smoothed" );
		smoothed.addChangeListener( this );

		JButton next = new JButton( "Next" );
		next.addActionListener( this );

		bottom.add( previous );
		bottom.add( smoothed );
		bottom.add( next );
		content.add( bottom, BorderLayout.SOUTH );

		// Create the right panel with the legend for each of the files
		JPanel right = new JPanel();
		right.setLayout( new BoxLayout( right, BoxLayout.Y_AXIS ) );
		for ( TableElement curr : elements )
			right.add( createLegend( curr ) );
		frame.add( right, BorderLayout.EAST );

		// Show the window
		frame.pack();
		frame.setLocationRelativeTo( null );
		frame.setVisible( true );
	}

	private TableElement calculateRatio(TableElement element1, TableElement element2) {
		TableElement ratio = new TableElement();

		TableElement second;
		TableElement first;
		if ( element2.getSpectrumDataX()[0] <= element1.getSpectrumDataX()[0] ) {
			second = element1;
			first = element2;
		}
		else
		{
			second = element2;
			first = element1;
		}

		int offset = Arrays.binarySearch( first.getSpectrumDataX(), second.getSpectrumDataX()[0] );

		// If the data are different lengths then use the minimum for computing the ratios		
		int n = Math.min( second.getSpectrumDataX().length, first.getSpectrumDataX().length - offset );

		float[] ratioX = new float[n];
		float[] ratioY = new float[n];
		for ( int i = 0; i < n; i++ ) {
			ratioX[i] = second.getSpectrumDataX()[i];

			// Divide each y value by the other corresponding one
			ratioY[i] = second.getSpectrumDataY()[i] / first.getSpectrumDataY()[i + offset];
		}

		ratio.setSpectrumData( ratioX, ratioY );

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
		JPanel panel = new JPanel( new GridLayout( 5, 2 ) );
		JLabel lblRA = new JLabel( "RA:" );
		lblRA.setForeground( color );
		panel.add( lblRA );

		// Create coordinate labels and retrieve these values
		JLabel txtRA = new JLabel( Double.toString( element.getCoords()[0] ) );
		txtRA.setForeground( color );
		panel.add( txtRA );

		JLabel lblDEC = new JLabel( "DEC:" );
		lblDEC.setForeground( color );
		panel.add( lblDEC );

		JLabel lbldec = new JLabel( Double.toString( element.getCoords()[1] ) );
		lbldec.setForeground( color );
		panel.add( lbldec );

		// Create plate information labels and retrieve these values
		JLabel lblMJD = new JLabel( "MJD:" );
		lblMJD.setForeground( color );
		panel.add( lblMJD );

		JLabel lblmjd = new JLabel( Integer.toString( element.getPlateInfo()[0] ) );
		lblmjd.setForeground( color );
		panel.add( lblmjd );

		JLabel lblPLATEID = new JLabel( "PLATE:" );
		lblPLATEID.setForeground( color );
		panel.add( lblPLATEID );

		JLabel lblplateid = new JLabel( Integer.toString( element.getPlateInfo()[1] ) );
		lblplateid.setForeground( color );
		panel.add( lblplateid );

		JLabel lblFIBERID = new JLabel( "FIBER:" );
		lblFIBERID.setForeground( color );
		panel.add( lblFIBERID );

		JLabel lblfiberid = new JLabel( Integer.toString( element.getPlateInfo()[2] ) );
		lblfiberid.setForeground( color );
		panel.add( lblfiberid );

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		switch ( event.getActionCommand() ) {
		case "Next":
			// TODO stuff...
			System.out.println( "Next" );
			break;
		case "Previous":
			// TODO stuff...
			System.out.println( "Previous" );
			break;
		}
	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		JCheckBox smoothed = (JCheckBox) ce.getSource();
		for ( SpectrumPlotter curr : plots )
			curr.setSmoothed( smoothed.isSelected() );
		redrawAllPlots();
	}

	public void redrawAllPlots() {
		for ( SpectrumPlotter curr : plots )
			curr.redraw();
	}

	public void updateWindow(float center, float zoom) {
		for ( SpectrumPlotter curr : plots )
			curr.updateWindow( center, zoom );
		redrawAllPlots();
	}

	public void repaintAllPlots() {
		// this is a built in function which calls upon paintComponent() from SpectrumPlotter,
		// which is overridden for this purpose
		for ( SpectrumPlotter curr : plots )
			curr.repaint();
	}

	public void updateTrace(float currenttrace) {
		for ( SpectrumPlotter curr : plots )
			curr.updateTrace( currenttrace );
		repaintAllPlots();
	}

}

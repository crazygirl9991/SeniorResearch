package downloadCenter;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nom.tam.fits.Fits;
import nom.tam.fits.Header;

/**
 * Keeps track of all administrative information of a .FITS file, knowing how
 * to parse from and create Strings for outputting to a table file.
 * @author victoria
 *
 */
public class TableElement implements Comparable<TableElement> {
	private int _uniqueID; // not assigned until added to the table
	private String _filename;
	private double[] _coords = {0, 0}; // spherical { theta, phi }
	private int[] _plateInfo = {0, 0, 0};
	
	private static SDSS _release;
	
	// These are only used when plotting the spectrum
	private float[] _dataX;
	private float[] _dataY;
	private Color _color;
	
	/* SciencePrimary category will have a 0 if there 
	  * is another spectrum of the same object with better 
	  * quality (which would be marked with a SciencePrimary of 1). 
	  */ 
	//TODO decide about this: private Boolean _sciencePrimary;
	
	// this is a comma delimited list of uniqueIDs for different spectra describing the same object
	private ArrayList<Integer> _matches = new ArrayList<Integer>();

	public TableElement() {
		_filename = "unknown";
	}
	
	public TableElement(String filename) {
		_filename = filename;
	}
	
	/**
	 * Opens a fits file, retrieves the two necessary coefficients and
	 * flux data, calculates the necesary x-axis information, and
	 * stores all of this as member variables of the class (updating itself).
	 */
	public void initializeSpectrum() {
		try {
			Fits fitFileImport = new Fits( new File( WorkingDirectory.DOWNLOADS.toString(), getFilename() ) );
			Header header = fitFileImport.getHDU( 0 ).getHeader();
		
			// read these two coefficients from the header
			double c0 = header.getDoubleValue( "COEFF0" );
			double c1 = header.getDoubleValue( "COEFF1" );

			float[] dataX, dataY;

			// read in the flux data
			dataY = _release.getDataY(fitFileImport);
			
			// generate the wavelength data
			dataX = new float[dataY.length];
			for ( int i = 0; i < dataX.length; i++ )
				dataX[i] = (float) Math.pow( 10, ( c0 + c1 * i ) );

			setSpectrumData( dataX, dataY );
		
			fitFileImport.getStream().close();
		} catch (Exception e) {
			ErrorLogger.update( "Could not load file: " + getFilename(), e );
			//TODO error logger used here
		}
	}

	/**
	 * Opens a fits file, retrieves information regarding plate and position,
	 * and returns a TableElement initialized with these details. There are 
	 * different labels for RA and Dec between data releases, and these are
	 * retrieved from SDSS.java.
	 * information.
	 */
	public static TableElement ParseFitFile(File uneditedFileURL) throws IOException {
		// remove the URL to get just the filename //
		String filename = uneditedFileURL.getName();

		// then read in the fits file and extract the plate and coordinate information from the header //
		TableElement element = new TableElement(filename);

		try {
			Fits fitFileImport = new Fits( new File( WorkingDirectory.DOWNLOADS.toString(), filename ) );
			Header header = fitFileImport.getHDU( 0 ).getHeader();

			double[] coords = { 0, 0 };
			int[] plateInfo = { 0, 0, 0 };

			// These header labels need to be read in this order only  //
			// so that the data can be stored in the table accurately. //
			// coords={RAOBJ,DECOBJ}; plateInfo={MJD,PLATEID,FIBERID}  //
			plateInfo[0] = header.getIntValue("MJD");
			plateInfo[1] = header.getIntValue("PLATEID");
			plateInfo[2] = header.getIntValue("FIBERID");
			
			// Must set plate info before looking up coords so that _release is initialized //
			element.setPlateInfo(plateInfo);
			
			coords[0] = header.getDoubleValue(_release.RA_HEADER);
			coords[1] = header.getDoubleValue(_release.DEC_HEADER);
			element.setCoords(coords);

			fitFileImport.getStream().close();

		} catch ( Exception e ) {
			ErrorLogger.update( "Could not load file: " + filename, e );
			element = null;
		}

		return element;
	}
	
	public int getUniqueID() { return _uniqueID; }
	public void setUniqueID(int ID) { _uniqueID = ID; }
	
	public String getFilename() { return _filename; }
	public void setFilename(String newName) { _filename = newName; }
	
	public double[] getCoords() { return _coords; }

	/**
	 * Takes a string formatted as "RA,Dec".
	 * @param undelimited
	 * @throws UnsupportedOperationException
	 */
	public void setCoords(String undelimited) throws UnsupportedOperationException {
	String errorMessage = "Please submit coords in the following format: \"RA,Dec\". Given: " + undelimited;
		
		if( !undelimited.equals("") ) {
			String[] coords = undelimited.split(Configurations.LIST_DELIMITER);
		
			try {
				if(coords.length == 2) {
					double tmp0 = Double.parseDouble( coords[0].trim() );
					double tmp1 = Double.parseDouble( coords[1].trim() );
				
					setCoords(tmp0, tmp1);
				} else
					throw (new UnsupportedOperationException(errorMessage) );
			} catch (Exception e) {
				throw (new UnsupportedOperationException(errorMessage, e) );
			}
		}
	}
	
	public void setCoords(double[] coords) throws UnsupportedOperationException {
		String errorMessage = "There should be 2 elements. Given: " + dats(Configurations.LIST_DELIMITER, coords);
		
		if(coords.length == 2) {
			setCoords(coords[0], coords[1]);
		} else {
			throw (new UnsupportedOperationException(errorMessage) );
		}
	}
	
	public void setCoords(double ra, double dec) throws UnsupportedOperationException {
		_coords[0] = ra;
		_coords[1] = dec;
	}
	
	public int[] getPlateInfo() { return _plateInfo; }
	/**
	 * Takes an string formatted as "MJD,Plate,Fiber".
	 * @param undelimited
	 * @throws UnsupportedOperationException
	 */
	public void setPlateInfo(String undelimited) throws UnsupportedOperationException {		
		String errorMessage = "Please submit plate info in the following format: \"MJD,Plate,Fiber\". Given: " + undelimited;
		
		if( !undelimited.equals("") ) {
			try {
				String[] plateInfo = undelimited.split(Configurations.LIST_DELIMITER);
			
				if(plateInfo.length == 3) {
					int tmp0 = Integer.parseInt( plateInfo[0].trim() );
					int tmp1 = Integer.parseInt( plateInfo[1].trim() );
					int tmp2 = Integer.parseInt( plateInfo[2].trim() );
			
					setPlateInfo(tmp0, tmp1, tmp2);
				} else
					throw (new UnsupportedOperationException(errorMessage) );
			} catch(Exception e) {
				throw (new UnsupportedOperationException(errorMessage) );
			}
		}
	}
	
	public void setPlateInfo(int[] plateInfo) throws UnsupportedOperationException {
		String errorMessage = "There should be 3 elements. Given: " + iats(Configurations.LIST_DELIMITER, plateInfo);
			
			if( plateInfo.length == 3 ) {
				setPlateInfo(plateInfo[0], plateInfo[1], plateInfo[2]);
			} else
				throw (new UnsupportedOperationException(errorMessage) );
		}
	
	public void setPlateInfo(int mjd, int plate, int fiber) throws UnsupportedOperationException {		
		_plateInfo[0] = mjd;
		_plateInfo[1] = plate;
		_plateInfo[2] = fiber;
		
		_release = SDSS.getInstance(_plateInfo);
	}
	
	/**
	 * Returns a copy of the list as its own instance, and not a reference to the list.
	 */
	public ArrayList<Integer> getMatches() { return (new ArrayList<Integer>(_matches) ); }
	public void setMatches(String newMatches) throws UnsupportedOperationException { 
		if( newMatches.equals("") ) {
			_matches = null;
		} else if( newMatches.equals("none") ) {
			if( _matches == null ) // if it's null, reinitialize
				_matches = new ArrayList<Integer>();
		} else {
			try {
				if( _matches == null ) // if it's null, reinitialize
					_matches = new ArrayList<Integer>();
			
				String[] strArray = newMatches.split(Configurations.LIST_DELIMITER);
		
				for(int i = 0; i < strArray.length; i++)
					_matches.add( Integer.parseInt(strArray[i]) );
			} catch (Exception e) {
				throw (new UnsupportedOperationException("ERROR: newMatches should be comma delimited list of uniqueIDs, i.e. integer values. Was given: " + newMatches, e) );
			}
		}
	}
	
	public SDSS getRelease() { return _release; }
	
	public float[] getSpectrumDataX() { return _dataX; }
	public float[] getSpectrumDataY() { return _dataY; }
	
	public void setSpectrumData(float[] dataX, float[] dataY) {
		_dataX = dataX;
		_dataY = dataY;
	}
	
	public Color getColor() { return _color; }
	public void setColor(Color color) { _color = color; }
	
	/**
	 * Returns true if there is a match on record for given table element.
	 */
	public Boolean hasMatch() {
		if( _matches == null )
			return false;
		else if( _matches.size() == 0 )
			return false;
		else
			return true;
	}
	
	/**
	 * Converts an integer value (meant to be the matching element's
	 * uniqueID) and adds it to the String of matches store for this element.
	 */
	public void addMatch(TableElement match) {
		_matches.add( match.getUniqueID() );
	}
	
	/**
	 * Returns true if two table elements fall within 2.0 arcseconds of each other.
	 */
	public Boolean isMatch(TableElement that) {
		double r1 = this.getCoords()[0] * Math.cos(this.getCoords()[1] * Math.PI / 180);
		double r2 = that.getCoords()[0] * Math.cos(that.getCoords()[1] * Math.PI / 180);
		double d1 = this.getCoords()[1];
		double d2 = that.getCoords()[1];
		
		double angularDistance = Math.sqrt( (r1-r2)*(r1-r2) + (d1-d2)*(d1-d2) )*3600; // in arcsecs
		
		if( angularDistance <= Configurations.ANGULAR_DISTANCE_THRESHOLD)
			return true;
		else
			return false;
	}
	
	/**
	 * Splits input string (meant to be line from table) by delimiter
	 * and stores those values in returned TableElement.
	 */
	public static TableElement parse(String str) {
		TableElement temp = new TableElement();
		
		String[] delim = str.split(Configurations.TABLE_COLUMN_DELIMITER);
		
		temp.setUniqueID( Integer.parseInt(delim[0]) );
		temp.setFilename( delim[1] );
		temp.setCoords( delim[2] );
		temp.setPlateInfo( delim[3] );
		temp.setMatches( delim[4] );
		
		return temp;
	}
	
	/**
	 * Outputs table element as printed in the table (with _columnDelimiter and
	 * _listElementDelimiter defined above). 
	 * uniqueID|filename|RA,Dec|MJD,Plate,Fiber|Match1,Match2,etc
	 */
	public String toString() {
		String col = Configurations.TABLE_COLUMN_DELIMITER;
		String lis = Configurations.LIST_DELIMITER;
		
		double[] coords = { _coords[0], _coords[1] }; // no need to output the unit radius of 1
		
		String str = _uniqueID + col + _filename + col + dats(lis, coords) + col
				   + iats(lis, _plateInfo) + col;
		
		if( _matches == null )
			str += "none";
		else if( _matches.size() == 0 )
			str += "none";
		else {
			str += ilts(lis, _matches);
		}
		
		return str;
	}

	/**
	 * Tells the elements how to sort themselves by plate info.
	 */
	@Override
	public int compareTo(TableElement other) {
		for(int i = 0; i < _plateInfo.length; i++) {
			if(_plateInfo[i] > other._plateInfo[i])
				return 1;
			else if(_plateInfo[i] < other._plateInfo[i])
				return -1;
		}
		return 0;
	}
	
	/**
	 * Converts int array to string.
	 * @param array
	 * @return
	 */
	public static String iats(String delimiter, int[] array) {
		String str;
		
		if(array.length == 0)
			str = "";
		else if(array.length == 1)
			str = Integer.toString( array[0] );
		else {
			str = "";
			int i = 0;
			
			while( i < array.length-1 ) {
				str += Integer.toString( array[i] ) + delimiter;
				i++;
			}
			str += Integer.toString( array[i] );
		}
		return str;
	}

	/**
	 * Converts double array to string.
	 * @param array
	 * @return
	 */
	public static String dats(String delimiter, double[] array) {
		String str;
		
		if(array.length == 0)
			str = "";
		else if(array.length == 1)
			str = Double.toString( array[0] );
		else {
			str = "";
			int i = 0;
			
			while( i < array.length-1 ) {
				str += Double.toString( array[i] ) + delimiter;
				i++;
			}
			str += Double.toString( array[i] );
		}
		return str;
	}
	
	/**
	 * Returns list of integers converted to a string with given delimiter.
	 * @return
	 */
	public static String ilts(String delimiter, List<Integer> list) {
		String str;
		
		if(list.size() == 0)
			str = "";
		else if(list.size() == 1)
			str = Integer.toString( list.get(0) );
		else {
			str = "";
			int i = 0;
			
			while( i < list.size()-1 ) {
				str += Integer.toString( list.get(i) ) + delimiter;
				i++;
			}
			str += Integer.toString( list.get(i) );
		}
		return str;
	}
	
}




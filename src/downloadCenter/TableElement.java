package downloadCenter;

import java.awt.Color;
import java.util.ArrayList;

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
	
	// These are only used when plotting the spectrum
	private float[] _dataX;
	private float[] _dataY;
	private Color _color; // TODO auto-color based on the existence of matches??
	
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
			String[] coords = undelimited.split(TableManager.LIST_DELIMITER);
		
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
		String errorMessage = "There should be 2 elements. Given: " + Utility.toString(TableManager.LIST_DELIMITER, coords);
		
		if(coords.length == 2) {
			setCoords(coords[0], coords[1]);
		} else {
			throw (new UnsupportedOperationException(errorMessage) );
		}
	}
	
	public void setCoords(double ra, double dec) throws UnsupportedOperationException {
		//String errorMessage = "";
		//TODO  checks?
//		if(ra < 0)
//			errorMessage += "RA = " + ra + " is not valid. ";
//		if(dec < 0)
//			errorMessage += "Dec = " + dec + "is not valid. ";
//		
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
				String[] plateInfo = undelimited.split(TableManager.LIST_DELIMITER);
			
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
		String errorMessage = "There should be 3 elements. Given: " + Utility.toString(TableManager.LIST_DELIMITER, plateInfo);
			
			if( plateInfo.length == 3 )
				setPlateInfo(plateInfo[0], plateInfo[1], plateInfo[2]);
			else
				throw (new UnsupportedOperationException(errorMessage) );
		}
	
	public void setPlateInfo(int mjd, int plate, int fiber) throws UnsupportedOperationException {		
		//TODO should there be validity checks here?
		_plateInfo[0] = mjd;
		_plateInfo[1] = plate;
		_plateInfo[2] = fiber;
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
			
				String[] strArray = newMatches.split(TableManager.LIST_DELIMITER);
		
				for(int i = 0; i < strArray.length; i++)
					_matches.add( Integer.parseInt(strArray[i]) );
			} catch (Exception e) {
				throw (new UnsupportedOperationException("ERROR: newMatches should be comma delimited list of uniqueIDs, i.e. integer values. Was given: " + newMatches, e) );
			}
		}
	}
	
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
	 * Equation used is: cos(theta) = (a dot b) / mag(a)*mag(b), except that 
	 * mag(a) = mag(b) = 1 because the radius is arbitrary.
	 * 
	 * TODO refine this algorithm for matching accuracy
	 */
	public Boolean isMatch(TableElement that) {
		// Radius = 1 (unit circle), RA [[hours]], Dec [[degrees]] are spherical
		double[] tmp0 = {1.0, this.getCoords()[0], this.getCoords()[1]};
		double[] tmp1 = {1.0, that.getCoords()[0], that.getCoords()[1]};
		
		double r1 = tmp0[1] * Math.cos( Utility.degreesToRadians(tmp0[2]) );
		double r2 = tmp1[1] * Math.cos( Utility.degreesToRadians(tmp1[2]) );
		double d1 = tmp0[2];
		double d2 = tmp1[2];
		
		double angularDistance = Math.sqrt( (r1-r2)*(r1-r2) + (d1-d2)*(d1-d2) )*3600; // in arcsecs
		
		if( angularDistance <= TableManager.DISTANCE_THRESHOLD)
			return true;
		else
			return false;
		
//		try {
//			double angularDistance = Math.acos( Utility.dot( Utility.toCartesian(tmp0), Utility.toCartesian(tmp1) ) );
//		
//			if( angularDistance <= Utility.degreesToRadians(TableManager.DISTANCE_THRESHOLD) )
//				return true;
//			else
//				return false;
//		} catch (Exception e) {
//			throw ( new UnsupportedOperationException("ERROR: match calculation failed!", e) );
//		}
	}
	
	/**
	 * Splits input string (meant to be line from table) by delimiter
	 * and stores those values in returned TableElement.
	 */
	public static TableElement parse(String str) {
		TableElement temp = new TableElement();
		
		String[] delim = str.split(TableManager.COLUMN_DELIMITER);
		
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
		String col = TableManager.COLUMN_DELIMITER;
		String lis = TableManager.LIST_DELIMITER;
		
		double[] coords = { _coords[0], _coords[1] }; // no need to output the unit radius of 1
		
		String str = _uniqueID + col + _filename + col + Utility.toString(lis, coords) + col
				   + Utility.toString(lis, _plateInfo) + col;
		
		if( _matches == null )
			str += "none";
		else if( _matches.size() == 0 )
			str += "none";
		else {
			str += Utility.toString(lis, _matches);
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
	
}




package downloadCenter;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of all administrative information of a .FITS file, knowing how
 * to parse from and create Strings for outputting to a table file.
 * @author victoria
 *
 */
public class TableElement {
	private int _uniqueID; // not assigned until added to the table
	private String _filename;
	private double[] _coords = {1, 0, 0}; // spherical { rho, theta, phi }
	private int[] _plateInfo = {0, 0, 0};
	
	/* SciencePrimary category will have a 0 if there 
	  * is another spectrum of the same object with better 
	  * quality (which would be marked with a SciencePrimary of 1). 
	  */ 
	//TODO decide about this: private Boolean _sciencePrimary;
	
	// this is a comma delimited list of uniqueIDs for different spectra describing the same object
	private List<Integer> _matches = new ArrayList<Integer>();

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
	 * Takes an string formatted as "RA,Dec".
	 * @param undelimited
	 * @throws UnsupportedOperationException
	 */
	public void setCoords(String undelimited) throws UnsupportedOperationException {
	String errorMessage = "Please submit coords in the following format: \"RA,Dec\". Given: " + undelimited;
		
		if( !undelimited.equals("") ) {
			String[] coords = undelimited.split(TableManager.LIST_DELIMITER);
		
			try {
				if( coords.length == 2 ) {
					_coords[0] = 1.0;
					_coords[1] = Double.parseDouble( coords[0].trim() );
					_coords[2] = Double.parseDouble( coords[1].trim() );
				} else if( coords.length == 3 ) {
					_coords[0] = Double.parseDouble( coords[0].trim() );
					_coords[1] = Double.parseDouble( coords[1].trim() );
					_coords[2] = Double.parseDouble( coords[2].trim() );
				} else {
					throw (new UnsupportedOperationException(errorMessage) );
				} 
			} catch (Exception e) {
				throw (new UnsupportedOperationException(errorMessage, e) );
			}
		} else {
			throw (new UnsupportedOperationException(errorMessage) );
		}
	}
	
	public void setCoords(double[] coords) throws UnsupportedOperationException {
	String errorMessage = "Please submit coords in the following format: \"RA,Dec\". Given: " + Utility.toString(TableManager.LIST_DELIMITER, coords);
		
		try {
			if( coords.length == 2 ) {
				_coords[0] = 1.0;
				_coords[1] = coords[0];
				_coords[2] = coords[1];
			} else if( coords.length == 3 ) {
				_coords[0] = coords[0];
				_coords[1] = coords[1];
				_coords[2] = coords[2];
			} else {
				throw (new UnsupportedOperationException(errorMessage) );
			}
		} catch (Exception e) {
			throw (new UnsupportedOperationException(errorMessage, e) );
		}
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
			String[] plateInfo = undelimited.split(TableManager.LIST_DELIMITER);
			
			try {
				if( plateInfo.length != 3 )
					throw (new UnsupportedOperationException(errorMessage) );
				else {
					_plateInfo[0] = Integer.parseInt( plateInfo[0].trim() );
					_plateInfo[1] = Integer.parseInt( plateInfo[1].trim() );
					_plateInfo[2] = Integer.parseInt( plateInfo[2].trim() );
				}
			}  catch (Exception e) {
				throw (new UnsupportedOperationException(errorMessage, e) );
			}
		}
	}
	
	public void setPlateInfo(int[] plateInfo) throws UnsupportedOperationException {
		String errorMessage = "Please submit plate info in the following format: \"MJD,Plate,Fiber\". Given: " + Utility.toString(TableManager.LIST_DELIMITER, plateInfo);
			
			try {
				if( plateInfo.length != 3 )
					throw (new UnsupportedOperationException(errorMessage) );
				else {
					_plateInfo[0] = plateInfo[0];
					_plateInfo[1] = plateInfo[1];
					_plateInfo[2] = plateInfo[2];
				}
			} catch (Exception e) {
				throw (new UnsupportedOperationException(errorMessage, e) );
			}
		}
	
	public List<Integer> getMatches() { return _matches; }
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
	public void addMatch(int match) {
		_matches.add(match);
	}
	
	/**
	 * Returns true if two table elements fall within 2.0 arcseconds of each other.
	 * Equation used is: cos(theta) = (a dot b) / mag(a)*mag(b), except that 
	 * mag(a) = mag(b) = 1 because the radius is arbitrary.
	 * 
	 * TODO refine this algorithm for matching accuracy
	 */
	public Boolean isMatch(TableElement that) throws UnsupportedOperationException {
		try {
			// Radius = 1 (unit circle), RA [[hours]], Dec [[degrees]] are spherical
			double angularDistance = Math.acos( Utility.dot( Utility.toCartesian( this.getCoords() ), Utility.toCartesian( that.getCoords() ) ) );
		
			if( angularDistance <= Utility.degreesToRadians(TableManager.DISTANCE_THRESHOLD) )
				return true;
			else
				return false;
		} catch (Exception e) {
			throw ( new UnsupportedOperationException("ERROR: match calculation failed!", e) );
		}
	}
	
	/**
	 * Splits input string (meant to be line from table) by delimiter
	 * and stores those values in returned TableElement.
	 */
	public TableElement parse(String str) {
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
		
		double[] coords = { _coords[1], _coords[2] }; // no need to output the unit radius of 1
		
		String str = _uniqueID + col + _filename + col + Utility.toString(lis, coords) + col
				   + Utility.toString(lis, _plateInfo) + col;
		
		if( _matches == null )
			str += "none";
		else if( _matches.size() == 0 )
			str += "none";
		else {
			str += Utility.toString(lis, _matches);
		}
		
		//str += "\n";
		return str;
	}
	
}




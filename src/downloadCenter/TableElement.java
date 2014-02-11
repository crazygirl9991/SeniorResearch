package downloadCenter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TableElement {
	private static String _tableName = "QuasarSpectraTable.qst";
	private static String _fileHeader = "## uniqueID    filename   RA,Dec    MJD,Plate,Fiber    matches ##\n";
	private static String _columnDelimiter = "\t";
	private static String _listElementDelimiter = ",";
	//TODO requires really accurate measurements, i.e. accurate to more than 3 decimal places, in order to show differences.
	private static Double _distanceThreshold = 2.0 / 3600; // 2 arcsecs in degrees
	 
	@SuppressWarnings("unused")
	private static Double _fiberDistanceThreshold = 55.0; // arcsecs - physical limitation of drilling fibers on a plate
	
	private int _uniqueID; // not assigned until added to the table
	private String _filename;
	private double[] _coords = {1, 0, 0}; // spherical { rho, theta, phi }
	private double[] _plateInfo = {0, 0, 0};
	
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
	
	public TableElement(String filename, String tableName) {
		_filename = filename;
		_tableName = tableName;
	}
	
	/**
	 * TODO
	 * @param ce
	 * @return
	 * @throws IOException
	 */
	public String makeBackup(CommandExecutor ce) throws IOException {
		String backupFileName = renameForBackup(_tableName) ;
		
		try {
			ce.copy(_tableName, backupFileName);
		} catch (Exception e) {
			throw ( new IOException("ERROR: Could not backup table: " + _tableName, e) );
		}
		
		return backupFileName;
	}
	
	/**
	 * TODO
	 * @param filename
	 * @return
	 */
	public String renameForBackup(String filename) {
		String basefilename, path = "";
		
		if( _tableName.equals("") ) {
			basefilename = "default";
		} else {
			int indexOfExt = _tableName.lastIndexOf('.');
			basefilename = _tableName.substring(0, indexOfExt);
			
			if( basefilename.contains("/") ) {
				int indexOfSlash = basefilename.lastIndexOf('/');
				path = basefilename.substring(0, indexOfSlash+1);
				basefilename = basefilename.substring( indexOfSlash+1, basefilename.length() );
			}
		}
		
		String renamed = path + "cp-" + basefilename + ".backup";
		
		return renamed;
	}
	
	/**
	 * TODO
	 * @param ce
	 * @throws IOException
	 */
	public void restore(CommandExecutor ce, String filename) throws IOException {
		try {
			ce.copy(filename, _tableName);
		} catch (Exception e) {
			throw ( new IOException("ERROR: could not restore file " + filename + " to table " + _tableName, e) );
		}
	}
	
	/**
	 * Imports current table, locates matches, and assigns
	 * uniqueID to this table element. Then updates the table.
	 */
	public void SaveToTable(CommandExecutor ce) throws Exception {
		List<TableElement> table = new ArrayList<TableElement>();
		String backup = makeBackup(ce);
		
		try {
			Scanner scanner = new Scanner( new FileReader(_tableName) );
			
			//TODO abstract this for possible future improved match algorithm
			while( scanner.hasNextLine() ) {
				String nextLine = scanner.nextLine();
				if( !nextLine.startsWith("#") && !nextLine.equals("") ) {
					TableElement that = parse(nextLine);
			
					if( this.isMatch(that) )
						this.addMatch( that.getUniqueID() );
			
					table.add(that); // add current table element to list for pending updates
				}
			}
			
			scanner.close();
			
			//TODO move this down outside of try or something this is broken you're dumb
			
			//TODO also round off error.... FIX IT
			/* now that we have imported everything, we know what the uniqueID of the latest
			 * table element is going to be. Here we set that, and append this element to the table.
			 */
			this.setUniqueID( table.size() );
			table.add(this);
			
			// update already existing elements with the uniqueID of this within our list
			for(int i = 0; i < this.getMatches().size(); i++) { 
				// the indexes to modify are stored in the _matches list
				int index = this.getMatches().get(i);
				table.get(index).addMatch( this.getUniqueID() );
			}
			
		} catch(FileNotFoundException f) {
			ce.createFile(_tableName);
		} catch(Exception e) {
			restore(ce, backup);
			throw ( new IOException("ERROR: Could not read table data.", e) );
		}
		
		try {
			// and then push these changes back into the table
			BufferedWriter writer = new BufferedWriter( new FileWriter(_tableName) );
			writer.write(_fileHeader);
			for(int i = 0; i < table.size(); i++)
				writer.write( table.get(i).toString() );
			
			writer.close();
		} catch(Exception e) {
			restore(ce, backup);
			throw ( new IOException("ERROR: Could not write updated data to file " + _tableName, e) );
		}
	}
	
	public String getTableName() { return _tableName; }
	public void setTableName(String tableName) { _tableName = tableName; }
	
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
		UnsupportedOperationException exception = new UnsupportedOperationException("Please submit coords in the following format: \"RA,Dec\"");
		
		if( !undelimited.equals("") ) {
			String[] coords = undelimited.split(_listElementDelimiter);
		
			if( coords.length != 2 )
				throw exception;
			else {
				_coords[1] = Integer.parseInt( coords[0].trim() );
				_coords[2] = Integer.parseInt( coords[1].trim() );
			}
		} else {
			throw exception;
		}
	}
	
	public double[] getPlateInfo() { return _plateInfo; }
	/**
	 * Takes an string formatted as "MJD,Plate,Fiber".
	 * @param undelimited
	 * @throws UnsupportedOperationException
	 */
	public void setPlateInfo(String undelimited) throws UnsupportedOperationException {
		UnsupportedOperationException exception = new UnsupportedOperationException("Please submit plate info in the following format: \"MJD,Plate,Fiber\"");
		
		if( !undelimited.equals("") ) {
			String[] plateInfo = undelimited.split(_listElementDelimiter);

			if( plateInfo.length != 3 )
				throw exception;
			else {
				_plateInfo[0] = Integer.parseInt( plateInfo[0].trim() );
				_plateInfo[1] = Integer.parseInt( plateInfo[1].trim() );
				_plateInfo[2] = Integer.parseInt( plateInfo[2].trim() );
			}
		} 
	}
	
	public List<Integer> getMatches() { return _matches; }
	public void setMatches(String newMatches) throws UnsupportedOperationException { 
		if( newMatches.equals("") ) {
			_matches = null;
		} else if( newMatches.equals("none") ) {
			if( _matches == null ) // if it's null, reinitialize
				_matches = new ArrayList<Integer>();
			
	//		_matches.add(-1);
		} else {
			try {
				if( _matches == null ) // if it's null, reinitialize
					_matches = new ArrayList<Integer>();
			
				String[] strArray = newMatches.split(_listElementDelimiter);
		
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
		
			if( angularDistance <= Utility.degreesToRadians(_distanceThreshold) )
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
	private TableElement parse(String str) {
		TableElement temp = new TableElement();
		
		String[] delim = str.split(_columnDelimiter);
		
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
		String col = _columnDelimiter;
		String lis = _listElementDelimiter;
		
		String str = _uniqueID + col + _filename + col + Utility.toString(lis, _coords) + col
				   + Utility.toString(lis, _plateInfo) + col;
		
		if( _matches == null )
			str += "none";
		else if( _matches.size() == 0 )
			str += "none";
		else {
			str += Utility.toString(lis, _matches);
		}
		
		str += "\n";
		return str;
	}
	
}




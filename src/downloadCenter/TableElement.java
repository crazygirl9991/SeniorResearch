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
	private static String _columnDelimiter = "|";
	private static String _listElementDelimiter = ",";
	//TODO comment and add variable for physical distance versus fiber distance
	private static Double _distanceThreshold = 55.0; // arc seconds - 55 is the distance of physical fibers
	
	private int _uniqueID; // not assigned until added to the table
	private String _filename;
	private String[] _coords;
	private String[] _plateInfo;
	
	/* SciencePrimary category will have a 0 if there 
	  * is another spectrum of the same object with better 
	  * quality (which would be marked with a SciencePrimary of 1). 
	  */ 
	//TODO decide about this: private Boolean _sciencePrimary;
	
	// this is a comma delimited list of uniqueIDs for different spectra describing the same object
	private List<Integer> _matches;

	public TableElement() {
		_filename = "unknown";
		
	}
	
	public TableElement(String filename) {
		_filename = filename;
	}
	
	/**
	 * Imports current table, locates matches, and assigns
	 * uniqueID to this table element. Then updates the table.
	 */
	public void SaveToTable() throws Exception {
		List<TableElement> table = new ArrayList<TableElement>();
		try {
			Scanner scanner = new Scanner( new FileReader(_tableName) );
		
			while( scanner.hasNext() ) {
				TableElement that = parse( scanner.nextLine() );
			
				if( this.isMatch(that) )
					this.addMatch( that.getUniqueID() );
			
				table.add(that); // add current table element to list for pending updates
				scanner.close();
			}
		} catch(FileNotFoundException f) {
			Runtime rt = Runtime.getRuntime();
			try {//TODO write better comment
				String command = "echo '## Table has not yet been written. ##' > " + _tableName;
				rt.exec(command);
			} catch (Exception e) {
				throw ( new IOException("ERROR: Can't create table: " + _tableName, e) );
			}
		} catch(Exception io) {
			throw ( new IOException("ERROR: Could not read table data.", io) );
		}
		
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
		
		try {
			// and then push these changes back into the table
			BufferedWriter writer = new BufferedWriter( new FileWriter(_tableName) );
			for(int i = 0; i < table.size(); i++)
				writer.write( table.get(i).toString() );
			
			writer.close();
			
		} catch(Exception e) {
			throw ( new IOException("ERROR: Could not write updated data to file " + _tableName, e) );
		}
	}
	
	public int getUniqueID() { return _uniqueID; }
	public void setUniqueID(int ID) { _uniqueID = ID; }
	
	public String getFilename() { return _filename; }
	public void setFilename(String newName) { _filename = newName; }
	
	public String[] getCoords() { return _coords; }
	public void setCoords(String undelimited) {
		_coords = undelimited.split(_listElementDelimiter);
	}
	
	public String[] getPlateInfo() { return _plateInfo; }
	public void setPlateInfo(String undelimited) {
		_plateInfo = undelimited.split(_listElementDelimiter);
	}
	
	public List<Integer> getMatches() { return _matches; }
	public void setMatches(String newMatches) { 
		String[] strArray = newMatches.split(_listElementDelimiter);
		
		for(int i = 0; i < strArray.length; i++)
			_matches.add( Integer.parseInt(strArray[i]) );
	}
	
	/**
	 * Returns true if there is a match on record for given table element.
	 */
	public Boolean hasMatch() {
		if( _matches.equals("") )
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
	 * Returns true if two table elements fall within 55 arcseconds of each other.
	 * Equation used is: cos(theta) = (a dot b) / mag(a)*mag(b), except that 
	 * mag(a) = mag(b) = 1 because the radius is arbitrary.
	 * 
	 * TODO refine this algorithm for matching accuracy
	 */
	public Boolean isMatch(TableElement that) throws UnsupportedOperationException {
		try {
			// RA [[hours]], Dec [[degrees]] are spherical
			double angularDistance = Math.acos( dot( toCartesian( this.getCoords() ), toCartesian( that.getCoords() ) ) );
		
			if(angularDistance >= _distanceThreshold)
				return true;
			else
				return false;
		} catch(Exception e) {
			throw ( new UnsupportedOperationException("ERROR: match calculation failed!", e) );
		}
	}
	
	/**
	 * Returns <x,y,z> = < sin(phi)*cos(theta), sin(phi)*sin(theta), cos(phi) >.
	 */
	private double[] toCartesian(String[] spherical) {
		double[] vector = new double[3];
		
		vector[0] = Math.sin( Double.parseDouble(spherical[1]) ) * Math.cos( Double.parseDouble(spherical[0]) );
		vector[1] = Math.sin( Double.parseDouble(spherical[1]) ) * Math.sin( Double.parseDouble(spherical[0]) );
		vector[2] = Math.cos( Double.parseDouble(spherical[1]) );
		
		return vector;
	}
	
	/**
	 * Vectors must be three dimensional.
	 */
	private double dot(double[] v1, double[] v2) {
		return (v1[0]*v2[0]) + (v1[1]*v2[1]) + (v1[2]*v2[2]);
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
	 * uniqueID|filename|RA,Dec|MJD,Plate,Dec|Match1,Match2,etc
	 */
	public String toString() {
		String col = _columnDelimiter;
		String lis = _listElementDelimiter;
		
		String str = _uniqueID + col + _filename + col + _coords[0] + lis + _coords[1] + col
				   + _plateInfo[0] + lis + _plateInfo[1] + lis + _plateInfo[2] + col;
		
		int i;
		// loop to size - 1 so that the last element isn't followed by a comma
		for(i = 0; i < _matches.size()-1; i++)
			str += _matches.get(i) + lis;
		str += _matches.get( i+1 ); // add last element with no comma
		
		return str;
	}
	
}




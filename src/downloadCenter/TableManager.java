package downloadCenter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Stores table administrative information (e.g. name, table format, etc.) and contains all functionality required to 
 * update the table, including provisions for creating and restoring table back-ups and
 * comparing/storing table elements which match.
 * @author victoria
 *
 */
public class TableManager {
	public static String TABLE_NAME = "QuasarSpectraTable.qst";
	public static String FILE_HEADER = "## uniqueID\tfilename\tRA,Dec\tMJD,Plate,Fiber\tmatches ##\n";
	public static String COLUMN_DELIMITER = "\t";
	public static String LIST_DELIMITER = ",";

	public static Double DISTANCE_THRESHOLD = 2.0 / 3600; // 2 arcsecs in degrees
	
	@SuppressWarnings("unused")
	private static Double FIBER_DISTANCE_THRESHOLD = 55.0; // arcsecs - physical limitation of drilling fibers on a plate

	/**
	 * TODO
	 * @param ce
	 * @return
	 * @throws IOException
	 */
	private static String makeBackup() throws IOException {
		String backupFileName = renameForBackup(TABLE_NAME) ;
		
		try {
			CommandExecutor.copy(TABLE_NAME, backupFileName);
		} catch (Exception e) {
			throw ( new IOException("ERROR: Could not backup table: " + TABLE_NAME, e) );
		}
		
		return backupFileName;
	}
	
	/**
	 * TODO
	 * @param filename
	 * @return
	 */
	private static String renameForBackup(String filename) {
		String basefilename, path = "";
		
		if( TABLE_NAME.equals("") ) {
			basefilename = "default";
		} else {
			int indexOfExt = TABLE_NAME.lastIndexOf('.');
			basefilename = TABLE_NAME.substring(0, indexOfExt);
			
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
	private static void restore(String filename) throws IOException {
		try {
			CommandExecutor.copy(filename, TABLE_NAME);
			CommandExecutor.remove(filename);
		} catch (Exception e) {
			throw ( new IOException("ERROR: could not restore file " + filename + " to table " + TABLE_NAME, e) );
		}
	}
	
	/**
	 * Imports current table, locates matches, and assigns
	 * uniqueID to this table element. Then updates the table.
	 */
	public static void SaveToTable(TableElement element) throws Exception {
		List<TableElement> table = new ArrayList<TableElement>();
		String backup = makeBackup();
		
		try {
			Scanner scanner = new Scanner( new FileReader(TABLE_NAME) );
			
			//TODO implement improved match algorithm
			while( scanner.hasNextLine() ) {
				String nextLine = scanner.nextLine();
				if( !nextLine.startsWith("#") && !nextLine.equals("") ) {
					TableElement that = element.parse(nextLine);
			//TODO TODO prevent duplicate files from being written to the table!!
					if( element.isMatch(that) )
						element.addMatch( that.getUniqueID() );
			
					table.add(that); // add current table element to list for pending updates
				}
			}
			
			scanner.close();
			
			//TODO move this down outside of try or something this is broken you're dumb
			
			/* now that we have imported everything, we know what the uniqueID of the latest
			 * table element is going to be. Here we set that, and append this element to the table.
			 */
			element.setUniqueID( table.size() );
			table.add(element);
			
			// update already existing elements with the uniqueID of this within our list
			for(int i = 0; i < element.getMatches().size(); i++) { 
				// the indexes to modify are stored in the _matches list
				int index = element.getMatches().get(i);
				table.get(index).addMatch( element.getUniqueID() );
			}
			
		} catch(FileNotFoundException f) {
			CommandExecutor.createFile(TABLE_NAME);
		} catch(Exception e) {
			restore(backup);
			throw ( new IOException("ERROR: Could not read table data.", e) );
		}
		
		try {//TODO extract this into ce?
			// and then push these changes back into the table
			BufferedWriter writer = new BufferedWriter( new FileWriter(TABLE_NAME) );
			writer.write(FILE_HEADER);
			for(int i = 0; i < table.size(); i++)
				writer.write( table.get(i).toString() );
			
			writer.close();
		} catch(Exception e) {
			restore(backup);
			throw ( new IOException("ERROR: Could not write updated data to file " + TABLE_NAME, e) );
		}
	}
	
	
	
}

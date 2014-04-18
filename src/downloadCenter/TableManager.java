package downloadCenter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	 * TODO
	 * @return
	 * @throws Exception
	 */
	private static ArrayList<TableElement> importTable() throws Exception {
		ArrayList<TableElement> table = new ArrayList<TableElement>();
		Scanner scanner;
	
		try {
			scanner = new Scanner( new FileReader(TABLE_NAME) );
		
			while( scanner.hasNextLine() ) {
				String nextLine = scanner.nextLine();
				if( !nextLine.startsWith("#") && !nextLine.equals("") ) {
					TableElement that = TableElement.parse(nextLine);
					table.add(that);
				}
			}
			
			scanner.close();
		} catch(FileNotFoundException f) {
			throw f;
		}
		
		return table;
	}
	

	/**
	 * TODO
	 * 
	 * @param filename
	 * @param data
	 */
	private static void writeTable(ArrayList<TableElement> table) throws Exception {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(TABLE_NAME));

			writer.write(FILE_HEADER);
			for (TableElement current : table)
				writer.write( current.toString() );

			writer.close();
		} catch (Exception e) {
			throw (new UnsupportedOperationException("ERROR: Could not write updated data to table: " + TABLE_NAME, e));
		}
	}
	
	/**
	 * TODO
	 * @return
	 */
	public static String[] getDisplay() {
		String[] tableArray;
		try {
			ArrayList<TableElement> table = importTable();
			tableArray = new String[table.size()];
			for(int i = 0; i < table.size(); i++)
				tableArray[i] = table.get(i).toString();
		
		} catch (FileNotFoundException f) {
			tableArray = new String[1];
			
			tableArray[0] = "File not found: " + TABLE_NAME + " does not exist. Have any files been downloaded yet?";
		} catch (Exception e) {
			e.printStackTrace();
			
			tableArray = new String[1];
		}
		
		return tableArray;
	}
	
	/**
	 * Imports current table, locates matches, and assigns
	 * uniqueID to this table element. Then updates the table.
	 */
	public static void SaveToTable(TableElement element) throws Exception {
		String backup = makeBackup();
		
		try {			
			ArrayList<TableElement> table = importTable();
			//TODO implement improved match algorithm
			//TODO TODO prevent duplicate files from being written to the table!!
			element.setUniqueID( table.size() );
			
			for(TableElement that : table) {
				if( element.isMatch(that) ) { 
					element.addMatch(that);
					that.addMatch(element);
				}
			}
		
			table.add(element);
			
			writeTable(table);
		} catch(FileNotFoundException f) {
			CommandExecutor.createFile(TABLE_NAME);
		} catch(Exception e) {
			restore(backup);
			throw ( new IOException("ERROR: Table IOS failed.", e) );
		}	
	}
}

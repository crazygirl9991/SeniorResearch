package downloadCenter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

/**
 * Contains algorithms to download files and update the table
 * with new downloads and matches asynchronously, meaning that
 * UI operation will continue throughout and the spectra reviewer
 * can still be used in the meantime.
 * 
 * @author victoria
 *
 */
public class FileManager {
	private ArrayList<String> _downloadUrls = new ArrayList<String>();
	private static WorkingDirectory WORKING_DIRECTORY = WorkingDirectory.DOWNLOADS;
	
	private Status _Status = Status.IDLE;
	private String _file = "";
	private PropertyChangeSupport _propertyChangeSupport = new PropertyChangeSupport(this);
	
	/**
	 * Uses an HttpUrlConnection to access URLs as specified in previous
	 * methods (and stored as a member of this class) and downloads them
	 * to the working directory specified as a member of this class. If a
	 * file already exists in that directory, then it overwrites it.
	 * 
	 * @throws IOException
	 */
	public Boolean download() throws IOException {
		try {
			ExecutorService pool = Executors.newFixedThreadPool(30);
			for (final String str : _downloadUrls) {
				pool.submit(new Runnable() {
					@Override
					public void run() {
						try {
							File destination = new File(WORKING_DIRECTORY.toString(), str.substring(str.lastIndexOf("/") + 1));
							
							URL url = new URL(str);
							HttpURLConnection connect = (HttpURLConnection) url.openConnection();
							Files.copy(connect.getInputStream(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
							connect.disconnect();	 
						} catch (Exception e) {
							ErrorLogger.update("ERROR: Can't retrieve files.", e);
						}
					}
				});
			}
			pool.shutdown();
			pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			
			return true;
		} catch ( Exception e ) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Prepares for downloading files by compiling a list of URLs.
	 * If a 0 is specified for the fiber number, then the entire 
	 * plate will be downloaded (the number of files for each release
	 * is stored in SDSS.java). This method is the main interface for
	 * downloading files, updating the table, and communicating these
	 * updates to the display.
	 * 
	 * @param plateInfos
	 * 			- a list of all mjd,plate,fiber which needs downloading
	 */
	public void downloadInBackground(final ArrayList<int[]> plateInfos) {	
		new SwingWorker<Void,Void>(){		
			@Override
			protected Void doInBackground() throws Exception {
				setStatus(Status.DOWNLOADING);
				
				// Establishes download list //
				for(int[] current : plateInfos) {
					SDSS release = SDSS.getInstance(current);
					
					// Checks if fiber = 0, then download entire plate //
					if(current[2] == 0) {
						int tmp[] = new int[3];
						tmp[0] = current[0];
						tmp[1] = current[1];
						
						for(int i = 0; i <= release.FIBERS; i++) {
							tmp[2] = i;
							_downloadUrls.add(release.formatUrl(tmp));
						}
					} else
						_downloadUrls.add(release.formatUrl(current));
				}
				
				download();
				updateTable();
				MainAndDownloadUI.setData( importTable() );
				setStatus(Status.IDLE);
				return null;
			}
		}.execute();
	}

	/**
	 * Backups the table, creating a new name with a made up extension.
	 * @return
	 * @throws IOException
	 */
	public String makeBackupTable() throws IOException {
		String basefilename, path = "";

		if (Configurations.TABLE_NAME.equals("")) {
			basefilename = "default";
		} else {
			int indexOfExt = Configurations.TABLE_NAME.lastIndexOf('.');
			basefilename = Configurations.TABLE_NAME.substring(0, indexOfExt);

			if (basefilename.contains("/")) {
				int indexOfSlash = basefilename.lastIndexOf('/');
				path = basefilename.substring(0, indexOfSlash + 1);
				basefilename = basefilename.substring(indexOfSlash + 1, basefilename.length());
			}
		}

		String renamed = path + "cp-" + basefilename + ".backup";

		try {
			copy(Configurations.TABLE_NAME, renamed);
		} catch (Exception e) {
			throw (new IOException("ERROR: Could not backup table: " + Configurations.TABLE_NAME, e));
		}

		return renamed;
	}

	/**
	 * Restores the backed-up table if there was an error.
	 * @param filename
	 * @throws IOException
	 */
	private void restoreBackupTable(String filename) throws IOException {
		try {
			copy(filename, Configurations.TABLE_NAME);
			remove(filename);
		} catch (Exception e) {
			throw (new IOException("ERROR: could not restore file " + filename + " to table " + Configurations.TABLE_NAME, e));
		}
	}
	
	/**
	 * Copies and renames a source file from any path to any
	 * destination (so long as administrative access is not required).
	 * 
	 * @param source
	 *            - source file, including path if necessary
	 * @param target
	 *            - target file name and directory path
	 */
	public void copy(String source, String target) {
		try {
			File file1 = new File(source), file2 = new File(target);
			file1.renameTo(file2);
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't copy file " + source + " as file " + target, e);
			//TODO used logger
		}
	}
	
	/**
	 * Removes a file
	 * 
	 * @param filename
	 * 			- file to be removed and containing directory
	 */
	public void remove(String filename) {
		try {
			File file = new File(filename);
			file.delete();
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't delete file " + filename, e);
			//TODO used logger
		}
	}
	
	/**
	 * Imports the table as an ArrayList of TableElements.
	 * @return
	 * @throws FileNotFoundException
	 */
	public ArrayList<TableElement> importTable() throws FileNotFoundException {
		ArrayList<TableElement> lines = new ArrayList<TableElement>();

		try {
			Scanner scanner = new Scanner( new FileReader(Configurations.TABLE_NAME) );

			while (scanner.hasNextLine()) {
				String nextLine = scanner.nextLine();
				
				if (!nextLine.startsWith("#") && !nextLine.equals("")) {
					TableElement that = TableElement.parse(nextLine);
					lines.add(that);
				}
			}

			scanner.close();
		} catch (FileNotFoundException e) {
			ErrorLogger.update("ERROR: Can't import table: " + Configurations.TABLE_NAME, e);
			//TODO logger used here
			throw e;
		}

		return lines;
	}
	
	/**
	 * Outputs all of the variables to the table file.
	 * @param table
	 */
	public void writeTable(ArrayList<TableElement> table) {
		try {
			BufferedWriter writer = new BufferedWriter( new FileWriter(Configurations.TABLE_NAME) );
			
			writer.write(Configurations.TABLE_HEADER);
			for(TableElement current : table) {
				writer.write( current.toString() );
				writer.newLine();
			}
			
			writer.close();
		} catch (Exception e) {
			ErrorLogger.update("ERROR: Can't write table: " + Configurations.TABLE_NAME, e);
			//TODO logger used here
		}
	}

	/**
	 * Updates the table specified in Configurations.java by checking 
	 * which files are in the "downloads/" directory. Sets statuses
	 * in real time to reflect current stage of process. Compares
	 * downloaded files to find matches and updates the table with
	 * this information as well. Backup table is created beforehand.
	 * 
	 * @throws IOException
	 */
	public void updateTable() throws IOException {
		String backup = makeBackupTable();
		ArrayList<TableElement> table = new ArrayList<TableElement>();
		try {
			setStatus(Status.READING);
			File pwd = new File(WORKING_DIRECTORY.toString());
			for (File current : pwd.listFiles()) {
				setFile(current.getName());
				TableElement temp = TableElement.ParseFitFile(current);
				if(temp != null)
					table.add( temp );
			}
			setFile("");
			setStatus(Status.SORTING);
			Collections.sort(table);
			for(int i = 0; i < table.size(); i++)
				table.get(i).setUniqueID(i);
			setStatus(Status.MATCHING);
			for(int i = 0; i < table.size(); i++) {
				TableElement tei = table.get(i);
				setFile(tei.getFilename());
				for(int j = i + 1; j < table.size(); j++) {
					TableElement tej = table.get(j);
					if( tei.isMatch(tej) ) {
						tei.addMatch(tej);
						tej.addMatch(tei);
					}
				}
			}
			setFile("");
			setStatus(Status.WRITING);
			writeTable(table);
			remove(backup);
		} catch (Exception e) {
			restoreBackupTable(backup);
			throw (new IOException("ERROR: Table IOS failed.", e));
		}
	}
	
	public String getStatus() {
		return _Status.toString();
	}

	public void setStatus(Status Status) {
		Status oldStatus = _Status;
		_Status = Status;
		_propertyChangeSupport.firePropertyChange("Status", oldStatus, Status);
	}

	public String getFile() {
		return _file;
	}

	public void setFile(String file) {
		String oldfile = _file;
		_file = file;
		_propertyChangeSupport.firePropertyChange("file", oldfile, file);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		_propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		_propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Status simplifies real-time updates to the main UI.
	 * @author victoria
	 *
	 */
	private enum Status {
		IDLE("Idle"), 
		DOWNLOADING("Downloading"), 
		READING("Checking for Updated Files"), 
		SORTING("Sorting Updated Files"), 
		MATCHING("Finding Matches"), 
		WRITING("Writing Table");
		
		private String _name;
		
		private Status(String name) { _name = name; }
		
		@Override
		public String toString() {	return _name; }
	};
}

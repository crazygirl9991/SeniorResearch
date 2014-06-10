package downloadCenter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * A class designed to allow easy error logging. TODO Not
 * currently in use.
 * 
 * @author victoria
 *
 */
public class ErrorLogger {
	public static String LOG = createUniqueFileName();
	private static WorkingDirectory _directory = WorkingDirectory.ERRORS;
	
	private static ArrayList<Exception> _errors = new ArrayList<Exception>();
	
	public  static void update(String message) {
		_errors.add( new Exception(message) );
	}
	
	public static void update(String message, Exception e) {
		_errors.add( new Exception(message, e) );
	}
	
	public static void update(String message, Exception e, WorkingDirectory wd) {
		_directory = wd;
		_errors.add( new Exception(message, e) );
	}
	
	/**
	 * Returns a file name of format LOG[ddMMyyy-HHmmss].txt where []
	 * refers to the current Date-Time and is unique to each run.
	 */
	private static String createUniqueFileName() {
		// use calendar to create unique file ID
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy-HHmmss");
		String dateStr = sdf.format(cal.getTime());

		return ("ErrorLog-" + dateStr + ".txt");
	}
	
	/**
	 * Takes in a JFrame and a message and displays an error dialogue in that frame.
	 */
	public static void DIALOGUE(JFrame frame, String details) {
		String windowTitle = "ERROR";
		
		JOptionPane.showMessageDialog(frame, "Requested operation is invalid. " + details, windowTitle, JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Logs an error
	 * @param message
	 */
	public static void LOG() {
		ArrayList<String> strArray = new ArrayList<String>();
		
		for(Exception e : _errors)
			strArray.add( e.getMessage() );
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter( new FileWriter(_directory+LOG) );
			
			for(String current : strArray) {
				writer.write(current);
				writer.newLine();
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			//TODO what to do if logger even fails. wahhhh
		}
	}

}

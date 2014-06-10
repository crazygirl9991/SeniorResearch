package downloadCenter;

/**
 * This class contains all of the globally-used settings for the
 * application, such as the table name and formatting, the angular
 * distance for defining a match, and the distance between
 * fibers on a plate. 
 * 
 * @author victoria
 *
 */
public class Configurations {
	public static String TABLE_NAME = "QuasarSpectraTable.qst";
	public static String TABLE_HEADER = "## uniqueID\tfilename\tRA,Dec\tMJD,Plate,Fiber\tmatches ##\n";
	public static String TABLE_COLUMN_DELIMITER = "\t";
	public static String LIST_DELIMITER = ",";

	public static Double ANGULAR_DISTANCE_THRESHOLD = 2.0 / 3600; // 2 arcsecs in degrees
	public static Double FIBER_DISTANCE_THRESHOLD = 55.0; // arcsecs - physical limitation of drilling fibers on a plate
	
}

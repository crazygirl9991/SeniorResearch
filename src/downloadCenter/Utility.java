package downloadCenter;

import java.util.List;

/**
 * Has functions to handle math and array behavior required in downloadCenter.
 */
public class Utility {
	private static String _delim = ","; // this is only for in class use (like in creating error messages)
	/**
	 * Converts from degrees to radians.
	 * @param degrees
	 * @return
	 */
	public static double degreesToRadians(double degrees) {
		double radians = degrees * Math.PI / 180;
		
		return radians;
	}
	
	/**
	 * Converts string array to string.
	 * @param array
	 * @return
	 */
	public static String toString(String delimiter, String[] array) {
		String str;
		
		if(array.length == 0)
			str = "";
		else if(array.length == 1)
			str = array[0];
		else {
			str = "";
			int i = 0;
			
			while( i < array.length-1 ) {
				str += array[i] + delimiter;
				i++;
			}
			str += array[i];
		}
		return str;
	}
	
	/**
	 * Converts int array to string.
	 * @param array
	 * @return
	 */
	public static String toString(String delimiter, int[] array) {
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
	public static String toString(String delimiter, double[] array) {
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
	public static String toString(String delimiter, List<Integer> list) {
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
	
	/**
	 * Returns the dot product of two 2D or 3D vectors (length of vectors must be equal).
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public static double dot(double[] v1, double[] v2) throws UnsupportedOperationException {
		double product = 0;
		
		if( v1.length == 3 && v2.length == 3 )
			product = (v1[0]*v2[0]) + (v1[1]*v2[1]) + (v1[2]*v2[2]);
		else if( v1.length == 2 && v2.length == 2 )
			product = (v1[0]*v2[0]) + (v1[1]*v2[1]);
		else {
			String message = "ERROR: Vectors should be equal and have either two or three elements. Was given v1: " + toString(_delim, v1) + " and v2: " + toString(_delim, v2);
			throw  (new UnsupportedOperationException(message) );
		}
		
		return product;
	}
	
	/**
	 * Returns <x,y,z> = < rho*sin(phi)*cos(theta), rho*sin(phi)*sin(theta), rho*cos(phi) >.
	 */
	public static double[] toCartesian(String[] spherical) throws UnsupportedOperationException {
		if( spherical.length != 3 ) {
			String message = "ERROR: Should have three array elements, rho,theta,phi. Was given: " + toString(_delim, spherical);
			throw  (new UnsupportedOperationException(message) );
		}
		
		try {
			double[] vector = new double[3];
		
			double rho = Double.parseDouble(spherical[0]);
			double theta = degreesToRadians( Double.parseDouble(spherical[1]) );
			double phi = degreesToRadians( Double.parseDouble(spherical[2]) );
		
			vector[0] = rho * Math.sin(phi) * Math.cos(theta);
		
			vector[1] = rho * Math.sin(phi) * Math.sin(theta);
		
			vector[2] = rho * Math.cos(phi);
		
			return vector;
		} catch (Exception e) {
			String message = "ERROR: Conversion to Cartesian coordinates failed! Given coordinates: " + toString(_delim, spherical);
			throw (new UnsupportedOperationException(message, e) );
		}
	}
	
	/**
	 * Returns <x,y,z> = < rho*sin(phi)*cos(theta), rho*sin(phi)*sin(theta), rho*cos(phi) >.
	 */
	public static double[] toCartesian(double[] spherical) throws UnsupportedOperationException {
		if( spherical.length != 3 ) {
			String message = "ERROR: Should have three array elements, rho,theta,phi. Was given: " + toString(_delim, spherical);
			throw  (new UnsupportedOperationException(message) );
		}
		
		try {
			double[] vector = new double[3];
		
			double rho = spherical[0];
			double theta = degreesToRadians( spherical[1] );
			double phi = degreesToRadians( spherical[2] );
		
			vector[0] = rho * Math.sin(phi) * Math.cos(theta);
		
			vector[1] = rho * Math.sin(phi) * Math.sin(theta);
		
			vector[2] = rho * Math.cos(phi);
		
			return vector;
		} catch (Exception e) {
			String message = "ERROR: Conversion to Cartesian coordinates failed! Given coordinates: " + toString(_delim, spherical);
			throw (new UnsupportedOperationException(message, e) );
		}
	}
}

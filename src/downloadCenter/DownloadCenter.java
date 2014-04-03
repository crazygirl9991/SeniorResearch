package downloadCenter;

import java.util.ArrayList;

/**
 * Main controller for anything download or table related. Interfaces directly with command line.
 * @author victoria
 *
 */
public class DownloadCenter {
	public static double _plateOverlapThreshold = 2.98; // degrees - diameter of each plate
	
	public static void main(String args[]) {
		
		double[] coords1 = {1, 187.465, 12.4877};
		double[] coords2 = {1, 189.271, 12.4811};
		double[] coords3 = {1, 188.062, 27.4019};
		double[] coords4 = {1, 189.767, 27.4614};
		 
		int FIBERS = 640;
		
		// Radius = 1 (unit circle), RA [[hours]], Dec [[degrees]] are spherical
		double angularDistance1 = Math.acos( Utility.dot( Utility.toCartesian(coords1), Utility.toCartesian(coords2) ) );
		double angularDistance2 = Math.acos( Utility.dot( Utility.toCartesian(coords3), Utility.toCartesian(coords4) ) );
		
		Boolean compare1 = ( angularDistance1 < Utility.degreesToRadians(_plateOverlapThreshold) );
		Boolean compare2 = ( angularDistance2 < Utility.degreesToRadians(_plateOverlapThreshold) );
		
		if( compare1 && compare2 ) {
			
			ArrayList< int[] > downloadList = new ArrayList< int[] >();
			for(int i = 100; i < FIBERS; i++) {
				
				int[] plateInfo1 = {53166, 1615, i};
				downloadList.add(plateInfo1);
				
				int[] plateInfo2 = {53169, 1616, i};
				downloadList.add(plateInfo2);
				
				int[] plateInfo3 = {53847, 2235, i};
				downloadList.add(plateInfo3);
				
				int[] plateInfo4 = {53729, 2236, i};
				downloadList.add(plateInfo4);
			}
		
			try {
				FitFileStore store = new FitFileStore(downloadList);
			
				store.Download();
				store.UpdateTable();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Plates do not overlap. Distance between 1st: " + angularDistance1 + ", and 2nd: " + angularDistance2);
		}
	}
}

package downloadCenter;

import java.io.IOException;
import java.util.ArrayList;

public class DownloadCenter {
	public static double _plateOverlapThreshold = 2.98; // degrees - diameter of each plate
	
	public void main() {
		
		double[] plateInfo1 = {51602, 266, 0};
		double[] coords1 = {145.892, 0.059372};
		
		double[] plateInfo2 = {51608, 267, 0};
		double[] coords2 = {147.732, -0.033455};
		
		int FIBERS = 640;
		
		// Radius = 1 (unit circle), RA [[hours]], Dec [[degrees]] are spherical
		double angularDistance = Math.acos( Utility.dot( Utility.toCartesian(coords1), Utility.toCartesian(coords2) ) );
		
		if( angularDistance < Utility.degreesToRadians(_plateOverlapThreshold) ) {
			
			ArrayList< double[] > downloadList = new ArrayList< double[] >();
			for(int i = 0; i < FIBERS; i++) {
				plateInfo1[2] = i;
				plateInfo2[2] = i;
				downloadList.add(plateInfo1);
				downloadList.add(plateInfo2);
			}
		
			try {
				DownloadStore store = new PlateInfoDownloadStore(downloadList);
				CommandExecutor ce = new CommandExecutor();
			
				store.Write(ce);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Plates do not overlap. Distance between them: " + angularDistance);
		}
	}
}

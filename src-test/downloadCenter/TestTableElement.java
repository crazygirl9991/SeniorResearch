package downloadCenter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestTableElement {
	
	@Test
	public void testTableElement() {
		String filename0 = "some_file0.fits", filename1 = "some_file1.fits";
		double[] coordsArray0 = {1.0, 205.5470, 28.3770}, coordsArray1 = {205.5470, 28.3770};
		int[] plateInfoArray = {56899, 1320, 122};
		String coords0 = coordsArray0[0] + "," + coordsArray0[1] + "," + coordsArray0[2];
		String coords1 = coordsArray1[0] + "," + coordsArray1[1];
		String plateInfo = plateInfoArray[0] + "," + plateInfoArray[1] + "," + plateInfoArray[2];
		int ID = 0;
		
		TableElement te0 = new TableElement(filename0);
		te0.setUniqueID(ID);
		
		assertEquals(te0.getUniqueID(), ID);
		
		// set these using Strings //
		te0.setCoords(coords0);
		for(int i = 0; i < coordsArray0.length; i++)
			assertEquals( te0.getCoords()[i], coordsArray0[i], 0 );
		
		te0.setCoords(coords1);
		for(int i = 0; i < coordsArray1.length; i++)
			assertEquals( te0.getCoords()[i+1], coordsArray1[i], 0 );
		
		te0.setPlateInfo(plateInfo);
		for(int i = 0; i < plateInfoArray.length; i++)
			assertEquals( te0.getPlateInfo()[i], plateInfoArray[i], 0 );
		

		// set these using arrays //
		te0.setCoords(coordsArray0);
		for(int i = 0; i < coordsArray0.length; i++)
			assertEquals( te0.getCoords()[i], coordsArray0[i], 0 );
		
		te0.setCoords(coordsArray1);
		for(int i = 0; i < coordsArray1.length; i++)
			assertEquals( te0.getCoords()[i+1], coordsArray1[i], 0 );
		
		te0.setPlateInfo(plateInfoArray);
		for(int i = 0; i < plateInfoArray.length; i++)
			assertEquals( te0.getPlateInfo()[i], plateInfoArray[i], 0 );
				
		
		TableElement te1 = new TableElement();
		assertEquals(te1.getFilename(), "unknown");
		
		te1.setFilename(filename1);
		assertEquals(te1.getFilename(), filename1);
	}

	
	@Test
	public void testTableSetterExceptions() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		double[] coordsArray = {1, 205.547, 28.377};
		String plateInfo0 = "56899,1320,122";
		int[] plateInfoArray = {56899, 1320, 122};
		
		TableElement te0 = new TableElement(filename0);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		assertEquals(te0.getFilename(), filename0);
		
		// try variations which should break the coords setter
		try {
			te0.setCoords("1");
		} catch (UnsupportedOperationException u) {
			// verify that coords are not changed when error
			for(int i = 0; i < coordsArray.length; i++)
				assertEquals( te0.getCoords()[i], coordsArray[i], 0 );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}

		try {
			te0.setCoords("");
		} catch (UnsupportedOperationException u) {
			// verify that coords are not changed when error
			for(int i = 0; i < coordsArray.length; i++)
				assertEquals( te0.getCoords()[i], coordsArray[i], 0 );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
		
		// try variations which should break the plate info setter
		try {
			te0.setPlateInfo("1,2,3,4");
		} catch (UnsupportedOperationException u) {
			// verify that plate info is not changed when error
			for(int i = 0; i < plateInfoArray.length; i++)
				assertEquals( te0.getPlateInfo()[i], plateInfoArray[i], 0 );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
		
		
		try {
			te0.setPlateInfo("");
		} catch (UnsupportedOperationException u) {
			// verify that plate info are not changed when error
			for(int i = 0; i < plateInfoArray.length; i++)
				assertEquals( te0.getPlateInfo()[i], plateInfoArray[i], 0 );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testTableElementSetMatches() {
		String filename0 = "some_file0.fits";
		String coords0 = "1,205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		
		TableElement te0 = new TableElement(filename0);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		try {
			// shouldn't have any matches to start...
			if( te0.hasMatch() == true )
				assert(false);
		
			// ... or if matches is null...
			te0.setMatches("");
			if( te0.hasMatch() == true )
				assert(false);
		
			// ... or if it is imported, then it likely says there are "none"...
			te0.setMatches("none");
			if( te0.hasMatch() == true )
				assert(false);
		
			te0.setMatches(""); // tests null branch in both else if statements
			
			// ... but if it is set manually then there should be
			te0.setMatches("1,2");
			if( te0.hasMatch() == false )
				assert(false);
			
		} catch (Exception e) { // the above should all succeed
			e.printStackTrace();
			assert(false);
		}
		
		try { // non-numerical values will break the math
			te0.setCoords("lolly,pop");
			te0.isMatch(te0);
		} catch (UnsupportedOperationException u) {
			assertEquals(u.getCause().getClass(), NumberFormatException.class);
		} catch(Exception e) { // wrong type of exception means failure
			e.printStackTrace();
			assert(false);
		}
		
		try { // checks exception thrown if matches are formatted correctly but are not integers
			te0.setMatches("lolly,pop");
		} catch (UnsupportedOperationException u) {
			assert(true);
		} catch(Exception e) { // wrong type of exception means failure
			e.printStackTrace();
			assert(false);
		}
		
		assert(false); // if no exception is thrown, then something is wrong with this test
		
	}
	
	@Test
	public void testTableElementParse() {
		String filename0 = "some_file0.fits";
		String coords0 = "1,205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		int ID = 0;
		
		TableElement te0 = new TableElement(filename0);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		te0.setUniqueID(ID);
		
		String tableDisplay = te0.toString();
		
		TableElement te1 = new TableElement();
		te1.parse(tableDisplay);
		
		if( te0.isMatch(te1) )
			assert(true);
		
	}
	
}

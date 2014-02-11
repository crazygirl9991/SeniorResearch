package downloadCenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.PrintWriter;

import org.junit.Test;

public class TestTableElement {
	
	@Test
	public void testTableElement() {
		String filename0 = "some_file0.fits", filename1 = "some_file1.fits", filename2 = "some_file2.fits", filename3 = "some_file3.fits";
		String coords0 = "205.5470,28.3770", coords1 = "205.5470,28.3771", coords2 = "205.5470,28.3765", coords3 = "198,22";
		String plateInfo0 = "56899,1320,122", plateInfo1 = "53166,1615,513", plateInfo2 = "53166,1614,400", plateInfo3 = "52000,1615,500";
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String testTableName = "src-test/downloadCenter/TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		String backupFileName = te0.renameForBackup(testTableName); // this will be the same for all of them
		
		TableElement te1 = new TableElement();
		te1.setFilename(filename1);
		te1.setTableName(testTableName);
		te1.setCoords(coords1);
		te1.setPlateInfo(plateInfo1);
		
		TableElement te2 = new TableElement(filename2);
		te2.setTableName(testTableName);
		te2.setCoords(coords2);
		te2.setPlateInfo(plateInfo2);
		
		TableElement te3 = new TableElement(filename3, testTableName);
		te3.setCoords(coords3);
		te3.setPlateInfo(plateInfo3);
		
		try {
			// have to mock both calls in case exception is thrown...
			doNothing().when(mockCE).copy(testTableName, backupFileName);
			doNothing().when(mockCE).copy(backupFileName, testTableName);
			
			te0.SaveToTable(mockCE);
			te1.SaveToTable(mockCE);
			te2.SaveToTable(mockCE);
			te3.SaveToTable(mockCE);
			
			// ... but only those which run every time can be verified here (see below for rest)
			verify(mockCE, times(4)).copy(testTableName, backupFileName);
			
			// clears the test file for next run of this test
			PrintWriter writer = new PrintWriter(testTableName);
			writer.write("");
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testTableElementGenericInstantiationException() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String testTableName = "src-test/downloadCenter/TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		te0.setMatches("");
		
		String backupFileName = te0.renameForBackup(testTableName);
		
		try {
			doNothing().when(mockCE).copy(testTableName, backupFileName);
			doNothing().when(mockCE).copy(backupFileName, testTableName);
			
			te0.SaveToTable(mockCE);
		} catch(IOException io) {
			// check that restore() was called in addition to makeBackup()
			verify(mockCE).copy(testTableName, backupFileName);
			verify(mockCE).copy(backupFileName, testTableName);
			assert(true);
		} catch (Exception e) { // IOException is expected, nothing else should allow a pass
			e.printStackTrace();
			assert(false);
		}
		
		assert(false); // if no exception is thrown, then something is wrong with this test
	}
	
	@Test 
	public void testTableElementWriteException() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String testTableName = "madeUpDirectory/TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		String backupFileName = te0.renameForBackup(testTableName);
		
		try {
			// have to all three of these
			doNothing().when(mockCE).createFile(testTableName);
			doNothing().when(mockCE).copy(testTableName, backupFileName);
			doNothing().when(mockCE).copy(backupFileName, testTableName);
			
			te0.SaveToTable(mockCE);
		} catch (IOException io) {
			// check that restore() was called in addition to makeBackup()
			verify(mockCE).copy(testTableName, backupFileName);
			verify(mockCE).copy(backupFileName, testTableName);
			assert(true);
		} catch (Exception e) { // IOException is expected, nothing else should allow a pass
			e.printStackTrace();
			assert(false);
		}
		
		assert(false); // if no exception is thrown, then something is wrong with this test
	}
	
	@Test
	public void testTableElementBackUpExceptions() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		
		CommandExecutor mockCE0 = mock(CommandExecutor.class), mockCE1 = mock(CommandExecutor.class);
		String testTableName = "madeUpDirectory/TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		String backupFileName = te0.renameForBackup(testTableName);
		
		try { // this tests makeBackup() exception
			// have to all three of these
			doNothing().when(mockCE0).createFile(testTableName);
			doThrow( new RuntimeException() ).when(mockCE0).copy(testTableName, backupFileName);
			doNothing().when(mockCE0).copy(backupFileName, testTableName);
			
			te0.SaveToTable(mockCE0);
		} catch (IOException io) {
			// restore() should not have been called
			verify(mockCE0).copy(testTableName, backupFileName);
			assertEquals(io.getCause().getClass(), RuntimeException.class);
		} catch (Exception e) { // IOException is expected, nothing else should allow a pass
			e.printStackTrace();
			assert(false);
		}
		
		try { // this tests restore() exception
			// have to all three of these
			doNothing().when(mockCE1).createFile(testTableName);
			doNothing().when(mockCE1).copy(testTableName, backupFileName);
			doThrow( new RuntimeException() ).when(mockCE1).copy(backupFileName, testTableName);
			
			te0.SaveToTable(mockCE1);
		} catch (IOException io) {
			// check that restore() was called in addition to makeBackup()
			verify(mockCE1).copy(testTableName, backupFileName);
			verify(mockCE1).copy(backupFileName, testTableName);
			assertEquals(io.getCause().getClass(), RuntimeException.class);
			assert(true);
		} catch (Exception e) { // IOException is expected, nothing else should allow a pass
			e.printStackTrace();
			assert(false);
		}
		
		assert(false); // if no exception is thrown, then something is wrong with this test
	}
	
	@Test
	public void testTableElementGettersAndSetters() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		String[] coordsArray = {"205.547", "28.377"};
		String plateInfo0 = "56899,1320,122";
		String[] plateInfoArray = {"56899", "1320", "122"};
	
		String testTableName = "TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
		te0.setCoords(coords0);
		te0.setPlateInfo(plateInfo0);
		
		assertEquals(te0.getTableName(), testTableName);
		assertEquals(te0.getFilename(), filename0);
		
		// try variations which should break the coords setter
		try {
			te0.setCoords("1");
		} catch (UnsupportedOperationException u) {
			// verify that coords are not changed when error
			for(int i = 0; i < coordsArray.length; i++)
				assertEquals( te0.getCoords()[i], coordsArray[i] );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
		
		try {
			te0.setCoords("");
		} catch (UnsupportedOperationException u) {
			// verify that coords are not changed when error
			for(int i = 0; i < coordsArray.length; i++)
				assertEquals( te0.getCoords()[i], coordsArray[i] );
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
				assertEquals( te0.getPlateInfo()[i], plateInfoArray[i] );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
		
		
		try {
			te0.setPlateInfo("");
		} catch (UnsupportedOperationException u) {
			// verify that plate info are not changed when error
			for(int i = 0; i < plateInfoArray.length; i++)
				assertEquals( te0.getPlateInfo()[i], plateInfoArray[i] );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testTableMatchFunctions() {
		String filename0 = "some_file0.fits";
		String coords0 = "205.547,28.377";
		String plateInfo0 = "56899,1320,122";
		
		String testTableName = "TestTable.qst";
		
		TableElement te0 = new TableElement(filename0, testTableName);
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
			assertEquals(u.getCause().getClass(), UnsupportedOperationException.class);
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
}

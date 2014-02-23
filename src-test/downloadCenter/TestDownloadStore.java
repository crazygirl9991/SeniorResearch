package downloadCenter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.Test;

/**
 * Tests all of the functionality in downloadStore.java except those which
 * depend on instantiating a command executor (as opposed to using a mock), 
 * which is tested independently. 
 */
public class TestDownloadStore { 
	@Test
	public void testRename() {
		String testFilename = "TESTFILE.txt";
		WorkingDirectory wd = WorkingDirectory.STUB;
		DownloadStore store = new ListDownloadStore(testFilename, wd);
		
		assertEquals( store.getInputfile(), testFilename );
		
		if( store.getOutputfile().contains(".lis") && !store.getOutputfile().equals( store.getInputfile() ) )
			assert(true);
		else
			assert(false);
	}
	
	@Test
	public void testRenameDefault() {
		WorkingDirectory wd = WorkingDirectory.STUB;
		DownloadStore store = new PlateInfoDownloadStore(1, 1, 1, wd);
		
		assertEquals( store.getInputfile(), "" );
		
		if( store.getOutputfile().contains("default") && store.getOutputfile().contains(".lis") )
			assert(true);
		else
			assert(false);
	}
	
	@Test
	public void testDownload() {//TODO BROKEN	
		WorkingDirectory wd = WorkingDirectory.STUB;
		DownloadStore store = new PlateInfoDownloadStore(1, 1, 1, wd);
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String path = store.getOutputfile();
		
		try {
			doNothing().when(mockCE).wget( path, wd.toString() );
			store.Download(mockCE);
			verify(mockCE).wget( path, wd.toString() );
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testUpdateTable() {
		WorkingDirectory wd = WorkingDirectory.STUB;
		DownloadStore store = new PlateInfoDownloadStore(1, 1, 1, wd);
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String tableName = "QuasarSpectraTable.qst";
		
		try {
			doThrow( new UnsupportedOperationException() ).when(mockCE).createFile(tableName);
			store.Download(mockCE);
			verify(mockCE).createFile(tableName);
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testClean() {//TODO paths yuck wut?
		
		WorkingDirectory wd = WorkingDirectory.STUB;
		DownloadStore store = new PlateInfoDownloadStore(1, 1, 1, wd);
		
		CommandExecutor mockCE = mock(CommandExecutor.class);
		String outputfilePath = store.getOutputfile();
		
		try {
			doNothing().when(mockCE).remove(outputfilePath);
			store.Clean(mockCE);
			verify(mockCE).remove(outputfilePath);
		} catch (Exception e) {
			e.printStackTrace();
			assert(false);
		}
	}
	
	@Test
	public void testGettersAndSetters() {
		WorkingDirectory wd = WorkingDirectory.STUB, testWD = WorkingDirectory.UNKNOWN;
		String inputfile = "input.txt", testType = "test";
		
		DownloadStore store = new ListDownloadStore(inputfile, wd);
		
		assertEquals( store.getInputfile(), inputfile );
		if( store.getOutputfile().equals("") )
			assert(false);
		
		store.setType(testType);
		assertEquals( store.getType(), testType );
		
		store.setWorkingDir(testWD);
		assertEquals(store.getWorkingDir(), testWD);
	}
	

}
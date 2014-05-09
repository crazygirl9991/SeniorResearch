package downloadCenter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.SwingWorker;

enum STATUS {
	IDLE("Idle"), DOWNLOADING("Downloading"), READING("Reading"), SORTING("Sorting"), MATCHING("Matching"), WRITING("Writing");
	String _name;
	private STATUS(String name) {
		_name = name;
	}
	@Override
	public String toString() {
		return _name;
	}
};

public class BackgroundDownload {

	STATUS _status = STATUS.IDLE;
	String _file = "";
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private FitFileStore store;

	public BackgroundDownload(int[] plateInfo) {
		super();
		store = new FitFileStore(plateInfo);
		new SwingWorker<Void,Void>(){		
		@Override
		protected Void doInBackground() throws Exception {
			setStatus(STATUS.DOWNLOADING);
			store.Download();
			updateTable();
			Main.setData(TableManager.importTable());
			setStatus(STATUS.IDLE);
			return null;
		}
		}.execute();
	}

	public STATUS getStatus() {
		return _status;
	}

	public void setStatus(STATUS status) {
		STATUS oldstatus = _status;
		_status = status;
		pcs.firePropertyChange("status", oldstatus, status);
	}

	public String getFile() {
		return _file;
	}

	public void setFile(String file) {
		String oldfile = _file;
		_file = file;
		pcs.firePropertyChange("file", oldfile, file);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}
	
	/**
	 * TODO
	 * @throws IOException
	 */
	public void updateTable() throws IOException {
		String backup = TableManager.makeBackup();
		ArrayList<TableElement> table = new ArrayList<TableElement>();
		try {
			setStatus(STATUS.READING);
			File pwd = new File(WorkingDirectory.DOWNLOADS.toString());
			for (File current : pwd.listFiles()) {
				setFile(current.getName());
				TableElement temp = FitFileStore.ParseFitFile(current);
				if(temp != null)
					table.add( temp );
			}
			setFile("");
			setStatus(STATUS.SORTING);
			Collections.sort(table);
			for(int i = 0; i < table.size(); i++)
				table.get(i).setUniqueID(i);
			setStatus(STATUS.MATCHING);
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
			setStatus(STATUS.WRITING);
			TableManager.writeTable(table);
			CommandExecutor.remove(backup);
		} catch (Exception e) {
			TableManager.restore(backup);
			throw (new IOException("ERROR: Table IOS failed.", e));
		}
	}

}

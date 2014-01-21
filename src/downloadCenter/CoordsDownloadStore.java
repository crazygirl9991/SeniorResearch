package downloadCenter;

import java.io.IOException;

public class CoordsDownloadStore extends DownloadStore {
	public CoordsDownloadStore() {
		setType("coords");
		Rename();
	}
	
	//TODO figure out the logic of this. 
	// --> is it possible to download by coordinate and if so how?
	@Override
	public void Write(CommandExecutor ce) throws IOException {
	}
}

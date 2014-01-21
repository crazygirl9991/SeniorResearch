package downloadCenter;

import java.io.IOException;

public class DownloadCenter {
	public void main() {
		DownloadStore store = new ListDownloadStore("name");
		CommandExecutor ce = new CommandExecutor();
		try {
			store.Write(ce);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

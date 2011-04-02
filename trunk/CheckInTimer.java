
public class CheckInTimer extends Thread {
	// CheckIn once every 5 minutes.
	public final long checkInTimer = 1000*60*5;
	public Server server;
	
	public CheckInTimer(Server server) {
		this.server = server;
	}
	
	@Override
	public void run() {
		while(server.isPublic()) {
			try {
				sleep(checkInTimer);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			server.masterCheckIn();
		}
		
		server.masterCheckOut();
	}

}

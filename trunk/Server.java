import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.SimpleByteBufferAllocator;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.DatagramConnector;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;


public class Server implements Runnable {
	public MainGUI gui;
	private Logger logger;
	
	private CheckInTimer checkinTimer;
	
	private DatagramAcceptor UDPacceptor;
	
	private Map<String, Player> playerList;
	
	private int[] usageLast5Min;
	private int[] usageLast20Min;
	private int[] usageLast1Hour;
	private int[] usageLast1Day;
	private int minPassed;
	private int min5Passed;
	private int min20Passed;
	private int hourPassed;
	private final long usageUpdateInterval = 1000*60;
	private Thread usageTimer;
	
	private ServerConfiguration config;
	
	public Server(ServerConfiguration c) {
		// Initialize the logger
		logger = JMIX.getLogger();
		
		// Create a HashMap to store a list of players;
        playerList = new HashMap<String, Player>();
        
        minPassed = 0;
        min5Passed = 0;
        min20Passed = 0;
        hourPassed = 0;
        
        usageLast5Min = new int[5];
        for (int i = 0; i < 5; i++)
        	usageLast5Min[i] = 0;
        usageLast20Min = new int[4];
        for (int i = 0; i < 4; i++)
        	usageLast20Min[i] = 0;
        usageLast1Hour = new int[3];
        for (int i = 0; i < 3; i++)
        	usageLast1Hour[i] = 0;
        usageLast1Day = new int[24];
        for (int i = 0; i < 24; i++)
        	usageLast1Day[i] = 0;
		
        // Set server configuration
        config = c;
        
		// Start listening TCP and UDP sockets
		if (config.autoListen) {
			start();
		}
		
		// Fetch master server address and port
		if (config.fetchPublic) {
			Utilities.updateMaster(config.game);
		}
		
		// Check-In with the master server if we are allowed
		if (Utilities.allowPublic()) {
			if (config.autoPublic) {
				masterCheckIn();
				checkinTimer = new CheckInTimer(this);
				checkinTimer.start();
			}
		}
	
        if (config.isGUI) {
        	gui = new MainGUI();
        }
        
        // Begin updating usage statistics
        usageTimer = new Thread(this);
        
		JMIX.getLogger().log(Level.INFO, "Startup complete.");
	}
	
	/**
	 * Starts server listening for UDP and TCP connections
	 */
	public void start() {
        ByteBuffer.setUseDirectBuffers(false);
        ByteBuffer.setAllocator(new SimpleByteBufferAllocator());
        startTCP();
        startUDP();
	}
	
	/**
	 * Starts the TCP listener on the server
	 */
	private void startTCP() {
        //Set up acceptor for TCP connections
        IoAcceptor acceptor = new SocketAcceptor();
        
        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        //cfg.getFilterChain().addLast( "logger", new LoggingFilter() );
        cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter( new WoSCodecFactory() ));

        try {
			acceptor.bind( new InetSocketAddress(config.port), new ClientHandler(this), cfg);
        } catch (IOException e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO, "TCP Acceptor Started.");
	}
	
	/**
	 * Starts the UDP listener on the server
	 */
	private void startUDP() {
        //Set up acceptor for UDP connections
        UDPacceptor = new DatagramAcceptor();
        
        DefaultIoFilterChainBuilder chain = UDPacceptor.getFilterChain();
        //chain.addLast("logger", new LoggingFilter());
        
        try {
			UDPacceptor.bind(new InetSocketAddress(config.port), new UDPHandler(this));
        } catch (IOException e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO, "UDP Acceptor Started.");
	}
	
	/**
	 * Sends the check-in packet to the MIX Master server
	 */
	public void masterCheckIn() {
		if (UDPacceptor == null)
			startUDP();
		DatagramConnector connector = new DatagramConnector();
        //ConnectFuture connFuture = connector.connect(new InetSocketAddress(MASTER_ADDRESS, MASTER_PORT), new UDPMasterHandler(server));
		//ConnectFuture connFuture = connector.connect(new InetSocketAddress(MASTER_ADDRESS, MASTER_PORT), new InetSocketAddress(PORT), new UDPMasterHandler(server));
        IoSession session = UDPacceptor.newSession(new InetSocketAddress(Utilities.getMasterAddress(config.game), Utilities.getMasterPort(config.game)), new InetSocketAddress(config.port));
        // If !! is checked, the port number the server is set to run on is sent
        // If !! is not checked, the port number sent is 0
        String sendData = "!version=" + Utilities.getVersionNumMIX() + "" +
        				",nump=" + getPlayerCount() + 
        				",gameid= " + Utilities.getGameID(config.game) +
        				",game=" + config.game + 
        				",host=" + Utilities.getHost() + 
        				",id=" + Utilities.getAdminID() + 
        				",port=" + config.port + 
        				",info=" +
        				",name=" + config.serverName;
        int capacity = sendData.length() + 1;
        ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
        buffer.put(sendData.getBytes(Charset.forName("us-ascii")));
        buffer.put((byte) 00);
        buffer.flip();
        session.write(buffer);
        session.close();
        
        /*connFuture.addListener(new IoFutureListener(){
            public void operationComplete(IoFuture future) {
                ConnectFuture connFuture = (ConnectFuture)future;
                if (connFuture.isConnected()) {
                    IoSession session = future.getSession();
                    String sendData = "!version=41252,nump=0,gameid=0,game=,host=Dell-Inspiron,id=77EBDE4D,port=2222,info=,name=MIX Server";
                    int capacity = sendData.length() + 1;
                    ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
                    buffer.put(sendData.getBytes(Charset.forName("us-ascii")));
                    buffer.put((byte) 00);
                    buffer.flip();
                    session.write(buffer);
                    //send report to MIX master here
                }
            }
        });*/
        
        logger.log(Level.INFO, "Checking-In with Master");
	}
	
	/**
	 * Sends the check-out packet to the MIX Master server
	 */
	public void masterCheckOut() {
		if (UDPacceptor == null)
			startUDP();
		DatagramConnector connector = new DatagramConnector();
        IoSession session = UDPacceptor.newSession(new InetSocketAddress(Utilities.getMasterAddress(config.game), Utilities.getMasterPort(config.game)), new InetSocketAddress(config.port));
        int capacity = 2;
        ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
        buffer.put((byte) 58);
        buffer.put((byte) 00);
        buffer.flip();
        session.write(buffer);
        session.close();
        
        logger.log(Level.INFO, "Checking-Out from Master");
	}
	
	public ServerConfiguration getServerConfig() {
		return config;
	}
	
	public int getPlayerCount() {
		return playerList.size();
	}
	
	public int getUsage20Min() {
		int usage = 0;
		for (int i = 0; i < 4; i++) // To get 20 minute history, add up the 5 minute histories
			usage += usageLast20Min[i];
		
		if (usage == 0) { // If no 20 minute history is available yet, use the last 5 minutes
			for (int i = 0; i < 5; i++)
				usage += usageLast5Min[i];
		}
		return usage;
	}
	
	public int getUsage1Hr() {
		int usage = 0;
		
		for (int i = 0; i < 3; i++) // To get 1 hour history, add up the 20 minute histories
			usage += usageLast1Hour[i];
		
		if (usage == 0)
			for (int i = 0; i < 4; i++) // If no 1 hour history yet, use the last 20 minutes
				usage += usageLast20Min[i];
		
		if (usage == 0)  // If no 20 minute history is available yet, use the last 5 minutes
			for (int i = 0; i < 5; i++)
				usage += usageLast5Min[i];
		
		return usage;
	}
	
	public int getUsage1Day() {
		int usage = 0;
		
		for (int i = 0; i < 24; i++)
			usage += usageLast1Day[i];
		
		if (usage == 0)
			for (int i = 0; i < 3; i++) // If no day history yet, use hourly history
				usage += usageLast1Hour[i];
		
		if (usage == 0)
			for (int i = 0; i < 4; i++) // If no 1 hour history yet, use the last 20 minutes
				usage += usageLast20Min[i];
		
		if (usage == 0)  // If no 20 minute history is available yet, use the last 5 minutes
			for (int i = 0; i < 5; i++)
				usage += usageLast5Min[i];
		
		return usage;
	}
	
	/**
	 * Adds a player to the online player list
	 * @param serNum
	 * @param player
	 */
	public void addPlayer(String serNum, Player player) {
		playerList.put(serNum, player);
		usageLast5Min[0] = playerList.size();
		
		if (config.publicServer) // update the player count the master server has for us
			masterCheckIn();
	}
	
	/**
	 * Removes a player from the online player list
	 * @param serNum
	 */
	public void removePlayer(String serNum) {
		// TODO Make player disappear to other players if needed
		playerList.remove(serNum);
	}
	
	/**
	 * Send msg to all serNum entries in playerList
	 * @param msg
	 */
	public void sendAll(String msg) {
		for (String s : playerList.keySet()) {
			playerList.get(s).getIoSession().write(msg);
		}
	}
	
	/**
	 * Send msg to all serNum entries in playerList except serNum
	 * @param serNum
	 * @param msg
	 */
	public void sendAllExcept(String serNum, String msg) {
		for (String s : playerList.keySet()) {
			if (!s.equals(serNum)) {
				playerList.get(s).getIoSession().write(msg);
			}
		}
	}
	
	/**
	 * Send msg to serNum
	 * @param serNum
	 * @param msg
	 */
	public void sendTo(String serNum, String msg) {
		playerList.get(serNum).getIoSession().write(msg);
	}
	
	public void sendTo(IoSession session, String msg) {
		session.write(msg);
	}
	
	public void sendServerMessage(String msg) {
		sendAll(":SR@M" + msg);
		logger.log(Level.INFO, "Server Message: " + msg);
	}
	
	public void sendServerMessageTo(String serNum, String msg) {
		sendTo(serNum, ":SR@M" + msg);
		logger.log(Level.INFO, "Server Message to " + serNum + ": " + msg);
	}
	
	public void sendServerMessageTo(IoSession session, String msg) {
		session.write(":SR@M" + msg);
		logger.log(Level.INFO, "Server Message: " + msg);
	}
	
	public void sendServerRules(IoSession session) {
		session.write(":SR$" + config.rules);
	}
	
	public String getSerNumList() {
		String s = "";
		for (String serNum : playerList.keySet()) {
			s = s + Utilities.stripLeadingZeroes(serNum) + ",";
			System.out.println(s);
		}
		return s;
	}
	
	public String getIntroPacketFor(String me) {
		String s = "";
		for (String serNum : playerList.keySet()) {
			if (!serNum.equals(me))
				s = s + Utilities.stripLeadingZeroes(playerList.get(serNum).getSerNum()) + "=" + Utilities.IPList.get(serNum) + ",";
		}
		return s;
	}
	
	public String getIntroPacket() {
		String s = "";
		for (String serNum : playerList.keySet()) {
				s = s + Utilities.stripLeadingZeroes(playerList.get(serNum).getSerNum()) + "=" + Utilities.IPList.get(serNum) + ",";
		}
		return s;
	}
	
	public String getFirstPlayer() {
		for (String serNum : playerList.keySet()) {
			return playerList.get(serNum).getSerNum();
		}
		return null;
	}

	@Override
	public void run() {
		int sum;
		while(true) {
			try {
				Thread.sleep(usageUpdateInterval);
				minPassed++; // Every minute, we update our count and shift the history of minutes by 1
				for (int i = 4; i > 0; i++)
					usageLast5Min[i] = usageLast5Min[i-1];
				
				if (minPassed == 5) { // Every 5 minutes, update our shorter history
					minPassed = 0;
					for (int i = 3; i > 0; i++)
						usageLast20Min[i] = usageLast20Min[i-1];
					
					sum = 0;
					for (int i = 0; i < 5; i++)
						sum += usageLast5Min[i];
					usageLast20Min[0] = sum;
					
					min5Passed++;
					if (min5Passed == 4) { // Every 20 minutes, update our 20 minute history
						min5Passed = 0;
						for (int i = 2; i > 0; i++)
							usageLast1Hour[i] = usageLast1Hour[i-1];
						
						sum = 0;
						for (int i = 0; i < 4; i++)
							sum += usageLast20Min[i];
						usageLast1Hour[0] = sum;
						
						min20Passed++;
						if (min20Passed == 3) { // Every hour, update our hourly history
							min20Passed = 0;
							for (int i = 24; i > 0; i++)
								usageLast1Day[i] = usageLast1Day[i-1];
							
							sum = 0;
							for (int i = 0; i < 2; i++)
								sum += usageLast1Hour[i];
							usageLast1Day[0] = sum;
							
							hourPassed++;
							if (hourPassed == 24) {
								hourPassed = 0;
							}
						}
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

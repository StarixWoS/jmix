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


public class Server {
	public MainGUI gui;
	private Logger logger;
	
	private CheckInTimer checkinTimer;
	
	private DatagramAcceptor UDPacceptor;
	
	private Map<String, Player> playerList;
	
	private ServerConfiguration config;
	
	public Server(ServerConfiguration c) {
		// Initialize the logger
		logger = JMIX.getLogger();
		
		// Create a HashMap to store a list of players;
        playerList = new HashMap<String, Player>();
		
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
		return 0;
	}
	
	public int getUsage1Hr() {
		return 0;
	}
	
	public int getUsage1Day() {
		return 0;
	}
	
	/**
	 * Adds a player to the online player list
	 * @param serNum
	 * @param player
	 */
	public void addPlayer(String serNum, Player player) {
		playerList.put(serNum, player);
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
				s = s + Utilities.stripLeadingZeroes(playerList.get(serNum).getSerNum()) + "=" + playerList.get(serNum).getRandHex() + ",";
		}
		return s;
	}
}

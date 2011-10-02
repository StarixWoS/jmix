import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;


public class UDPHandler extends IoHandlerAdapter {
	private Server server;
	private Logger logger;
	
	public UDPHandler(Server server) {
		this.server = server;
		logger = Logger.getLogger("JMIX");
	}
	
	public void sessionCreated(IoSession session) throws Exception {

	}  
	
	public void messageReceived(IoSession session, Object message) throws Exception {
		String msg = message.toString();
		logger.log(Level.INFO, "Received Packet: " + msg);
		ByteBuffer mg = (ByteBuffer) message;
		String s = new String(mg.array(), "US-ASCII");
		System.out.println(s);
		msg = mg.getString(Charset.forName("US-ASCII").newDecoder());
		logger.log(Level.INFO, "Received Packet2: " + msg);
		
		// Client Master Convos
		// d = Date from registry entry software/Synthetic-Reality/Network Settings/Date
		// ?alias=Breork,name=Royeu Iukan,email=unknown,loc=unknown,sernum=SOUL 2465,HHMM=57:15,d=16D2D9D8,v=071DFC29,w=826EB976<byte 00>
		// 02 02 24 a1 09 00 0c 00  c0 a8 01 6b 3f c5 40 4e
		// 01 04 00 00 02 00 24 00  45 f8 a5 7e 90 1f 00 00
		// 00 00 24 00 a6 a4 6e 4e  e7 04 00 00 02 00 24 00
		// 18 de bb 3a 9a 02 00 00  07 00 24 00 45 45 6c 14
		// 34 25 00 00 00 00 24 00  45 45 6c 14 38 25 00 00
		// 00 00 24 00 45 45 6c 14  37 25 00 00 00 00 24 00
		// 72 4d 7d dc 0f 27 00 00  01 00 24 00 18 9f 1c 8b
		// e8 fd 00 00 13 00 24 00
		// listing started at byte 13 (3f c5 40 4e 01 04 00 00 02 00 24 00)
		// first 4 bytes are ip address, next two flipped is port #
		// next 2 are always null(maybe for really big port numbers?)... then 1 (maybe 2?) bytes for # players online
		// then 24 00 at end (2nd to last appears to be the MIX server version - A1.24)
		// servers
		// 63.197.64.78 : 1025
		// 69.248.165.126 : 8080
		// 166.164.110.78 : 1255
		// 24.222.187.58 : 666
		// 69.69.108.20 : 9524
		// 69.69.108.20 : 9528
		// 69.69.108.20 : 9527
		// 114.77.125.220 : 9999
		// 24.159.28.139 : 65000
		
		
		// MIX Master conversations
		// !version=41252,nump=0,gameid=0,game=,host=Dell-Inspiron,id=77EBDE4D,port=2222,info=,name=MIX Server<byte 00>
		// No secrets here. If port=0, master uses whatever port server checked-in from. Not sure what this id is for though...
		// 58 00 (X)
		// Sent when "Public Server" checkbox is unticked. Removes us from the public list.
		// 4d 00 00 00 46 e7 92 36  ae 08 00 00 00 00 00 00
		// Sent by the master server after check-in. First 4 non-null bytes are our public IP. Next 2 get flipped and we get the port we listen to.
		// game= can be WoS... and what else? Can be left blank, and might default to WoS? W97, TOY, and RC
		// gameid= 0 appears to be WoS. Check for other games.
		
		
		// Client MIX Convos
		// Palias=Breork,name=Royeu Iukan,email=unknown,loc=unknown,sernum=SOUL 2465,HHMM=57:15,d=16D2D9D8,v=071DFC29,w=826EB976<byte 00>
		// #name=The Worlds End Server //Rules: World=WorldsEnd //ID:9E6B1A3B //TM:4A5C253B //US:1.1.26<byte 00>
		// TM is the time where the server is. (Re: Time Delta, hidden column on MIX server list)
		// ID is short for Admin ID. No idea where it comes from though. Mine is 76193DEA.
		// 51 00 (Q)
		// Requesting player list
		// Q4F4DBBA0,5F1621B2,<byte 00>
		// QCA1,CD5,C9C,8E2,9A6,B12,CD4,<byte 00>
		// List of online players>
		
		// A player sent us their info
		if (msg.substring(0,1).equals("P")) {
			Player newPlayer = new Player();
			String[] params = msg.substring(1,msg.length()).trim().split(",");
			for (String p : params) {
				String[] temp = p.split("=");
				String field = temp[0];
				String val = temp[1];
				if (field.startsWith("sernum"))
					newPlayer = Utilities.loadPlayerInfo(val);
			}
			for (String p : params) {
				String[] temp = p.split("=");
				String field = temp[0];
				String val = temp[1];
				if (field.startsWith("alias"))
					newPlayer.setAlias(val);
				else if (field.startsWith("name"))
					newPlayer.setName(val);
				else if (field.startsWith("email"))
					newPlayer.setEmail(val);
				else if (field.startsWith("loc"))
					newPlayer.setLoc(val);
				else if (field.startsWith("sernum")) {
					newPlayer.setSerNum(val);
					Utilities.IPList.put(val, Utilities.encodedIPAddress(session.getRemoteAddress().toString()));
				}
				else if (field.startsWith("HHMM"))
					newPlayer.setAge(val);
				else if (field.startsWith("d"))
					newPlayer.setD(val);
				else if (field.startsWith("v"))
					newPlayer.setV(val);
				else if (field.startsWith("w"))
					newPlayer.setW(val);
			}
			
			Utilities.savePlayerInfo(newPlayer);
			
			// Send our info in reply
			String response = "#name=" + server.getServerConfig().serverName + 
								" //Rules: " + server.getServerConfig().rules + 
								" //ID:" + Utilities.getAdminID() + 
								" //TM:" + Utilities.getTimeHex().toUpperCase() + 
								" //US:" + server.getUsage20Min() + "." +
											server.getUsage1Hr() + "." +
											server.getUsage1Day();
			int capacity = response.length() + 1;
            ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
            buffer.put(response.getBytes(Charset.forName("us-ascii")));
            buffer.put((byte) 00);
            buffer.flip();
			session.write(buffer);
			logger.log(Level.INFO, "Sent Packet: " + response);
		// A player wants a list of who is on this server
		} else if (msg.substring(0,1).equals("Q")) {
			String response = "Q" + server.getSerNumList();
			int capacity = response.length() + 1;
			ByteBuffer buffer = ByteBuffer.allocate(capacity, false);
			buffer.put(response.getBytes(Charset.forName("us-ascii")));
			buffer.put((byte) 00);
			buffer.flip();
			session.write(buffer);
			logger.log(Level.INFO, "Sent Packet: " + response);
		// Apparently a player wants to tell us what world they selected (if no world in server rules?)
		} else if (msg.substring(0,1).equals("G")) {
			server.getServerConfig().playerWorld = msg.substring(1);
		// The MIX Master is telling us our public ip and port
		} else if (msg.substring(0,1).equals("M")) {
			// TODO take this info out of the packet and put in a format we can use
		}
	}
	 
	public void sessionClosed(IoSession session) throws Exception {

	}
	
	public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		// t.printStackTrace();
	}
}

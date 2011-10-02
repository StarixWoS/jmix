import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.TransportType;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

public class ClientHandler extends IoHandlerAdapter {
	private Server server;
	private Logger logger;
	
	public ClientHandler(Server server) {
		this.server = server;
		logger = Logger.getLogger("JMIX");
	}
	
	public void messageReceived(IoSession session, Object msg) throws Exception {
		String line = msg.toString().trim();
		logger.log(Level.INFO, "Received Packet: " + line);
		Player player = null;
		
		if (line.startsWith(":SR")) {
			handleSR(line.substring(3), session);
		} else if (line.startsWith(":MIX")) {
			handleMIX(line.substring(4), session);
		}
	}
	
	/**
	 * Handles packets with a :SR prefix
	 * @param msg
	 * @param session
	 */
	public void handleSR(String msg, IoSession session) {
		Player player = null;
		
		// Check if the player already has a session (Player object) associated with them
		if (session.containsAttribute("player")) {
			player = (Player) session.getAttribute("player");
			// Do not let sneaky cheaters send modified SSVs, rules, or server messages directly
			if (!(msg.startsWith("@M") || msg.startsWith("@V") || msg.startsWith("$"))) {
				if (player.getWhisperSerNum().equals("")) {
					server.sendAllExcept(player.getSerNum(), ":SR" + msg);
				}
				else {
					server.sendTo(player.getWhisperSerNum(), ":SR" + msg);
					player.setWhisperSerNum("");
				}
			}
		} else {
			//Sign-in the player with the server, otherwise ignore them
			if (msg.startsWith("?")) {
				String firstPlayer = server.getFirstPlayer();
				String serNum = msg.substring(1, msg.length());
				if (serNum.length() < 8)
					while (serNum.length() < 8)
						serNum = "0" + serNum;
				if (serNum.length() > 8) {
					logger.log(Level.WARNING, ":SR? packet serNum too long");
				} else {
					player = Utilities.loadPlayerInfo(serNum);
					if (player.getSerNum() == null) {
						player.setSerNum(serNum);
					}
					session.setAttribute("player", player);
					player.setIoSession(session);
					server.addPlayer(player.getSerNum(), player);
					// Send :SR? packet to all players - prob not needed... try skipping sending to all
					// Send :SR!000009A11500000AEA to at least 1st player. Right after 9A1 is a number(player number to use?)
					if (!player.isRandHex())
						player.initRandHex();
					
					// :SR?63FD0466
					server.sendAllExcept(player.getSerNum(), ":SR" + msg);

					// :SR!63FD04660B000006FE
					if (firstPlayer != null)
						server.sendAll(":SR!" + player.getSerNum() + Utilities.padToLength(Integer.toHexString(server.getPlayerCount()), 2) + firstPlayer);
					
					/* Need to figure out how this works; player list with random id's seems to always
					 * get sent first thing, before player even fully connects. Doesn't seem to need to
					 * contain every single player either, so maybe this works in a way different than
					 * I originally thought
					// :SR@I530EBD3F=AD6B1B04,6D3=88358212,AEA=CF388F21,677=949F5923,974=B0C3990F,CDC=FDF066EE,7392C0C1=229BFA5B,8B6=D602C200,69A96075=2DE1BA5B,B6E=EB209805,6A0BB124=2759E719,3EE=C959529B,66C=461FA604,B4A=C81D5804,B66=5D61E2EE,9A1=9F158205,7FD0C147=DFDFCD05,
					// :SR@I9A1=9F158205,
					// Send list of online players to me
					server.sendTo(player.getSerNum(), ":SR@I" + server.getIntroPacketFor(player.getSerNum()));
					// Send my introduction to everyone
					server.sendAll(":SR@I" + Utilities.stripLeadingZeroes(player.getSerNum()) + "=" + player.getRandHex() + ",");
					//server.sendAllExcept(player.getSerNum(), ":SR?" + player.getSerNum());
					 */
				}
			}
		}
	}
	
	/**
	 * Handles packets with a :MIX prefix
	 * @param msg
	 * @param session
	 */
	public void handleMIX(String msg, IoSession session) {
		Player player = null;
		
		// Check if the player already has a session (Player object) associated with them
		if (session.containsAttribute("player")) {
			player = (Player) session.getAttribute("player");
			// 3rd number after mix appears to be some form of player id num used by WoS.. could be which player num you are on server
			// TODO :MIX001000009A11A sent alot when in camp scenes; followed by soul id of camp owner; perhaps redirection packet for next thing said
			// See if a similar packet gets sent when we leave
			// TODO MIX3 - see if it gets sent on join server("Play Game")
			// TODO ID 8(quite possibly requesting the var): <worldName>,<catName>,<varName>, ?
			// TODO 9 - <world name>,<section name>,<variable name>,<string> ?
			// TODO :SR@V<sernum_that_changed_it><worldName>,<sectionName>,<variableName>,<value> ?
			if (msg.startsWith(":MIX4")) {
				// Next packet goes to the specified soul id
				player.setWhisperSerNum(msg.substring(7, 15));
			} else if (msg.startsWith(":MIX5")) {
				// This is a comment - :MIX501000009A1(Whoj): hi33
				logger.log(Level.INFO, "Comment: " + msg.substring(15));
				// Message from admin to player - :SR@M<message from server/admin> 
				server.sendServerMessageTo(player.getSerNum(), "You're such a big noob.");
			} else if (msg.startsWith(":MIX6")) {
				// This is an admin cmd - :MIX601000009A1123 msg 1234 hiF8
				// pass cmd target/all msg
				String[] admincmd = msg.substring(15).substring(0, msg.length() - 17).split(" ");
				logger.log(Level.INFO, "[ADMIN]pass=" + admincmd[0] + " cmd=" + admincmd[1] + " target=" + admincmd[2] + " msg=" + admincmd[3]);
			} else if (msg.startsWith(":MIX8")) {
				server.sendTo(player.getSerNum(), Utilities.getSSV("world", "cat", "var"));
			}
		}
	}
	
	public void sessionCreated(IoSession session) throws Exception {
		logger.log(Level.INFO, "Session created");
		
		if (session.getTransportType() == TransportType.SOCKET)
			((SocketSessionConfig) session.getConfig())
			.setReceiveBufferSize(2048);
		// 2048, 8192
		server.sendServerMessageTo(session, server.getServerConfig().motd);
		server.sendTo(session, ":SR@I" + server.getIntroPacket());
		server.sendServerRules(session);
		session.setIdleTime(IdleStatus.BOTH_IDLE, 10);
	}
	
	public void sessionClosed(IoSession session) throws Exception {
		logger.log(Level.INFO, "Session closed");
		
		super.sessionClosed(session);
		Player player = null;
		if (session.containsAttribute("player")) {
			player = (Player) session.getAttribute("player");
			logger.log(Level.INFO, "Soul ID " + player.getSerNum() + " disconnected");
			server.removePlayer(player.getSerNum());
		}
		session.close();
	}
	
	public void exceptionCaught(IoSession session, Throwable t) throws Exception {
		logger.log(Level.INFO, "Session closed");
		Player player = null;
		t.printStackTrace();
		if (session.containsAttribute("player")) {
			player = (Player) session.getAttribute("player");
			logger.log(Level.INFO, "Soul ID " + player.getSerNum() + " disconnected");
			server.removePlayer(player.getSerNum());
		}
		session.close();
	}
}

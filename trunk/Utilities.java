import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Utilities {
	private static final String updateMasterURL = "http://synthetic-reality.com/synreal.ini";
	private static final String versionA = "1.10";
	private static final int version = 41252;
	private static Map<String, String> MASTER_ADDRESS = new HashMap<String,String>();
	private static Map<String, Integer> MASTER_PORT = new HashMap<String, Integer>();
	private static boolean allowPublic = true;
	// os.arch/name/version user.name
	private static String host = System.getProperty("os.arch");
	private static String adminID = "76193DEA";
	public static Map<String, SSVWorld> SSVList;
	
	static {
		MASTER_ADDRESS.put("WoS", "63.197.64.78");
		MASTER_PORT.put("WoS", 23999);
		
		java.net.InetAddress address;
		try {
			address = java.net.InetAddress.getLocalHost();
			java.net.NetworkInterface ni = java.net.NetworkInterface.getByInetAddress(address);
			host = System.getProperty("os.arch") + "-" + ni.getDisplayName();
			byte[] mac = ni.getHardwareAddress();
			//adminID = getHexString(mac).substring(4).toUpperCase();
		} catch (Exception e) {
			host = System.getProperty("os.arch");
			//adminID = "FFFFFF";
		}
		
		adminID = createAdminID();
	}
	
	// Function from: http://rgagnon.com/javadetails/java-0596.html
	public static String getHexString(byte[] b) throws Exception {
		  String result = "";
		  for (int i=0; i < b.length; i++) {
		    result +=
		          Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		  }
		  return result;
		}
	
	// I believe this is what the actual server uses when generating a new ID.
	// Should find where it saves it to on Win, or save the one we generate on *nix
	public static String createAdminID() {
		java.util.Random test = new java.util.Random(System.currentTimeMillis());
		int id = test.nextInt();
		id = id << 4;
		id = id ^ test.nextInt();
		id = id << 4;
		
		int temp = test.nextInt();
		temp = temp << 10;
		
		id = id ^ temp;
		
		id = id ^ test.nextInt();
		id = id << 4;
		temp = test.nextInt();
		temp = temp << 10;
		id = id ^ temp;
		
		id = id ^ test.nextInt();
		return Integer.toHexString(id).toUpperCase();
	}
	
	public static String stripLeadingZeroes(String s) {
		while (s.startsWith("0")) {
			s = s.substring(1);
		}
		return s;
	}
	
	public static int getGameID(String game) {
		// TODO make sure these game IDs are correct
		if (game.equals("WoS"))
			return 0;
		else if (game.equals("W97"))
			return 1;
		else if (game.equals("TOY"))
			return 2;
		else if (game.equals("RC"))
			return 3;
		return 0;
	}
	
	public static String getTimeHex() {
		return Integer.toHexString((int) (System.currentTimeMillis() / 1000L));
	}
	
	public static String getHost() {
		return host;
	}
	
	public static String getAdminID() {
		return adminID;
	}
	
	public static void savePlayerInfo(Player info) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "Serializing player to " + info.getSerNum() + ".dat");
		
		try { 
			FileOutputStream fos = new FileOutputStream("players" + System.getProperty("file.separator") + info.getSerNum() + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(info);
			oos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Player loadPlayerInfo(String serNum) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "Deserializing player from" + serNum + ".dat");
		
		Player info;
		info = new Player();
		
		try {
			FileInputStream fis = new FileInputStream("players" + System.getProperty("file.separator") + serNum + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			info = (Player) ois.readObject();
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return info;
	}
	
	public static void loadSSVs(String world) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "Loading SSVs for world " + world);
		
		try {
			FileInputStream fis = new FileInputStream("worlds" + System.getProperty("file.separator") + world + ".dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			SSVList.put(world, (SSVWorld) ois.readObject());
			ois.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void unloadSSVs(String world) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "Unloading SSVs for world " + world);
		SSVList.remove(world);
	}
	
	public static void saveSSVs(String world) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "Saving SSVs for world " + world);
		
		try { 
			FileOutputStream fos = new FileOutputStream("worlds" + System.getProperty("file.separator") + world + ".dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(SSVList.get(world));
			oos.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getSSV(String world, String cat, String var) {
		return SSVList.get(world).get(cat, var);
	}
	
	public static void setSSV(String world, String cat, String var, String val) {
		SSVList.get(world).put(cat, var, val);
	}
	
	public static boolean allowPublic() {
		return allowPublic;
	}
	
	public static String getVersionMIX() {
		return versionA;
	}
	
	public static int getVersionNumMIX() {
		return version;
	}
	
	/**
	 * Returns master server address
	 * @return
	 */
	public static String getMasterAddress(String game) {
		return MASTER_ADDRESS.get(game);
	}
	
	/**
	 * Returns master server port
	 * @return
	 */
	public static int getMasterPort(String game) {
		return MASTER_PORT.get(game);
	}
	
	/**
	 * Updates the master server info
	 * from synthetic-reality.com/synreal.ini
	 */
	public static void updateMaster(String game) {
		Logger logger = JMIX.getLogger();
		logger.log(Level.INFO, "START UPDATE MASTER");
		String section = "";
		URL synreal;
		InputStream is = null;
		try {
		synreal = new URL(updateMasterURL);
		BufferedReader br;
		String line;
			is = synreal.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			
			while ((line = br.readLine()) != null) {
				if (line.startsWith("[") && line.endsWith("]")) {
					section = line.substring(1, line.length() - 1);
				}
				if (section.equals("MIX")) {
					if (line.startsWith("currVersion=")){
						if (!versionA.equals(line.substring(12))) {
							allowPublic = false;
							logger.log(Level.WARNING, "Version Mismatch. Cannot be public server.");
						}
					}
				} else if (section.equals(game)) {
					if (line.startsWith("master=")) {
						line = line.substring(7);
						String[] temp = line.split(":");
						MASTER_ADDRESS.put(game, temp[0]);
						MASTER_PORT.put(game, Integer.valueOf(temp[1]));
						logger.log(Level.INFO, "Master Info Updated: " + MASTER_ADDRESS.get(game) + ":" + MASTER_PORT.get(game));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
		logger.log(Level.INFO, "END UPDATE MASTER");
	}
}

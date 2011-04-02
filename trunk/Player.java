import java.io.Serializable;

import org.apache.mina.common.IoSession;

public class Player implements Serializable {
	private String m_sernum;
	private String m_alias;
	private String m_name;
	private String m_email;
	private String m_loc;
	private String m_age;
	private String m_d;
	private String m_v;
	private String m_w;
	private IoSession m_session;
	
	private int numP = 1;
	private String randHex = "FFFFFFFF";
	
	private String m_whisperSerNum = "";
	
	public Player() {
		
	}
	
	public Player(String serNum) {
		setSerNum(serNum);
	}
	
	public void setIoSession(IoSession session) {
		m_session = session;
	}
	
	public IoSession getIoSession() {
		return m_session;
	}
	
	public void setSerNum(String serNum) {
		m_sernum = serNum;
	}
	
	public String getSerNum() {
		return m_sernum;
	}

	public void setAlias(String m_alias) {
		this.m_alias = m_alias;
	}

	public String getAlias() {
		return m_alias;
	}

	public void setName(String m_name) {
		this.m_name = m_name;
	}

	public String getName() {
		return m_name;
	}

	public void setEmail(String m_email) {
		this.m_email = m_email;
	}

	public String getEmail() {
		return m_email;
	}

	public void setLoc(String m_loc) {
		this.m_loc = m_loc;
	}

	public String getLoc() {
		return m_loc;
	}

	public void setAge(String m_age) {
		this.m_age = m_age;
	}

	public String getAge() {
		return m_age;
	}

	public void setD(String m_d) {
		this.m_d = m_d;
	}

	public String getD() {
		return m_d;
	}

	public void setV(String m_v) {
		this.m_v = m_v;
	}

	public String getV() {
		return m_v;
	}

	public void setW(String m_w) {
		this.m_w = m_w;
	}

	public String getW() {
		return m_w;
	}

	public boolean isWhisper() {
		return m_whisperSerNum != null ? true : false;
	}
	
	public void setWhisperSerNum(String m_whisperSerNum) {
		this.m_whisperSerNum = m_whisperSerNum;
	}

	public String getWhisperSerNum() {
		return m_whisperSerNum;
	}
	
	public boolean isRandHex() {
		return randHex.equals("FFFFFFFF") ? false : true;
	}
	
	public void initRandHex() {
		randHex = Long.toHexString((long) Math.random() * Long.decode("0XFFFFFFFF"));
	}
	
	public String getRandHex() {
		return randHex;
	}
}

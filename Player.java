import java.io.Serializable;
import java.util.ArrayList;

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
	
	private ArrayList<Player> m_subscribers;
	private String m_subscribedTo = "";
	
	private int numP = 1;
	
	private String m_whisperSerNum = "";
	
	private boolean m_sendNextToSubscribers = false;
	
	public Player() {
		m_subscribers = new ArrayList<Player>();
	}
	
	public Player(String serNum) {
		m_subscribers = new ArrayList<Player>();
		setSerNum(serNum);
	}
	
	public String getSubscribedTo() {
		return m_subscribedTo;
	}
	
	public void setSubscribedTo(String s) {
		m_subscribedTo = s;
	}
	
	public void subscribeTo(Player p) {
		if (!m_subscribers.contains(p))
			m_subscribers.add(p);
	}
	
	public void unsubscribeFrom(Player p) {
		while (m_subscribers.contains(p))
			m_subscribers.remove(p);
	}
	
	public void sendToSubscribers(String s) {
		m_sendNextToSubscribers = false;
		try {
			for (Player p : m_subscribers) {
				p.getIoSession().write(s);
			}
		} catch (NullPointerException e) {
			m_subscribers = new ArrayList<Player>();
		}
	}
	
	public void setSendToSubscribers() {
		m_sendNextToSubscribers = true;
	}
	
	public boolean isSendToSubscribers() {
		return m_sendNextToSubscribers;
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
}

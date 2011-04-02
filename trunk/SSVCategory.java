import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SSVCategory implements Serializable {
	private Map<String, String> vars;
	
	public SSVCategory() {
		vars = new HashMap<String, String>();
	}
	
	public void put(String var, String val) {
		vars.put(var, val);
	}
	
	public String get(String var) {
			return vars.get(var);
		
	}
}

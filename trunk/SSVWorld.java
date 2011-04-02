import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class SSVWorld implements Serializable {
	private Map<String, SSVCategory> cats;
	
	public SSVWorld() {
		cats = new HashMap<String, SSVCategory>();
	}
	
	public void put(String cat, String var, String val) {
		if (cats.containsKey(cat))
			cats.get(cat).put(var, val);
		else
		{
			SSVCategory newCat = new SSVCategory();
			newCat.put(var, val);
			cats.put(cat, newCat);
		}
	}
	
	public String get(String cat, String var) {
		String val = cats.get(cat).get(var);
		if (val != null)
			return cats.get(cat).get(var);
		else
			return "";
	}
}

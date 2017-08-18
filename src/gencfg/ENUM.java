package gencfg;

import gencfg.data.Data;
import org.w3c.dom.Element;

import java.util.*;

public final class ENUM {
	private final static HashMap<String, ENUM> enums = new HashMap<>();
	public static boolean isEnum(String name) {
		return enums.containsKey(name);
	}
	
	public static ENUM get(String name) {
		return enums.get(name);
	}
	
	public static void put(String name, ENUM e) {
		enums.put(name, e);
	}
	
	public static Collection<ENUM> getExports() {
		return enums.values();
	}
	
	public String getName() {
		return name;
	}

	public String getFullname() {
		return fullname;
	}
	
	public HashMap<String, Integer> getCases() {
		return cases;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	private final String namespace;
	private final String name;
	private final HashMap<String, Integer> cases = new LinkedHashMap<>();
	private final HashMap<String, String> aliass = new HashMap<>();
	private final HashSet<String> groups = new HashSet<>();

	private final String fullname;
	public ENUM(String namespace, Element ele) {
		this.namespace = namespace;
		name = ele.getAttribute("name");
		check(!name.isEmpty(), "enum name can't be empty");
		this.fullname = Utils.combineFullName(namespace, name);
		check(!Utils.existType(fullname), "name duplicate");
		put(fullname, this);
		
		final String NULL = Data.NULL_STR.toUpperCase() ;
		cases.put(NULL, Data.NULL_VALUE);
		aliass.put(Data.NULL_STR, NULL);
		aliass.put(NULL, NULL);
		int enumValue = 0;
		for(Element c : Utils.getChildsByTagName(ele, "const")) {
			final String cname = c.getAttribute("name");
			final String strValue = c.getAttribute("value");
			if(!strValue.isEmpty()) {
				enumValue = Integer.parseInt(c.getAttribute("value"));
			}
			check(cases.put(cname, enumValue) == null, String.format("const:%s duplicate!", cname));
			enumValue++;
			aliass.put(cname, cname);
			for(String aliasName : Utils.split(c, "alias")) {
				check(aliass.put(aliasName, cname) == null, String.format("enum const alias<%s, %s> duplicate!", cname, aliasName));
			}
		}
		groups.addAll(Arrays.asList(Utils.split(ele, "group")));
		Utils.verbose("%s", this);
	}

	@Override
	public String toString() {
		return String.format("enum{name:%s cases:%s alias:%s}", fullname, cases, aliass);
	}
	
	public int getEnumValueByName(String name) {
		final String cname = aliass.get(name);
		check(cname != null, "const:" + name + " unknown");
		return cases.get(cname);
	}
	
	public String getDefaultConstName() {
		return cases.isEmpty() ? "" : cases.keySet().iterator().next();
	}

	public void check(boolean cond, String msg) {
		if(!cond)
			error(msg);
	}

	public void error(String err) {
		Utils.error("enum:%s %s", fullname, err);
	}
}

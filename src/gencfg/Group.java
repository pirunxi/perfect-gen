package gencfg;

import org.w3c.dom.Element;

import java.util.Collections;
import java.util.HashSet;

public final class Group {
	// group 集合
	public final static HashSet<String> groups = new HashSet<>();
	
	static {
		groups.add("all");
	}
	
	public static void load(Element data) {
		Collections.addAll(groups, Utils.split(data, "name"));
	}
	
	public static boolean isGroup(String name) {
		return groups.contains(name);
	}
}

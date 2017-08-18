package gencfg;

import gencfg.type.TBean;
import gencfg.type.TList;
import gencfg.type.TType;
import org.w3c.dom.Element;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Field {
	private final Bean host;
	private final String name;
	private final String fullType;
	private final HashSet<String> indexs = new HashSet<>();
	private final HashSet<String> groups = new HashSet<>();

	private final Map<String, String> attrs = new HashMap<>();

	private final static HashSet<String> ReserveNames = new HashSet<>(Arrays.asList("end", "base", "super", "typeid", "type", "friend", "for", "do"));
	private final static Pattern namePattern = Pattern.compile("[a-z]\\w*");

	TType type;

	public Field(Bean host, Element ele) {
		this.host = host;
		this.name = ele.getAttribute("name");
		check(!this.name.isEmpty(), "name missing");

		final Matcher matcher = namePattern.matcher(name);
		check(!ReserveNames.contains(name),"reserved name:" + name);
		check(matcher.matches(), "invalid name:" + name);

		this.fullType = ele.getAttribute("type");
		check(!this.fullType.isEmpty(), "type missing");

		Collections.addAll(this.groups, Utils.split(ele, "group"));
		check(!this.groups.isEmpty(), "group missing");

		Collections.addAll(this.indexs, Utils.split(ele, "index"));

		addAttr("index", ele);
		addAttr("localized", ele);
		addAttr("delimiter", ele);
		addAttr("ref", ele);
		addAttr("refpath", ele);
	}

	private void addAttr(String attrName, Element ele) {
		this.attrs.put(attrName, ele.getAttribute(attrName));
	}

	public TType getTType() {
		return type;
	}


	public void check(boolean cond, String msg) {
		if(!cond)
			Utils.error("bean:%s field:%s %s", host.getFullname(), name, msg);
	}

	public String getName() {
		return name;
	}


	
	public final HashSet<String> getGroups() {
		return groups;
	}


	public final boolean checkInGroup(String exportGroup) {
		return Utils.checkInGroup(groups, exportGroup);
	}

	public final HashSet<String> getIndexs() {
		return indexs;
	}

	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field{name=").append(name);
		sb.append(",fulltype:").append(fullType);
		sb.append(",index:").append(indexs);
		sb.append(",attrs:").append(attrs);
		sb.append(",group=").append(groups).append(",");
		sb.append("}}");
		return sb.toString();
	}

	
	public void verifyDefine() {
		type = TType.create(host.getNamespace(), fullType, attrs);
		type.verify();

		for(String g : groups) {
			check(Group.isGroup(g), "unknown group:" + g);
		}

		if(type instanceof TList) {
			TList ltype = (TList)type;
			final TType vtype = ltype.getValueType();
			if(vtype.isBean()) {
				Bean bean = ((TBean)vtype).getBean();
				for(String idxName : indexs) {
					check(bean.getField(idxName) != null, "index:" + idxName + " unknown");
				}
			}
		}
	}

}

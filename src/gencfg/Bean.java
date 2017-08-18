package gencfg;

import gencfg.type.TList;
import gencfg.type.TMap;
import gencfg.type.TType;
import org.w3c.dom.Element;

import java.util.*;
import java.util.stream.Collectors;

public final class Bean {
	private final static HashMap<String, Bean> beans = new HashMap<>();
	
	public static Bean get(String name) {
		return beans.get(name);
	}
	
	public static Bean add(Bean bean) {
		return beans.put(bean.fullname, bean);
	}
	
	public static HashMap<String, Bean> getBeans() {
		return beans;
	}
	
	public static Collection<Bean> getExports() {
		return beans.values();//.stream().filter(s -> s.inGroup(Main.exportGroup)).collect(Collectors.toList());
	}

	private final int typeid;
	private final String namespace;
	private final String name;
	private final String fullname;
	private final ArrayList<Field> fields = new ArrayList<>();
	private final ArrayList<Field> selfAndParentFields = new ArrayList<>();
	private final List<Bean> children = new ArrayList<>();
	private final HashSet<String> groups = new HashSet<>();
	
	private final static HashSet<Integer> typeids = new HashSet<>();
    private final String delimiter;

    private Bean parent;
	
	public Bean(String namespace, Element data)  {
		this(namespace, data, null);
	}
	
	public Bean(String namespace, Element data, Bean parent) {
		this.namespace = namespace;
		name = data.getAttribute("name");
		check(!name.isEmpty(), "name is empty");
		this.fullname = Utils.combineFullName(namespace, name);
		check(!Utils.existType(fullname), "duplicate");
		int newTypeid = this.fullname.hashCode();
		while(!typeids.add(newTypeid))
			newTypeid++;
		this.typeid = newTypeid;
		this.parent = parent;


		groups.addAll(Arrays.asList(Utils.split(data, "group")));

		for(Element ele : Utils.getChildsByTagName(data, "field")) {
			fields.add(new Field(this, ele));
		}
		if(parent != null)
			selfAndParentFields.addAll(parent.selfAndParentFields);
		selfAndParentFields.addAll(fields);

		for(Element ele : Utils.getChildsByTagName(data, "bean")) {
			Bean c = new Bean(namespace, ele, this);
			if(c.isDynamic()) {
				children.addAll(c.getChildren());
			} else {
				children.add(c);
			}
		}

		Utils.foreach(data, (tag, ele) -> {
			switch (tag) {
				case "field" :
				case "bean" : break;
				default : error("element:" + tag + " 未知");
			}
		});
        this.delimiter = Utils.getAttrOrDefualt(data, "delimiter", null);

		add(this);
		Utils.verbose("%s", this);
	}
	
	public String getFullname() {
		return fullname;
	}
	
	public String getName() {
		return name;
	}
	
	public int getTypeId() {
		return typeid;
	}
	
	public boolean isDynamic() {
		return !children.isEmpty();
	}

	public Bean getParent() {
		return parent;
	}

	public List<Bean> getChildren() {
		return children;
	}
	
	public ArrayList<Field> getFields() {
		return fields;
	}

	public ArrayList<Field> getSelfAndParentFields() {
		return selfAndParentFields;
	}

    public String getdelimiter() {
        return delimiter;
    }

    public boolean isCompound() {
        return !delimiter.isEmpty();
    }
	
	public Field getField(String name) {
		return selfAndParentFields.stream().filter(f -> f.getName().equals(name)).findAny().orElse(null);
	}
	
	public final String getNamespace() {
		return namespace;
	}
	
	public boolean isDeriveFrom(Bean ancestor) {
		return ancestor.children.contains(fullname);
	}
	
	public final boolean inGroup(String exportGroup) {
		return Utils.checkInGroup(groups, exportGroup);
	}

	public Set<String> getRefStructs() {
		final Set<String> refs = new HashSet<>();
		for(Field f : fields) {
			TType ftype = f.getTType();
			if(ftype.isBean()) {
				refs.add(ftype.getBoxType());
			} else if(ftype instanceof TList) {
				TList ltype = (TList) ftype;
				TType vtype = ltype.getValueType();
				if (vtype.isBean())
					refs.add(ftype.getBoxType());
			} else if(ftype instanceof TMap) {
				TMap ltype = (TMap) ftype;
				TType ktype = ltype.getKeyType();
				if(ktype.isBean())
					refs.add(ktype.getBoxType());
				TType vtype = ltype.getValueType();
				if (vtype.isBean())
					refs.add(ftype.getBoxType());
			}
		}
		return refs;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("struct{name=").append(fullname);
		if(parent != null)
			sb.append(",parent=").append(parent.getFullname());
		sb.append(",delimiter:").append(delimiter);
		sb.append(",fields:").append(fields);
		sb.append(",children:").append(children.stream().map(c -> c.fullname).collect(Collectors.toList()));
		sb.append("}");
		return sb.toString();
	}

	public void check(boolean cond, String msg) {
		if(!cond)
			error(msg);
	}

	public void error(String err) {
		throw new RuntimeException("struct:" + name + " err:" + err);
	}
	
	public void verityDefine() {
		HashSet<String> fnames = new HashSet<String>();
		for(Field f : selfAndParentFields) {
			check(fnames.add(f.getName()), "field duplicate:" + f.getName());
		}

		fields.forEach(Field::verifyDefine);

		for(String g : groups) {
			check(Group.isGroup(g), "group:" + g + " unknown");
		}
	}

}

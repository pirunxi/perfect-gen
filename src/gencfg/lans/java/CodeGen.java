package gencfg.lans.java;

import gencfg.*;
import gencfg.type.TBean;
import gencfg.type.TList;
import gencfg.type.TMap;
import gencfg.type.TType;
import gendb.Consts;

import java.util.*;
import java.util.stream.Collectors;

public class CodeGen {

	public void gen() {
		Bean.getExports().forEach(this::genBean);
		ENUM.getExports().forEach(this::genEnum);
		Collection<Table> tables = Table.getExportTables();
//		tables.forEach(this::genTable);
		genTables(tables);
	}

	private void genEnum(ENUM e) {
		final ArrayList<String> ls = new ArrayList<>();
		final String namespace = e.getNamespace();
		ls.add("package $namespace;");
		final String name = e.getName();
		ls.add("public enum $name {");

		ls.add(e.getCases().entrySet().stream().map(ee -> ee.getKey() + "(" + ee.getValue() + ")").collect(Collectors.joining(",\n", "", ";")));
		ls.add("private final int num;");
		ls.add("$name(int num) { this.num = num; }");
		ls.add("public int value() { return num; }");
		ls.add("public static $name valueOf(int num) { for($name e : $name.values()) if(e.num == num) return e; throw new RuntimeException(num + \" isn't valid $name value\");}");
		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		Utils.saveByNamespace(e.getFullname(), ls, replacer);
	}


	private void genBean(Bean bean) {
		final ArrayList<String> ls = new ArrayList<>();
		final String namespace = bean.getNamespace();
		ls.add("package $namespace;");
		
		final Bean parent = bean.getParent();
		final String name = bean.getName();
		final boolean isDynamic = bean.isDynamic() ;
		ls.add(String.format("public %s class $name extends %s {", (isDynamic ? "abstract" : "final"), (parent == null ? "cfg.CfgObject" : parent.getFullname())));
		
		if(!isDynamic) {
			ls.add(String.format("	public final static int TYPEID = %s;", bean.getTypeId()));
			ls.add("	final public int getTypeId() { return TYPEID; }");
		}

		for(Field f : bean.getFields()) {
			if(!f.checkInGroup(Main.exportGroup)) continue;
			TType ftype = f.getTType();
			ls.add(String.format("public final %s %s;", ftype.getNotBoxType(), f.getName()));

			if(ftype instanceof TList) {
				TList ltype = (TList)ftype;
				if(ltype.getIndexs() != null) {
					for(String idx : ltype.getIndexs()) {
						TBean vtype = (TBean) ltype.getValueType();
						TType itype = vtype.getBean().getField(idx).getTType();
						TMap mtype = new TMap(itype, vtype, Collections.emptyMap());
						ls.add(String.format("public final %s %s_%s = new %s();", mtype.getBoxType(), f.getName(), idx, mtype.getBoxType()));
					}
				}
			}
		}

		ls.add(String.format("	public %s(cfg.DataStream fs) {", name));
		if(parent != null) {
			ls.add("		super(fs);");
		}
		
		for(Field f : bean.getFields()) {
			if(!f.checkInGroup(Main.exportGroup)) continue;
			TType ftype = f.getTType();
			final String fname = f.getName();
			ls.add(ftype.unmarshalJava(fname, "fs"));

			if(ftype instanceof TList) {
				TList ltype = (TList)ftype;
				if(ltype.getIndexs() != null) {
					ls.add(String.format("for(%s _v : this.%s) {", ltype.getValueType().getBoxType(), fname));
					for(String idx : ltype.getIndexs()) {
						ls.add(String.format("this.%s_%s.put(_v.%s, _v);", fname, idx, idx));
					}
					ls.add("}");
				}
			}
		}
		
		ls.add("	}");

		Table table = Table.get(bean.getName());
		if(table != null)
			genTable(ls, table);
		
		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		replacer.put("$datastream", Consts.DATASTREAM);
		Utils.saveByNamespace(bean.getFullname(), ls, replacer);
	}

	private void genTable(ArrayList<String> ls, Table table) {
		TType type = table.getActualType();
		ls.add(String.format("private static %s _values;", type.getBoxType()));
		ls.add("public static void load(String dataPath) {");
		ls.add("final java.util.List<$datastream> dss = $datastream.records(dataPath, \"utf8\");");
		switch (table.getMode()) {
			case "map" : {
				TMap mtype = (TMap)type;
				TBean vtype = (TBean)mtype.getValueType();
				ls.add(String.format("final %s temp = new %s();", type.getBoxType(), type.getBoxType()));
				ls.add(String.format("for($datastream ds : dss) { %s _v = %s; temp.put(_v.%s, _v); }",
						vtype.getNotBoxType(), vtype.unmarshalJava("ds"), vtype.getBean().getFields().get(0).getName()));
				ls.add("_values = temp;");
				break;
			}
			case "list": {
				TList mtype = (TList)type;
				TType vtype = mtype.getValueType();
				ls.add(String.format("final %s temp = new %s();", type.getBoxType(), type.getBoxType()));
				ls.add(String.format("for($datastream ds : dss) { temp.add(%s); }",
						vtype.unmarshalJava("ds")));
				ls.add("_values = temp;");
				break;
			}
			case "one" : {
				ls.add("if(dss.size() != 1) throw new RuntimeException(\"table one should have only one record.\");");
				ls.add(String.format("_values = %s;", type.unmarshalJava("dss.get(0)")));
				break;
			}
			default:
				Utils.error("table:%s unknown mode:%s", table.getFullname(), table.getMode());
		}
		ls.add("}");

		switch (table.getMode()) {
			case "map" : {
				TMap mtype = (TMap)type;
				TBean vtype = (TBean)mtype.getValueType();
				if(vtype.getBean().isDynamic()) {
					ls.add(String.format("@SuppressWarnings(\"unchecked\") public static <T extends %s> T get(%s key) { return (T)_values.get(key); }", vtype.getBoxType(), mtype.getKeyType().getNotBoxType()));
				} else {
					ls.add(String.format("public static %s get(%s key) { return _values.get(key); }", vtype.getBoxType(), mtype.getKeyType().getNotBoxType()));
				}
				ls.add(String.format("public static %s values() { return _values; }", mtype.getBoxType()));
				break;
			}
			case "list": {
				TList mtype = (TList)type;
				TType vtype = mtype.getValueType();
				if(vtype.isBean() && ((TBean)vtype).getBean().isDynamic()) {
					ls.add(String.format("@SuppressWarnings(\"unchecked\") public static <T extends %s> T get(int index) { return (T)_values.get(index); }",
							vtype.getBoxType()));
				} else {
					ls.add(String.format("public static %s get(int index) { return _values.get(index); }", vtype.getBoxType()));
				}
				ls.add(String.format("public static %s values() { return _values; }", mtype.getBoxType()));
				break;
			}
			case "one" : {
				if(type.isBean() && ((TBean)type).getBean().isDynamic()) {
					ls.add(String.format("@SuppressWarnings(\"unchecked\") public static <T extends %s> T get() { return (T)_values; }",
							type.getBoxType()));
				} else {
					ls.add(String.format("public static %s get() { return _values; }", type.getBoxType()));
				}
				break;
			}
			default:
				Utils.error("table:%s unknown mode:%s", table.getFullname(), table.getMode());
		}
	}

	private void genTables(Collection<Table> tables) {
		final ArrayList<String> ls = new ArrayList<>();
		String fullname = Main.tablesName;
		int lastIndexOfDot = fullname.lastIndexOf('.');
		String name = fullname.substring(lastIndexOfDot + 1, fullname.length());
		String namespace = fullname.substring(0, lastIndexOfDot);

		ls.add("package $namespace;");
		ls.add("public final class $name {");
		ls.add("private static String dataDir; public static void setDataDir(String dir) { dataDir = dir; }");
		ls.add("public static void load() {");
		for(Table table : tables) {
			ls.add(String.format("%s.load(dataDir + \"/%s\");", table.getFullname(), table.getOutputFile()));
		}
		ls.add("}");

		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		Utils.saveByNamespace(fullname, ls, replacer);
	}
}

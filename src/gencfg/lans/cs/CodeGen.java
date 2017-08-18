package gencfg.lans.cs;

import gencfg.*;
import gencfg.type.TBean;
import gencfg.type.TList;
import gencfg.type.TMap;
import gencfg.type.TType;
import gendb.Consts;

import java.util.*;

public class CodeGen {

	public void gen() {
		Utils.suffix = "cs";
		ENUM.getExports().forEach(this::genEnum);
		Bean.getExports().forEach(this::genBean);
		Collection<Table> tables = Table.getExportTables();
		genTables(tables);
	}

	private void genEnum(ENUM e) {
		final ArrayList<String> ls = new ArrayList<>();
		final String namespace = e.getNamespace();
		ls.add("namespace $namespace{");
		final String name = e.getName();
		ls.add("public enum $name {");
		for(Map.Entry<String, Integer> c : e.getCases().entrySet()) {
			ls.add(String.format("%s = %s,", c.getKey(), c.getValue()));
		}
		ls.add("}");
		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		Utils.saveByNamespace(e.getFullname(), ls, replacer);
	}




	private void genBean(Bean bean) {
		final ArrayList<String> ls = new ArrayList<>();
		final String namespace = bean.getNamespace();
		ls.add("namespace $namespace{");

		final Bean parent = bean.getParent();
		final String name = bean.getName();
		final boolean isDynamic = bean.isDynamic() ;
		ls.add(String.format("public %s class $name : %s {", (isDynamic ? "abstract" : "sealed"), (parent == null ? "cfg.CfgObject" : parent.getFullname())));

		if(!isDynamic) {
			ls.add(String.format("public const int TYPEID = %s;", bean.getTypeId()));
			ls.add("public override int GetTypeId() { return TYPEID; }");
		}

		for(Field f : bean.getFields()) {
			if(!f.checkInGroup(Main.exportGroup)) continue;
			TType ftype = f.getTType();
			ls.add(String.format("public readonly %s %s;", ftype.getCsType(), f.getName()));

			if(ftype instanceof TList) {
				TList ltype = (TList)ftype;
				if(ltype.getIndexs() != null) {
					for(String idx : ltype.getIndexs()) {
						TBean vtype = (TBean) ltype.getValueType();
						TType itype = vtype.getBean().getField(idx).getTType();
						TMap mtype = new TMap(itype, vtype, Collections.emptyMap());
						ls.add(String.format("public readonly %s %s_%s = new %s();", mtype.getCsType(), f.getName(), idx, mtype.getCsType()));
					}
				}
			}
		}

		ls.add(String.format("public %s(cfg.DataStream fs) %s {", name, (parent != null ? ": base(fs)" : "")));

		for(Field f : bean.getFields()) {
			if(!f.checkInGroup(Main.exportGroup)) continue;
			TType ftype = f.getTType();
			final String fname = f.getName();
			ls.add(ftype.unmarshalCs(fname, "fs"));

			if(ftype instanceof TList) {
				TList ltype = (TList)ftype;
				if(ltype.getIndexs() != null) {
					ls.add(String.format("foreach(var _v in this.%s) {", fname));
					for(String idx : ltype.getIndexs()) {
						ls.add(String.format("this.%s_%s.Add(_v.%s, _v);", fname, idx, idx));
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
		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		replacer.put("$datastream", Consts.DATASTREAM);
		Utils.saveByNamespace(bean.getFullname(), ls, replacer);
	}

	private void genTable(final ArrayList<String> ls, Table table) {

		TType type = table.getActualType();
		ls.add(String.format("private static %s _values;", type.getCsType()));
		ls.add("public static void Load(string dataPath) {");
		ls.add("var dss = $datastream.Records(dataPath, \"utf8\");");
		switch (table.getMode()) {
			case "map" : {
				TMap mtype = (TMap)type;
				TBean vtype = (TBean)mtype.getValueType();
				ls.add(String.format("var temp = new %s();", type.getCsType()));
				ls.add(String.format("foreach(var ds in dss) { var _v = %s; temp.Add(_v.%s, _v); }",
						vtype.unmarshalCs("ds"), vtype.getBean().getFields().get(0).getName()));
				ls.add("_values = temp;");
				break;
			}
			case "list": {
				TList mtype = (TList)type;
				TType vtype = mtype.getValueType();
				ls.add(String.format("var temp = new %s();", type.getCsType()));
				ls.add(String.format("foreach(var ds in dss) { temp.Add(%s); }",
						vtype.unmarshalCs("ds")));
				ls.add("_values = temp;");
				break;
			}
			case "one" : {
				ls.add("if(dss.Count != 1) throw new System.Exception(\"table one should have only one record.\");");
				ls.add(String.format("_values = %s;", type.unmarshalCs("dss[0]")));
				break;
			}
			default:
				Utils.error("table:%s unknown mode:%s", table.getFullname(), table.getMode());
		}
		ls.add("}");

		switch (table.getMode()) {
			case "map" : {
				TMap mtype = (TMap)type;
				TType ktype = mtype.getKeyType();
				TBean vtype = (TBean)mtype.getValueType();
				if(vtype.getBean().isDynamic()) {
					ls.add(String.format("public static T Get<T>(%s key) where T : %s { %s value; return _values.TryGetValue(key, out value) ? (T)value : null; }", ktype.getCsType(), vtype.getCsType(), vtype.getCsType()));
				} else {
					ls.add(String.format("public static %s Get(%s key) { %s value; return _values.TryGetValue(key, out value) ? value : null; }", vtype.getCsType(), ktype.getCsType(), vtype.getCsType()));
				}
				ls.add(String.format("public static %s values() { return _values; }", mtype.getCsType()));
				break;
			}
			case "list": {
				TList mtype = (TList)type;
				TType vtype = mtype.getValueType();
				if(vtype.isBean() && ((TBean)vtype).getBean().isDynamic()) {
					ls.add(String.format("public static T Get<T>(int index) where T : %s { return (T)_values[index]; }",
							vtype.getCsType()));
				} else {
					ls.add(String.format("public static %s Get(int index) { return _values.get(index); }", vtype.getCsType()));
				}
				ls.add(String.format("public static %s Values() { return _values; }", mtype.getCsType()));
				break;
			}
			case "one" : {
				if(type.isBean() && ((TBean)type).getBean().isDynamic()) {
					ls.add(String.format("public static T Get<T>() where T : %s { return (T)_values; }",
							type.getCsType()));
				} else {
					ls.add(String.format("public static %s Get() { return _values; }", type.getCsType()));
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

		ls.add("namespace $namespace{");
		ls.add("public sealed class $name {");
		ls.add("public static string DataDir { get; set; }");
		ls.add("public static void Load() {");
		for(Table table : tables) {
			ls.add(String.format("%s.Load(DataDir + \"/%s\");", table.getFullname(), table.getOutputFile()));
		}
		ls.add("}");
		ls.add("}");
		ls.add("}");

		Map<String, String> replacer = new HashMap<>();
		replacer.put("$name", name);
		replacer.put("$namespace", namespace);
		Utils.saveByNamespace(fullname, ls, replacer);
	}

	/*
	public void genMarshallCode() {
		genEnumXmlMarshal(ENUM.getExports());
		Bean.getExports().forEach(s -> genStructXmlMarshallCode(s));
	}
	
	
	public String toMarshalType(String type) {
		int idx = type.lastIndexOf('.');
		return idx < 0 ? type : xmlPrefix + type;
	}
	
	private final static String xmlPrefix = "xml.";
	private final static String enumClass = "Enums";

	public String toXmlJavaType(String rawType) {
		return ENUM.isEnum(rawType) ? "string" : rawType;
	}
	void genEnumXmlMarshal(Collection<ENUM> enums) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace = xmlPrefix + "cfg";
		ls.add("using System.Collections.Generic;");
		ls.add("namespace " + namespace + "{");
		final String name = enumClass;
		ls.add(String.format("public sealed class %s {", name));
		for(ENUM e : enums) {
			ls.add(String.format("public static List<string> %s = new List<string>{%s};",
				e.getName(), e.getCases().keySet().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","))));
		}
		ls.add("}");
		ls.add("}");
		final String code = ls.stream().collect(Collectors.joining("\n"));
		final String outFile = String.format("%s/%s.%s.cs", Main.csmarshalcodeDir, namespace, name);
		Utils.save(outFile, code);
	}
	
	String getNamespaceOfType(String type) {
		int idx = type.lastIndexOf('.');
		return idx < 0 ? type : type.substring(0, idx);
	}
	
	String upperFirstChar(String type) {
		return type.substring(0, 1).toUpperCase() + type.substring(1);
	}
	
	String readXmlType(String os, String type) {
		final String marshalType = toMarshalType(type);
		switch(type) {
			case "bool":
			case "int":
			case "long":
			case "float":
			case "string":
			 return String.format("Read%s(%s)", upperFirstChar(type), os);
			default: {
				Bean bean = Bean.get(type);
				return bean.isDynamic() ?
					String.format("ReadDynamicObject<%s>(%s, \"%s\")", marshalType, os, getNamespaceOfType(marshalType))
				:   String.format("ReadObject<%s>(%s, \"%s\")", marshalType, os, marshalType);
			}
		}
	}

	String getRawTypeDefaultValue(String type) {
		switch (type) {
			case "bool": return "false";
			case "int":
			case "long": return "0";
			case "float": return "0f";
			case "string": return "\"\"";
		}
		throw new RuntimeException("unknow rawtype:" + type);
	}

	private void genStructXmlMarshallCode(Bean bean) {
		final ArrayList<String> ls = new ArrayList<String>();
		final String namespace =  xmlPrefix + bean.getNamespace();
		ls.add("using System;");
		ls.add("namespace " + namespace + " {");
		
		final String base = bean.getParentTypeFullName();
		final String name = bean.getName();
		final boolean isDynamic = bean.isDynamic();
		ls.add(String.format("public %s class %s %s {", isDynamic ? "abstract" : "partial", name, (base.isEmpty() ? ": xml.cfg.XmlMarshaller" : ": " + toMarshalType(base))));
		
		genStructConsts(bean, ls);
		
		final ArrayList<String> ds = new ArrayList<String>();
		final ArrayList<String> ws = new ArrayList<String>();
		final ArrayList<String> rs = new ArrayList<String>();
		
		final String VAR1 = "_1";
		final String VAR2 = "_2";
		final String VAR3 = "_3";
		ws.add(String.format("public override void Write(System.IO.TextWriter %s) {", VAR1));
		rs.add(String.format("public override void Read(System.Xml.XmlNode %s) {", VAR1));
		if(!base.isEmpty()) {
			ws.add(String.format("base.Write(%s);", VAR1));
			rs.add(String.format("base.Read(%s);", VAR1));
		}
		
		rs.add(String.format("foreach (System.Xml.XmlNode %s in GetChilds(%s))", VAR2, VAR1));
		rs.add("{");
		rs.add(String.format("switch(%s.Name)", VAR2));
		rs.add("{");
		
		for(Field f : bean.getFields()) {
			String ftype = f.getType();
			String jtype = toXmlJavaType(ftype);
			final String fname = f.getName();
			final List<String> ftypes = f.getTypes();
			ws.add(String.format("Write(%s, \"%s\", this.%s);", VAR1, fname, fname));
			if (f.isRaw()) {
				ds.add(String.format("public %s %s = %s;", jtype, fname, getRawTypeDefaultValue(ftype)));
				rs.add(String.format("case \"%s\": this.%s = %s; break;", fname, fname, readXmlType(VAR2, jtype)));
			} else if (f.isStruct()) {
				ds.add(String.format("public %s %s;", toMarshalType(jtype), fname));
				rs.add(String.format("case \"%s\": this.%s = %s; break;", fname, fname, readXmlType(VAR2, jtype)));
			} else if(f.isEnum()) {
				final ENUM e = ENUM.get(ftype);
				ds.add(String.format("public string %s = \"%s\";", fname, e.getDefaultConstName()));
				rs.add(String.format("case \"%s\": this.%s = %s; break;", fname, fname, readXmlType(VAR2, "string")));
				ws.add(String.format("if(!%scfg.%s.%s.Contains(this.%s)) throw new Exception(\"%s.%s=\" + this.%s + \" isn't valid enum value\");", 
					xmlPrefix, enumClass, e.getName(), fname, e.getName(), fname, fname));
			} else if (f.isContainer()) {
				switch (ftype) {
				case "list": {
					final String valueType = toXmlJavaType(ftypes.get(1));
					ds.add(String.format(
							"public readonly System.Collections.Generic.List<%s> %s = new System.Collections.Generic.List<%s>();",
							toMarshalType(valueType), fname, toMarshalType(valueType)));
					rs.add(String.format("case \"%s\": GetChilds(%s).ForEach(%s => this.%s.Add(%s)); break;",
						fname, VAR2, VAR3, fname, readXmlType(VAR3, valueType)));
					break;
				}
				case "set": {
					final String valueType = toXmlJavaType(ftypes.get(1));
					ds.add(String.format(
							"public readonly System.Collections.Generic.HashSet<%s> %s = new System.Collections.Generic.HashSet<%s>();",
							toMarshalType(valueType), fname, toMarshalType(valueType)));
					rs.add(String.format("case \"%s\": GetChilds(%s).ForEach(%s => this.%s.Add(%s)); break;",
							fname, VAR2, VAR3, fname, readXmlType(VAR3, valueType)));
					break;
				}
				case "map": {
					final String keyType = toXmlJavaType(ftypes.get(1));
					final String valueType = toXmlJavaType(ftypes.get(2));
					ds.add(String.format(
							"public readonly System.Collections.Generic.Dictionary<%s, %s> %s = new System.Collections.Generic.Dictionary<%s, %s>();",
							toMarshalType(keyType), toMarshalType(valueType), fname, toMarshalType(keyType),
							toMarshalType(valueType)));
					rs.add(String.format("case \"%s\": GetChilds(%s).ForEach(%s => this.%s.Add(%s, %s)); break;",
							fname, VAR2, VAR3, fname,
							readXmlType(String.format("GetOnlyChild(%s, \"key\")", VAR3), keyType), 
							readXmlType(String.format("GetOnlyChild(%s, \"value\")", VAR3), valueType)));
					break;
				}
				}
			} else {
				throw new RuntimeException("unknown type:" + jtype);
			}
		}
		
		ws.add("}");
		rs.add("}");
		rs.add("}");
		rs.add("}");
		ls.addAll(ds);
		ls.addAll(ws);
		ls.addAll(rs);
		
		ls.add("}");
		ls.add("}");
		
		final String code = ls.stream().collect(Collectors.joining("\n"));
		//Main.println(code);
		final String outFile = String.format("%s/%s.%s.cs", Main.csmarshalcodeDir, namespace, name);
		Utils.save(outFile, code);
	}
*/
}

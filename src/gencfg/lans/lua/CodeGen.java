package gencfg.lans.lua;

import gencfg.*;
import gencfg.type.TType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGen {

	public void gen() {
		genEnums(ENUM.getExports());
		genBeans(Bean.getExports());
		genTables(Table.getExportTables());
	}

	private void genEnums(Collection<ENUM> enums) {
		final ArrayList<String> ls = new ArrayList<>();
		ls.add("return {");
		for(ENUM e : enums) {
			ls.add(String.format("[\"%s\"] = {", e.getFullname()));
			for(Map.Entry<String, Integer> c : e.getCases().entrySet()) {
				ls.add(String.format("%s = %s,", c.getKey(), c.getValue()));
			}
			ls.add("},");
		}
		ls.add("}");

		final String outFile = Utils.combine(Main.codeDir, "enums.lua");
		Utils.save(outFile, ls);
	}

	private void genBeans(Collection<Bean> beans) {
		final ArrayList<String> ls = new ArrayList<>();
		ls.add("return {");

		for(Bean b : beans) {
			ls.add("{");
			ls.add(String.format("name = '%s',", b.getFullname()));
			if(b.getParent() != null) {
				ls.add(String.format("parent = '%s',", b.getParent().getFullname()));
			}
			if(!b.getChildren().isEmpty()) {
				ls.add(String.format("children={%s},", b.getChildren().stream().map(c -> "'" + c.getFullname() + "'").collect(Collectors.joining(","))));
			}
			ls.add("fields={");
			for(Field e : b.getFields()) {
				TType ftype = e.getTType();
				if(!e.checkInGroup(Main.exportGroup)) continue;
				ls.add(String.format("{name = '%s', %s, %s},", e.getName(), ftype.luaTypeDefine(), ftype.luaIndexsDefine()));
			}
			ls.add("}");
			ls.add("},");
		}

		ls.add("}");

		final String outFile = Utils.combine(Main.codeDir, "beans.lua");
		Utils.save(outFile, ls);
	}

	private void genTables(Collection<Table> tables) {
		final ArrayList<String> ls = new ArrayList<>();
		ls.add("return {");
		for(Table table : tables) {
			if(!table.inGroup(Main.exportGroup)) continue;
			if(table.getMode().equals("map")) {
				ls.add(String.format("{name='%s', type='%s', mode='%s', file='%s', key='%s'},", table.getName(), table.getFullname(), table.getMode(), table.getOutputFile(), Bean.get(table.getFullname()).getSelfAndParentFields().get(0).getName()));
			} else {
				ls.add(String.format("{name='%s', type='%s', mode='%s', file='%s'},", table.getName(), table.getFullname(), table.getMode(), table.getOutputFile()));
			}
		}
		ls.add("}");

		final String outFile = Utils.combine(Main.codeDir, "tables.lua");
		Utils.save(outFile, ls);
	}
}

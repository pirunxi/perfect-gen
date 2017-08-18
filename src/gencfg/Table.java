package gencfg;


import gencfg.data.Data;
import gencfg.data.DataVisitor;
import gencfg.data.FBean;
import gencfg.type.TBean;
import gencfg.type.TList;
import gencfg.type.TMap;
import gencfg.type.TType;
import org.w3c.dom.Element;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2017/5/22.
 */
public class Table {
    private final static Map<String, Table> tables = new TreeMap<>();

    public static Map<String, Table> getTables() {
        return tables;
    }

    public static void add(Table table) {
        String simpleName = table.getName().toLowerCase();
        Table old = tables.put(simpleName, table);
        if(old != null)
            Utils.error("table:%s simplename:%s duplicate with table:%s", table.fullname, simpleName, old.fullname);
    }

    public static Table get(String name) {
        return tables.get(name.toLowerCase());
    }

    private final String namespace;
    private final String name;
    private final String fullname;
    private final String relateDir;
    private final String[] inputFiles;
    private final String outputFile;
    private final Set<String> groups = new HashSet<>();

    private final String mode; // map,list,one
    private final boolean oneRecordPerCsvLine; // 一行一个记录,只对csv生效在

    private static List<String> validLines = Arrays.asList("true", "false", "");
    private final List<Data> datas = new ArrayList<>();
    private final Map<Data, Data> indexDatas = new HashMap<>();

    private TBean vtype;
    private TType actualType;

    public Table(String namespace, Element data, String relateDir) {
        this.namespace = namespace;

        this.name = data.getAttribute("name");

        if(name.isEmpty())
            Utils.error("table name is missing");

        this.fullname = Utils.combineFullName(namespace, name);
        add(this);


        this.mode = Utils.getAttrOrDefualt(data, "mode", "map");

        String lineStr = data.getAttribute("line");
        check(validLines.contains(lineStr), "line:" + lineStr + " unknown");
        this.oneRecordPerCsvLine = lineStr.equalsIgnoreCase("true");
        this.relateDir = relateDir;

        this.groups.addAll(Arrays.asList(Utils.split(data, "group")));
        check(!this.groups.isEmpty(), "group is empty");

        this.inputFiles = Utils.split(data, "input");
        check(this.inputFiles.length > 0, "input is missing!");
        this.outputFile = fullname.toLowerCase() + ".data";

        Utils.verbose("%s", this);
    }

    @Override
    public String toString() {
        return String.format("table{name:%s input:%s output:%s mode:%s group:%s}",
                fullname, Arrays.asList(inputFiles), outputFile, mode, groups);
    }

    private void check(boolean cond, String msg) {
        if(!cond)
            error(msg);
    }

    private void error(String str) {
        throw new RuntimeException("table:" + fullname + " " + str);
    }

    public String getName() {
        return name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFullname() {
        return fullname;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getMode() {
        return mode;
    }

    public TType getActualType() {
        return actualType;
    }

    public TType getValueType() {
        return vtype;
    }

    public void verifyDefine() {
        if(name.isEmpty()) {
            throw new RuntimeException("Config name is missing");
        }

        TType valueType = TType.create(namespace, name, new HashMap<>());
        check(valueType.isBean(), "type:" + fullname + "must be bean:");
        this.vtype = (TBean)valueType;
        check(!this.vtype.getBean().getSelfAndParentFields().isEmpty(), "bean:" + fullname + " is table' value type. can't be empty!");
        for(String g : groups) {
            check(Group.isGroup(g), "group:" + g + " unknown");
        }
        switch (mode) {
            case "map": {
                List<Field> fields = vtype.getBean().getSelfAndParentFields();
                check(!fields.isEmpty(), "need key field");
                TType ktype = fields.get(0).getTType();
                check(ktype.isBasic(), "key must be basic type");
                actualType = new TMap(ktype, vtype, Collections.emptyMap());
                break;
            }
            case "list": {
                actualType = new TList(vtype, Collections.emptyMap());
                break;
            }
            case "one": {
                actualType = vtype;
                break;
            }
            default:
                check(false, "mode:" + mode + " unknown");
        }
    }

    private void collectFiles(String fileName, TreeMap<String, File> files) {
        final File file = new File(fileName);
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                final String subName = f.getName();
                String[] tokens = subName.split("[\\\\|/]");
                String selfName = tokens[tokens.length - 1];
                if(selfName.startsWith(".") || selfName.startsWith("~")) continue;
                if(f.isDirectory()) {
                    collectFiles(subName, files);
                } else {
                    files.put(subName, f);
                }
            }
        } else {
            files.put(fileName, file);
        }
    }

    public void loadData() throws Exception {
        for (String fileName : inputFiles) {
            String fullPath = Utils.combine(relateDir, fileName);
                final File file = new File(fullPath);
                if(file.isDirectory()) {
                    final TreeMap<String, File> subFiles = new TreeMap<>();
                    collectFiles(fullPath, subFiles);
                    for (File f : subFiles.values()) {
                        datas.add(vtype.loadOneRecord(f));
                    }
                } else {
                    vtype.loadMultiRecord(file, datas, oneRecordPerCsvLine);
                }
        }
        if(mode.equals("map")) {
            for(Data data : datas) {
                Data key = ((FBean)data).getIndexField(0);
                check(indexDatas.put(key, data) == null, "key:"  + key + " duplicate");
            }
        }
    }

    public boolean existKey(Data key) {
        return indexDatas.containsKey(key);
    }

    public boolean inGroup(String exportGroup) {
        return Utils.checkInGroup(groups, exportGroup);
    }

    public static List<Table> getExportTables() {
        return tables.values().stream().filter(c -> c.inGroup(Main.exportGroup)).collect(Collectors.toList());
    }

    public void save(String exportGroup) {
        if(!inGroup(exportGroup)) return;
        String result = datas.stream().map(d -> {
            DataVisitor visitor = new DataVisitor(exportGroup);
            d.accept(visitor);
            return visitor.toData();
        }).collect(Collectors.joining("\n"));
        Utils.save(Utils.combine(Main.dataDir, outputFile), result);
    }

    public void verifyData() {
        Main.setCurVerifyTable(this);
        for(Data data : datas) {
            Main.setCurVerifyData(data);
            data.verify();
        }
        switch (mode) {
            case "one" : check(datas.size() == 1, "mode:one should have only one record."); break;
        }
    }
}

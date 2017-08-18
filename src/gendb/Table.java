package gendb;

import gendb.type.TType;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Table {
    private final static LinkedHashMap<String, Table> tables = new LinkedHashMap<>();

    public static Map<String, Table> getTable() {
        return tables;
    }

    public static void add(Table table) {
        if(tables.put(table.getFullname(), table) != null)
            throw new RuntimeException("table:" + table.getFullname() + " duplicate");
    }

    public static Table getTable(String tableName) {
        return tables.get(tableName.toLowerCase());
    }

    private final String name;
    private final String namespace;
    private final String fullname;
    private final String keyType;
    private final String valueType;

    private TType ktype;
    private TType vtype;
    private final boolean isPersist;
    public Table(String namespace, Element ele) {
        this.namespace = namespace;
        this.name = ele.getAttribute("name");
        if(this.name.isEmpty())
            throw new RuntimeException("Table name missing");
        this.fullname = Utils.combineFullName(namespace, name);
        this.keyType = ele.getAttribute("key");
        this.valueType = ele.getAttribute("value");

        final String persistStr = ele.getAttribute("persistent").toLowerCase();
        switch (persistStr) {
            case "":
            case "true":
                this.isPersist = true;
                break;
            case "false":
                this.isPersist = false;
                break;
                default:
                    throw new RuntimeException("table:" + this.name + " unknown persisttype:" + persistStr);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getFullname() {
        return fullname;
    }

    public String getLowercasename() {
        return this.name.toLowerCase();
    }

    public String getNamespace() {
        return namespace;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getValueType() {
        return valueType;
    }

    public boolean isPersist() {
        return isPersist;
    }

    public TType getKtype() {
        return ktype;
    }

    public TType getVtype() {
        return vtype;
    }

    private final static List<String> keyTypes = Arrays.asList("long", "string", "int", "binary");
    public void init() {
        if(!keyTypes.contains(keyType))
            throw new RuntimeException("table:" + name + " key type:" + keyType + " should be " + keyTypes);
        ktype = TType.create(namespace, keyType);
        vtype = TType.create(namespace, valueType);
    }

    @Override
    public String toString() {
        return String.format("Table{name=%s,key=%s,value=%s,persistant=%s}", name, keyType, valueType, isPersist);
    }
}

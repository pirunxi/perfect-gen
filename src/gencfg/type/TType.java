package gencfg.type;

import gencfg.*;
import gencfg.data.Data;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Element;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public abstract class TType {
    protected final Map<String, String> attrs;
    private Table[] refs;
    private String[] refPaths;
    private String[] indexs;
    private String delimiter;
    public TType(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public Map<String, String> getAttrs() {
        return attrs;
    }

    public String getAttr(String attr) {
        String value = attrs.get(attr);
        return value != null && !value.isEmpty() ? value : null;
    }

    public Table[] getRefs() {
        return refs;
    }

    public String[] getRefPaths() {
        return refPaths;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public String[] getIndexs() {
        return indexs;
    }

    public void verify() {
        String sref = getAttr("ref");
        if(sref != null) {
            String[] srefs = sref.split(":");
            refs = new Table[srefs.length];
            for(int i = 0 ; i < srefs.length ; i++) {
                Table table = Table.get(srefs[i]);
                check(table != null, "ref:" + srefs[i] + " not exist");
                refs[i] = table;
            }
        }
        String spath = getAttr("refpath");
        if(spath != null) {
            refPaths = spath.split("\\|");
        }

        String sindex = getAttr("index");
        if(sindex != null) {
            check(this instanceof TList, String.format("type:%s can't has attr <index:%s>", getBoxType(), sindex));
            TList ltype = (TList)this;
            check(ltype.getValueType().isBean(), String.format("list's value type:%s can't index", ltype.getValueType().getBoxType()));
            indexs = sindex.split(":");
            Bean vtype = ((TBean)ltype.getValueType()).getBean();
            for(String idx : indexs) {
                check(vtype.getField(idx) != null, String.format("index:%s must be bean:%s's field", idx, vtype.getFullname()));
            }
        }

        delimiter = getAttr("delimiter");
        if(delimiter == null && (this instanceof TBean)) {
            delimiter = ((TBean)this).getBean().getdelimiter();
        }
    }

    public boolean isBasic() { return true; }
    public boolean isEnum() { return false; }
    public boolean isBean() { return false; }
    public boolean isContainer() { return false; }
    public boolean isLocalized() { return "true".equals(attrs.get("localized")); }

    public abstract String getNotBoxType();
    public abstract String getBoxType();
    public String unmarshalJava(String fieldName, String fsName) {
        return "this." + fieldName + " = " + unmarshalJava(fsName) + ";";
    }
    public abstract String unmarshalJava(String fsName);


    public abstract String luaDefine();
    public String luaTypeDefine() {
        return String.format("type='%s'", luaDefine());
    }
    public String luaIndexsDefine() {
        if(indexs == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("index={");
        sb.append(Stream.of(indexs).map(s -> "'" + s + "'").collect(Collectors.joining(",")));
        sb.append("}");
        return sb.toString();
    }

    public abstract String getCsType();
    public String unmarshalCs(String fieldName, String fsName) {
        return "this." + fieldName + " = " + unmarshalCs(fsName) + ";";
    }
    public abstract String unmarshalCs(String fsName);

    public abstract Data newData(FlatStream fs);
    public abstract Data newData(Element ele);
    public abstract Data newData(LuaValue lua);


    public static TType create(String namespace, String type, Map<String, String> attrs) {
        String[] types = type.split(":");
        final String baseType = types[0];
        switch (baseType) {
            case "bool" :
            case "boolean": check(types, 1, type); return new TBool(attrs);
            case "int" : check(types, 1, type); return new TInt(attrs);
            case "long" : check(types, 1, type); return new TLong(attrs);
            case "float" : check(types, 1, type); return new TFloat(attrs);
            case "double" : check(types, 1, type); return new TDouble(attrs);
            case "string" : check(types, 1, type); return new TString(attrs);
            case "list" :
            case "vector": check(types, 2, type); return new TList(create(namespace, types[1], attrs), attrs);
            case "set" : check(types, 2, type); return new TSet(create(namespace, types[1], attrs), attrs);
            case "map" : check(types, 3, type); {
                Map<String, String> keyAttrs = new HashMap<>();
                Map<String, String> valueAttrs = new HashMap<>();
                for(Map.Entry<String, String> e : attrs.entrySet()) {
                    String[] strs = e.getValue().split(":");
                    keyAttrs.put(e.getKey(), strs.length > 0 ? strs[0] : "");
                    valueAttrs.put(e.getKey(), strs.length > 1 ? strs[1] : "");
                }
                return new TMap(create(namespace, types[1], keyAttrs), create(namespace, types[2], valueAttrs), attrs);
            }
            default:
                check(types, 1, type);
                ENUM e = ENUM.get(baseType);
                if(e == null) {
                    e = ENUM.get(Utils.combineFullName(namespace, baseType));
                }
                if(e != null)
                    return new TEnum(e, attrs);

                Bean bean = Bean.get(baseType);
                if(bean == null) {
                    bean = Bean.get(Utils.combineFullName(namespace, baseType));
                }
                if(bean == null)
                    Utils.error("unknown type:" + baseType + " , namespace:" + namespace);
                return new TBean(bean, attrs);
        }
    }

    @SuppressWarnings("unchecked")
    public Data loadOneRecord(File file) {
        Data data;
        try {
            final Object content = Utils.parseAsXmlOrLuaOrFlatStream(file.getAbsolutePath());
            if(content instanceof org.w3c.dom.Element) {
                return newData((org.w3c.dom.Element)content);
            } else if(content instanceof List){
                final FlatStream is = new FlatStream((List<List<String>>) content);
                data = newData(is);
                if (!is.isStreamEnd())
                    Utils.error("file:%s has some unread data.", file.toString());
            } else {
                data = newData((LuaTable)content);
            }
            Utils.verbose("file:%s data:%s", file, data);
            return data;
        } catch (Exception e) {
            System.out.printf("load file:%s type:%s fail.\n", file, getBoxType());
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public void loadMultiRecord(File file, List<Data> datas, boolean oneRecordPerCsvLine) throws Exception {
        try {
            final Object content = Utils.parseAsXmlOrLuaOrFlatStream(file.getAbsolutePath());
            if (content instanceof org.w3c.dom.Element) {
                Utils.foreach((Element) content, (tag, ele) -> {
                    switch (tag) {
                        case "item":
                            Data data = newData(ele);
                            Utils.verbose("file:%s data:%s", file, data);
                            datas.add(data);
                        default:
                            Utils.error("file:%s type:%s unknown tag:%s", file, getBoxType(), tag);
                    }
                });
            } else if (content instanceof List) {
                final FlatStream is = new FlatStream((List<List<String>>) content);
                while (!is.isSectionEnd()) {
                    if(oneRecordPerCsvLine) {
                        FlatStream line = is.getLine();
                        Data data = newData(line);
                        Utils.verbose("file:%s data:%s", file, data);
                        if (!line.isStreamEnd())
                            Utils.error("file:%s has some unread data.%n line   ==> %s remain ==> %s", file, line, line.getLine());
                        datas.add(data);
                    } else {
                        Data data = newData(is);
                        Utils.verbose("file:%s data:%s", file, data);
                        datas.add(data);
                    }
                }
            } else {
                LuaTable luaTable = (LuaTable) content;
                for (int i = 1, n = luaTable.rawlen(); i <= n; i++) {
                    Data data = newData(luaTable.rawget(i));
                    Utils.verbose("file:%s data:%s", file, data);
                    datas.add(data);
                }
            }
        } catch (Exception e) {
            System.out.printf("load file:%s type:%s fail.\n", file, getBoxType());
            throw e;
        }
    }

    protected static void check(String[] types, int size, String fullType) {
        if(types.length != size)
            Utils.error("type:%s is invalid.", fullType);
    }

    public void check(boolean cond, String err) {
        if(!cond)
            Utils.error("%s", err);
    }
}

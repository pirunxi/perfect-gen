package genmsg.type;

import genmsg.Bean;
import genmsg.Utils;

import java.util.Collections;
import java.util.Set;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public abstract class TType {
    public final static String COLLECTION = "perfect.txn.collections";
    public abstract boolean hasSetter();
    public abstract boolean maybeNull();
    public Set<String> collectDependenceBeans() { return Collections.emptySet(); }

    public abstract String getNotBoxType();
    public abstract String getBoxType();
    public abstract String getDefaultDataValue();

    public boolean isBean() { return false; }

    public abstract String marshalJava(String osname, String varname);
    public abstract String unmarshalJava(String osname, String varname);
    public abstract String getUnmarshalMethodJava(String os);

    public abstract String luaDefine();
    public String luaTypeDefine() {
        return String.format("type='%s'", luaDefine());
    }

    public static TType create(String namespace, String type) {
        String[] types = type.split(":");
        final String baseType = types[0];
        switch (baseType) {
            case "bool" :
            case "boolean": return new TBool();
            case "int" : return new TInt();
            case "long" : return new TLong();
            case "float" : return new TFloat();
            case "double" : return new TDouble();
            case "string" : return new TString();
            case "binary":
            case "octets" : return new TBinary();
            case "list" :
            case "vector": return new TList(create(namespace, types[1]));
            case "set" : return new TSet(create(namespace, types[1]));
            case "map" : return new TMap(create(namespace, types[1]), create(namespace, types[2]));
            default:
                Bean bean = Bean.getBean(baseType);
                if(bean == null) {
                    bean = Bean.getBean(Utils.combineFullName(namespace, baseType));
                }
                if(bean == null)
                    throw new RuntimeException("unknown type:" + baseType + " , namespace:" + namespace);
                return new TBean(bean);
        }
    }
}

package gendb.type;

import gendb.Bean;
import gendb.Utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public abstract class TType {
    public final static String COLLECTION = "perfect.txn.collections";
    public abstract boolean hasSetter();
    public abstract boolean maybeNull();
    public abstract String getInterfaceMethodReturnType();
    public abstract String getApplyInterfaceType();

    public abstract String getBoxType();
    public abstract String getDefaultDataValue();
    public String getDefaultBeanValue() {
        return getDefaultDataValue();
    }

    public boolean isBean() { return false; }
    public abstract String getTagid();
    public abstract String marshal(String osname, String varname);
    public abstract String unmarshal(String osname, String varname);
    public abstract String getUnmarshalMethod(String os);
    public String getNewKeyMethod() {
        throw new UnsupportedOperationException();
    }

    public String dataVariableName(String name) { return "_" + name; }

    public String toCopyBean(String varname) {
        if(hasSetter()) {
            return varname;
        } else {
            return varname + ".copy()";
        }
    }

    public String toNoTransactionCopyBean(String varname) {
        if(hasSetter()) {
            return varname;
        } else {
            return varname + ".noTransactionCopy()";
        }
    }

    public boolean isPredictable() { return true; }
    public List<TType> getCompatibleTypes() { return Collections.emptyList(); }


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

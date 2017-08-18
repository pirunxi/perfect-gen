package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TMap extends TType {
    private final TType keyType;
    private final TType valueType;
    public TMap(TType keyType, TType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public boolean hasSetter() {
        return false;
    }

    @Override
    public boolean maybeNull() {
        return true;
    }

    @Override
    public String getInterfaceMethodReturnType() {
        return COLLECTION + ".XMap<" + keyType.getBoxType() + "," + valueType.getBoxType() + ">";
    }
    @Override
    public String getApplyInterfaceType() {
        throw new UnsupportedOperationException();
    }
    @Override
    public String getBoxType() {
        return getInterfaceMethodReturnType();
    }


    @Override
    public String getDefaultDataValue() {
        return COLLECTION + ".Utils.newDataMap(" + valueType.isBean() + ")";
    }

    @Override
    public String getDefaultBeanValue() {
        return COLLECTION + ".Utils.newBeanMap(" + valueType.isBean() + ")";
    }

    @Override
    public String getTagid() {
        return "Tag.MAP";
    }

    @Override
    public String marshal(String osname, String vname) {
        return String.format("%s.marshal(%s, (os, x) -> {%s}, (os, x) -> {%s});",
                vname, osname, keyType.marshal("os", "x"), valueType.marshal("os", "x"));
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return String.format("%s.unmarshal(%s, os -> %s, os -> %s);",
                varname, osname, keyType.getUnmarshalMethod("os"), valueType.getUnmarshalMethod("os"));
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return null;
    }

    @Override
    public boolean isPredictable() {
        return false;
    }
}

package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TSet extends TType {

    private final TType valueType;
    public TSet(TType valueType) {
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
        return COLLECTION + ".XSet<" + valueType.getBoxType() + ">";
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
        return COLLECTION + ".Utils.newDataSet()";
    }

    @Override
    public String getDefaultBeanValue() {
        return COLLECTION + ".Utils.newBeanSet()";
    }

    @Override
    public String getTagid() {
        return "Tag.SET";
    }

    @Override
    public String marshal(String osname, String vname) {
        return String.format("%s.marshal(%s, (os, x) -> {%s});",
                vname, osname, valueType.marshal("os", "x"));
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return String.format("%s.unmarshal(%s, os -> %s);",
                varname, osname, valueType.getUnmarshalMethod("os"));
    }

    public boolean canDirectlyUnmarshal() {
        return true;
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

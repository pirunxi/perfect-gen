package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TDouble extends TType {

    @Override
    public boolean hasSetter() {
        return true;
    }

    @Override
    public boolean maybeNull() {
        return false;
    }

    @Override
    public String getInterfaceMethodReturnType() {
        return "double";
    }

    @Override
    public String getApplyInterfaceType() {
        return "DoubleTransform";
    }

    @Override
    public String getBoxType() {
        return "Double";
    }

    @Override
    public String getDefaultDataValue() {
        return "0.0";
    }

    @Override
    public String getTagid() {
        return "Tag.DOUBLE";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeDouble(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readDouble();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readDouble()";
    }
}

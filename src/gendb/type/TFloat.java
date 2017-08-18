package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TFloat extends TType {

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
        return "float";
    }

    @Override
    public String getApplyInterfaceType() {
        return "FloatTransform";
    }

    @Override
    public String getBoxType() {
        return "Float";
    }

    @Override
    public String getDefaultDataValue() {
        return "0f";
    }

    @Override
    public String getTagid() {
        return "Tag.FLOAT";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeFloat(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readFloat();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readFloat()";
    }
}

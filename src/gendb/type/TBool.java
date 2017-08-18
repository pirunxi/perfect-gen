package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TBool extends TType {

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
        return "boolean";
    }

    @Override
    public String getApplyInterfaceType() {
        return "BoolTransform";
    }

    @Override
    public String getBoxType() {
        return "Boolean";
    }

    @Override
    public String getDefaultDataValue() {
        return "false";
    }

    @Override
    public String getTagid() {
        return "Tag.BOOL";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeBool(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readBool();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".writeBool()";
    }
}

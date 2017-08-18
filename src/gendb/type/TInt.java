package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TInt extends TType {

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
        return "int";
    }

    @Override
    public String getApplyInterfaceType() {
        return "IntTransform";
    }

    @Override
    public String getBoxType() {
        return "Integer";
    }

    @Override
    public String getDefaultDataValue() {
        return "0";
    }

    @Override
    public String getTagid() {
        return "Tag.INT";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeInt(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readInt();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readInt()";
    }

    @Override
    public String getNewKeyMethod() {
        return "newInt";
    }
}

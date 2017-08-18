package gendb.type;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TString extends TType {

    @Override
    public boolean hasSetter() {
        return true;
    }

    @Override
    public boolean maybeNull() {
        return true;
    }

    @Override
    public String getInterfaceMethodReturnType() {
        return "String";
    }

    @Override
    public String getApplyInterfaceType() {
        return "StringTransform";
    }

    @Override
    public String getBoxType() {
        return getInterfaceMethodReturnType();
    }

    @Override
    public String getDefaultDataValue() {
        return "\"\"";
    }

    @Override
    public String getTagid() {
        return "Tag.STRING";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeString(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readString();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readString()";
    }

    @Override
    public String getNewKeyMethod() {
        return "newString";
    }
}

package genmsg.type;

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
    public String getNotBoxType() {
        return "String";
    }

    @Override
    public String getBoxType() {
        return getNotBoxType();
    }

    @Override
    public String getDefaultDataValue() {
        return "\"\"";
    }

    @Override
    public String marshalJava(String osname, String varname) {
        return osname + ".writeString(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readString();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readString()";
    }

    @Override
    public String luaDefine() {
        return "string";
    }
}

package genmsg.type;

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
    public String getNotBoxType() {
        return "float";
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
    public String marshalJava(String osname, String varname) {
        return osname + ".writeFloat(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readFloat();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readFloat()";
    }

    @Override
    public String luaDefine() {
        return "float";
    }
}

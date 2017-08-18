package genmsg.type;

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
    public String getNotBoxType() {
        return "double";
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
    public String marshalJava(String osname, String varname) {
        return osname + ".writeDouble(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readDouble();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readDouble()";
    }

    @Override
    public String luaDefine() {
        return "double";
    }
}

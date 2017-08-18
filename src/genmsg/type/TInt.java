package genmsg.type;

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
    public String getNotBoxType() {
        return "int";
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
    public String marshalJava(String osname, String varname) {
        return osname + ".writeInt(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readInt();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readInt()";
    }

    @Override
    public String luaDefine() {
        return "int";
    }
}

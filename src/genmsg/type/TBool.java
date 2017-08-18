package genmsg.type;

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
    public String getNotBoxType() {
        return "boolean";
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
    public String marshalJava(String osname, String varname) {
        return osname + ".writeBool(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readBool();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".writeBool()";
    }

    @Override
    public String luaDefine() {
        return "bool";
    }
}

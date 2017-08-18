package genmsg.type;


import gendb.Consts;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TBinary extends TType {

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
        return "byte[]";
    }


    @Override
    public String getBoxType() {
        return getNotBoxType();
    }

    @Override
    public String getDefaultDataValue() {
        return Consts.BINARY + ".emptyByteArray()";
    }


    @Override
    public String marshalJava(String osname, String varname) {
        return osname + ".writeByteArray(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readByteArray();";
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readByteArray()";
    }

    @Override
    public String luaDefine() {
        return "binary";
    }
}

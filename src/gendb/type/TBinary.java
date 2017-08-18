package gendb.type;

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
    public String getInterfaceMethodReturnType() {
        return Consts.BINARY;
    }

    @Override
    public String getApplyInterfaceType() {
        return "BinaryTransform";
    }

    @Override
    public String getBoxType() {
        return getInterfaceMethodReturnType();
    }

    @Override
    public String getDefaultDataValue() {
        return Consts.BINARY + ".empty()";
    }

    @Override
    public String getTagid() {
        return "Tag.BINARY";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeBinary(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readBinary();";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readBinary()";
    }

    @Override
    public String getNewKeyMethod() {
        return "newBinary";
    }
}

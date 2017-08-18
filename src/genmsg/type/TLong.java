package genmsg.type;

import java.util.Arrays;
import java.util.List;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TLong extends TType {

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
        return "long";
    }

    @Override
    public String getBoxType() {
        return "Long";
    }

    @Override
    public String getDefaultDataValue() {
        return "0L";
    }

    @Override
    public String marshalJava(String osname, String varname) {
        return osname + ".writeLong(" + varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        return varname + " = " + osname + ".readLong();";
    }

    private static final List<TType> compatibleTypes = Arrays.asList(new TInt());

    @Override
    public String getUnmarshalMethodJava(String os) {
        return os + ".readLong()";
    }

    @Override
    public String luaDefine() {
        return "long";
    }
}

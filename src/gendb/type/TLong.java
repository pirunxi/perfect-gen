package gendb.type;

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
    public String getInterfaceMethodReturnType() {
        return "long";
    }

    @Override
    public String getApplyInterfaceType() {
        return "LongTransform";
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
    public String getTagid() {
        return "Tag.LONG";
    }

    @Override
    public String marshal(String osname, String varname) {
        return osname + ".writeLong(" + varname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + " = " + osname + ".readLong();";
    }

    private static final List<TType> compatibleTypes = Arrays.asList(new TInt());

    @Override
    public String getUnmarshalMethod(String os) {
        return os + ".readLong()";
    }

    @Override
    public String getNewKeyMethod() {
        return "newLong";
    }

    @Override
    public List<TType> getCompatibleTypes() {
        return compatibleTypes;
    }
}

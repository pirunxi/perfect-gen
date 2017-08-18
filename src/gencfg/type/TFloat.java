package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FFloat;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TFloat extends TType {
    public TFloat(Map<String, String> attrs) {
        super(attrs);
    }

    @Override
    public String getBoxType() {
        return "Float";
    }

    @Override
    public String getNotBoxType() {
        return "float";
    }

    @Override
    public String unmarshalJava(String fsName) {
        return fsName + ".getFloat()";
    }

    @Override
    public String luaDefine() {
        return "float";
    }

    @Override
    public String getCsType() {
        return "float";
    }

    @Override
    public String unmarshalCs(String fsName) {
        return fsName + ".GetFloat()";
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FFloat(this, fs.getFloat());
    }

    @Override
    public Data newData(Element ele) {
        return null;
    }

    @Override
    public Data newData(LuaValue lua) {
        return null;
    }

}

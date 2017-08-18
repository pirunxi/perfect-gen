package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FInt;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TInt extends TType {
    public TInt(Map<String, String> attrs) {
        super(attrs);
    }

    @Override
    public String getBoxType() {
        return "Integer";
    }

    @Override
    public String getNotBoxType() {
        return "int";
    }

    @Override
    public String unmarshalJava(String fsName) {
        return fsName + ".getInt()";
    }

    @Override
    public String luaDefine() {
        return "int";
    }

    @Override
    public String getCsType() {
        return "int";
    }

    @Override
    public String unmarshalCs(String fsName) {
        return fsName + ".GetInt()";
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FInt(this, fs.getInt());
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

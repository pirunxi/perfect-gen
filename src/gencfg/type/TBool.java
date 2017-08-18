package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FBool;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TBool extends TType {
    public TBool(Map<String, String> attrs) {
        super(attrs);
    }

    @Override
    public String getBoxType() {
        return "Boolean";
    }

    @Override
    public String getNotBoxType() {
        return "boolean";
    }

    @Override
    public String unmarshalJava(String fsName) {
        return fsName + ".getBool()";
    }

    @Override
    public String luaDefine() {
        return "bool";
    }

    @Override
    public String getCsType() {
        return "bool";
    }

    @Override
    public String unmarshalCs(String fsName) {
        return fsName + ".GetBool()";
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FBool(this, fs.getBool());
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

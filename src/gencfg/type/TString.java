package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FString;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TString extends TType {
    public TString(Map<String, String> attrs) {
        super(attrs);
    }

    @Override
    public String getBoxType() {
        return "String";
    }

    @Override
    public String getNotBoxType() {
        return getBoxType();
    }

    @Override
    public String unmarshalJava(String fsName) {
        return fsName + ".getString()";
    }

    @Override
    public String luaDefine() {
        return "string";
    }

    @Override
    public String getCsType() {
        return "string";
    }

    @Override
    public String unmarshalCs(String fsName) {
        return fsName + ".GetString()";
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FString(this, fs.getString());
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

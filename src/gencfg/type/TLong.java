package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FLong;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TLong extends TType {
    public TLong(Map<String, String> attrs) {
        super(attrs);
    }


    @Override
    public String getBoxType() {
        return "Long";
    }

    @Override
    public String getNotBoxType() {
        return "long";
    }

    @Override
    public String unmarshalJava(String fsName) {
        return fsName + ".getLong()";
    }

    @Override
    public String luaDefine() {
        return "long";
    }

    @Override
    public String getCsType() {
        return "long";
    }

    @Override
    public String unmarshalCs(String fsName) {
        return fsName + ".GetLong()";
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FLong(this, fs.getLong());
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

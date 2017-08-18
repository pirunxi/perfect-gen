package gencfg.type;

import gencfg.ENUM;
import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FEnum;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;
import java.util.Map;

/**
 * Created by HuangQiang on 2017/5/24.
 */
public class TEnum extends TType {
    private final ENUM e;
    public TEnum(ENUM e, Map<String, String> attrs) {
        super(attrs);
        this.e = e;
    }

    @Override
    public boolean isContainer() {  return false; }
    public boolean isEnum() { return true; }

    @Override
    public String getNotBoxType() {
        return e.getFullname();
    }

    @Override
    public String getBoxType() {
        return e.getFullname();
    }

    @Override
    public String luaDefine() {
        return "int";
    }

    @Override
    public String getCsType() {
        return e.getFullname();
    }

    @Override
    public String unmarshalCs(String fsName) {
        return String.format("(%s)%s.GetInt()", e.getFullname(), fsName);
    }

    @Override
    public String unmarshalJava(String fsName) {
        return String.format("%s.valueOf(%s.getInt())", e.getFullname(), fsName);
    }

    @Override
    public Data newData(FlatStream fs) {
        return new FEnum(this, e.getEnumValueByName(fs.getString()));
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

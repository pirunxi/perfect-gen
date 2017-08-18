package gencfg.type;

import gencfg.FlatStream;
import gencfg.Utils;
import gencfg.data.Data;
import gencfg.data.FSet;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Element;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TSet extends TType {

    private final TType valueType;
    public TSet(TType valueType, Map<String, String> attrs) {
        super(attrs);
        this.valueType = valueType;
    }

    @Override public boolean isBasic() { return false; }
    @Override public boolean isContainer() { return true; }

    @Override
    public String getBoxType() {
        return "java.util.HashSet<" + valueType.getBoxType() + ">";
    }

    @Override
    public String getNotBoxType() {
        return getBoxType();
    }
    @Override
    public String unmarshalJava(String fieldName, String fsName) {
        return String.format("this.%s = new %s(); for(int n = %s.getInt(); n > 0 ; n--) { this.%s.add(%s); }",
                fieldName, getBoxType(), fsName, fieldName, valueType.unmarshalJava(fsName));
    }

    @Override
    public String unmarshalJava(String fsName) {
        throw new UnsupportedOperationException();
    }

    public String luaDefine() {
        throw new UnsupportedOperationException();
    }

    public String luaTypeDefine() {
        return String.format("type='set', value='%s'", valueType.luaDefine());
    }

    @Override
    public String getCsType() {
        return "System.Collections.Generic.HashSet<" + valueType.getCsType() + ">";
    }

    @Override
    public String unmarshalCs(String fsName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String unmarshalCs(String fieldName, String fsName) {
        return String.format("this.%s = new %s(); for(int n = %s.GetInt(); n > 0 ; n--) { this.%s.Add(%s); }",
                fieldName, getCsType(), fsName, fieldName, valueType.unmarshalCs(fsName));
    }


    @Override
    public Data newData(FlatStream fs) {
        Set<Data> values = new HashSet<>();
        while(!fs.isSectionEnd()) {
            Data value = valueType.newData(fs);
            if(!values.add(value))
                Utils.error("value:%s duplicate", value);
        }
        return new FSet(this, values);
    }

    @Override
    public Data newData(Element ele) {
        return null;
    }

    @Override
    public Data newData(LuaValue lua) {
        return null;
    }

    @Override
    public void verify() {
//        super.verify();
        valueType.verify();
    }
}

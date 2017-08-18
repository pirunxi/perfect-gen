package gencfg.type;

import gencfg.FlatStream;
import gencfg.data.Data;
import gencfg.data.FList;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TList extends TType {
    private final TType valueType;
    public TList(TType valueType, Map<String, String> attrs) {
        super(attrs);
        this.valueType = valueType;
    }

    public TType getValueType() {
        return valueType;
    }

    @Override public boolean isBasic() { return false; }
    @Override public boolean isContainer() { return true; }

    @Override
    public String getBoxType() {
        return "java.util.ArrayList<" + valueType.getBoxType() + ">";
    }

    @Override
    public String getNotBoxType() {
        return getBoxType();
    }

    @Override
    public String unmarshalJava(String fieldName, String fsName) {
//        StringBuilder sb = new StringBuilder();
//        sb.append("this.").append(fieldName).append(" = new ").append(getBoxType()).append(";");

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
        return String.format("type='list', value='%s'", valueType.luaDefine());
    }

    @Override
    public String getCsType() {
        return "System.Collections.Generic.List<" + valueType.getCsType() + ">";
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
        List<Data> values = new ArrayList<>();
        while(!fs.isSectionEnd()) {
            values.add(valueType.newData(fs));
        }
        return new FList(this, values);
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
        super.verify();

        valueType.verify();
    }
}

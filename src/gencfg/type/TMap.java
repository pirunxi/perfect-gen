package gencfg.type;

import gencfg.FlatStream;
import gencfg.Utils;
import gencfg.data.Data;
import gencfg.data.FMap;
import org.luaj.vm2.LuaValue;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TMap extends TType {
    private final TType keyType;
    private final TType valueType;
    public TMap(TType keyType, TType valueType, Map<String, String> attrs) {
        super(attrs);
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override public boolean isBasic() { return false; }
    @Override public boolean isContainer() { return true; }

    public TType getKeyType() {
        return keyType;
    }

    public TType getValueType() {
        return valueType;
    }

    @Override
    public String getBoxType() {
        return "java.util.HashMap<" + keyType.getBoxType() + "," + valueType.getBoxType() + ">";
    }

    @Override
    public String getNotBoxType() {
        return getBoxType();
    }

    @Override
    public String unmarshalJava(String fieldName, String fsName) {
//        return String.format("this.%s = new %s(); for(int n = %s.getInt(); n > 0 ; n--) { %s _key = %s; %s _value = %s; this.%s.put(_key, _value); }",
//                fieldName, getBoxType(), fsName, keyType.getNotBoxType(), keyType.unmarshalJava(fsName), valueType.getNotBoxType(), valueType.unmarshalJava(fsName), fieldName);
        // java 明确规定了函数参数求值顺序从左往右,故无需用上面的办法
        return String.format("this.%s = new %s(); for(int n = %s.getInt(); n > 0 ; n--) { this.%s.put(%s, %s); }",
                fieldName, getBoxType(), fsName, fieldName, keyType.unmarshalJava(fsName), valueType.unmarshalJava(fsName));
    }

    @Override
    public String unmarshalJava(String fsName) {
        throw new UnsupportedOperationException();
    }

    public String luaDefine() {
        throw new UnsupportedOperationException();
    }

    public String luaTypeDefine() {
        return String.format("type='map', key='%s', value='%s'", keyType.luaDefine(), valueType.luaDefine());
    }

    @Override
    public String getCsType() {
        return "System.Collections.Generic.Dictionary<" + keyType.getCsType() + "," + valueType.getCsType() + ">";
    }

    @Override
    public String unmarshalCs(String fsName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String unmarshalCs(String fieldName, String fsName) {
        return String.format("this.%s = new %s(); for(int n = %s.GetInt(); n > 0 ; n--) { this.%s.Add(%s, %s); }",
                fieldName, getCsType(), fsName, fieldName, keyType.unmarshalCs(fsName), valueType.unmarshalCs(fsName));
    }

    @Override
    public Data newData(FlatStream fs) {
        Map<Data, Data> values = new HashMap<>();
        while(!fs.isSectionEnd()) {
            Data key = keyType.newData(fs);
            if(values.put(key, valueType.newData(fs)) != null) {
                Utils.error("key:%s duplicate", key);
            }
        }
        return new FMap(this, values);
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
        keyType.verify();
        valueType.verify();
    }
}

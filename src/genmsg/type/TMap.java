package genmsg.type;

import java.util.Set;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TMap extends TType {
    private final TType keyType;
    private final TType valueType;
    public TMap(TType keyType, TType valueType) {
        this.keyType = keyType;
        this.valueType = valueType;
    }

    @Override
    public boolean hasSetter() {
        return false;
    }

    @Override
    public boolean maybeNull() {
        return true;
    }

    @Override
    public Set<String> collectDependenceBeans() {
        return valueType.collectDependenceBeans();
    }

    @Override
    public String getNotBoxType() {
        return "java.util.HashMap<" + keyType.getBoxType() + "," + valueType.getBoxType() + ">";
    }

    @Override
    public String getBoxType() {
        return getNotBoxType();
    }


    @Override
    public String getDefaultDataValue() {
        return "new " + getNotBoxType() + "()";
    }

    @Override
    public String marshalJava(String osname, String vname) {
        return String.format("%s.writeCompactUint(%s.size()); for(java.util.Map.Entry<%s,%s> _e : %s.entrySet()) { %s %s }",
                osname, vname, keyType.getBoxType(), valueType.getBoxType(), vname,
                keyType.marshalJava(osname, "_e.getKey()"), valueType.marshalJava(osname, "_e.getValue()"));
    }

    @Override
    public String unmarshalJava(String osname, String vname) {
        return String.format("for(int n = %s.readCompactUint(); --n >= 0 ; ) { %s.put(%s, %s); }",
                osname, vname, keyType.getUnmarshalMethodJava(osname), valueType.getUnmarshalMethodJava(osname));
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        throw new UnsupportedOperationException();
    }


    @Override
    public String luaDefine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String luaTypeDefine() {
        return String.format("type='map', key='%s', value='%s'", keyType.luaDefine(), valueType.luaDefine());
    }
}

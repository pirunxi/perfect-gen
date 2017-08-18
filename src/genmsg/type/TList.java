package genmsg.type;

import java.util.Set;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TList extends TType {
    private final TType valueType;
    public TList(TType valueType) {
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
        return "java.util.ArrayList<" + valueType.getBoxType() + ">";
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
        return String.format("%s.writeCompactUint(%s.size()); for(%s _v : %s) { %s }",
                osname, vname, valueType.getNotBoxType(), vname, valueType.marshalJava(osname, "_v"));
    }

    @Override
    public String unmarshalJava(String osname, String vname) {
        return String.format("for(int n = %s.readCompactUint(); --n >= 0 ; ) { %s.add(%s); }",
                osname, vname, valueType.getUnmarshalMethodJava(osname));
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
        return String.format("type='list', value='%s'", valueType.luaDefine());
    }
}

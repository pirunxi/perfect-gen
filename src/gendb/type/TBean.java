package gendb.type;

import gendb.Bean;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TBean extends TType {
    private final Bean bean;
    public TBean(Bean bean) {
        this.bean = bean;
    }

    public Bean getBean() {
        return bean;
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
    public String getInterfaceMethodReturnType() {
        return bean.getFullname();
    }

    @Override
    public String getApplyInterfaceType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBoxType() {
        return getInterfaceMethodReturnType();
    }

    @Override
    public String getDefaultDataValue() {
        return "new " + bean.getFullname() + "()";
    }

    @Override
    public String getDefaultBeanValue() {
        return bean.getFullname() + ".newBean()";
    }

    @Override
    public boolean isBean() { return true; }

    @Override
    public String getTagid() {
        return "Tag.BEAN";
    }

    @Override
    public String marshal(String osname, String varname) {
        return bean.getFullname() + ".marshalBean(" + varname + ", " +  osname + ");";
    }

    @Override
    public String unmarshal(String osname, String varname) {
        return varname + ".unmarshal(" + osname + ");";
    }

    @Override
    public String getUnmarshalMethod(String os) {
        return bean.getFullname() + ".unmarshalBean(" + os + ")";
    }

    @Override
    public boolean isPredictable() {
        return false;
    }
}

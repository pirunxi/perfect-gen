package genmsg.type;

import genmsg.Bean;

import java.util.Set;

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
    public Set<String> collectDependenceBeans() {
        bean.collectDependences();
        return bean.getDepBeans();
    }

    @Override
    public String getNotBoxType() {
        return bean.getFullname();
    }

    @Override
    public String getBoxType() {
        return getNotBoxType();
    }

    @Override
    public String getDefaultDataValue() {
        if(bean.isDynamic()) {
            return "null";
        } else {
            return "new " + bean.getFullname() + "()";
        }
    }


    @Override
    public boolean isBean() { return true; }

    @Override
    public String marshalJava(String osname, String varname) {
        return bean.getFullname() + ".marshalBean(" + osname + ", " +  varname + ");";
    }

    @Override
    public String unmarshalJava(String osname, String varname) {
        if(bean.isDynamic()) {
            return varname + " = " + bean.getFullname() + ".unmarshalBean(" + osname + ");";
        } else {
            return varname + ".unmarshal(" + osname + ");";
        }
    }

    @Override
    public String getUnmarshalMethodJava(String os) {
        return bean.getFullname() + ".unmarshalBean(" + os + ")";
    }

    @Override
    public String luaDefine() {
        return bean.getFullname();
    }
}

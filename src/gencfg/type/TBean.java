package gencfg.type;

import gencfg.Bean;
import gencfg.FlatStream;
import gencfg.Utils;
import gencfg.data.Data;
import gencfg.data.FBean;
import org.luaj.vm2.LuaValue;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class TBean extends TType {
    private final Bean bean;
    public TBean(Bean bean, Map<String, String> attrs) {
        super(attrs);
        this.bean = bean;
    }

    public Bean getBean() {
        return bean;
    }

    @Override public boolean isBasic() { return false; }
    @Override public boolean isBean() { return true; }

    @Override
    public String getBoxType() {
        return bean.getFullname();
    }

    @Override
    public String getNotBoxType() {
        return getBoxType();
    }

    @Override
    public String unmarshalJava(String fsName) {
        return (bean.isDynamic() ? String.format("(%s)(%s.getObject(\"%s.\" + %s.getString()))", bean.getFullname(), fsName, bean.getNamespace(), fsName)
                : String.format("new %s(%s)", bean.getFullname(), fsName));
    }

    @Override
    public String luaDefine() {
        return bean.getFullname();
    }

    @Override
    public String getCsType() {
        return bean.getFullname();
    }

    @Override
    public String unmarshalCs(String fsName) {
        return (bean.isDynamic() ? String.format("(%s)(%s.GetObject(\"%s.\" + %s.GetString()))", bean.getFullname(), fsName, bean.getNamespace(), fsName)
                : String.format("new %s(%s)", bean.getFullname(), fsName));
    }

    @Override
    public Data newData(FlatStream fs) {
        Bean actual;
        if(bean.isDynamic()) {
            String actualFullName = Utils.combineFullName(bean.getNamespace(), fs.getString());
            actual = Bean.get(actualFullName);
            if (actual == null)
                Utils.error("base:%s bean:%s can't find.", bean.getFullname(), actualFullName);
            if(actual.isDynamic())
                Utils.error("base:%s bean:%s can't be dynamic", bean.getFullname(), actualFullName);
            if(actual.isDeriveFrom(bean))
                Utils.error("bean:%s isn't derived from base:%s", actualFullName, bean.getFullname());
        } else {
            actual = bean;
        }
        return new FBean(this, actual, actual.getSelfAndParentFields().stream().map(f -> f.getTType().newData(fs)).collect(Collectors.toList()));
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

//        bean.getFields().forEach(f -> f.getTType().verify());
    }
}

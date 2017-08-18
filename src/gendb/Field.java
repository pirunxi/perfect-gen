package gendb;

import org.w3c.dom.Element;
import gendb.type.TType;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Field {
    private final String bean;
    private final int id;
    private final String name;
    private final String typeName;
    private TType type;
    public Field(String bean, Element ele) {
        this.bean = bean;
        this.name = ele.getAttribute("name");
        if(name.isEmpty())
            error("filed name missing");
        try {
            this.id = Integer.parseInt(ele.getAttribute("id"));
        } catch (Exception e) {
            error("field " + name + " id invalid");
            throw e;
        }

        this.typeName = ele.getAttribute("type");
    }

    private void error(String msg) {
        throw new RuntimeException("bean:" + bean + ", msg:" + msg);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public TType getType() {
        return type;
    }

    public void setType(TType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("Field{name=%s,id=%s,type=%s}", name, id, typeName);
    }
}

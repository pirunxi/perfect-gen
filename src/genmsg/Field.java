package genmsg;

import org.w3c.dom.Element;
import genmsg.type.TType;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Field {
    private final String host;
    private final String namespace;
    private final String name;
    private final String typeName;
    private TType type;
    public Field(String host, String namespace, Element ele) {
        this.host = host;
        this.namespace = namespace;
        this.name = ele.getAttribute("name");
        if(name.isEmpty())
            error("filed name missing");

        this.typeName = ele.getAttribute("type");
    }

    private void error(String msg) {
        throw new RuntimeException("bean:" + host + ", msg:" + msg);
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

    @Override
    public String toString() {
        return String.format("Field{name=%s,type=%s}", name, typeName);
    }

    public void resolve() {
        type = TType.create(namespace, typeName);
    }
}

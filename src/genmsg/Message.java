package genmsg;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by HuangQiang on 2017/5/26.
 */
public class Message extends MsgLike {

    public Message(String namespace, Element ele) {
        super(namespace, ele);


        Utils.foreach(ele, (tag, node) -> {
            switch (tag) {
                case "field" : fields.add(new Field(getFullname(), namespace, node)); break;
                default: Utils.error("msg:%s tag:%s unknown", getFullname(), tag);
            }
        });
        for(Element e : Utils.getChildsByTagName(ele, "field")) {

        }

        Utils.verbose(this);
    }

    private final List<Field> fields = new ArrayList<>();

    @Override
    public String toString() {
        return String.format("msg{name:%s,id:%s,fields:%s}", getFullname(), getTypeId(), fields);
    }

    public List<Field> getFields() {
        return fields;
    }

    @Override
    public void resolve() {
        fields.forEach(Field::resolve);
    }


    private final Set<String> depBeans = new HashSet<>();

    public Set<String> getDepBeans() {
        return depBeans;
    }

    public void collectDependences() {
        for(Field field : fields) {
            depBeans.addAll(field.getType().collectDependenceBeans());
        }
    }
}

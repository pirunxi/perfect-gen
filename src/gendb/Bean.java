package gendb;

import gendb.type.TBean;
import gendb.type.TType;
import org.w3c.dom.Element;

import java.util.*;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Bean {
    private final static LinkedHashMap<String, Bean> beans = new LinkedHashMap<>();

    public static LinkedHashMap<String, Bean> getBeans() {
        return beans;
    }

    public static void add(Bean bean) {
        if(beans.put(bean.getFullname(), bean) != null)
            throw new RuntimeException("bean:" + bean.getFullname() + " duplicate");
    }

    public static Bean getBean(String beanName) {
        return beans.get(beanName);
    }

    private final int id;
    private final String name;
    private final String namespace;
    private final String fullname;
    private final List<Field> fields = new ArrayList<>();
    private final List<Field> selfAndParentFields = new ArrayList<>();
    private final List<Bean> children = new ArrayList<>();
    private final Bean parent;
    public Bean(String namespace, Element ele) {
        this(null, namespace, ele);
    }

    public Bean(Bean parent, String namespace, Element ele) {
        this.parent = parent;
        this.name = ele.getAttribute("name");
        if(this.name.isEmpty())
            throw new RuntimeException("bean name missing");
        this.namespace = namespace;
        this.fullname = Utils.combineFullName(namespace, name);
        for(Element e : Utils.getChildsByTagName(ele, "field")) {
            fields.add(new Field(fullname, e));
        }
        if(parent != null) {
            selfAndParentFields.addAll(parent.selfAndParentFields);
        }
        selfAndParentFields.addAll(fields);

        for(Element e : Utils.getChildsByTagName(ele, "bean")) {
            Bean child = new Bean(this, namespace, e);
            Bean.add(child);
            if(!child.isDynamic()) {
                children.add(child);
            } else {
                children.addAll(child.getChildren());
            }
        }

        if(parent != null && children.isEmpty()) {
            String idStr = ele.getAttribute("id");
            try {
                this.id = Integer.parseInt(idStr);
            } catch (Exception e) {
                throw new RuntimeException("bean:" + this.name + " is children bean.but id:" + idStr + " is invalid.");
            }
        } else {
            this.id = 0;
        }

        final Set<String> names = new HashSet<>();
        final Set<Integer> ids = new HashSet<>();
        selfAndParentFields.stream().forEach(f -> {
            if(!names.add(f.getName().toLowerCase()))
                throw new RuntimeException("bean(or parent):" + this.name + " duplicate field name:" + f.getName());
            if(!ids.add(f.getId()))
                throw new RuntimeException("bean(or parent):" + this.name + " duplicate field id:" + f.getId());
        });
    }

    public int getId() {
        return id;
    }

    public Bean getParent() {
        return parent;
    }

    public boolean isDynamic() {
        return !children.isEmpty();
    }

    public List<Bean> getChildren() {
        return children;
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getFullname() {
        return fullname;
    }

    public List<Field> getFields() {
        return fields;
    }

    public List<Field> getSelfAndParentFields() {
        return selfAndParentFields;
    }

    public void init() {
        for(Field field : fields) {
            try {
                final TType type = TType.create(namespace, field.getTypeName());
                field.setType(type);
                if(type.isBean() && ((TBean)type).getBean().isDynamic()) {
                    throw new RuntimeException("bean:" + fullname + ",field:" + field.getName() + " can't be dynamic");
                }
            } catch (Exception e) {
                throw new RuntimeException("bean:" + fullname + " field:" + field.getName() +  " type:" + field.getTypeName() + " error");
            }
        }

        if(parent == null) {
            HashSet<Integer> ids = new HashSet<>();
            for(Bean c : children) {
                if(!ids.add(c.getId()))
                    throw new RuntimeException("bean:" + c.getFullname() + ",id=" + c.getId() + " duplicate");
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Bean{name=%s}", fullname));
        sb.append(fields);
        return sb.toString();
    }
}

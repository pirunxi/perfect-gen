package genmsg;

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

    private int id;
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
            fields.add(new Field(fullname, namespace, e));
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
                error("bean id:" + idStr + " is invalid.");
            }
        } else {
            this.id = 0;
        }

        final Set<String> names = new HashSet<>();
        selfAndParentFields.stream().forEach(f -> {
            if(!names.add(f.getName().toLowerCase()))
                error("duplicate field name:" + f.getName());
        });

        Utils.verbose(this);
    }

    private void error(String msg) {
        throw new RuntimeException("bean:" + fullname + " err:" + msg);
    }

    public int getTypeId() {
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

    public void resolve() {
        fields.forEach(Field::resolve);

        if(parent == null) {
            HashSet<Integer> ids = new HashSet<>();
            for(Bean c : children) {
                if(!ids.add(c.getTypeId()))
                    throw new RuntimeException("bean:" + c.getFullname() + ",id=" + c.getTypeId() + " duplicate");
            }
        }
    }

    private boolean collected = false;

    private final Set<String> depBeans = new HashSet<>();

    public Set<String> getDepBeans() {
        return depBeans;
    }

    public void collectDependences() {
        if(collected) return;
        collected = true;
        depBeans.add(fullname);
        for(Bean c : children) {
            c.collectDependences();
            depBeans.addAll(c.getDepBeans());
        }
        for(Field field : fields) {
            Set<String> fdeps = field.getType().collectDependenceBeans();
            if(fdeps != depBeans)
                depBeans.addAll(fdeps);
        }


        if(parent != null) {
            parent.collectDependences();
            depBeans.addAll(parent.getDepBeans());
        }
    }

    @Override
    public String toString() {
        return String.format("bean{name:%s, fields:%s}", fullname, fields);
    }
}

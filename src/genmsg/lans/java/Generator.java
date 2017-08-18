package genmsg.lans.java;

import gendb.*;
import genmsg.*;
import genmsg.Bean;
import genmsg.Field;
import genmsg.Utils;
import genmsg.type.TType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Generator {

    public static void gen() {
        Utils.suffix = "java";
        Utils.getExportBeans().forEach(Generator::genBean);
        Collection<MsgLike> msgs = Utils.getExportMsgs();
        msgs.forEach(Generator::genMsgLike);
        genMessages(msgs);
        genRef(Utils.getExportRefSets());
    }

    private static void genBean(Bean bean) {
        final ArrayList<String> ls = new ArrayList<>();
        final String namespace = bean.getNamespace();
        ls.add("package $namespace;");

        final Bean parent = bean.getParent();
        final String name = bean.getName();
        final boolean isDynamic = bean.isDynamic() ;
        List<Field> fields = bean.getFields();

        ls.add(String.format("public %s class $name %s {", (isDynamic ? "abstract" : "final"), (parent == null ? "implements $IO_BEAN" : "extends " + parent.getFullname())));

        if(isDynamic) {
            ls.add("public static void marshalBean($BSTREAM bs, $fullname bean) { bs.writeInt(bean.getTypeId()); bean.marshal(bs); }");
            ls.add("public static $name unmarshalBean($BSTREAM bs) { switch(bs.readInt()) { ");
            for(Bean c : bean.getChildren()) {
                ls.add(String.format("case %s.TYPEID : return %s.unmarshalBean(bs);", c.getFullname(), c.getFullname()));
            }
            ls.add("default: throw new RuntimeException(\"unknown type\");");
            ls.add("}}");
        } else {
            ls.add("public static void marshalBean($BSTREAM bs, $fullname bean) { bean.marshal(bs); }");
            ls.add("public static $name unmarshalBean($BSTREAM bs) { $name _value = new $name(); _value.unmarshal(bs); return _value; }");
        }

        genTypeid(ls, isDynamic, bean.getTypeId());
        genFieldDefineAndMarshal(ls, parent, fields, bean.getSelfAndParentFields());

        ls.add("}");

        Map<String, String> replacer = new HashMap<>(Consts.consts);
        replacer.put("$name", name);
        replacer.put("$fullname", bean.getFullname());
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(bean.getFullname(), ls, replacer);
    }

    private static void genTypeid(List<String> ls, boolean isDynamic, int typeid) {
        if(!isDynamic) {
            ls.add(String.format("	public final static int TYPEID = %s;", typeid));
            ls.add("	final public int getTypeId() { return TYPEID; }");
        }
    }

    private static void genFieldDefineAndMarshal(List<String> ls, Object parent, List<Field> selfFields, List<Field> selfAndParentFields) {
        for(Field f : selfFields) {
            TType ftype = f.getType();
            ls.add(String.format("public %s %s;", ftype.getNotBoxType(), f.getName()));
        }

        ls.add("public $name() {");
        for(Field f : selfFields) {
            TType ftype = f.getType();
            if(ftype.maybeNull())
                ls.add(String.format("this.%s = %s;", f.getName(), ftype.getDefaultDataValue()));
        }
        ls.add("}");


        if(!selfAndParentFields.isEmpty()) {
            ls.add(String.format("public $name(%s) {", selfAndParentFields.stream().map(f -> f.getType().getNotBoxType() + " " + f.getName())
                    .collect(Collectors.joining(","))));
            List<Field> parentFields = selfAndParentFields.subList(0, selfAndParentFields.size() - selfFields.size());
            if (!parentFields.isEmpty()) {
                ls.add(String.format("super(%s);", parentFields.stream()
                        .map(Field::getName).collect(Collectors.joining(","))));
            }
            for (Field f : selfFields) {
                ls.add(String.format("this.%s = %s;", f.getName(), f.getName()));
            }
            ls.add("}");
        }



        ls.add("public String toString() { StringBuilder sb = new StringBuilder();");
        ls.add("sb.append( this.getClass().getName()).append(\"{\");");
        if(parent != null) {
            ls.add("sb.append(super.toString());");
        }

        for(Field f : selfFields) {
            String fname = f.getName();
            ls.add("sb.append(\"," + fname + ":\").append(" + fname + ");");
        }
        ls.add("sb.append(\"}\");");
        ls.add("return sb.toString();}");

        ls.add("public void marshal($BSTREAM bs) {");
        if(parent != null) {
            ls.add("super.marshal(bs);");
        }
        for(Field f : selfFields) {
            TType ftype = f.getType();
            final String fname = f.getName();
            ls.add(ftype.marshalJava("bs", fname));
        }
        ls.add("}");

        ls.add("public void unmarshal($BSTREAM bs) {");
        if(parent != null) {
            ls.add("super.unmarshal(bs);");
        }
        for(Field f : selfFields) {
            TType ftype = f.getType();
            final String fname = f.getName();
            ls.add(ftype.unmarshalJava("bs", fname));
        }
        ls.add("}");
    }

    private static void genMsgLike(MsgLike msg) {
        if(msg instanceof Message) {
            genMsg((Message)msg);
        } else {
            genRpc((Rpc)msg);
        }
    }

    private static void genMsg(Message msg) {
        final ArrayList<String> ls = new ArrayList<>();
        final String namespace = msg.getNamespace();
        ls.add("package $namespace;");

        final String name = msg.getName();
        List<Field> fields = msg.getFields();

        ls.add("public final class $name extends $MSG {");
        genTypeid(ls, false, msg.getTypeId());
        genFieldDefineAndMarshal(ls, null, fields, fields);

        ls.add("public $name newObject() { return new $name(); }");
        ls.add("public void run() { if(handler != null) handler.process(this); }");
        ls.add("public interface Handler { void process($name msg); } public static Handler handler;");

        ls.add("}");

        Map<String, String> replacer = new HashMap<>(Consts.consts);
        replacer.put("$name", name);
        replacer.put("$fullname", msg.getFullname());
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(msg.getFullname(), ls, replacer);
    }

    private static void genRpc(Rpc rpc) {

    }

    private static void genRef(Collection<RefSet> refs) {
        final ArrayList<String> ls = new ArrayList<>();
        final String namespace = "msg";
        ls.add("package $namespace;");

        final String name = "_Refs_";
        final String fullname = Utils.combineFullName(namespace, name);

        ls.add("public final class $name {");

        for(RefSet ref : refs) {
            ls.add(String.format("public final static java.util.HashMap<Integer, $MSG> %s = new java.util.HashMap<>();", ref.getName()));
            ls.add("static {");
            for(String msg : ref.getDepMsgs()) {
                ls.add(String.format("%s.put(%s.TYPEID, new %s());", ref.getName(), msg, msg));
            }
            ls.add("}");
        }

        ls.add("}");

        Map<String, String> replacer = new HashMap<>(Consts.consts);
        replacer.put("$name", name);
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(fullname, ls, replacer);
    }

    private static void genMessages(Collection<MsgLike> msgs) {
        final ArrayList<String> ls = new ArrayList<>();
        final String namespace = "msg";
        ls.add("package $namespace;");

        final String name = "_Messages_";
        final String fullname = Utils.combineFullName(namespace, name);

        ls.add("public final class $name {");
        ls.add("public final static java.util.HashMap<Integer, $MSG> msgs = new java.util.HashMap<>();");
        ls.add("static {");
        for(MsgLike msg : msgs) {
            ls.add(String.format("msgs.put(%s.TYPEID, new %s());", msg.getFullname(), msg.getFullname()));
        }
        ls.add("}");
        ls.add("}");

        Map<String, String> replacer = new HashMap<>(Consts.consts);
        replacer.put("$name", name);
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(fullname, ls, replacer);
    }
}

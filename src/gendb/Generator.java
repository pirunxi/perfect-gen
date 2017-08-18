package gendb;

import gendb.type.TBean;
import gendb.type.TType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static gendb.Consts.consts;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Generator {



    static void genInterface(Bean bean) {
        System.out.println("gen interface " + bean.getFullname());
        String name= bean.getName();
        String namespace = bean.getNamespace();
        List<Field> fields = bean.getFields();
        List<String> codes = new ArrayList<>();
        codes.add("package $namespace;");
        if(bean.getParent() != null) {
            codes.add("public interface $name extends " + bean.getParent().getName() + "{");
        } else {
            codes.add("public interface $name extends $BEAN<$name> {");
        }
        if(bean.isDynamic()) {
            codes.add("static void marshalBean($name bean, $BSTREAM os) { os.writeInt(bean.getBeanId()); bean.marshal(os); }");
            codes.add("static $name unmarshalBean($BSTREAM os) {");
            codes.add("switch(os.readInt()){");
            for(Bean c : bean.getChildren()) {
                codes.add(String.format("case %d: return %s.unmarshalBean(os);", c.getId(), c.getName()));
            }
            codes.add("default: throw new RuntimeException(\"unknown bean type\");");
            codes.add("}}");
        } else {
            codes.add("static $name newBean() { return new __$name(); }");
            codes.add("static void marshalBean($name bean, $BSTREAM os) { bean.marshal(os); }");
            codes.add("static $name unmarshalBean($BSTREAM os) { $name v = newBean(); v.unmarshal(os); return v; }");
        }

        for(Field field : fields) {
            final TType ftype = field.getType();
            final String fname = field.getName();
            final String uname = Utils.upperCaseFirstChat(fname);
            codes.add(String.format("    %s get%s();", ftype.getInterfaceMethodReturnType(), uname));
            if(ftype.hasSetter()) {
                codes.add(String.format("    void set%s(%s %s);", uname, ftype.getInterfaceMethodReturnType(), fname));
                codes.add(String.format("    %s apply%s(%s %s);", ftype.getInterfaceMethodReturnType(), uname, ftype.getApplyInterfaceType(), fname));
            }
        }
        codes.add("}");
        final Map<String, String> replacer = new HashMap<>(consts);
        replacer.put("$name", name);
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(bean.getFullname(), codes, replacer);
    }

    static void genBean(Bean bean) {
        String iname = bean.getName();
        String name = "__" + iname;
        String namespace = bean.getNamespace();
        String fullname = Utils.combineFullName(namespace, name);
        List<Field> fields = bean.getSelfAndParentFields();

        List<String> codes = new ArrayList<>();

        codes.add("package $namespace;");
        codes.add("final class $name implements $iname {");
        for(Field field : fields) {
            TType ftype = field.getType();
            String fname = field.getName();
            codes.add(String.format("private %s _%s;", ftype.getInterfaceMethodReturnType(), fname));
        }
        if(!fields.isEmpty()) {
            codes.add(String.format("public $name() { this(%s); }",
                    fields.stream().map(f -> f.getType().getDefaultBeanValue()).collect(Collectors.joining(","))
            ));
        }
        codes.add(String.format("public $name(%s) {\n %s \n}",
                fields.stream().map(f -> String.format("%s _%s_", f.getType().getInterfaceMethodReturnType(), f.getName())).collect(Collectors.joining(",")),
                fields.stream().map(f -> String.format("this._%s = _%s_;", f.getName(), f.getName())).collect(Collectors.joining("\n"))
        ));

        if(bean.getParent() != null) {
            codes.add(String.format("public static final int TYPEID = %s;", bean.getId()));
            codes.add("public int getBeanId() { return TYPEID; }");
        }

        codes.add(String.format("@Override public String toString() { return \"$name{\" + %s + \"}\"; }",
                (!fields.isEmpty()) ? fields.stream().map(f -> String.format("\"%s=\" + get%s()", f.getName(), Utils.upperCaseFirstChat(f.getName()))).collect(Collectors.joining("+ \",\" +")) : "\"\""));

        for(Field field : fields) {
            final TType ftype = field.getType();
            final String fname = field.getName();
            final String tname = ftype.getInterfaceMethodReturnType();
            final String uname = Utils.upperCaseFirstChat(fname);
            if (ftype.hasSetter()) {
                final String lname = "Log" + fname;
                final int index = field.getId();
                codes.add(String.format(
                        "private final class %s extends $FIELD_LOG { %s(%s value) { this.value = value; } %s value; @Override public void commit() { $name.this._%s = this.value; }}",
                        lname, lname, tname, tname, fname));

                codes.add(String.format("public %s get%s() { $TRANSACTION txn = $TRANSACTION.get(); %s log = (%s)txn.getField(this, %s); return log == null ? _%s : log.value; }",
                        tname, uname, lname, lname, index, fname));
                if(!ftype.maybeNull()) {
                    codes.add(String.format("public void set%s(%s x) { $TRANSACTION txn = $TRANSACTION.get(); txn.putField(this, %s, new %s(x)); }",
                            uname, tname, index, lname));
                    codes.add(String.format("public %s apply%s(%s transform) { $TRANSACTION txn = $TRANSACTION.get(); "
                                    + "%s _result_ = transform.apply(_%s); txn.putField(this, %s, new %s(_result_)); return _result_; }",
                            tname, uname, ftype.getApplyInterfaceType(), tname, fname, index, lname));
                } else {
                    codes.add(String.format("public void set%s(%s x) { if(x == null) throw new NullPointerException(); $TRANSACTION txn = $TRANSACTION.get(); txn.putField(this, %s, new %s(x)); }",
                            uname, tname, index, lname));
                    codes.add(String.format("public %s apply%s(%s transform) { $TRANSACTION txn = $TRANSACTION.get(); "
                                    + "%s _result_ = transform.apply(_%s); if(_result_ == null) throw new NullPointerException(); txn.putField(this, %s, new %s(_result_)); return _result_; }",
                            tname, uname, ftype.getApplyInterfaceType(), tname, fname, index, lname));
                }
            } else {
                codes.add(String.format("  public %s get%s() { return _%s; }", tname, uname, fname));
            }
        }

        codes.add("private $TKEY _root_;");
        codes.add("@Override public $TKEY getRootDirectly() { return _root_; } ");
        codes.add("@Override public void setRootDirectly($TKEY root) { _root_ = root; }");
        codes.add(String.format("@Override public void setChildrenRootInTxn($TRANSACTION txn, $TKEY root) { %s }",
                fields.stream().filter(f -> !f.getType().hasSetter()).map(f -> String.format("this._%s.setRootInTxn(txn, root);", f.getName())).collect(Collectors.joining("\n"))));
        codes.add(String.format("@Override public void applyChildrenRootInTxn($TKEY root) { %s }",
                fields.stream().filter(f -> !f.getType().hasSetter()).map(f -> String.format("this._%s.applyRootInTxn(root);", f.getName())).collect(Collectors.joining("\n"))));

        codes.add(String.format("@Override public $name copy() { return new $name(%s); }",
                fields.stream().map(f -> f.getType().toCopyBean("get" + Utils.upperCaseFirstChat(f.getName()) + "()")).collect(Collectors.joining(","))
        ));
        codes.add(String.format("@Override public $name noTransactionCopy() { return new $name(%s); }",
                fields.stream().map(f -> f.getType().toNoTransactionCopyBean("_" + f.getName())).collect(Collectors.joining(","))));

        codes.add("@Override public void marshal($BSTREAM _os_) {");

        codes.add(String.format("_os_.writeCompactUint(%d);", fields.size()));
        for(Field field : fields) {
            final TType ftype = field.getType();
            final String fname = "this._" + field.getName();
            codes.add(String.format("_os_.writeFixShort((short)(%5s|%3s));", ftype.getTagid(), field.getId()));
            if(!ftype.isPredictable()) {
                codes.add("{final $BSTREAM _temp_ = _os_; _os_ = new $BSTREAM();");
            }
            codes.add(ftype.marshal("_os_", fname));
            if(!ftype.isPredictable()) {
                codes.add("_os_.writeTo(_temp_); _os_ = _temp_;}");
            }
        }
        codes.add("}");
        codes.add("@Override public void unmarshal($BSTREAM _os_) {");

        codes.add("for(int _var_num_ = _os_.readCompactUint() ; _var_num_-- > 0 ; ) {");
        codes.add("final short _id_ = _os_.readFixShort();");
        codes.add("switch(_id_) {");

        for(Field field : fields) {
            final TType ftype = field.getType();
            final int id = field.getId();
            final String fname = "this._" + field.getName();
            codes.add(String.format("case (%5s|%3s):", ftype.getTagid(), id));
            final boolean notPredictable = !ftype.isPredictable();
            if(notPredictable) {
                codes.add("{final $BSTREAM _temp_ = _os_; _os_ = $BSTREAM.wrap(_temp_.readBinary());");
            }
            codes.add(ftype.unmarshal("_os_", fname));
            if(notPredictable) {
                codes.add("_os_ = _temp_;}");
            }
            codes.add("break;");
            for(TType compatibleType : ftype.getCompatibleTypes()) {
                codes.add(String.format("case (%5s|%3s):", compatibleType.getTagid(), id));
                if(notPredictable) {
                    codes.add("{final $BSTREAM _temp_ = _os_; _os_ = $BSTREAM.wrap(_temp_.readBinary());");
                }
                codes.add(compatibleType.unmarshal("_os_", fname));
                if(notPredictable) {
                    codes.add("_os_ = _temp_;}");
                }
                codes.add("break;");
            }
        }
        codes.add("default: $BEAN.skipUnknownField(_id_, _os_);");
        codes.add("}");
        codes.add("}");
        codes.add("}");

        codes.add("}");

        final Map<String, String> replacer = new HashMap<>(consts);
        replacer.put("$name", name);
        replacer.put("$iname", iname);
        replacer.put("$namespace", namespace);
        replacer.put("$fullname", fullname);
        Utils.saveByNamespace(fullname, codes, replacer);
    }

    static void genTable(Table table) {
        final List<String> cs = new ArrayList<>();
        final String name = table.getName();
        final String lname = name.toLowerCase();
        final String uname = Utils.upperCaseFirstChat(name);
        final TType kType = table.getKtype();
        final TType vType = table.getVtype();
        final boolean isDynamic = vType.isBean() && ((TBean)vType).getBean().isDynamic();

        cs.add("package $namespace;");

        cs.add("public final class $name {");

//        cs.add("public static long nextid() { return xdb.Xdb.getNormalExecutor().nextid(table); }");
        cs.add(String.format("private static final $TABLE<$ktype,$vtype> table = new $TABLE<$ktype,$vtype>(\"$lname\", $persistent, %s){", vType.isBean()));
        cs.add(String.format("@Override public void marshalKey($BSTREAM os, $ktype key) { %s; }", kType.marshal("os", "key")));
        cs.add(String.format("@Override public $ktype unmarshalKey($BSTREAM os) { return %s; }", kType.getUnmarshalMethod("os")));
        cs.add(String.format("@Override public void marshalValue($BSTREAM os, $vtype value) { %s; }", vType.marshal("os", "value")));
        cs.add(String.format("@Override public $vtype unmarshalValue($BSTREAM os) { return %s; }", vType.getUnmarshalMethod("os")));
        cs.add(String.format("@Override public $TKEY makeTKey($ktype key) { return $TKEY.%s(getId(), key); }", kType.getNewKeyMethod()));
        cs.add("};");
        cs.add("public static $TABLE<$ktype, $vtype> getTable() { return table; }");
        cs.add("public static $vtype get($ktype key) { return table.get(key); }");
        if(!isDynamic) {
            cs.add(String.format("public static $vtype createIfNotExist($ktype key) { $vtype value = get(key); if(value == null) { value = %s; table.add(key, value); } return value; }", vType.getDefaultBeanValue()));
        }
        cs.add("public static void insert($ktype key, $vtype value) { table.insert(key, value); }");
//        if(kType instanceof TLong) {
//            cs.add("public static long insert($vtype value) { long id = xdb.Xdb.getNormalExecutor().nextid(table); table.insert(id, value); return id; }");
//        }
        cs.add("public static void remove($ktype key) { table.delete(key); }");
        cs.add("public static void put($ktype key, $vtype value) { table.add(key, value); }");
        cs.add("public static $vtype select($ktype key) { return table.select(key); }");
        cs.add("public static <T> T select($ktype key, java.util.function.Function<$vtype,T> apply) { return table.select(key, apply); }");

        cs.add("");

        /*
        for(Field field : ((TBean)table.getVtype()).getBean().getFields()) {
            final String fname = field.getName().toLowerCase();
            final Type ftype = field.getType();

                cs.add(String.format("public static %s %s($ktype key) { return table.select(key, $vtype::%s); }",
                        ftype.getBoxType(), fname, fname));
        }
        */
        cs.add("}");

        final Map<String, String> replacer = new HashMap<>(consts);
        replacer.put("$name", name);
        replacer.put("$namespace", table.getNamespace());
        replacer.put("$lname", lname);
        replacer.put("$uname", uname);
        replacer.put("$ktype", kType.getBoxType());
        replacer.put("$vtype", vType.getBoxType());
        replacer.put("$persistent", Boolean.toString(table.isPersist()));

        Utils.saveByNamespace(table.getFullname(), cs, replacer);
    }

    static void genAllTables() {
        final List<String> cs = new ArrayList<>();
        String fullname = Main.allTablesFullName;
        int lastIndexDot = fullname.lastIndexOf('.');
        String name = fullname.substring(lastIndexDot + 1);
        String namespace = fullname.substring(0, lastIndexDot);
        cs.add("package $namespace;");
        cs.add("public final class $name {");
        cs.add("private final static java.util.List<$TABLE<?,?>> tables = java.util.Arrays.asList(");
        cs.add(Table.getTable().values().stream().map(t -> t.getFullname() + ".getTable()").collect(Collectors.joining(",")));
        cs.add(");");
        cs.add("public static java.util.List<$TABLE<?,?>> getTables() { return tables; }");
        cs.add("}");

        final Map<String, String> replacer = new HashMap<>(consts);
        replacer.put("$name", name);
        replacer.put("$namespace", namespace);
        Utils.saveByNamespace(fullname, cs, replacer);
    }

}

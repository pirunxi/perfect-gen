package genmsg.lans.lua;

import genmsg.*;
import genmsg.type.TType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Generator {

    public static void gen() {
        Utils.suffix = "java";

        genBeans(Utils.getExportBeans());
        genMsgs(Utils.getExportMsgs());
        genRefs(Utils.getExportRefSets());
    }

    private static void genBeans(Collection<Bean> beans) {
        final ArrayList<String> ls = new ArrayList<>();
        ls.add("return {");

        for(Bean b : beans) {
            ls.add("{");
            ls.add(String.format("name = '%s', %s %s", b.getFullname(),
                    (b.getTypeId() != 0 ? String.format("typeid=%s,", b.getTypeId()) : ""),
                    (b.getChildren().isEmpty()) ? "" : ("children={" + (b.getChildren().stream().map(c -> "'" + c.getFullname() + "'").collect(Collectors.joining(","))) + "},")));
            if(b.getParent() != null) {
                ls.add(String.format("parent = '%s',", b.getParent().getFullname()));
            }
            ls.add("fields={");
            for(Field e : b.getFields()) {
                TType ftype = e.getType();
                ls.add(String.format("{name = '%s', %s},", e.getName(), ftype.luaTypeDefine()));
            }
            ls.add("}");
            ls.add("},");
        }

        ls.add("}");

        final String outFile = Utils.combine(Main.codeDir, "beans.lua");
        Utils.save(outFile, ls);
    }

    private static void genMsgs(Collection<MsgLike> msgs) {
        final ArrayList<String> ls = new ArrayList<>();
        ls.add("return {");

        for(MsgLike b : msgs) {
            ls.add("{");
            ls.add(String.format("name = '%s', typeid=%s,", b.getFullname(), b.getTypeId()));
            ls.add("fields={");
            for(Field e : ((Message)b).getFields()) {
                TType ftype = e.getType();
                ls.add(String.format("{name = '%s', %s},", e.getName(), ftype.luaTypeDefine()));
            }
            ls.add("}");
            ls.add("},");
        }

        ls.add("}");

        final String outFile = Utils.combine(Main.codeDir, "msgs.lua");
        Utils.save(outFile, ls);
    }

    private static void genRefs(Collection<RefSet> refs) {
        final ArrayList<String> ls = new ArrayList<>();
        ls.add("return {");

        for(RefSet ref : refs) {
            ls.add(String.format("%s = {%s},", ref.getName(),
                    ref.getDepMsgs().stream().map(m -> Integer.toString(MsgLike.get(m).getTypeId())).collect(Collectors.joining(","))));
        }

        ls.add("}");

        final String outFile = Utils.combine(Main.codeDir, "refs.lua");
        Utils.save(outFile, ls);
    }

}

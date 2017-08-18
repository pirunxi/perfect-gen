package genmsg;

import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Main {

    public static String xmlFileName;
    public static String codeDir;
    public static String lan;
    public static String group;
    public static boolean verbose;

    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar genmsg.jar [options]");
        System.out.println("    -xml  <xml file>     root xml file");
        System.out.println("    -codedir <code dir>        output code directory.");
        System.out.println("    -lan <lan>  java or lua or cs");
        System.out.println("    -group <group>");
        System.out.println("    -verbose    print detail information");
        System.out.println("    --help show usage");

        Runtime.getRuntime().exit(1);
    }

    private static String getArg(String[] argv, int index) {
        if(index >= argv.length)
            usage("not enough arguments");
        return argv[index];
    }

    public static void main(String[] argv) throws Exception {
        for(int i = 0 ; i < argv.length ; i++) {
            switch (argv[i]) {
                case "-xml" : xmlFileName = getArg(argv, ++i); break;
                case "-codedir" : codeDir = getArg(argv, ++i); break;
                case "-lan" : lan = getArg(argv, ++i); break;
                case "-group" : group = getArg(argv, ++i); break;
                case "-verbose" : verbose = true; break;
                case "-help":
                case "--help": usage("");
                default: usage("unknown arg:" + argv[i]);
            }
        }

        if(xmlFileName == null)
            usage("-xml missing");
        if(codeDir == null)
            usage("-codedir missing");
        if(lan == null)
            usage("-lan missing");
        if(group == null)
            usage("-group missing");

        Utils.codeDir = codeDir;
        Utils.bverbose = verbose;

        load(
                DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFileName).getDocumentElement(), "",
                Paths.get(xmlFileName).getParent().toAbsolutePath().toString());

        Utils.deleteDirectory(codeDir);
        gen();
    }

    static void load(Element root, String parentNamespace, String xmlPath) {
        final String moduleName = root.getAttribute("name");
        if(moduleName.isEmpty())
            throw new RuntimeException("module name is missing");
        final String namespace = parentNamespace.isEmpty() ? moduleName : parentNamespace + "." + moduleName;
        System.out.println("module " + namespace);

        Utils.foreach(root, (tag, ele) -> {
            switch(tag) {
                case "include" : {
                    final String relatePath = ele.getAttribute("name");
                    Path includeXmlPath = Paths.get(xmlPath, relatePath);
                    System.out.println("include " + includeXmlPath);
                    try {
                        load(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(includeXmlPath.toString()).getDocumentElement(), "", includeXmlPath.getParent().toAbsolutePath().toString());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    break;
                }
                case "module": {
                    load(ele, namespace, xmlPath);
                    break;
                }
                case "msg" : {
                    Message.add(new Message(namespace, ele));
                    break;
                }
                /*
                case "rpc" : {
                    Message.add(new Rpc(namespace, ele));
                    break;
                }
                */
                case "bean" : {
                    Bean.add(new Bean(namespace, ele));
                    break;
                }
                case "ref" : {
                    RefSet.add(new RefSet(ele));
                    break;
                }
                default:
                    Utils.error("unknown tag:%s", tag);
            }
        });

        for (Bean bean : Bean.getBeans().values()) {
            bean.resolve();
        }
        for(MsgLike msg : Message.getMsgs().values()) {
            msg.resolve();
        }
        for(RefSet ref : RefSet.getRefs().values()) {
            ref.resolve();
        }

        for (Bean bean : Bean.getBeans().values()) {
            bean.collectDependences();
        }
        for(MsgLike msg : Message.getMsgs().values()) {
            msg.collectDependences();
        }
        for(RefSet ref : RefSet.getRefs().values()) {
            ref.collectDependences();
        }
    }

    private static void gen() {
        switch (lan) {
            case "java" : genmsg.lans.java.Generator.gen(); break;
            case "lua" : genmsg.lans.lua.Generator.gen(); break;
            default: Utils.error("unknown lan:" + lan);
        }
    }
}

package gendb;

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
    public static String outputEncoding = "utf-8";
    public static String allTablesFullName = "db._Tables_";

    private static void usage(String reason) {
        System.out.println(reason);

        System.out.println("Usage: java -jar gendb.jar [options]");
        System.out.println("    -xml       root xml file");
        System.out.println("    -codedir         output code directory.");
        System.out.println("    -tablesname alltablsnames default db._Tables_");
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
                case "-tablesname" : allTablesFullName = getArg(argv, ++i); break;
                case "-help":
                case "--help": usage("");
                default: usage("unknown arg:" + argv[i]);
            }
        }

        if(xmlFileName == null)
            usage("-xml missing");
        if(codeDir == null)
            usage("-codedir missing");
        Utils.codeDir = codeDir;

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
                case "table" : {
                    final Table table = new Table(namespace, ele);
                    Table.add(table);
                    break;
                }
                case "bean" : {
                    final Bean bean = new Bean(namespace, ele);
                    Bean.add(bean);
                    break;
                }
                default:
                    Utils.error("unknown tag:%s", tag);
            }
        });

        for (Table table : Table.getTable().values()) {
            table.init();
        }
        for (Bean bean : Bean.getBeans().values()) {
            bean.init();
        }
    }

    static void gen() {
        for(Bean bean : Bean.getBeans().values()) {
            Generator.genInterface(bean);
            if(!bean.isDynamic())
                Generator.genBean(bean);
        }

        for(Table table : Table.getTable().values()) {
            Generator.genTable(table);
        }

        Generator.genAllTables();
    }
}

package gendb;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Utils {
    public static boolean bverbose = false;
    public static void verbose(String fmt, Object... params) {
        if(bverbose)
            System.out.println(String.format(fmt, params));
    }

    public static void verbose(Object msg) {
        if(bverbose)
            System.out.println(msg);
    }

    public static String getAttrOrDefualt(Element ele, String attrName, String def) {
        String value = ele.getAttribute(attrName);
        return value.isEmpty() ? def : value;
    }

    public static String combineFullName(String namespace, String name) {
        return namespace.isEmpty() ? name : namespace + "." + name;
    }


    public static String combine(String parent, String sub) {
        return parent.isEmpty() ? sub : parent + "/" + sub;
    }

    public static String outputEncoding = "utf8";
    public static void save(String file, List<String> lines) {
        save(file, lines.stream().collect(Collectors.joining("\n")));
    }

    public static void save(String file, String text) {
        try {
            File f = new File(file);
            if(!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            Files.write(new File(file).toPath(), text.getBytes(outputEncoding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Element> getChildsByTagName(Element data, String tag) {
        ArrayList<Element> eles = new ArrayList<Element>();
        final NodeList nodes = data.getChildNodes();
        for(int i = 0 ; i < nodes.getLength() ; i++) {
            final Node node = nodes.item(i);
            if (Node.ELEMENT_NODE != node.getNodeType() || !tag.equals(node.getNodeName())) continue;
            eles.add((Element)node);
        }
        return eles;
    }

    public interface Visitor {
        void onTag(String tagName, Element data);
    }
    public static void foreach(Element data, Visitor visitor)  {
        final NodeList nodes = data.getChildNodes();
        for(int i = 0, n = nodes.getLength(); i < n ; i++) {
            final Node node = nodes.item(i);
            if (Node.ELEMENT_NODE != node.getNodeType()) continue;
            Element ele = (Element) node;
            visitor.onTag(ele.getNodeName(), ele);
        }
    }

    public static String upperCaseFirstChat(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
    }

    public static void saveByPath(String file, String text) {
        try {
            File f = new File(file);
            if(!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            Files.write(new File(file).toPath(), text.getBytes(Main.outputEncoding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String codeDir;

    public static void saveByNamespace(String fullTypeName, List<String> codes) {
        final String outFile = String.format("%s/%s.java", codeDir, fullTypeName.replace('.', '/'));
        Utils.saveByPath(outFile, codes.stream().collect(Collectors.joining("\n")));
    }

    public static String suffix = "java";
    public static void saveByNamespace(String fullTypeName, List<String> codes, Map<String, String> replacer) {
        final String outFile = String.format("%s/%s.%s", codeDir, fullTypeName.replace('.', '/'), suffix);
        String code = codes.stream().collect(Collectors.joining("\n"));
        for(Map.Entry<String, String> e : replacer.entrySet()) {
            code = code.replace(e.getKey(), e.getValue());
        }
        Utils.saveByPath(outFile, code);
    }

    static public void deleteDirectory(String dir) {
        deleteDirectory(new File(dir));
    }

    static public void deleteDirectory(File path) {
        if (path.exists()) {
            for (File file : path.listFiles()) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
            path.delete();
        }
    }

    public static void error(String fmt, Object... params) {
        throw new RuntimeException(String.format(fmt, params));
    }

    public static String upperFirstChar(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}

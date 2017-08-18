package gencfg;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Paths;

public final class Main {
	private static String xmlSchemeFile = "";
	public static String csvDir = "";
	public static String codeDir = "";
	static String dataDir = "";
	public static String csmarshalcodeDir = "";
	static String outputEncoding = "utf8";
	static String inputEncoding = "GB2312";
	public static boolean verbose = false;
	private static boolean check = false;
	public static String tablesName = "cfg._Tables_";
	private static String inputLocalizedFile = null;
	private static String outputLocalizedFile = null;
	private static String outputUnlocalizedFile = null;

	public static final String magicStringForNewLine = "#enter#";
	public static final String magicStringForComma = "#comma#";
	
	private static String language = null;
	public static String exportGroup = "all";


	
	public static void main(String[] args) throws Exception {
		loadOptions(args);
        final long startTime = System.currentTimeMillis();
        loadDefine(xmlSchemeFile);
        verifyDefine();
		genCodes();
      	loadAndGenDatas();
        final long endTime = System.currentTimeMillis();
        System.out.printf("====> cost time %.2f s <====%n", (endTime - startTime) / 1000.0);
	}

	private static void usage(String reason) {
		System.out.println(reason);

		System.out.println("Usage: java -jar gen.jar [options]");
		System.out.println("    -lan cs or lua or java     language type");
		System.out.println("    -xml       Table root type xml file");
		System.out.println("    -codedir         output code directory");
		System.out.println("    -datadir output data directory");
		System.out.println("    -csmarshalcodedir   csharp marshalJava code output directory" );
		System.out.println("    -group server or client or all or xxx   group to export");
		System.out.println("    -verbose  show detail. default not");
		System.out.println("    -check load and check");
		System.out.println("    -localized  inputlocalizedfile:outputlocalizedfile:outputunlocalizedfile  ");
		System.out.println("    -tablesname set tables class name, default is cfg._Tables_");
		System.out.println("    --help show usage");

		Runtime.getRuntime().exit(1);
	}

	private static String Arg(String[] args, int index) {
		if(index >= args.length)
			usage("argument not enough");
		return args[index];
	}

	private static void loadOptions(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
				case "-lan":
					language =Arg(args, ++i);
					break;
				case "-xml":
					xmlSchemeFile =Arg(args, ++i);
					break;
				case "-codedir":
					codeDir =Arg(args, ++i);
					break;
				case "-csmarshalcodedir":
					csmarshalcodeDir =Arg(args, ++i);
					break;
				case "-datadir":
					dataDir =Arg(args, ++i);
					break;
				case "-group":
					exportGroup =Arg(args, ++i);
					break;
				case "-outputencoding":
					outputEncoding =Arg(args, ++i);
					break;
				case "-inputencoding":
					inputEncoding =Arg(args, ++i);
					break;
				case "-verbose":
					verbose = true;
					break;
				case "-check":
					check = true;
					break;
				case "-localized": {
					final String[] params =Arg(args, ++i).split(":");
					if(params.length != 3)
						usage("-localized");
					inputLocalizedFile = params[0];
					outputLocalizedFile = params[1];
					outputUnlocalizedFile = params[2];
					break;
				}
				case "-tablesname":
					tablesName =Arg(args, ++i);
					break;
				case "--help":
					usage("");
					break;
				default:
					usage("unknown args " + args[i]);
					break;
			}
		}

		if(xmlSchemeFile.isEmpty())
			usage("-definexml miss");
		if(csmarshalcodeDir.isEmpty() && exportGroup.isEmpty())
			usage("-group miss");
		if(codeDir.isEmpty() && language != null)
			usage("-codedir miss");
		if(codeDir.isEmpty() && dataDir.isEmpty() && csmarshalcodeDir.isEmpty() && !check)
			usage("needs -codedir or -datadir or csmarshalcodedir or -check");
		Utils.codeDir = codeDir;
		Utils.bverbose = verbose;
		csvDir = Paths.get(xmlSchemeFile).getParent().toString();
	}

	private static String curXml = "";
    private static void loadDefine(String defineXmlFile) {
    	curXml = defineXmlFile;
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(defineXmlFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		Element ele = doc.getDocumentElement();
		String namespace = ele.getAttribute("name");
		 if(namespace.isEmpty())
		 	Utils.error("xml:%s module name can't be empty", defineXmlFile);
		 loadDefine(ele, namespace, Paths.get(defineXmlFile).getParent().toString());
	}

	private static void loadDefine(Element root, String namespace, String relateDir) {
		Utils.foreach(root, (tag, ele) -> {
			switch (tag) {
				case "group":  Group.load(ele); break;
				case "enum" : new ENUM(namespace, ele); break;
				case "bean" : new Bean(namespace, ele); break;
				case "table" : new Bean(namespace, ele); new Table(namespace, ele, relateDir); break;
				case "module" : {
					final String name = ele.getAttribute("name");
					if(name.isEmpty())
						Utils.error("xml:%s module's name missing!", root);
					loadDefine(ele, Utils.combineFullName(namespace, name), relateDir);
					break;
				}
				case "include" : {
					final String oldXml = curXml;
					String file = ele.getAttribute("name");
					check(!file.isEmpty(), "import name can't be empty");
					loadDefine(Utils.combine(relateDir, file));
					curXml = oldXml;
					break;
				}
				default: {
					Utils.error("namespace:%s unknown tag:%s", namespace, tag);
				}
			}
		});
	}

	private static void check(boolean cond, String err) {
    	if(!cond)
    		Utils.error("xml:%s %s", curXml, err);
	}

	private static void verifyDefine() {
		Bean.getBeans().values().forEach(Bean::verityDefine);
		Table.getTables().values().forEach(Table::verifyDefine);
	}

	private static void loadData() throws Exception{
		if(inputLocalizedFile != null)
			Localized.Ins.load(inputLocalizedFile);
        for(Table c : Table.getTables().values()) {
			setLastLoadData(null);
			System.out.printf(".");
			final long t1 = System.currentTimeMillis();
			c.loadData();
			final long t2 = System.currentTimeMillis();
			if (t2 - t1 > 1000) {
				System.out.printf("%nload Table:%s cost time:%.2f s%n", c.getName(), (t2 - t1) / 1000.0);
			}
		}
		if(outputLocalizedFile != null)
			Localized.Ins.saveLocalizedAs(outputLocalizedFile);
		if(outputUnlocalizedFile != null)
			Localized.Ins.saveUnLocalizedAs(outputUnlocalizedFile);
	}
	
	private static void verifyData() {
		Table.getTables().values().parallelStream().forEach(Table::verifyData);
	}

	private static void genCodes() {
		if(!codeDir.isEmpty() && language != null) {
			// lua版代码就两个文件,特殊处理不删目录
			if(!language.equals("lua")) {
				Utils.deleteDirectory(codeDir);
			}
			switch (language) {
				case "java" : new gencfg.lans.java.CodeGen().gen(); break;
				case "lua" : new gencfg.lans.lua.CodeGen().gen(); break;
				case "cs" : new gencfg.lans.cs.CodeGen().gen(); break;
				default: Utils.error("unknown lang:%s", language);
			}
		}

		if(!csmarshalcodeDir.isEmpty()) {
			Utils.deleteDirectory(csmarshalcodeDir);
			new gencfg.lans.cs.XmlCodeGen().gen();
		}
	}

	private static void loadAndGenDatas() {
		if(!dataDir.isEmpty() || check) {
			try {
				loadData();
				verifyData();
			} catch(Exception e) {
				System.out.println();
				System.out.println("=================last datas=====================");
				System.out.println(lastLoadData.get());
				System.out.println("=================last datas=====================");
				e.printStackTrace();
				System.exit(1);
			}

			if(!dataDir.isEmpty()) {
				genData();
			}
		}
	}

	private static void genData() {
		Utils.deleteDirectory(dataDir);
		Table.getTables().values().forEach(c -> c.save(Main.exportGroup));
	}

	private static final ThreadLocal<Object> lastLoadData = new ThreadLocal<>();

    private static ThreadLocal<Table> curVerifyTable = new ThreadLocal<>();
    private static ThreadLocal<Object> curVerifyData = new ThreadLocal<>();

    public static Table getCurVerifyTable() {
        return curVerifyTable.get();
    }

    static void setCurVerifyTable(Table curVerifyTable) {
        Main.curVerifyTable.set(curVerifyTable);
    }

    public static Object getCurVerifyData() {
        return curVerifyData.get();
    }

    public static void setCurVerifyData(Object curVerifyData) {
        Main.curVerifyData.set(curVerifyData);
    }

    public static void setLastLoadData(Object data) {
		lastLoadData.set(data);
	}

}

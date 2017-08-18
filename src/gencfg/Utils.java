package gencfg;

import org.apache.poi.ss.usermodel.*;
import org.luaj.vm2.Globals;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Utils extends gendb.Utils {



	public static boolean existType(String name) {
		return Bean.get(name) != null || ENUM.isEnum(name);
	}

	
	public static String[] split(Element ele, String attr) {
		final String str = ele.getAttribute(attr);
		if(str.isEmpty()) {
			return new String[0];
		} else {
			return str.split(":");
		}
	}

    public static String[] split(Element ele, String attr, String delimiter) {
        final String str = ele.getAttribute(attr);
        if(str.isEmpty()) {
            return new String[0];
        } else {
            return str.split(delimiter);
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
	
	public static String getFileExtension(String file) {
		try {
			return file.substring(file.lastIndexOf('.') + 1);
		} catch (Exception e) {
			return "";
		}
	}

	public static Object parseAsXmlOrLuaOrFlatStream(String file)  {
		try {
			switch (getFileExtension(file)) {
				case "csv":
					return parseCSV(file);
				case "lne":
					return parseLineFile(file);
				case "xml":
					return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(file)).getDocumentElement();
				case "lua":
					return parseLua(file);
				default:
					return parseExcel(file);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

	public static Object parseLua(String file) {
		Globals g = JsePlatform.standardGlobals();
		g.loadfile(file).call();
		return g.loadfile(file).call();
	}
	
	private static List<List<String>> parseLineFile(String file) throws Exception {
		final List<String> lines = Files.readAllLines(new File(file).toPath());
		final List<List<String>> rowcol = new ArrayList<>();
		rowcol.add(lines);
		return rowcol;
	}

	public static List<List<String>> parseCSV(String file) throws IOException {
		return parseCSV(file, Main.inputEncoding);
	}

	public static List<List<String>> parseCSV(String file, String encoding) throws IOException {
		return CSV.parse(new BufferedReader(new InputStreamReader(new FileInputStream(new File(file)), encoding)));
	}
	
	public static List<List<String>> parseExcel(String file) throws Exception {
		final Workbook workbook = WorkbookFactory.create(new File(file));
		final List<List<String>> lines = new ArrayList<>();
		final FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		for(int i = 0 , n = workbook.getNumberOfSheets() ; i < n ; i++) {
			final Sheet sheet = workbook.getSheetAt(i);
			for(Row row : sheet) {
				final List<String> line = new ArrayList<>();
				lines.add(line);
				
				for(Cell cell : row) {
		            switch (cell.getCellType()) {
	                case Cell.CELL_TYPE_STRING:
	                    line.add(cell.getRichStringCellValue().getString());
	                    break;
	                case Cell.CELL_TYPE_NUMERIC:
	                    if (DateUtil.isCellDateFormatted(cell)) {
	                        line.add(cell.getDateCellValue().toString());
	                    } else {
	                        line.add(niceConvert(cell.getNumericCellValue()));
	                    }
	                    break;
	                case Cell.CELL_TYPE_BOOLEAN:
	                    line.add(Boolean.toString(cell.getBooleanCellValue()));
	                    break;
	                case Cell.CELL_TYPE_FORMULA:
	                    final CellValue value = evaluator.evaluate(cell);
	                    switch(value.getCellType()) {
	                    case Cell.CELL_TYPE_BOOLEAN: line.add(Boolean.toString(value.getBooleanValue())); break;
	                    case Cell.CELL_TYPE_NUMERIC: line.add(niceConvert(value.getNumberValue())); break;
	                    case Cell.CELL_TYPE_STRING: line.add(value.getStringValue()); break;
	                    case Cell.CELL_TYPE_BLANK : break;
	                    default : throw new RuntimeException("unknown formula result:" + value);
	                    }
	                    break;
	                case Cell.CELL_TYPE_BLANK:
	                	break;
	                default:
	                    throw new RuntimeException("unknown cell type:" + cell.getCellType());
	            }
				}
			}
			
		}
		return lines;	
	}
	
	public static String niceConvert(double value) {
		final long lvalue = (long)value;
		if(lvalue == value) {
			return Long.toString(lvalue);
		} else {
			return Double.toString(value);
		}
		
	}
	
	public static void createDirIfNotExist(String dir) {
		final File dirFile = new File(dir);
		if(!dirFile.exists()) {
			dirFile.mkdirs();
		}
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
	
	public static boolean checkInGroup(Set<String> toCheck, String exportGroup) {
		if(toCheck.contains("all")) return true;
		if(exportGroup.equals("all")) return true;
		return toCheck.contains(exportGroup);
	}
	
	public static void main(String[] args) throws Exception {
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.csv"));
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.xlsx"));
//		System.out.println(parse("F:/cfggen.git/trunk/csv/test.xls"));
        System.out.println(parseAsXmlOrLuaOrFlatStream("D:\\workspace\\luxianres\\branches\\test826\\csv\\ectype\\ectypebasic2.xml"));
	}
}

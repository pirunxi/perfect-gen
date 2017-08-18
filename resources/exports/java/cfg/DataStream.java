package cfg;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DataStream {
	private final List<String> lines;
	private int index;
	
	public DataStream(List<String> data) {
		lines = data;
		index = 0;
	}
	
	private String getNext() {
		return index < lines.size() ? lines.get(index++) : null;
	}
	
	private void error(String err) {
		throw new RuntimeException(String.format("%d %s", index, err));
	}
	
	private String getNextAndCheckNotEmpty() {
		final String s = getNext();
		if(s == null) 
			error("read not enough");
		return s;
	}
	
	public boolean getBool() {
		final String s = getNextAndCheckNotEmpty().toLowerCase();
		switch (s) {
			case "true":
				return true;
			case "false":
				return false;
			default:
				error(s + " isn't bool");
				break;
		}
		return false;
	}
	
	public int getInt() {
		final String s = getNextAndCheckNotEmpty();
		return Integer.parseInt(s);
	}
	
	public long getLong() {
		final String s = getNextAndCheckNotEmpty();
		return Long.parseLong(s);
	}
	
	public float getFloat() {
		final String s = getNextAndCheckNotEmpty();
		return Float.parseFloat(s);
	}

	public double getDouble() {
		final String s = getNextAndCheckNotEmpty();
		return Double.parseDouble(s);
	}

	public String getString() {
		return getNextAndCheckNotEmpty().replace("#enter#", "\n").replace("#comma#", ",");
	}

	public cfg.CfgObject getObject(String name) {
		try {
			return (cfg.CfgObject)Class.forName(name).getConstructor(cfg.DataStream.class).newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static List<DataStream> records(String dataFile, String inputEncoding) {
		try {
			final List<DataStream> dss = new ArrayList<>();
			for(String line : Files.readAllLines(new File(dataFile).toPath(), Charset.forName(inputEncoding))) {
				final String[] datas;
				if(line.endsWith(",")) {
					datas = (line + " ").split(",");
					datas[datas.length - 1] = "";
				} else {
					datas = line.split(",");
				}
				dss.add(new DataStream(Arrays.asList(datas)));
			}
			return dss;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("data file:" + dataFile + " loads fail!");
		}
	}
}

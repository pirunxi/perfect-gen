package gencfg.data;

import gencfg.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public final class DataMarshal {
	private final List<String> line = new ArrayList<>();
	
	private DataMarshal put(String x) {
		line.add(x);
		return this;
	}
	
	public DataMarshal putBool(boolean x) {
		return put(Boolean.toString(x));
	}
	
	public DataMarshal putInt(int x) {
		return put(Integer.toString(x));
	}
	
	public DataMarshal putLong(long x) {
		return put(Long.toString(x));
	}
	
	public DataMarshal putFloat(float x) {
		final long lx = (long)x;
		final String s;
		if(lx == x)
			s = Long.toString(lx);
		else {
            float y = x;
            for(int i = 1 ; i < 8; i++) {
                y *= 10;
                if(Math.abs(y - Math.round(y)) < 1.0e-7) {
                    return put(String.format("%." + i + "f", x));
                }
            }
            s = String.format("%f", x);
        }

		return put(s);
	}

	public DataMarshal putDouble(double x) {
		final long lx = (long)x;
		final String s;
		if(lx == x)
			s = Long.toString(lx);
		else {
			double y = x;
			for(int i = 1 ; i < 8; i++) {
				y *= 10;
				if(Math.abs(y - Math.round(y)) < 1.0e-14) {
					return put(String.format("%." + i + "f", x));
				}
			}
			s = String.format("%f", x);
		}

		return put(s);
	}
	
	public DataMarshal putString(String x) {
		return put(x.replace("\r\n", Main.magicStringForNewLine).replace("\n\r", Main.magicStringForNewLine)
				.replace("\r", Main.magicStringForNewLine).replace("\n", Main.magicStringForNewLine)
				.replace(",", Main.magicStringForComma));
	}
	
	public String toData() {
		return line.stream().collect(Collectors.joining(","));
	}
}

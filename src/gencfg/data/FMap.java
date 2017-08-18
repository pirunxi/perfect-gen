package gencfg.data;

import gencfg.type.TType;

import java.util.Map;

public class FMap extends Data {
	public final Map<Data, Data> values;

	public FMap(TType type, Map<Data, Data> values) {
		super(type);
		this.values = values;
	}


	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(Map.Entry<Data, Data> e : values.entrySet()) {
			sb.append("").append(e.getKey()).append("=>").append(e.getValue()).append(",");
		}
		sb.append("}");
		return sb.toString();
	}
	

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public void verify() {
		for(Map.Entry<Data, Data> e : values.entrySet()) {
			e.getKey().verify();
			e.getValue().verify();
		}
	}
}

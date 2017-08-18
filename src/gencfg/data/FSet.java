package gencfg.data;

import gencfg.type.TType;

import java.util.Set;

public class FSet extends Data {
	final public Set<Data> values;
	public FSet(TType type, Set<Data> values) {
		super(type);
		this.values = values;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		values.forEach(v -> sb.append(v).append(","));
		sb.append("}");
		return sb.toString();
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}

	@Override
	public void verify() {
		for(Data data : values)
			data.verify();
	}

	@Override
	public boolean isNull() {
		return false;
	}
}

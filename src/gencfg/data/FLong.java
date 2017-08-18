package gencfg.data;

import gencfg.Table;
import gencfg.type.TType;

import java.util.stream.Stream;

public final class FLong extends Data {
	public FLong(TType type, long value) {
		super(type);
		this.value = value;
	}

	public final long value;

	public String toString() {
		return "long:" + value;
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FLong)) return false;
		return value == ((FLong)o).value;
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
	
	@Override
	public boolean isNull() {
		return value == NULL_VALUE;
	}

	@Override
	public void verify() {
		{
			Table[] refs = type.getRefs();
			if(refs != null) {
				check(Stream.of(refs).anyMatch(t -> t.existKey(this)), "key:" + this + " can't find in " + type.getAttr("ref"));
			}
		}
	}
}

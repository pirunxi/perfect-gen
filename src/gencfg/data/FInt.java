package gencfg.data;

import gencfg.Table;
import gencfg.type.TType;

import java.util.stream.Stream;

public final class FInt extends Data {
	public FInt(TType type, int value) {
		super(type);
		this.value = value;
	}

	public final int value;

	public String toString() {
		return "int:" + value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FInt)) return false;
		return value == ((FInt)o).value;
	}
	
	@Override
	public int hashCode() {
		return value;
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

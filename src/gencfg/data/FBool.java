package gencfg.data;

import gencfg.type.TType;

public class FBool extends Data {
	public FBool(TType type, boolean v) {
		super(type);
		value = v;
	}

	public final boolean value;
	
	public String toString() {
		return "bool:" + value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FBool)) return false;
		return value == ((FBool)o).value;
	}
	
	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public void verify() {

	}
}

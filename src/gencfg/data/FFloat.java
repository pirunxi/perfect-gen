package gencfg.data;

import gencfg.type.TType;

public class FFloat extends Data {
	public FFloat(TType type, float value) {
		super(type);
		this.value = value;
	}

	public final float value;
	
	public String toString() {
		return String.format("float:%.2f", value);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FFloat)) return false;
		return value == ((FFloat)o).value;
	}
	
	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}

	@Override
	public boolean isNull() {
		return value == NULL_VALUE;
	}

	@Override
	public void verify() {

	}
}

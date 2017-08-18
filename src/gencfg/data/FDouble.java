package gencfg.data;

import gencfg.type.TType;

public class FDouble extends Data {
	public FDouble(TType type, double value) {
		super(type);
		this.value = value;
	}

	public final double value;
	
	public String toString() {
		return String.format("double:%.2f", value);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FDouble)) return false;
		return value == ((FDouble)o).value;
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

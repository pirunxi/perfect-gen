package gencfg.data;

import gencfg.Utils;
import gencfg.type.TType;

public abstract class Data {
	public final static String NULL_STR = "null";
	public final static int NULL_VALUE = -1;
	
	protected final TType type;
	
	public Data(TType type) {
		this.type = type;
	}
	
	public final TType getType() {
		return type;
	}

	public abstract boolean isNull();

	public abstract void accept(Visitor visitor);

	public abstract void verify();

	protected void check(boolean cond, String err) {
		if(!cond)
			Utils.error("%s", err);
	}
}

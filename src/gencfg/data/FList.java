package gencfg.data;

import gencfg.Utils;
import gencfg.type.TType;

import java.util.HashSet;
import java.util.List;

public class FList extends Data {
	public final List<Data> values;

	public FList(TType type, List<Data> values) {
		super(type);
		this.values = values;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}

	@Override
	public void verify() {
		for(Data data : values)
			data.verify();

		if(type.getIndexs() != null) {
			HashSet<Data> indexs = new HashSet<Data>();
			for(String idx : type.getIndexs()) {
				indexs.clear();
				for(Data data : values) {
					if(!indexs.add(((FBean)data).getField(idx))) {
						Utils.error("type:%s index:%s key:%s duplicate", type.getBoxType(), idx, data);
					}
				}
			}
		}
	}


	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("{");
		values.forEach(v -> sb.append(v).append(","));
		sb.append("}");
		return sb.toString();
	}
}

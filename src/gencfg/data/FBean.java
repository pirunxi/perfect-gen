package gencfg.data;

import gencfg.Bean;
import gencfg.Field;
import gencfg.type.TBean;

import java.util.List;

public class FBean extends Data {
	private final List<Data> values;
	private final Bean bean;

	public FBean(TBean type, Bean actual, List<Data> values) {
		super(type);
		this.bean = actual;
		this.values = values;
	}

	public Bean getActualBean() {
		return bean;
	}

	public final List<Data> getValues() {
		return values;
	}
	
	public final Data getField(String name) {
		List<Field> fields = bean.getSelfAndParentFields();
		int idx = 0;
		for(Data t : values) {
			if(fields.get(idx).getName().equals(name))
				return t;
			idx++;
		}
		return null;
	}

	public final Data getIndexField(int index) {
		return values.get(index);
	}


	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.getBoxType()).append("{");
		List<Field> fields = bean.getSelfAndParentFields();
		for(int i = 0, n = fields.size() ; i < n ; i++) {
			sb.append(fields.get(i).getName()).append(":").append(values.get(i)).append(",");
		}
		sb.append("}");
		return sb.toString();
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

package gencfg.data;

import gencfg.Bean;
import gencfg.Field;
import gencfg.type.TBean;

import java.util.Map;


public class DataVisitor implements Visitor {
	private final String exportGroup;
	private final DataMarshal fs = new DataMarshal();
	public DataVisitor(String exportGroup) {
		this.exportGroup = exportGroup;
	}
	
	@Override
	public void accept(FBool x) {
			fs.putBool(x.value);
	}

	@Override
	public void accept(FFloat x) {
			fs.putFloat(x.value);
	}

	@Override
	public void accept(FInt x) {
			fs.putInt(x.value);
	}

	@Override
	public void accept(FDouble d) {
		fs.putDouble(d.value);
	}

	@Override
	public void accept(FLong x) {
			fs.putLong(x.value);
	}
	

	@Override
	public void accept(FString x) {
			fs.putString(x.value);
	}
	

	@Override
	public void accept(FEnum x) {
			fs.putInt(x.value);
	}

	@Override
	public void accept(FBean x) {
		TBean type = (TBean)x.getType();
		Bean actual = x.getActualBean();
		if(type.getBean().isDynamic()) {
			fs.putString(actual.getName());
		}
		int index = 0;
		for(Field f : actual.getSelfAndParentFields()) {
			if(f.checkInGroup(exportGroup)) {
				x.getValues().get(index).accept(this);
			}
			index++;
		}
	}

	@Override
	public void accept(FList x) {
			fs.putInt(x.values.size());
			for(Data field : x.values) {
				field.accept(this);
			}
	}
	
	@Override
	public void accept(FMap x) {
			fs.putInt(x.values.size());
			for(Map.Entry<Data, Data> field : x.values.entrySet()) {
				field.getKey().accept(this);
				field.getValue().accept(this);
			}
	}

	@Override
	public void accept(FSet x) {
			fs.putInt(x.values.size());
			for(Data field : x.values) {
				field.accept(this);
			}
	}
	
	public final String toData() {
		return fs.toData();
	}
}

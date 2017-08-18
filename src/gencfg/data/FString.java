package gencfg.data;

import gencfg.Localized;
import gencfg.Main;
import gencfg.Table;
import gencfg.Utils;
import gencfg.type.TType;

import java.io.File;
import java.util.stream.Stream;

public class FString extends Data {
	public FString(TType type, String value) {
		super(type);
		this.value = checkLocalized(value);
	}

	public final String value;

	public String toString() {
		return "string:'" + value + "'";
	}

	private String checkLocalized(String s) {
		if(!type.isLocalized()) return s;
		Localized loc = Localized.Ins;
		if(loc.isHasLocalized()) {
			if(s.trim().isEmpty()) return s;
			final String r = loc.getLocalizedStr(s);
			if(r != null) {
				loc.addHasLocalizedStr(s, r);
				return r;
			} else {
				loc.addNotLocalizedStr(s);
			}
		}
		// 那些需要本地化的字符串有时候即使相同的串,也要映射到不同的文字。这时候需要一个tag
		// 来区分他们。
		// 原来一个字符串是 xxxyyzz, 加了tag后为 xxxyyzz@name@
		// 如果没有找到本土化映射,会自动帮它脱去尾部的 @...@
		if(s.endsWith("@")) {
			return s.substring(0, s.lastIndexOf('@', s.length() - 2));
		}
		return s;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.accept(this);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(!(o instanceof FString)) return false;
		return this.value.equals(((FString)o).value);
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public boolean isNull() {
		return value.isEmpty();
	}

	@Override
	public void verify() {
		{
			Table[] refs = type.getRefs();
			if(refs != null) {
				check(Stream.of(refs).anyMatch(t -> t.existKey(this)), "key:" + this + " can't find in " + type.getAttr("ref"));
			}
		}
		{
			String[] refPaths = type.getRefPaths();
			if(refPaths != null) {
				check(Stream.of(refPaths).anyMatch(p -> new File(Utils.combine(Main.csvDir, p.replace("*", value.toLowerCase()).replace("?", value))).exists()),
						String.format("[%s] can't refpath in %s", value, type.getAttr("refpath")));
			}
		}
	}

	//    @Override
//    public void verifyData() {
//        super.verifyData();
//        final List<String> refPaths = type.getRefPath();
//        if(!refPaths.isEmpty() && !isNull()) {
//            final List<String> finalRefPaths = refPaths.stream().map(path -> toFinalPath(path)).collect(Collectors.toList());
//            if(finalRefPaths.stream().noneMatch(path -> new File(path).exists())) {
//                errorRef(this, finalRefPaths.stream().collect(Collectors.joining("] or [", "[", "]")));
//            }
//        }
//    }
}

namespace cfg.role{
public sealed class Name : cfg.CfgObject {
public const int TYPEID = 736962585;
public override int GetTypeId() { return TYPEID; }
public readonly System.Collections.Generic.List<cfg.role.Names> firstnames;
public readonly cfg.role.Names lastnames;
public readonly System.Collections.Generic.List<cfg.role.DecorateName> deconames;
public Name(cfg.DataStream fs)  {
this.firstnames = new System.Collections.Generic.List<cfg.role.Names>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.firstnames.Add(new cfg.role.Names(fs)); }
this.lastnames = new cfg.role.Names(fs);
this.deconames = new System.Collections.Generic.List<cfg.role.DecorateName>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.deconames.Add(new cfg.role.DecorateName(fs)); }
	}
private static cfg.role.Name _values;
public static void Load(string dataPath) {
var dss = cfg.DataStream.Records(dataPath, "utf8");
if(dss.Count != 1) throw new System.Exception("table one should have only one record.");
_values = new cfg.role.Name(dss[0]);
}
public static cfg.role.Name Get() { return _values; }
}
}
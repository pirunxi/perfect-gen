namespace cfg.role{
public sealed class Names : cfg.CfgObject {
public const int TYPEID = 1371003770;
public override int GetTypeId() { return TYPEID; }
public readonly System.Collections.Generic.List<string> names;
public Names(cfg.DataStream fs)  {
this.names = new System.Collections.Generic.List<string>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.names.Add(fs.GetString()); }
	}
}
}
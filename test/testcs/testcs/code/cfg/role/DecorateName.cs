namespace cfg.role{
public sealed class DecorateName : cfg.CfgObject {
public const int TYPEID = 2056063686;
public override int GetTypeId() { return TYPEID; }
public readonly string name;
public readonly cfg.role.NamePosition position;
public readonly int weight;
public DecorateName(cfg.DataStream fs)  {
this.name = fs.GetString();
this.position = (cfg.role.NamePosition)fs.GetInt();
this.weight = fs.GetInt();
	}
}
}
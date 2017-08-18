namespace cfg.role.task{
public sealed class Index : cfg.CfgObject {
public const int TYPEID = 741691895;
public override int GetTypeId() { return TYPEID; }
public readonly int id;
public readonly int id2;
public Index(cfg.DataStream fs)  {
this.id = fs.GetInt();
this.id2 = fs.GetInt();
	}
}
}
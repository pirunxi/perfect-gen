namespace cfg.role.task{
public sealed class RoleTable4 : cfg.role.task.RoleTable3 {
public const int TYPEID = 1022083127;
public override int GetTypeId() { return TYPEID; }
public readonly string a1;
public RoleTable4(cfg.DataStream fs) : base(fs) {
this.a1 = fs.GetString();
	}
}
}
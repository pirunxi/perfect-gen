namespace cfg.role.task{
public abstract class RoleTable3 : cfg.role.task.Roletable {
public readonly int a;
public RoleTable3(cfg.DataStream fs) : base(fs) {
this.a = fs.GetInt();
	}
}
}
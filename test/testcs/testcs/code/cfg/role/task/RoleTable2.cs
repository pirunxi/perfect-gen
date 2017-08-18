namespace cfg.role.task{
public sealed class RoleTable2 : cfg.role.task.Roletable {
public const int TYPEID = 1022083125;
public override int GetTypeId() { return TYPEID; }
public readonly int a;
public readonly System.Collections.Generic.List<cfg.role.task.Index> b;
public readonly System.Collections.Generic.Dictionary<int,cfg.role.task.Index> b_id = new System.Collections.Generic.Dictionary<int,cfg.role.task.Index>();
public readonly System.Collections.Generic.Dictionary<int,cfg.role.task.Index> b_id2 = new System.Collections.Generic.Dictionary<int,cfg.role.task.Index>();
public readonly cfg.role.EProfessionType pro;
public RoleTable2(cfg.DataStream fs) : base(fs) {
this.a = fs.GetInt();
this.b = new System.Collections.Generic.List<cfg.role.task.Index>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.b.Add(new cfg.role.task.Index(fs)); }
foreach(var _v in this.b) {
this.b_id.Add(_v.id, _v);
this.b_id2.Add(_v.id2, _v);
}
this.pro = (cfg.role.EProfessionType)fs.GetInt();
	}
}
}
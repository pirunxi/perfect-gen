namespace cfg.role.task{
public sealed class RoleTable5 : cfg.role.task.RoleTable3 {
public const int TYPEID = 1022083128;
public override int GetTypeId() { return TYPEID; }
public readonly int a2;
public readonly string a3;
public readonly int a4;
public readonly System.Collections.Generic.List<int> a5;
public readonly System.Collections.Generic.HashSet<int> a6;
public readonly System.Collections.Generic.Dictionary<int,int> a7;
public RoleTable5(cfg.DataStream fs) : base(fs) {
this.a2 = fs.GetInt();
this.a3 = fs.GetString();
this.a4 = fs.GetInt();
this.a5 = new System.Collections.Generic.List<int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a5.Add(fs.GetInt()); }
this.a6 = new System.Collections.Generic.HashSet<int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a6.Add(fs.GetInt()); }
this.a7 = new System.Collections.Generic.Dictionary<int,int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a7.Add(fs.GetInt(), fs.GetInt()); }
	}
}
}
namespace cfg.role{
public sealed class TestBean : cfg.CfgObject {
public const int TYPEID = -259429904;
public override int GetTypeId() { return TYPEID; }
public readonly bool a1;
public readonly int a2;
public readonly long a3;
public readonly float a4;
public readonly double a5;
public readonly string a6;
public readonly System.Collections.Generic.List<int> a7;
public readonly System.Collections.Generic.List<float> a8;
public readonly System.Collections.Generic.HashSet<int> a11;
public readonly System.Collections.Generic.HashSet<double> a12;
public readonly System.Collections.Generic.Dictionary<int,int> a13;
public readonly System.Collections.Generic.Dictionary<int,string> a14;
public readonly System.Collections.Generic.Dictionary<int,cfg.role.Vector2> a15;
public readonly cfg.role.Vector2 a16;
public readonly cfg.role.task.Roletable a17;
public TestBean(cfg.DataStream fs)  {
this.a1 = fs.GetBool();
this.a2 = fs.GetInt();
this.a3 = fs.GetLong();
this.a4 = fs.GetFloat();
this.a5 = fs.GetDouble();
this.a6 = fs.GetString();
this.a7 = new System.Collections.Generic.List<int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a7.Add(fs.GetInt()); }
this.a8 = new System.Collections.Generic.List<float>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a8.Add(fs.GetFloat()); }
this.a11 = new System.Collections.Generic.HashSet<int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a11.Add(fs.GetInt()); }
this.a12 = new System.Collections.Generic.HashSet<double>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a12.Add(fs.GetDouble()); }
this.a13 = new System.Collections.Generic.Dictionary<int,int>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a13.Add(fs.GetInt(), fs.GetInt()); }
this.a14 = new System.Collections.Generic.Dictionary<int,string>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a14.Add(fs.GetInt(), fs.GetString()); }
this.a15 = new System.Collections.Generic.Dictionary<int,cfg.role.Vector2>(); for(int n = fs.GetInt(); n > 0 ; n--) { this.a15.Add(fs.GetInt(), new cfg.role.Vector2(fs)); }
this.a16 = new cfg.role.Vector2(fs);
this.a17 = (cfg.role.task.Roletable)(fs.GetObject("cfg.role.task." + fs.GetString()));
	}
}
}
namespace cfg.role.task{
public abstract class Roletable : cfg.CfgObject {
public readonly int bornmap;
public readonly float viewportminradius;
public Roletable(cfg.DataStream fs)  {
this.bornmap = fs.GetInt();
this.viewportminradius = fs.GetFloat();
	}
private static System.Collections.Generic.Dictionary<int,cfg.role.task.Roletable> _values;
public static void Load(string dataPath) {
var dss = cfg.DataStream.Records(dataPath, "utf8");
var temp = new System.Collections.Generic.Dictionary<int,cfg.role.task.Roletable>();
foreach(var ds in dss) { var _v = (cfg.role.task.Roletable)(ds.GetObject("cfg.role.task." + ds.GetString())); temp.Add(_v.bornmap, _v); }
_values = temp;
}
public static T Get<T>(int key) where T : cfg.role.task.Roletable { cfg.role.task.Roletable value; return _values.TryGetValue(key, out value) ? (T)value : null; }
public static System.Collections.Generic.Dictionary<int,cfg.role.task.Roletable> values() { return _values; }
}
}
namespace cfg.role{
public sealed class Profession : cfg.CfgObject {
public const int TYPEID = 1653976074;
public override int GetTypeId() { return TYPEID; }
public readonly int id;
public readonly cfg.role.EProfessionType faction;
public readonly cfg.role.GenderType gender;
public readonly bool isopen;
public readonly string modelname;
public readonly int weight;
public readonly int skillactionid;
public readonly int defaultweaponid;
public readonly int createweaponid;
public readonly int createarmourid;
public Profession(cfg.DataStream fs)  {
this.id = fs.GetInt();
this.faction = (cfg.role.EProfessionType)fs.GetInt();
this.gender = (cfg.role.GenderType)fs.GetInt();
this.isopen = fs.GetBool();
this.modelname = fs.GetString();
this.weight = fs.GetInt();
this.skillactionid = fs.GetInt();
this.defaultweaponid = fs.GetInt();
this.createweaponid = fs.GetInt();
this.createarmourid = fs.GetInt();
	}
private static System.Collections.Generic.Dictionary<int,cfg.role.Profession> _values;
public static void Load(string dataPath) {
var dss = cfg.DataStream.Records(dataPath, "utf8");
var temp = new System.Collections.Generic.Dictionary<int,cfg.role.Profession>();
foreach(var ds in dss) { var _v = new cfg.role.Profession(ds); temp.Add(_v.id, _v); }
_values = temp;
}
public static cfg.role.Profession Get(int key) { cfg.role.Profession value; return _values.TryGetValue(key, out value) ? value : null; }
public static System.Collections.Generic.Dictionary<int,cfg.role.Profession> values() { return _values; }
}
}
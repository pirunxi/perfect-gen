namespace cfg.role{
public sealed class Vector2 : cfg.CfgObject {
public const int TYPEID = 1890453249;
public override int GetTypeId() { return TYPEID; }
public readonly float x;
public readonly float y;
public Vector2(cfg.DataStream fs)  {
this.x = fs.GetFloat();
this.y = fs.GetFloat();
	}
}
}
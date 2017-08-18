namespace cfg{
public sealed class _Tables_ {
public static string DataDir { get; set; }
public static void Load() {
cfg.role.Name.Load(DataDir + "/cfg.role.name.data");
cfg.role.Profession.Load(DataDir + "/cfg.role.profession.data");
cfg.role.task.Roletable.Load(DataDir + "/cfg.role.task.roletable.data");
}
}
}
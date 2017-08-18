package genmsg;

import java.util.Collection;

/**
 * Created by HuangQiang on 2016/12/12.
 */
public class Utils extends  gendb.Utils{

    public static Collection<Bean> getExportBeans() {
        return Bean.getBeans().values();
    }

    public static Collection<MsgLike> getExportMsgs() {
        return MsgLike.getMsgs().values();
    }

    public static Collection<RefSet> getExportRefSets() {
        return RefSet.getRefs().values();
    }
}

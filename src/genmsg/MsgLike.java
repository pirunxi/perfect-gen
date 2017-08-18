package genmsg;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by HuangQiang on 2017/5/26.
 */
public abstract class MsgLike {

    private final static Map<String, MsgLike> msgs = new HashMap<>();
    private final static Map<Integer, MsgLike> msgsByid = new HashMap<>();
    public static void add(MsgLike msg) {
        if(msgs.put(msg.getFullname(), msg) != null)
            Utils.error("msg:%s duplicate", msg);
        MsgLike old = msgsByid.put(msg.getTypeId(), msg);
        if(old != null)
            Utils.error("msg1:%s msg2:%s typeid:%d duplicate", msg.getFullname(), old.getFullname(), msg.getTypeId());

    }

    public static Map<String, MsgLike> getMsgs() {
        return msgs;
    }

    public static MsgLike get(String msgName) {
        return msgs.get(msgName);
    }



    private final String name;
    private final String namespace;
    private final String fullname;
    private final int typeid;

    private int groupid;
    public MsgLike(String namespace, Element ele) {
        this.name = ele.getAttribute("name");
        this.namespace = namespace;
        this.fullname = Utils.combineFullName(namespace, name);
        this.typeid = chooseTypeId(ele.getAttribute("id"));
        if(typeid >= MAX_ID)
            Utils.error("msg:%s id:%d > maxid:%d", fullname, typeid, MAX_ID);
    }

    private final static int GEN_ID_START = 10000;
    private final static int MAX_ID = 1 << 16;
    private int chooseTypeId(String str) {
        return str.isEmpty() ? Math.abs(fullname.hashCode()) % ((MAX_ID) - GEN_ID_START) + GEN_ID_START : Integer.parseInt(str);
    }

    public String getName() {
        return name;
    }


    public String getFullname() {
        return fullname;
    }


    public int getTypeId() {
        return (groupid << 16) + typeid;
    }


    public String getNamespace() {
        return namespace;
    }

    public void setGroupid(int groupid) {
        if(this.groupid != 0 && groupid != 0)
            Utils.error("msg:%s is both in group <%d> and <%d>", fullname, this.groupid, groupid);
        this.groupid = groupid;
    }

    public abstract void resolve();
    public abstract void collectDependences();
    public abstract Set<String> getDepBeans();
}

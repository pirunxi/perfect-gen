package genmsg;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by HuangQiang on 2017/5/26.
 */
public class RefSet {

    private final int groupid;
    private final static Map<String, RefSet> refSets = new HashMap<>();
    private final static Map<Integer, RefSet> refSetsByGroupid = new HashMap<>();
    public static void add(RefSet ref) {
        if(refSets.put(ref.getName(), ref) != null)
            Utils.error("refset:%s duplicate", ref.getName());
        if(ref.groupid  != 0) {
            RefSet old = refSetsByGroupid.get(ref.groupid);
            if(old != null)
                Utils.error("refset:%s groupid:%d duplicate with refset:%s", ref.getName(), ref.groupid, old.getName());
        }
    }

    public static Map<String, RefSet> getRefs() {
        return refSets;
    }

    private final String name;
    private final Set<String> msgs = new HashSet<>();
    private final Set<String> beans = new HashSet<>();
    private final Set<String> modules = new HashSet<>();
    private final Set<String> refs = new HashSet<>();
    public RefSet(Element ele) {
        this.name = ele.getAttribute("name");
        if(this.name.isEmpty())
            error("name missing");

        this.groupid = Integer.parseInt(Utils.getAttrOrDefualt(ele, "id", "0"));

        Utils.foreach(ele, (tag, node) -> {
            switch (tag) {
                case "module" : {
                    String s = node.getAttribute("name");
                    check(!s.isEmpty(), "module name missing");
                    check(modules.add(s), "module:" + s + " duplicate");
                    break;
                }
                case "msg" : {
                    String s = node.getAttribute("name");
                    check(!s.isEmpty(), "msg name missing");
                    check(msgs.add(s), "msg:" + s + " duplicate");
                    break;
                }
                case "bean" : {
                    String s = node.getAttribute("name");
                    check(!s.isEmpty(), "bean name missing");
                    check(beans.add(s), "bean:" + s + " duplicate");
                    break;
                }
                case "ref": {
                    String s = node.getAttribute("name");
                    check(!s.isEmpty(), "ref name missing");
                    check(refs.add(s), "ref:" + s + " duplicate");
                    break;
                }
                default: {
                    error("unknown tag:" + tag);
                }
            }
        });
        Utils.verbose(this);
    }

    private void check(boolean cond, String err) {
        if(!cond)
            error(err);
    }

    private void error(String err) {
        throw new RuntimeException("ref:" + this.name + " err:" + err);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("ref{name:%s,msgs:%s,beans:%s,modules:%s,refs:%s}", name, msgs, beans, modules, refs);
    }

    public void resolve() {
        check(groupid >= 0 && groupid < (1 << 16), "groupid:" + groupid + " is invalid");

        for(String s : msgs) {
            check(MsgLike.get(s) != null, "msg:" + s + " not exist");
        }
        for(String b : beans) {
            check(Bean.getBean(b) != null, "bean:" + b + " not exist");
        }
        for(String r : refs) {
            check(refSets.containsKey(r), "ref:" + r + " not exist");
        }
    }

    private boolean collected = false;

    private final Set<String> depBeans = new HashSet<>();
    private final Set<String> depMsgs = new HashSet<>();
    private final Set<String> depRefs = new HashSet<>();

    public Set<String> getDepBeans() {
        return depBeans;
    }

    public Set<String> getDepMsgs() {
        return depMsgs;
    }

    public Set<String> getDepRefs() {
        return depRefs;
    }

    public void collectDependences() {
        if(collected) return;
        collected = true;

        depMsgs.addAll(msgs);
        for(String m : modules) {
            String prefix = m + ".";
            for(String b : MsgLike.getMsgs().keySet()) {
                if(b.startsWith(prefix)) {
                    depMsgs.add(b);
                }
            }
        }

        for(String b : beans) {
            depBeans.addAll((Bean.getBean(b).getDepBeans()));
        }
        for(String m : depMsgs) {
            depBeans.addAll(MsgLike.get(m).getDepBeans());
        }

        for(String rname : refs) {
            RefSet r = refSets.get(rname);
            if(r == this) continue;
            r.collectDependences();
            depMsgs.addAll(r.depMsgs);
            depBeans.addAll(r.depBeans);
            depRefs.addAll(r.depRefs);
        }

        if(groupid != 0) {
            for(String m : depMsgs) {
                MsgLike.get(m).setGroupid(groupid);
            }
        }
    }
}

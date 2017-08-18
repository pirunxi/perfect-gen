package genmsg;

import genmsg.type.TType;
import org.w3c.dom.Element;

import java.util.Set;

/**
 * Created by HuangQiang on 2017/5/26.
 */
public class Rpc extends MsgLike {


    private final String argStr;
    private final String resStr;
    private final int timeout;

    private TType atype;
    private TType vtype;
    public Rpc(String namespace, Element ele) {
        super(namespace, ele);

        this.argStr = ele.getAttribute("arg");
        this.resStr = ele.getAttribute("res");

        String timeoutStr = ele.getAttribute("timeout");
        this.timeout = timeoutStr.isEmpty() ? 15 : Integer.parseInt(timeoutStr);

        Utils.verbose(this);
    }

    @Override
    public String toString() {
        return String.format("rpc{name:%s,id:%s,arg:%s, res:%s,timeout:%s}", getFullname(), getTypeId(), argStr, resStr, timeout);
    }

    @Override
    public void resolve() {
        this.atype = TType.create(getNamespace(), argStr);
        this.vtype = TType.create(getNamespace(), resStr);
    }

    @Override
    public void collectDependences() {

    }

    @Override
    public Set<String> getDepBeans() {
        return null;
    }
}

package gendb;

import java.util.HashMap;

/**
 * Created by HuangQiang on 2017/5/22.
 */
public class Consts {
    public final static String
            BEAN = "perfect.txn.Bean",
            BINARY_STREAM = "perfect.marshal.BinaryStream",
            BINARY = "perfect.marshal.Binary",
            MARSHAL = "perfect.marshal.Marshal",
            MARSHAL_EXCEPTION = "perfect.marshal.MarshalException",
            FIELD_LOG = "perfect.txn.logs.FieldLog",
            TRANSACTION = "perfect.txn.Transaction",
            TKEY = "perfect.txn.TKey",
            TABLE = "perfect.txn.Table",
            XDB = "perfect.db.Xdb",
            DATASTREAM = "cfg.DataStream",

             IO_BEAN ="perfect.io.Bean",
            MSG = "perfect.io.Message"

                    ;

    public final static HashMap<String,String> consts = new HashMap<>();
    static {
        consts.put("$BEAN", BEAN);
        consts.put("$BINARY", BINARY);
        consts.put("$BSTREAM", BINARY_STREAM);
        consts.put("$MARSHAL", MARSHAL);
        consts.put("$MEXCEPTION", MARSHAL_EXCEPTION);
        consts.put("$FIELD_LOG", FIELD_LOG);
        consts.put("$TRANSACTION", TRANSACTION);
        consts.put("$TKEY", TKEY);
        consts.put("$TABLE", TABLE);
        consts.put("$XDB", XDB);

        consts.put("$IO_BEAN", IO_BEAN);
        consts.put("$MSG", MSG);
    }
}

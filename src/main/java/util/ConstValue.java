package util;

import java.util.HashMap;

public class ConstValue {

    public static HashMap<Integer, String> ALG_TYPE = new HashMap<>();

    static {
        ALG_TYPE.put(0, "ALG_trussDecomp");
        ALG_TYPE.put(1, "ALG_TCPIndex");
        ALG_TYPE.put(2, "ALG_SupMainten");
        ALG_TYPE.put(3, "ALG_MultiEdgesMainten");
        ALG_TYPE.put(4, "ALG_PareEdgesMainten");
        ALG_TYPE.put(5, "ALG_MultiVerticsMainten");
        ALG_TYPE.put(6, "ALG_PareVerticsMainten");
    }

}

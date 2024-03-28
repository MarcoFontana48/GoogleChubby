package chubby.server;

import chubby.control.message.ChubbyRequest;
import chubby.utils.ChubbyUtils;

public class ChubbyRequestSerializer {
    public static String serialize(ChubbyRequest chubbyRequest) {
        return ChubbyUtils.gsonBuild().toJson(chubbyRequest, ChubbyRequest.class);
    }
}

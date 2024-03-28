package chubby.server;

import chubby.control.message.ChubbyRequest;
import chubby.utils.ChubbyUtils;
import io.etcd.jetcd.watch.WatchEvent;
import org.jetbrains.annotations.NotNull;


public class ChubbyRequestDeserializer {
    public static ChubbyRequest deserialize(String jsonRequest) {
        return ChubbyUtils.gsonBuild().fromJson(jsonRequest, ChubbyRequest.class);
    }

    public static ChubbyRequest deserialize(@NotNull WatchEvent watchEvent) {
        return ChubbyUtils.gsonBuild().fromJson(watchEvent.getKeyValue().getValue().toString(), ChubbyRequest.class);
    }
}

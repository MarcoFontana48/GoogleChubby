package chubby.server.node;

import chubby.utils.ChubbyUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class ChubbyNodeValueSerializer {
    private static final Gson gson = ChubbyUtils.gsonBuild();

    public static String serialize(@NotNull ChubbyNodeValue chubbyNodeValue) {
        JsonObject jsonObj = new JsonObject();

        JsonElement metadataJsonElem = null;

        if (chubbyNodeValue.getMetadata() != null) metadataJsonElem = gson.toJsonTree(chubbyNodeValue.getMetadata(), ChubbyNodeMetadata.class);

        jsonObj.addProperty("file_content", chubbyNodeValue.getFilecontent());
        jsonObj.add("metadata", metadataJsonElem);

        return gson.toJson(jsonObj);
    }
}

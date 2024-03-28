package chubby.server.node;

import chubby.utils.ChubbyUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class ChubbyNodeValueDeserializer {
    private static final Gson gson = ChubbyUtils.gsonBuild();

    public static @NotNull ChubbyNodeValue deserialize(String jsonString) {
        JsonObject jsonObj = gson.fromJson(jsonString, JsonObject.class);

        JsonElement metadataJsonElem = jsonObj.get("metadata");

        ChubbyNodeMetadata chubbyNodeMetadata = gson.fromJson(metadataJsonElem, ChubbyNodeMetadata.class);

        String fileContent = jsonObj.get("file_content").getAsString();

        return new ChubbyNodeValue(fileContent, chubbyNodeMetadata);
    }
}

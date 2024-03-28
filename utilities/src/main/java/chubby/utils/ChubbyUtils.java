package chubby.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.etcd.jetcd.ByteSequence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChubbyUtils {

    //cannot use standard File.isFile(Path) method, because it expects an existing path. However, this is a virtual path stored into jetcd's kv stored
    /**
     * Check if the specified path is a file.
     *
     * @param path path to be checked
     * @return true if the path is a file, false otherwise
     */
    public static boolean isFile(@NotNull Path path) {
        String nodeName;
        if (path.equals(Paths.get("\\"))) {
            nodeName = "\\";
        } else {
            nodeName = path.getFileName().toString();
        }

        //if path contains a period with at least one character before and after it, it's a file (example.txt), else is a directory
        String regex = "^[^.]+\\.[^.]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(nodeName);

        return matcher.matches();
    }

    /**
     * Check if the specified path is a file.
     *
     * @param pathBytesequence path to be checked
     * @return true if the path is a file, false otherwise
     */
    public static boolean isFile(@NotNull ByteSequence pathBytesequence) {
        return isFile(Paths.get(pathBytesequence.toString()));
    }

    public static class LocalDateAdapter extends TypeAdapter<LocalDate> {

        @Override
        public void write(@NotNull JsonWriter out, @NotNull LocalDate value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public LocalDate read(@NotNull JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString());
        }
    }

    public static class LocalTimeAdapter extends TypeAdapter<LocalTime> {

        @Override
        public void write(@NotNull JsonWriter out, @NotNull LocalTime value) throws IOException {
            out.value(value.toString());
        }

        @Override
        public LocalTime read(@NotNull JsonReader in) throws IOException {
            return LocalTime.parse(in.nextString());
        }
    }

    @Contract(" -> new")
    public static @NotNull Gson gsonBuild() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .create();
    }
}

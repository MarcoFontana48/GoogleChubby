package chubby.control.handle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChubbyHandleRequest {
    private final String requestedAbsolutePath;
    private final ChubbyHandleType chubbyHandleType;
    private final ChubbyLockDelay chubbyLockDelay;
    private final List<ChubbyEventType> chubbyEventTypeList;

    /**
     * Create a new ChubbyHandleRequest.
     *
     * @param requestedAbsolutePath  the absolute path of the handle
     * @param chubbyHandleType  the type of the handle
     * @param chubbyLockDelay  the lock delay
     * @param requestedChubbyEventTypes  the requested event types
     */
    public ChubbyHandleRequest(@NotNull Path requestedAbsolutePath, ChubbyHandleType chubbyHandleType, @Nullable ChubbyLockDelay chubbyLockDelay, @Nullable String... requestedChubbyEventTypes) {
        this.requestedAbsolutePath = requestedAbsolutePath.toString();
        this.chubbyHandleType = chubbyHandleType;
        this.chubbyLockDelay = Objects.requireNonNullElse(chubbyLockDelay, new ChubbyLockDelay());  //if null a default lock delay is set
        this.chubbyEventTypeList = parseEventType(requestedChubbyEventTypes);
    }

    /**
     * returns a list of chubby events from a string array
     * @param eventTypeStr string array of event types
     * @return list of ChubbyEventType
     */
    private static List<ChubbyEventType> parseEventType(String... eventTypeStr) {
        if (eventTypeStr == null || eventTypeStr.length == 0) {
            return new ArrayList<>();
        }

        boolean hasChildNodeModified = Stream.of(eventTypeStr)
                .map(String::toUpperCase)
                .anyMatch(eventType -> eventType.equals("CHILD_NODE_MODIFIED"));

        Stream<String> filteredStream = Stream.of(eventTypeStr)
                .map(String::toUpperCase)
                .distinct();

        if (hasChildNodeModified) {
            filteredStream = filteredStream
                    .filter(eventType -> !eventType.equals("CHILD_NODE_ADDED") && !eventType.equals("CHILD_NODE_REMOVED"));
        }

        return filteredStream
                .map(eventType -> {
                    try {
                        return ChubbyEventType.valueOf(eventType);
                    } catch (IllegalArgumentException e) {
                        return ChubbyEventType.NONE;
                    }
                })
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }


    public String getRequestedAbsolutePath() {
        return this.requestedAbsolutePath;
    }

    public ChubbyHandleType getChubbyHandleType() {
        return this.chubbyHandleType;
    }

    public ChubbyLockDelay getChubbyLockDelay() {
        return this.chubbyLockDelay;
    }

    public List<ChubbyEventType> getChubbyEventTypeList() {
        return Collections.unmodifiableList(this.chubbyEventTypeList);
    }
}

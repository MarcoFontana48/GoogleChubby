package chubby.server;

import io.etcd.jetcd.Watch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ChubbyUnsubscribeProcessor {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Unsubscribe to all events from currently held node.
     *
     * @param watcherList list of watchers to be unsubscribed
     */
    public static void process(@NotNull List<Watch.Watcher> watcherList) {
        logger.trace("requested unsubscribe to all events from currently held node");

        watcherList.forEach(watcher -> logger.trace(watcher.toString()));

        watcherList.forEach(Watch.Watcher::close);
    }
}

package chubby.server;

import chubby.server.node.ChubbyNode;

public class ChubbyCreateNodeResponse {
    private final ChubbyNode chubbyNode;
    private final boolean wasCreated;

    /**
     * Create a new ChubbyCreateNodeResponse.
     *
     * @param chubbyNode  the node that was created
     * @param wasCreated  true if the node was created, false otherwise
     */
    public ChubbyCreateNodeResponse(ChubbyNode chubbyNode, boolean wasCreated) {
        this.chubbyNode = chubbyNode;
        this.wasCreated = wasCreated;
    }

    public ChubbyNode getChubbyNode() {
        return this.chubbyNode;
    }

    public boolean wasCreated() {
        return this.wasCreated;
    }
}

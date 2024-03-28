package chubby.control.handle;

public enum ChubbyEventType {
    FILE_CONTENTS_MODIFIED,
    CHILD_NODE_ADDED,
    CHILD_NODE_REMOVED,
    CHILD_NODE_MODIFIED,
    HANDLE_INVALID,
    CONFLICTING_LOCK,
    NONE
}
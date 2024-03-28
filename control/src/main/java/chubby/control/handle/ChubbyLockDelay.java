package chubby.control.handle;

public class ChubbyLockDelay {
    private final short LOCK_DELAY_MIN = 0;
    private final short LOCK_DELAY_MAX = 60;
    private final short LOCK_DELAY_DEFAULT = 30;
    private final short lockDelay;

    /**
     * Create a new ChubbyLockDelay.
     *
     * @param lockdelayStr  the lock delay as a string
     * @throws NumberFormatException  if the lock delay is not a valid number
     */
    public ChubbyLockDelay(String lockdelayStr) throws NumberFormatException {
        short lockdelayParsed;

        lockdelayParsed = Short.parseShort(lockdelayStr);

        if (lockdelayParsed < this.LOCK_DELAY_MIN) {
            this.lockDelay = this.LOCK_DELAY_MIN;
        } else if (lockdelayParsed > this.LOCK_DELAY_MAX) {
            this.lockDelay = this.LOCK_DELAY_MAX;
        } else {
            this.lockDelay = lockdelayParsed;
        }
    }

    /**
     * Create a new ChubbyLockDelay.
     *
     * @param lockdelay  the lock delay
     */
    public ChubbyLockDelay(int lockdelay) {
        if (lockdelay < this.LOCK_DELAY_MIN) {
            this.lockDelay = this.LOCK_DELAY_MIN;
        } else if (lockdelay > this.LOCK_DELAY_MAX) {
            this.lockDelay = this.LOCK_DELAY_MAX;
        } else {
            this.lockDelay = (short) lockdelay;
        }
    }

    public ChubbyLockDelay() {
        this.lockDelay = this.LOCK_DELAY_DEFAULT;
    }

    public short getLOCK_DELAY_MIN() {
        return this.LOCK_DELAY_MIN;
    }

    public short getLOCK_DELAY_MAX() {
        return this.LOCK_DELAY_MAX;
    }

    public short getLOCK_DELAY_DEFAULT() {
        return this.LOCK_DELAY_DEFAULT;
    }

    public short getValue() {
        return this.lockDelay;
    }
}


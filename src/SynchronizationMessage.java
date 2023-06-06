package src;

/**
 * SynchronizationMessage.java
 * 
 * An enum for the messages used to synchronize the master and the slaves.
 */
public enum SynchronizationMessage {
    START, // when the master launches the mapping
    READY_TO_SHUFFLE, // when the slave is ready to shuffle
    SHUFFLE, // when the master launches the shuffle because all slaves are ready
    READY_TO_REDUCE, // when the slave is ready to reduce
    REDUCE, // when the master launches the reduce because all slaves are ready
    END, // at the end of a data transfer
    ERROR,
}

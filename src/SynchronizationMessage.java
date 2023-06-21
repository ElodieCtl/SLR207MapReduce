package src;

/**
 * SynchronizationMessage.java
 * 
 * An enum for the messages used to synchronize the master and the slaves.
 */
public enum SynchronizationMessage {
    MASTER_AWAKE, // when the master is launched
    READY_TO_MAP, // when the slave is ready to map
    START, // when the master launches the mapping
    READY_TO_SHUFFLE, // when the slave is ready to shuffle
    SHUFFLE, // when the master launches the shuffle because all slaves are ready
    READY_TO_REDUCE, // when the slave is ready to reduce
    REDUCE, // when the master launches the reduce because all slaves are ready
    REDUCE_END, // at the end of the reduce
    MAP, // when the master launches the second map
    READY_TO_COORDINATE, // when the slave is ready to coordinate for ranges
    COORDINATE, // when the master launches the coordination for the ranges
    // coordination for the ranges
    READY_TO_REDUCE_2, // when the slave is ready to reduce again,
    REDUCE_2, // when the master launches the reduce again because all slaves are ready
    END,
    ERROR,
}

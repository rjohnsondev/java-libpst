package com.pff;

import java.util.Date;

public interface ITask extends IMessage {
    /**
     * Status Integer 32-bit signed 0x0 => Not started
     */
    int getTaskStatus();

    /**
     * Percent Complete Floating point double precision (64-bit)
     */
    double getPercentComplete();

    /**
     * Is team task Boolean
     */
    boolean isTeamTask();

    /**
     * Date completed Filetime
     */
    Date getTaskDateCompleted();

    /**
     * Actual effort in minutes Integer 32-bit signed
     */
    int getTaskActualEffort();

    /**
     * Total effort in minutes Integer 32-bit signed
     */
    int getTaskEstimatedEffort();

    /**
     * Task version Integer 32-bit signed FTK: Access count
     */
    int getTaskVersion();

    /**
     * Complete Boolean
     */
    boolean isTaskComplete();

    /**
     * Owner ASCII or Unicode string
     */
    String getTaskOwner();

    /**
     * Delegator ASCII or Unicode string
     */
    String getTaskAssigner();

    /**
     * Unknown ASCII or Unicode string
     */
    String getTaskLastUser();

    /**
     * Ordinal Integer 32-bit signed
     */
    int getTaskOrdinal();

    /**
     * Is recurring Boolean
     */
    boolean isTaskFRecurring();

    /**
     * Role ASCII or Unicode string
     */
    String getTaskRole();

    /**
     * Ownership Integer 32-bit signed
     */
    int getTaskOwnership();

    /**
     * Delegation State
     */
    int getAcceptanceState();
}

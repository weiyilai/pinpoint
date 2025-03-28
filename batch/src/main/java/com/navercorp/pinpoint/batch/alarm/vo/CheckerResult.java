package com.navercorp.pinpoint.batch.alarm.vo;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;

public class CheckerResult {
    
    private int historyId;
    private String applicationName;
    private String checkerName;
    private String ruleId;
    private boolean detected;
    private int sequenceCount;
    private int timingCount;

    public CheckerResult() {
    }
    
    public CheckerResult(String ruleId, String applicationName, String checkerName, boolean detected, int sequenceCount, int timingCount) {
        this.ruleId = ruleId;
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
        this.checkerName = checkerName;
        this.detected = detected;
        this.sequenceCount = sequenceCount;
        this.timingCount = timingCount;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public int getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #getApplicationName()} instead.
     */
    @Deprecated
    public String getApplicationId() {
        return getApplicationName();
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #setApplicationName(String)} instead.
     */
    @Deprecated
    public void setApplicationId(String applicationName) {
        setApplicationName(applicationName);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
    }
    
    public String getCheckerName() {
        return checkerName;
    }
    
    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }
    
    public boolean isDetected() {
        return detected;
    }
    
    public void setDetected(boolean detected) {
        this.detected = detected;
    }
    
    public int getSequenceCount() {
        return sequenceCount;
    }
    
    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }
    
    public int getTimingCount() {
        return timingCount;
    }
    
    public void setTimingCount(int timingCount) {
        this.timingCount = timingCount;
    }

    public void increseCount() {
        ++sequenceCount;
        
        if (sequenceCount == timingCount) {
            timingCount = sequenceCount * 2 + 1;
        }
    }
}

package com.easyquery.core.model;

public class StrategyConfig {

    private Integer shardingCount;
    private Integer shardingStart;
    private Integer rangeStart;
    private Integer rangeStep;
    private Integer padZero;

    public Integer getShardingCount() {
        return shardingCount;
    }

    public void setShardingCount(Integer shardingCount) {
        this.shardingCount = shardingCount;
    }

    public Integer getShardingStart() {
        return shardingStart;
    }

    public void setShardingStart(Integer shardingStart) {
        this.shardingStart = shardingStart;
    }

    public Integer getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Integer rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Integer getRangeStep() {
        return rangeStep;
    }

    public void setRangeStep(Integer rangeStep) {
        this.rangeStep = rangeStep;
    }

    public Integer getPadZero() {
        return padZero;
    }

    public void setPadZero(Integer padZero) {
        this.padZero = padZero;
    }

}

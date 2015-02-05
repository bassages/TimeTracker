package timetracker.wiegman.nl.timetracker;

public class PeriodOverviewItem {

    private long periodId;
    private boolean isCurrentPeriod;
    private String periodName;
    private long billableDuration;

    public String getPeriodName() {
        return periodName;
    }

    public void setPeriodName(String periodName) {
        this.periodName = periodName;
    }

    public long getBillableDuration() {
        return billableDuration;
    }

    public void setBillableDuration(long billableDuration) {
        this.billableDuration = billableDuration;
    }

    public long getPeriodId() {
        return periodId;
    }

    public void setPeriodId(long periodId) {
        this.periodId = periodId;
    }

    public boolean isCurrentPeriod() {
        return isCurrentPeriod;
    }

    public void setCurrentPeriod(boolean isCurrentPeriod) {
        this.isCurrentPeriod = isCurrentPeriod;
    }
}

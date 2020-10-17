package org.imanity.framework.util;

public class CountdownData {

    private static final int[] COUNTDOWNS = {
            3200,
            1600,
            1200,
            600,
            300,
            180,
            120,
            60,
            30,
            15,
            10,
            9,
            8,
            7,
            6,
            5,
            4,
            3,
            2,
            1,
            0
    };

    private int currentCount;

    public CountdownData(int startCount) {
        for (int i = 0; i < COUNTDOWNS.length; i++) {
            if (COUNTDOWNS[i] <= startCount) {
                this.currentCount = i + 1;
                return;
            }
        }
        throw new IllegalStateException("The count " + startCount + " does not match to any of the COUNTDOWN we listed!");
    }

    public boolean isEnded() {
        return currentCount >= COUNTDOWNS.length - 1;
    }

    public boolean canAnnounce(int count) { // currentCount = 5, count = 9
        this.validCount(count);
        if (count <= COUNTDOWNS[this.currentCount]) {
            this.currentCount++;
            return true;
        }
        return false;
    }

    public void validCount(int count) {
        if (count > COUNTDOWNS[this.currentCount - 1]) {
            for (int i = this.currentCount; i > 0; i--) {
                if (count <= COUNTDOWNS[i]) {
                    this.currentCount = i;
                    break;
                }
            }
        }
    }

}

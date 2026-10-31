package dev.metallicarts.allomancy;

/** Prepay whole micrograms, then spend the fractional credit. Cancelling cannot erase unpaid debt. */
public final class BurnBudget {
    private long creditTwentieths;
    public long charge(long microgramsPerSecond) {
        if(microgramsPerSecond<1||microgramsPerSecond>100_000_000L)throw new IllegalArgumentException("Invalid burn rate");
        long owed=microgramsPerSecond-creditTwentieths;
        long debit=owed>0?(owed+19)/20:0;
        creditTwentieths+=debit*20-microgramsPerSecond;
        return debit;
    }
    public void reset(){creditTwentieths=0;}
}

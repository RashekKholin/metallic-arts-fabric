package dev.metallicarts.arts;
/** Carries paid fractional time through rate changes; repeated calls in one world tick cannot duplicate work. */
public final class FractionalClock {
 private double credit;private long last=Long.MIN_VALUE;
 public int advance(long tick,double rate){if(!Double.isFinite(rate)||rate<.1||rate>8)throw new IllegalArgumentException("Invalid local rate");if(tick==last)return 0;if(tick<last)credit=0;last=tick;double total=credit+rate;int steps=(int)Math.floor(total+1e-10);credit=Math.max(0,total-steps);return steps;}
 public long last(){return last;}
}

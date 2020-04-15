package net.frankheijden.insights.utils;

import java.util.concurrent.TimeUnit;

public class TimeUtils {

    public static String getHumanTime(int seconds) {
        String s_ = "second";
        String ss_ = "seconds";
        String m_ = "minute";
        String ms_ = "minutes";
        String h_ = "hour";
        String hs_ = "hours";
        String d_ = "day";
        String ds_ = "days";

        String and_ = "and";

        if (seconds < 60) {
            if (seconds == 1) {
                return seconds + " " + s_;
            } else {
                return seconds + " " + ss_;
            }
        } else {
            int minutes = seconds / 60;
            int s = 60 * minutes;
            int secondsLeft = seconds - s;
            if (minutes < 60) {
                if (secondsLeft > 0) {
                    String min = (minutes == 1) ? m_ : ms_;
                    String sec = (secondsLeft == 1) ? s_ : ss_;
                    return minutes + " " + min + " " + and_ + " " + secondsLeft + " " + sec;
                } else {
                    return (minutes == 1) ? minutes + " " + m_ : minutes + " " + ms_;
                }
            } else {
                String time;
                String h = hs_;
                String m = ms_;
                String se = ss_;
                String d = ds_;
                int days;
                int inMins;
                int leftOver;
                if (secondsLeft == 1) {
                    se = s_;
                }
                if (minutes < 1440) {
                    days = minutes / 60;
                    if (days == 1) {
                        h = h_;
                    }
                    time = days + " "+h+" ";
                    inMins = 60 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = m_;
                    }
                    if (leftOver >= 1) {
                        time = time + ", " + leftOver + " "+m+" ";
                    }

                    if (secondsLeft > 0) {
                        time = time + ", " + secondsLeft + " "+se;
                    }

                    return time;
                } else {
                    days = minutes / 1440;
                    if (days == 1) {
                        d = d_;
                    }
                    time = days + " "+d;
                    inMins = 1440 * days;
                    leftOver = minutes - inMins;
                    if (leftOver == 1) {
                        m = m_;
                    }
                    if (leftOver >= 1) {
                        if (leftOver < 60) {
                            time = time + ", " + leftOver + " "+m;
                        } else {
                            int hours = leftOver / 60;
                            if (hours == 1) {
                                h = h_;
                            }
                            int hoursInMins = 60 * hours;
                            int minsLeft = leftOver - hoursInMins;
                            if (minsLeft <= 0 && secondsLeft <= 0) {
                                time = time + " " + and_ + " " + hours + " "+h;
                            } else {
                                time = time + ", " + hours + " "+h;
                            }

                            if (secondsLeft > 0) {
                                if (minsLeft == 1) {
                                    time = time + ", " + minsLeft + " "+m_;
                                } else if (minsLeft >= 1) {
                                    time = time + ", " + minsLeft + " "+ms_;
                                }
                            } else {
                                if (minsLeft == 1) {
                                    time = time + " " + and_ + " " + minsLeft + " "+m_;
                                } else if (minsLeft >= 1) {
                                    time = time + " " + and_ + " " + minsLeft + " "+ms_;
                                }
                            }
                        }
                    }

                    if (secondsLeft > 0) {
                        time = time + " " + and_ + " " + secondsLeft + " "+se;
                    }
                    return time;
                }
            }
        }
    }

    public static String getDHMS(long startTime) {
        return getHumanTime((int) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
    }
}

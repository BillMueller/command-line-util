package de.billmueller.certutil;

import java.util.Calendar;
import java.util.Date;

public class Alarm implements Runnable {
    private Main main;
    private Calendar cal = Calendar.getInstance();
    private Date alarmTime = new Date();
    private String message;

    public Alarm(Main main, int[] time, String message) {
        this.main = main;
        this.message = message;
        if (time[0] != -1) {
            cal.set(Calendar.HOUR_OF_DAY, time[3]);
            cal.set(Calendar.MINUTE, time[4]);
            cal.set(Calendar.SECOND, 0);
            if (cal.before(Calendar.getInstance())) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            if (time[0] != 0) {
                cal.set(Calendar.YEAR, time[0]);
                cal.set(Calendar.MONTH, time[1] - 1);
                cal.set(Calendar.DAY_OF_MONTH, time[2]);
                alarmTime = cal.getTime();
            } else {
                alarmTime = new Date();
                alarmTime = cal.getTime();
            }
        } else
            alarmTime.setTime(new Date().getTime() + ((time[3] * 60) + time[4]) * 60000);
    }

    public void run() {
        System.out.println(alarmTime);
        try {
            while (alarmTime.after(new Date())) {
                Thread.sleep(1000);
            }
            main.printEditor("Alarm: " + message, true);
        }catch (InterruptedException ire){
            main.printError("the alarm thread got interrupted");
            main.printDebug("error message: " + ire.getMessage());
        }
    }
}

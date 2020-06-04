package com.sebastian_eggers.MediApp.Models;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.sebastian_eggers.MediApp.Activities.StartActivity;
import com.sebastian_eggers.MediApp.R;
import com.sebastian_eggers.MediApp.Receiver.NotificationReceiver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class Drug implements Comparable {
    private long id;
    private String name;
    private String description;
    private ArrayList<LocalTime> intake;
    private ArrayList<DayOfWeek> days;
    private int dosePerIntake;
    private String doseUnit;
    private String form;

    public Drug(String name, ArrayList<LocalTime> time, ArrayList<DayOfWeek> days, int dosePerIntake, DrugForm drugForm) {
        this.name = name;
        this.intake = time;
        this.days = days;
        this.dosePerIntake = dosePerIntake;
        this.form = drugForm.toString();
        this.id = -1;
    }

    public Drug(String name, ArrayList<LocalTime> time, ArrayList<DayOfWeek> days, int dosePerIntake, DrugForm drugForm, String description) {
        this(name, time, days, dosePerIntake, drugForm);
        this.description = description;
    }

    public Drug(String name, ArrayList<LocalTime> time, ArrayList<DayOfWeek> days, int dosePerIntake, DrugForm drugForm, String description, String doseUnit) {
        this(name, time, days, dosePerIntake, drugForm, description);
        this.doseUnit = doseUnit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getForm() {
        return form;
    }

    public void setForm(DrugForm form) {
        this.form = form.toString();
    }

    public ArrayList<LocalTime> getIntake() {
        return intake;
    }

    public void setIntake(ArrayList<LocalTime> intake) {
        this.intake = intake;
    }

    public ArrayList<DayOfWeek> getDays() {
        return days;
    }

    public void setDays(ArrayList<DayOfWeek> days) {
        this.days = days;
    }

    public int getDosePerIntake() {
        return dosePerIntake;
    }

    public void setDosePerIntake(int dosePerIntake) {
        this.dosePerIntake = dosePerIntake;
    }

    public String getDoseUnit() {
        return doseUnit;
    }

    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit;
    }

    public boolean doseUnitExists() {
        return this.doseUnit != null;
    }

    public boolean descriptionExists() {
        return this.description != null;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        if (this.id == -1)
            this.id = id;
        else
            throw new RuntimeException("You are not allowed to set a Drug ID after it's initially set.");
    }

    public boolean isNextIntakeToday() {
        return isNextIntakeToday(LocalDate.now(), LocalTime.now());
    }

    public boolean isNextIntakeToday(LocalDate localDate, LocalTime of) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();

        boolean found = false;
        for (DayOfWeek day: days) {
            if(dayOfWeek == day) {
                found = true;
                break;
            }
        }
        if(!found) return false;

        return nextIntake(of).compareTo(LocalTime.of(23, 59, 59)) < 0;
    }

    public LocalTime nextIntake() {
        return nextIntake(LocalTime.now());
    }

    public LocalTime nextIntake(LocalTime localTime) {
        LocalTime min = LocalTime.of(23, 59, 59);

        for (LocalTime time : intake) {
            if (localTime.compareTo(time) <= 0 && min.compareTo(time) >= 0) {
                min = time;
            }
        }
        return min;
    }

    @Override
    public int compareTo(Object o) {
        if (o.getClass() != this.getClass())
            return 0;

        Drug compareDrug = (Drug) o;
        if(compareDrug.isNextIntakeToday() && !isNextIntakeToday())
            return 1;
        else if(!compareDrug.isNextIntakeToday() && isNextIntakeToday())
            return -1;
        else if(compareDrug.isNextIntakeToday() && isNextIntakeToday())
            return nextIntake().compareTo(compareDrug.nextIntake());

        ArrayList<DayOfWeek> days = getDays();
        Collections.sort(days);
        ArrayList<DayOfWeek> compareDays = compareDrug.getDays();
        Collections.sort(compareDays);
        return days.get(0).compareTo(compareDays.get(0));
    }

    /**
     *  ------  Notifications  ------
     */
    private static int notificationId = 0;

    public void scheduleNotification(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        PendingIntent pending = buildNotification(context);

        Calendar now = Calendar.getInstance();
        for(LocalTime time: intake) {
            Calendar alarm = buildAlarmCalendar(time);

            if(days.size() == 7) {
                if(now.after(alarm))
                    alarm.add(Calendar.DATE, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pending);
            }
            else {
                for(DayOfWeek day: days) {
                    alarm.set(Calendar.DAY_OF_WEEK, day.getValue());
                    manager.setRepeating(AlarmManager.RTC_WAKEUP, alarm.getTimeInMillis(), AlarmManager.INTERVAL_DAY * 7, pending);
                }
            }
        }
    }

    public void cancelNotification(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert manager != null;
        PendingIntent pending = buildNotification(context);

        Calendar now = Calendar.getInstance();
        for(LocalTime time: intake) {
            Calendar alarm = buildAlarmCalendar(time);

            if(days.size() == 7) {
                if(now.after(alarm))
                    alarm.add(Calendar.DATE, 1);
                manager.cancel(pending);
            }
            else {
                for(DayOfWeek day: days) {
                    alarm.set(Calendar.DAY_OF_WEEK, day.getValue());
                    manager.cancel(pending);
                }
            }
        }
    }

    private PendingIntent buildNotification(Context context) {
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        alarmIntent.putExtra(NotificationReceiver.NOTIFICATION_ID, ++notificationId);
        alarmIntent.putExtra(NotificationReceiver.NOTIFICATION, getNotification(context));
        return PendingIntent.getBroadcast(context, Long.valueOf(getId()).intValue(),
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private Calendar buildAlarmCalendar(LocalTime time) {
        Calendar alarm = Calendar.getInstance();
        alarm.set(Calendar.HOUR_OF_DAY, time.getHour());
        alarm.set(Calendar.MINUTE, time.getMinute());
        alarm.set(Calendar.SECOND, time.getSecond());
        return alarm;
    }

    private Notification getNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, StartActivity.NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setContentText("Einnahme von " + name + " ist fällig.");
        builder.setSmallIcon(R.drawable.ic_launcher_foreground);
        builder.setAutoCancel(true);
        builder.setChannelId(Long.toString(getId()));
        return builder.build();
    }
}

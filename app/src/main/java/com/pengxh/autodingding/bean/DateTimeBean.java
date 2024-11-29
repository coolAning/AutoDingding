package com.pengxh.autodingding.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

@Entity
public class DateTimeBean {
    @Id(autoincrement = true)
    private Long id; // 主键ID

    private String uuid;
    private String date;
    private String time;
    private String weekDay;

    @Generated(hash = 590077470)
    public DateTimeBean(Long id, String uuid, String date, String time, String weekDay) {
        this.id = id;
        this.uuid = uuid;
        this.date = date;
        this.time = time;
        this.weekDay = weekDay;
    }

    @Generated(hash = 1790840121)
    public DateTimeBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getWeekDay() {
        return this.weekDay;
    }

    public void setWeekDay(String weekDay) {
        this.weekDay = weekDay;
    }

    // 添加延长任务时间一天的方法
    public void extendOneDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            date = sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    // 添加生成随机时间的方法
    public String getRandomTimeBefore() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(date + " " + time));
            Random random = new Random();
            int randomMinutes = random.nextInt(20); // 生成0到19之间的随机数
            calendar.add(Calendar.MINUTE, -randomMinutes);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date + " " + time;
    }
}
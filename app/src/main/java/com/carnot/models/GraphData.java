package com.carnot.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.OnReadAsync;
import com.activeandroid.OnReadData;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.carnot.global.Utility;
import com.carnot.libclasses.DateHelper;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by anarchistmegabyte on 31/8/16.
 */
@Table(name = "GraphData")
public class GraphData extends Model {
    @Column
    public int dayOfTheYear;
    @Column
    public double weekLabel;
    @Column
    public int dayOfTheMonth;
    @Column
    public double avg_mileage;
    @Column
    public int car_id;
    @Column
    public double avg_cost;
    @Column
    public double avg_distance;
    @Column
    public double avg_duration;
    @Column
    public double avg_drive_score;
    @Column
    public long timestamp;
    @Column
    public int day;
    @Column
    public int month;
    @Column
    public int year;
    @Column
    public double monthwiseDistanceValue;
    @Column
    public double monthwiseDriveScoreValue;
    @Column
    public double monthwiseCostValue;
    @Column
    public double monthwiseMileageValue;
    @Column
    public double monthwiseDurationValue;

    public static GraphData readSpecific(String carId, String day, String month, String year)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_id='" + carId + "' AND day='" + day + "' AND month='" + month + "' AND year='" + year +"'")
                .execute();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public static void readAllAsync(String carId, final OnReadData callback) {
        ArrayList<Graph> list = new Select().from(Graph.class)
                .where("car_id='" + carId + "'")
                .execute();

        new Select().from(Graph.class).where("car_id='" + carId + "'")
                .executeAsync(new OnReadAsync() {
                    @Override
                    public <T extends Model> void onRead(ArrayList<T> list) {
                        callback.onRead(null, list);
                    }
                });
    }

    public static boolean isCarAvailable(int carId)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_id='" + carId + "'")
                .execute();
        if(list != null)
            return true;
        else
            return false;
    }

    public static ArrayList<GraphData> readAllByWeekRule(String carID)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_id='" + carID + "'")
                .execute();
        ArrayList<GraphData> nonZeroList = new ArrayList<GraphData>();
        int i=-1;

        if(list!=null)
        {
            for(GraphData gd : list)
            {
                i++;
                if(gd.avg_distance !=0 ) {
                    break;
                }
            }
            Calendar calToday = Calendar.getInstance();
            if(list.size() > i)
            {
                int j = i;
                for(j = i; j < list.size() ; j++)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                    cal.set(Calendar.MONTH, list.get(j).month);
                    cal.set(Calendar.YEAR, list.get(j).year);
                    if(calToday.after(cal))
                        nonZeroList.add(list.get(j));
                    else
                    {
                        nonZeroList.add(list.get(j));
                        break;
                    }
                }
                j++;
                if(j<list.size())
                {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                    cal.set(Calendar.MONTH, list.get(j).month);
                    cal.set(Calendar.YEAR, list.get(j).year);
                    while(cal.get(Calendar.DAY_OF_WEEK) != 7 && j < list.size())
                    {
                        nonZeroList.add(list.get(j));
                        j++;
                        if(j<list.size())
                        {
                            cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                            cal.set(Calendar.MONTH, list.get(j).month);
                            cal.set(Calendar.YEAR, list.get(j).year);
                        }
                        else
                        {
                            break;
                        }
                    }
                    if(j<list.size())
                    {
                        nonZeroList.add(list.get(j));
                        j++;
                        if(j<list.size())
                            nonZeroList.add(list.get(j));
                    }
                }
                if(nonZeroList.size() != 0)
                    return nonZeroList;
                else
                    return null;
            }
        }
        return null;
    }

    public static ArrayList<GraphData> readAll(String carID)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_id='" + carID + "'")
                .execute();
        if(list != null)
            if(list.size() != 0)
                return list;
            else
                return null;
        else
            return null;
    }

    //MONTHLY RULE : always fetch all 31 or 30 days especially for current month
    public static ArrayList<GraphData> readAllByMonthlyRule(String carID)
    {
        Calendar today = Calendar.getInstance();

        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_id='" + carID + "'")
                .execute();
        ArrayList<GraphData> nonZeroList = new ArrayList<GraphData>();
        int i=-1;
        if(list != null)
        {
            for(GraphData gd : list)
            {
                i++;
                if(gd.avg_distance !=0 )
                    break;
            }
            if(list.size() > i)
            {
                Calendar calToday = Calendar.getInstance();
                int j;
                for(j = i; j < list.size() ; j++)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                    cal.set(Calendar.MONTH, list.get(j).month);
                    cal.set(Calendar.YEAR, list.get(j).year);
                    if(calToday.after(cal))
                        nonZeroList.add(list.get(j));
                    else
                    {
                        nonZeroList.add(list.get(j));
                        break;
                    }
                }
                Calendar currentMonth = Calendar.getInstance();
                currentMonth.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                currentMonth.set(Calendar.MONTH, list.get(j).month);
                currentMonth.set(Calendar.YEAR, list.get(j).year);
                j++;
                if(j < list.size())
                {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                    cal.set(Calendar.MONTH, list.get(j).month);
                    cal.set(Calendar.YEAR, list.get(j).year);
                    while(currentMonth.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && j < list.size())
                    {
                        //Log.e("Oye",cal.get(Calendar.DAY_OF_MONTH) + " /" + cal.get(Calendar.MONTH));
                        nonZeroList.add(list.get(j));
                        j++;
                        if(list.size() > j)
                        {
                            cal.set(Calendar.DAY_OF_MONTH, list.get(j).dayOfTheMonth);
                            cal.set(Calendar.MONTH, list.get(j).month);
                            cal.set(Calendar.YEAR, list.get(j).year);
                        }
                        else
                            break;
                    }
                }
                if(nonZeroList.size() != 0)
                    return nonZeroList;
                else
                    return null;
            }

        }
        return null;
    }

    private String getDurationString(int seconds) {

        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return hours + "." +minutes;
    }

    private String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
    public void roundValues() {
//        avg_drive_score = Utility.round(avg_drive_score);
        avg_mileage = Utility.round(avg_mileage, 1);
        avg_cost = Utility.roundToInt(avg_cost);
        avg_distance = Utility.round(avg_distance, 1);
        //avg_duration = Utility.roundToInt(avg_duration / 60 / 60);
        avg_duration = Float.parseFloat(getDurationString((int) avg_duration));
        //Log.e("AVG_MILEAGE" , String.valueOf(avg_duration));
    }

    public static ArrayList<GraphData> readSpecificMonth(String carID, int monthDay)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class)
                .where("car_Id='" + carID +"' AND dayOfTheYear='" + monthDay + "'")
                .execute();
        if(list.size() > 0)
            return list;
        else
            return null;
    }

    public static ArrayList<GraphData> readCurrentWeek(String carID, String tripTimeStamp)
    {
        Calendar cal = DateHelper.getCalendarFromServer(tripTimeStamp);

        int currentDayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheWeek + 2;
        int endDay = dayToFetch + 6;
        ArrayList<GraphData> list = new Select().from(GraphData.class).where("car_id='" + carID + "' AND dayOfTheYear>='" + dayToFetch + "' AND dayOfTheYear<='" + endDay + "' AND year='" + cal.get(Calendar.YEAR) + "'")
                .orderBy("dayOfTheYear ASC")
                .execute();
        if(list != null)
            return list;
        else
            return null;
    }

    public static ArrayList<GraphData> readCurrentMonth(String carID, String tripTimeStamp)
    {
        Calendar cal = DateHelper.getCalendarFromServer(tripTimeStamp);
        int currentDayOfTheMonth = cal.get(Calendar.DAY_OF_MONTH);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheMonth + 1;
        int endDay = dayToFetch + cal.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;
        ArrayList<GraphData> list = new Select().from(GraphData.class).where("car_id='" + carID + "' AND dayOfTheYear>='" + dayToFetch + "' AND dayOfTheYear<='" + endDay + "' AND year='" + cal.get(Calendar.YEAR) + "'")
                .orderBy("dayOfTheYear ASC")
                .execute();
        if(list != null)
            return list;
        else
            return null;
    }

    public static void updateAvgValueOfWeek(String tripTimeStamp, String carID, double avgDriveScoreForWeek)
    {
        Calendar cal = DateHelper.getCalendarFromServer(tripTimeStamp);
        int currentDayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheWeek + 2;

        new Update(GraphData.class).set("weekLabel=?", new String[]{String.valueOf(avgDriveScoreForWeek)}).where("car_id='" + carID + "' AND dayOfTheYear='" + dayToFetch + "' AND year='" + cal.get(Calendar.YEAR) + "'").execute();
    }

    public static void updateAvgValueOfMonth(String tripTimeStamp, String carID, double avgDriveScoreForMonth, double totalDistanceForMonth ,double avg_mileage, double costForTheMonth, double duration)
    {
        Calendar cal = DateHelper.getCalendarFromServer(tripTimeStamp);
        int currentDayOfTheMonth = cal.get(Calendar.DAY_OF_MONTH);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheMonth + 1;
        //Log.e("TAG","----- " + dayToFetch + "");
        new Update(GraphData.class).set("monthwiseDistanceValue=?,monthwiseDurationValue=?,monthwiseMileageValue=?,monthwiseCostValue=?,monthwiseDriveScoreValue=?", new String[]{String.valueOf(totalDistanceForMonth),String.valueOf(duration),String.valueOf(avg_mileage),String.valueOf(costForTheMonth),String.valueOf(avgDriveScoreForMonth)}).where("car_id='" + carID + "' AND dayOfTheYear='" + dayToFetch + "' AND year='" + cal.get(Calendar.YEAR) + "'").execute();
    }

    public static ArrayList<GraphData> fetchAvgValueForCurrentMonth(String carID)
    {
        Calendar cal = Calendar.getInstance();
        int currentDayOfTheMonth = cal.get(Calendar.DAY_OF_MONTH);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheMonth + 1;

        ArrayList<GraphData> list = new Select().from(GraphData.class).where("car_id='" + carID + "' AND dayOfTheYear='" + dayToFetch + "' AND year='" + cal.get(Calendar.YEAR) + "'")
                .execute();

        if(list != null)
            return list;
        else
            return null;
    }

    public static ArrayList<GraphData> fetchAvgValueForCurrentWeek(String carID)
    {
        Calendar cal = Calendar.getInstance();
        int currentDayOfTheWeek = cal.get(Calendar.DAY_OF_WEEK);
        int currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR);
        int dayToFetch = currentDayOfYear - currentDayOfTheWeek + 2;
        ArrayList<GraphData> list = new Select().from(GraphData.class).where("car_id='" + carID + "' AND dayOfTheYear='" + dayToFetch + "' AND year='" + cal.get(Calendar.YEAR) + "'")
                .execute();
        if(list != null)
            return list;
        else
            return null;
    }

    public static ArrayList<GraphData> readAllForAYear(String carID, int year)
    {
        ArrayList<GraphData> list = new Select().from(GraphData.class).where("car_id='" + carID + "' AND year='" + year + "'")
                .execute();
        return list;
    }

}

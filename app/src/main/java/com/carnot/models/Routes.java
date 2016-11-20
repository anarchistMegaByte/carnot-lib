package com.carnot.models;

import android.util.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.text.DecimalFormat;
import java.util.ArrayList;

@Table(name = "Routes")
public class Routes extends Model {

    @Column(name = "routeId")
    public Integer id;
    @Column
    public int tripId;
    @Column
    public String sts;
    @Column
    public Integer mil;
    @Column
    public double latitude;
    @Column
    public double longitude;
    @Column
    public double instant_mileage;
    @Column
    public double speed;

    public double getInstant_mileage() {
        try {
            DecimalFormat fmt = new DecimalFormat("0.00");
            this.instant_mileage = Double.parseDouble(fmt.format(instant_mileage));
        } catch (Exception e) {
            this.instant_mileage = instant_mileage;
        }
        return this.instant_mileage;
    }

    public static void removeExistingEntries(int tripId)
    {
        ArrayList<Routes> rt1 = new Select().from(Routes.class).where("tripId=?",String.valueOf(tripId)).execute();
        if(rt1 != null)
        {
           if(rt1.size() > 0)
           {
               new Delete().from(Routes.class).where("tripId=?",String.valueOf(tripId)).execute();
               //Log.e("Deleting : ",tripId + "");
           }
        }
        else
        {
            Log.e("Routes Table : ",rt1.toString());
        }

    }

}
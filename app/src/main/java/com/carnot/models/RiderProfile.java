package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

@Table(name = "RiderProfile")
public class RiderProfile extends Model {

    @Column(name = "riderId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;

    @Column
    public Integer driverscore;
    @Column
    public Integer nTrips;
    //    @Expose
//    public List<Trip> tips = new ArrayList<Trip>();
    @Column
    public Integer distance;
    @Column
    public Integer mileage;
    @Column
    public Integer hardBreak;
    @Column
    public List<Badges> badge = new ArrayList<Badges>();
    @Column
    public String gender;
    @Column
    public Integer hardAcc;
    @Column
    public String photo;
    @Column
    public Integer speed;
    @Column
    public List<Badges> recentBadges = new ArrayList<Badges>();
    @Column
    public Integer time;
    @Column
    public Integer idlingTime;
    @Column
    public Boolean isOnTrip;
    @Column
    public String name;
    @Column
    public Integer age;

    @Override
    public Long save() {
        long id = super.save();
        /*if (recentBadges != null && recentBadges.size() > 0) {
            for (Badges ba : recentBadges) {
                ba.save();
            }
        }
        if (badge != null && badge.size() > 0) {
            for (Badges ba : badge) {
                ba.save();
            }
        }*/
        return id;
    }

    public static ArrayList<RiderProfile> readAll() {
        ArrayList<RiderProfile> list = new Select().from(RiderProfile.class).execute();
        /*if (list != null && list.size() > 0) {
            for (RiderProfile model : list) {
                model.badge = new Select().from(Badges.class).where("carId=?", new String[]{model.id + ""}).execute();
            }
        }*/
        return list;
    }

}
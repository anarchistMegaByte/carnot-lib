package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.text.DecimalFormat;
import java.util.ArrayList;

@Table(name = "TripDetails")
public class TripDetails extends Model {

    @Column(name = "tripId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int trip_id;
    @Column
    public double distance;

    public double getDistance() {
        //Rounding value to 2 decimal places
        try {
            DecimalFormat fmt = new DecimalFormat("0.00");
            this.distance = Double.parseDouble(fmt.format(distance));
        } catch (Exception e) {
            this.distance = distance;
        }
        return this.distance;
    }

    @Column
    public Double end_lon;
    @Column
    public double drive_score;
    @Column
    public Double start_lat;
    @Column
    public Double end_lat;
    @Column
    public String end_time;
    @Column
    public ArrayList<Routes> route = new ArrayList<Routes>();
    @Column
    public Integer trip_time;
    @Column
    private double avg_mileage;

    public double getAvg_mileage() {
        //Rounding value to 2 decimal places
        try {
            DecimalFormat fmt = new DecimalFormat("0.00");
            this.avg_mileage = Double.parseDouble(fmt.format(avg_mileage));
        } catch (Exception e) {
            this.avg_mileage = avg_mileage;
        }
        return this.avg_mileage;
    }

    @Column
    public String avg_speed;
    @Column
    public String start_time;
    @Column
    public Double start_lon;


    /*public ArrayList<Graph> getGraphFromRoutes() {
        ArrayList<Graph> listGraph = new ArrayList<>();
        if (route != null) {
            for (Routes routes : route) {
                Graph graph = new Graph();
                graph.id = routes.id;
                graph.m = routes.getInstant_mileage();
                graph.sts = routes.sts;
                listGraph.add(graph);
            }
        }
        return listGraph;
    }*/

    @Override
    public Long save() {
        long id = super.save();
        if (route != null && route.size() > 0) {
            for (Routes rt : route) {
                rt.tripId = this.trip_id;
                rt.save();
            }
        }
        return id;
    }

    public static ArrayList<TripDetails> readAll() {
        ArrayList<TripDetails> list = new Select().from(TripDetails.class).execute();
        if (list != null && list.size() > 0) {
            for (TripDetails model : list) {
                model.route = new Select().from(Routes.class).where("tripId=?", new String[]{model.trip_id + ""}).execute();
            }
        }
        return list;
    }

    public static TripDetails readSpecific(String tripId) {
        ArrayList<TripDetails> list = new Select().from(TripDetails.class)
                .where("tripId='" + tripId + "'")
                .execute();
        if (list != null && list.size() > 0) {

            list.get(0).route = new Select().from(Routes.class).where("tripId=?", new String[]{list.get(0).trip_id + ""}).execute();

            return list.get(0);
        }
        return null;
    }
}
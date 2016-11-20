package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;

/**
 * Created by javid on 9/4/16.
 */
@Table(name = "Document")
public class Document extends Model {

    @Column(name = "dtype", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String dtype;
    @Column
    public String img;
    @Column
    public String car_id;
    @Column
    public String info;
    @Column
    public String sDesc;
    public boolean isAlert = false;

    //Meta information
    @Column
    public String date;
    @Column
    public String no;
    @Column
    public String regular_service;
    @Column
    public String battery_jumpstart;
    @Column
    public String battery_recharging;
    @Column
    public String polishing;
    @Column
    public String wheel_balanching;
    @Column
    public String wheel_aliging;

    public static ArrayList<Document> readAll(String carId) {
        ArrayList<Document> list = new Select().from(Document.class)
                .where("car_id='" + carId + "' AND dtype<>'profile' COLLATE NOCASE")
                .execute();
        return list;
    }

    public static ArrayList<Document> readDocument(String carId) {
        ArrayList<Document> list = new Select().from(Document.class)
                .where("car_id='" + carId + "' AND dtype<>'profile' COLLATE NOCASE AND dtype<>'ser' COLLATE NOCASE")
                .execute();
        return list;
    }

    public static ArrayList<Document> readServiceDetails(String carId) {
        ArrayList<Document> list = new Select().from(Document.class)
                .where("car_id='" + carId + "' AND dtype = 'ser' COLLATE NOCASE")
                .execute();
        return list;
    }

    public static Document getCarProfile(String carId) {
        ArrayList<Document> list = new Select().from(Document.class)
                .where("car_id='" + carId + "' AND dtype='profile' COLLATE NOCASE")
                .execute();
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}

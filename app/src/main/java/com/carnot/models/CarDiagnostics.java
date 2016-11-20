package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 28/4/16.
 */
@Table(name = "CarDiagnostics")
public class CarDiagnostics extends Model {

    @Column(name = "carId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;
    @Column
    public boolean engine_oil;
    @Column
    public int battery_health;
    @Column
    public int tyre_pressure_bottom_right;
    @Column
    public List<CarErrors> car_errors = new ArrayList<CarErrors>();
    @Column
    public boolean coolant_temperature;
    @Column
    public int tyre_pressure_bottom_left;
    @Column
    public int tyre_pressure_front_left;
    @Column
    public int cabin_temperature;
    @Column
    public int tyre_pressure_front_right;

    @Override
    public Long save() {
        long id = super.save();
        if (car_errors != null && car_errors.size() > 0) {
            for (CarErrors error : car_errors) {
                error.id = this.id;
                error.save();
            }
        }
        return id;
    }

    public static ArrayList<CarDiagnostics> readAll() {
        ArrayList<CarDiagnostics> list = new Select().from(CarDiagnostics.class).execute();
        if (list != null && list.size() > 0) {
            for (CarDiagnostics error : list) {
                error.car_errors = new Select().from(CarErrors.class).where("carId=?", new String[]{error.id + ""}).execute();
            }
        }
        return list;
    }

    public static CarDiagnostics readSpecific(String carId) {
        ArrayList<CarDiagnostics> list = new Select().from(CarDiagnostics.class)
                .where("carId='" + carId + "'")
                .execute();
        if (list != null && list.size() > 0) {

            list.get(0).car_errors = new Select().from(CarErrors.class).where("carId=?", new String[]{list.get(0).id + ""}).execute();

            return list.get(0);
        }
        return null;
    }
}

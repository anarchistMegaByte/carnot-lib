package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "CarErrors")
public class CarErrors extends Model {

    @Column(name = "carId", uniqueGroups = {"groupName"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public int id;
    @Column(uniqueGroups = {"groupName"}, onUniqueConflicts = {Column.ConflictAction.REPLACE})
    public String code = "abc";

    @Column
    public String timestamp;
    @Column
    public String title;
    @Column
    public int category;
    @Column
    public String desc;
    @Column
    public Double longitude;
    @Column
    public Double latitude;

    @Column
    public String metadata;

    @Override
    public String toString() {
        return code;
    }
}
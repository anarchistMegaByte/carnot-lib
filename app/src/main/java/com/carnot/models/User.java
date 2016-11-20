package com.carnot.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by root on 26/4/16.
 */
@Table(name = "User")
public class User extends Model {
    @Column(name = "userId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int id;
    @Column
    public String email;
    @Column
    public String token;
    @Column
    public String name;
    @Column
    public String ph;//photo
    @Column
    public String gender;
    @Column
    public String age;
    @Column
    public int iSpeedThreshold;
    @Column
    public String emergencyContacts;
    @Column
    public String registration_gcm;

}

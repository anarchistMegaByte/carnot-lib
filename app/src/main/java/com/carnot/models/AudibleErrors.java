package com.carnot.models;

import android.content.Context;
import android.media.MediaPlayer;

import java.util.ArrayList;

/**
 * Created by javid on 5/4/16.
 */
public class AudibleErrors {

    public String sFilename = "Bad Battery";
    public String sFileLength = "00:10";
    public int res;
    public String sFileDesc = "Looks like your battery has gone bad.It's possible that you have one of these errors";
    public ArrayList<CarErrors> arrErrors;
    public long milliseconds;

    public AudibleErrors(Context context, String title, int res, String desc) {
        this.sFilename = title;
        this.res = res;
        this.sFileDesc = title;
        this.milliseconds = MediaPlayer.create(context, res).getDuration();
    }

    public AudibleErrors() {
        arrErrors = new ArrayList<>();

        CarErrors errors = new CarErrors();
        errors.code = "404";

        CarErrors errors1 = new CarErrors();
        errors1.code = "405";

        arrErrors.add(errors);
        arrErrors.add(errors1);
    }
}

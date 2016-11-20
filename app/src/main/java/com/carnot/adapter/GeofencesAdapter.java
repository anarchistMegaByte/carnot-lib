package com.carnot.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.carnot.R;
import com.carnot.activity.ActivityGeofence;
import com.carnot.activity.ActivityNotificationSettings;
import com.carnot.models.Geofence;
import com.carnot.network.NetworkCallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chaks on 10/4/16.
 */

public class GeofencesAdapter extends BaseAdapter
{
    ArrayList<String> geo_names;
    private static LayoutInflater inflater=null;
    Context context;
    ArrayList<Integer> geo_ids;
    ArrayList<Boolean> geo_isLive;
    ArrayList<Geofence> geo_list;
    Snackbar snackbar;


    public GeofencesAdapter(Activity activityNotificationSettings, ArrayList<String> geo_names, ArrayList<Integer> geo_ids, ArrayList<Boolean> geo_isLive, ArrayList<Geofence> geo_list, Snackbar snackbar) {
        this.geo_names = geo_names;
        Log.e("chaks", "GeofencesAdapter: " + geo_names);
        inflater = ( LayoutInflater )activityNotificationSettings.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = activityNotificationSettings;
        this.geo_ids = geo_ids;
        this.geo_isLive = geo_isLive;
        this.geo_list = geo_list;
        this.snackbar = snackbar;
        Log.e("adapter", "GeofencesAdapter: geo_ids" + geo_ids );
    }

    @Override
    public int getCount() {
        Log.e("chaks", "getCount: " + geo_names.size() );
        return geo_names.size();
    }

    @Override
    public Object getItem(int i) {
        Log.e("chaks", "getItem: " + i );
        return i;
    }

    @Override
    public long getItemId(int i) {
        Log.e("chaks", "getItemId: " + i );
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        Log.e("chaks", "getView: " + position );
        View rowView;
        rowView = inflater.inflate(R.layout.single_geofence, null, true);
        EditText et=(EditText) rowView.findViewById(R.id.edittext_geo);
        CheckBox cb=(CheckBox) rowView.findViewById(R.id.chk_geofence);
        ImageButton ib=(ImageButton) rowView.findViewById(R.id.ib_geofence);
        et.setText(geo_names.get(position));
        ib.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ActivityGeofence.class);
                intent.putExtra("geo_id", String.valueOf(geo_ids.get(position)));
                intent.putExtra("geo_isLive", geo_isLive.get(position));
                context.startActivity(intent);
            }
        });
        if (geo_isLive.get(position)){
            cb.setChecked(true);
        }
        else{
            cb.setChecked(false);
        }

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

              @Override
              public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                  Geofence g;
                  if(isChecked){
                    geo_isLive.set(position, true);
                      Log.e("checkbox click", "onCheckedChanged: " + geo_list.get(position).geo_id );
                       g = new Select().from(Geofence.class).where("geo_id = ?", geo_list.get(position).geo_id).executeSingle();
                      geo_list.get(position).isLive = 1;

                      if (g!=null)
                      {
                          Log.e("hello", "onCheckedChanged: changing local db");
                          g.isLive = 1;
                      g.save();
                      }


                  }
                  else{
                      geo_isLive.set(position, false);
                      Log.e("checkbox click", "onCheckedChanged: " + geo_list.get(position).geo_id );
                      g = new Select().from(Geofence.class).where("geo_id = ?", geo_list.get(position).geo_id).executeSingle();
                      geo_list.get(position).isLive = 0;

                      if (g!=null)
                      {
                          Log.e("hello", "onCheckedChanged: changing local db lower");
                          g.isLive = 0;
                          g.save();}
                  }
                  HashMap<String, Object> metaList = new HashMap<String, Object>();
                  try {
                      metaList.put("name", geo_list.get(position).name);
                      metaList.put("radius", geo_list.get(position).radius);
                      metaList.put("lat", geo_list.get(position).lat);
                      metaList.put("lng", geo_list.get(position).lng);
                      metaList.put("status", geo_list.get(position).status);
                      metaList.put("isLive", geo_list.get(position).isLive);
                      metaList.put("geo_id", geo_list.get(position).geo_id);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
//                  Log.e("Data", "onCheckedChanged: " + Geofence.readAll(1));
                  Log.e("Error", "onCheckedChanged: " + geo_list.get(position).geo_id);
                  Geofence.edit_sync(geo_list.get(position).geo_id, metaList, "hi", snackbar);

              }
          }
        );
        return rowView;
    }
}

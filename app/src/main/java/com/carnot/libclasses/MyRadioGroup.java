package com.carnot.libclasses;

import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;

/**
 * Created by javid on 31/3/16.
 */
public class MyRadioGroup {

    ArrayList<RadioButton> list;

    public void addView(final RadioButton radio) {
        if (list == null)
            list = new ArrayList<RadioButton>();
        list.add(radio);
        radio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (RadioButton rd : list) {
                    rd.setChecked(false);
                }
                radio.setChecked(true);
                if (listener != null)
                    listener.onClick(v);
            }
        });
    }

   /* public int getCheckedPosition() {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isChecked()) {
                    return i;
                }


            }
        }
        return -1;
    }*/

    View.OnClickListener listener;

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}

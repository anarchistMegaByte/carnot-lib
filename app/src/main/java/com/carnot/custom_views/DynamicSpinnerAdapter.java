package com.carnot.custom_views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class DynamicSpinnerAdapter extends ArrayAdapter {
    private String[] _navigations;
    private int _resource;
    LayoutInflater inflater;
    Spinner spinner;
    public int firstItemWidth;
//        private int _textViewResourceId;

    public DynamicSpinnerAdapter(Context context, Spinner spinner, int resource, String[] objects) {
//            super(context, resource, objects);
        super(context, resource, objects);
        this.spinner = spinner;
        inflater = LayoutInflater.from(context);
        _navigations = objects;
        _resource = resource;
//            _textViewResourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        view = inflater.inflate(_resource, parent, false);

        TextView _textView = (TextView) view.findViewById(android.R.id.text1);
        _textView.setText(_navigations[position]);

//            Display display = mActivity.getWindowManager().getDefaultDisplay();
//            Point size = new Point();
//            display.getSize(size);
//            int _width = size.x;
//
//            view.setMinimumWidth(_width);

        if (view.getLayoutParams() == null) {
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        view.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED));
        int width = view.getMeasuredWidth();
        width += 45;
        spinner.getLayoutParams().width = width;
        if (position == 0)
            firstItemWidth = width;

        return view;
    }
}
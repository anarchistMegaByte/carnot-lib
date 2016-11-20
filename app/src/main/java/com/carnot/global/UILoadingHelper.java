package com.carnot.global;

import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by pankaj on 24/7/15.
 */
public class UILoadingHelper {
    private TextView txtEmpty;
    private ProgressBar progressBar;
    private View view;

    public void set(View txtEmpty, View progressBar, View view) {
        this.txtEmpty = (TextView) txtEmpty;
        this.txtEmpty.setGravity(Gravity.CENTER);
        this.progressBar = (ProgressBar) progressBar;
        this.view = view;
        this.txtEmpty.setVisibility(View.INVISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.view.setVisibility(View.INVISIBLE);

    }

    public void startProgress() {
        this.progressBar.setVisibility(View.VISIBLE);
        this.txtEmpty.setVisibility(View.INVISIBLE);
        this.view.setVisibility(View.INVISIBLE);
    }

    public void showError(String message) {
        this.txtEmpty.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.view.setVisibility(View.INVISIBLE);
        txtEmpty.setText(message);
    }

    public void showContent() {
        this.view.setVisibility(View.VISIBLE);
        this.progressBar.setVisibility(View.INVISIBLE);
        this.txtEmpty.setVisibility(View.INVISIBLE);
    }
}

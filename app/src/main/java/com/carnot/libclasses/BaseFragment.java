package com.carnot.libclasses;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.carnot.global.Utility;


public abstract class BaseFragment extends Fragment implements Act_ImpMethods {

    private View view;
    private int res;
    public Activity mActivity;
    public boolean isReloadFromBackStack = false, isAlreadyLoaded = false, hasOptionMenu = false;

    public void setContentView(int res) {
        this.res = res;
    }

    public void setContentView(int res, boolean isReloadFromBackStack) {
        this.res = res;
        this.isReloadFromBackStack = isReloadFromBackStack;
        this.hasOptionMenu = false;
    }

    public void setContentView(int res, boolean isReloadFromBackStack, boolean hasOptionMenu) {
        this.res = res;
        this.isReloadFromBackStack = isReloadFromBackStack;
        this.hasOptionMenu = hasOptionMenu;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    int _menuVisible = -1;
    boolean resumeCalled = false;

    @Override
    public void onResume() {
        super.onResume();

        if (_menuVisible == -1) {
            if (isMenuVisible())
                onFragmentResume();
        }
        _menuVisible = -1;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_menuVisible == -1) {
            if (isMenuVisible())
                onFragmentPause();
        }
        _menuVisible = -1;
    }

    @Override
    public void setMenuVisibility(boolean menuVisible) {
        super.setMenuVisibility(menuVisible);

        if (menuVisible) {
            this._menuVisible = 1;
            onFragmentResume();
        } else {
            this._menuVisible = 0;
            if (resumeCalled)
                onFragmentPause();
        }
    }

    public void onFragmentResume() {
        resumeCalled = true;
        Log.d("STATES", "onFragmentResume() called with: " + "");
    }

    public void onFragmentPause() {
        resumeCalled = false;
        Log.d("STATES", "onFragmentPause() called with: " + "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        if (isReloadFromBackStack) {
            if (view == null) {
                isAlreadyLoaded = false;
                view = inflater.inflate(res, container, false);
            } else {
                isAlreadyLoaded = true;
            }
        } else {
            isAlreadyLoaded = false;
            view = inflater.inflate(res, container, false);
        }
        setHasOptionsMenu(hasOptionMenu);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        try {
            super.onActivityCreated(savedInstanceState);
            mActivity = getActivity();

            initVariable();
            initView();
            postInitView();
            addAdapter();
            loadData();
        } catch (Exception e) {
            e.printStackTrace();
            showToast(e.toString());
            Utility.showLogE(BaseFragment.class, e.toString());
        }


    }

    public View links(int res) {
        return view.findViewById(res);
    }

    @Override
    public void postInitView() {
        // TODO Auto-generated method stub

    }

    public final void showSnackbar(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            if (links(android.R.id.content) != null)
                Snackbar.make(links(android.R.id.content), msg + "", Snackbar.LENGTH_SHORT).show();
            else
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    //test
    public final void showSnackbar(String msg, int type) {
        if (!TextUtils.isEmpty(msg)) {
//        Snackbar.make(findViewById(android.R.id.content), msg + "", Snackbar.LENGTH_SHORT).show();
            if (links(android.R.id.content) != null) {
                Snackbar snackbar = Snackbar.make(links(android.R.id.content), msg + "", Snackbar.LENGTH_LONG);
                if (type == MESSAGE_TYPE_SUCCESS) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#4F8A10"));
                } else if (type == MESSAGE_TYPE_WARNING) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#9F6000"));
                } else if (type == MESSAGE_TYPE_ERROR) {
                    snackbar.getView().setBackgroundColor(Color.parseColor("#D8000C"));
                }

                snackbar.show();
            } else {

                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public final void showToast(String msg) {
        if (!TextUtils.isEmpty(msg))
            Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
    }

    public boolean sendData(Bundle bnd) {
        return false;
    }

    /*@override
    public void startactivity(intent intent) {
        super.startactivity(intent);
        arraylist<class> list = new arraylist<class>(arrays.aslist(utility.exludeanimactivities));
        boolean isexludethis = (new arraylist<class>(arrays.aslist(utility.exludeanimactivities))).contains(mactivity);
        boolean isexludenew = false;
        try {
            isexludenew = list.contains(class.forname(intent.getcomponent().getclassname()));
        } catch (classnotfoundexception e) {
            e.printstacktrace();
        }

        mactivity.overridependingtransition(isexludenew ? 0 : r.anim.slide_in_right, isexludethis ? 0 : r.anim.slide_in_left);

    }

    @override
    public void startactivityforresult(intent intent, int requestcode) {
        super.startactivityforresult(intent, requestcode);
//        viewdraghelper
        arraylist<class> list = new arraylist<class>(arrays.aslist(utility.exludeanimactivities));
        boolean isexludethis = (new arraylist<class>(arrays.aslist(utility.exludeanimactivities))).contains(mactivity);
        boolean isexludenew = false;
        try {
            isexludenew = list.contains(class.forname(intent.getcomponent().getclassname()));
        } catch (classnotfoundexception e) {
            e.printstacktrace();
        }

        mactivity.overridependingtransition(isexludenew ? 0 : r.anim.slide_in_right, isexludethis ? 0 : r.anim.slide_in_left);
    }

    @override
    public void startactivityforresult(intent intent, int requestcode, bundle options) {
        super.startactivityforresult(intent, requestcode, options);
        arraylist<class> list = new arraylist<class>(arrays.aslist(utility.exludeanimactivities));
        boolean isexludethis = (new arraylist<class>(arrays.aslist(utility.exludeanimactivities))).contains(mactivity);
        boolean isexludenew = false;
        try {
            isexludenew = list.contains(class.forname(intent.getcomponent().getclassname()));
        } catch (classnotfoundexception e) {
            e.printstacktrace();
        }

        mactivity.overridependingtransition(isexludenew ? 0 : r.anim.slide_in_right, isexludethis ? 0 : r.anim.slide_in_left);
    }*/
}

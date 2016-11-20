package com.carnot.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.carnot.R;
import com.carnot.activity.ActivityAddNewCarSetup;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.PrefManager;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.models.Cars;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by pankaj on 1/4/16.
 */
public class FragmentAddSecurityCode extends BaseFragment {

    private final static String TAG = "FragmentAddSecurityCode";

    private final int TOTAL_LENGTH = 16;
    LinearLayout pinContainer1, pinContainer2, pinContainer3, pinContainer4;

    public FragmentAddSecurityCode() {
        setContentView(R.layout.fragment_add_new_car_setup_security_code);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {
        pinContainer1 = (LinearLayout) links(R.id.pin_container1);
        pinContainer2 = (LinearLayout) links(R.id.pin_container2);
        pinContainer3 = (LinearLayout) links(R.id.pin_container3);
        pinContainer4 = (LinearLayout) links(R.id.pin_container4);
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible) {
            //if this fragment becomes visible then directly makeing first EditText focusable and opening the keyboard
            Utility.showKeyboard(mActivity, ((EditText) pinContainer1.findViewById(0)));
        }
    }

    @Override
    public void postInitView() {
        super.postInitView();

        //Clearing all the child views initially
        pinContainer1.removeAllViews();
        pinContainer2.removeAllViews();
        pinContainer3.removeAllViews();
        pinContainer4.removeAllViews();

        //Adding 16 EditText in layout and adding listener for focous managing
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            if (i < TOTAL_LENGTH) {
                final EditText view = (EditText) LayoutInflater.from(mActivity).inflate(R.layout.include_pin_key, pinContainer1, false);
//                view.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                view.setId(i);

                //listener to make focus on next EditText
                view.addTextChangedListener(new MyTextWatcher(view));

                //Listener to manage clear button and focus
                view.setOnKeyListener(new CKeyListener(i > 0 ? (EditText) links(i - 1) : null));
                pinContainer1.addView(view);
            } /*else if (i < 8) {
                EditText view = (EditText) LayoutInflater.from(mActivity).inflate(R.layout.include_pin_key, pinContainer2, false);
                view.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                view.setId(i);
                view.addTextChangedListener(new MyTextWatcher(view));

                pinContainer2.addView(view);
            } else if (i < 12) {
                EditText view = (EditText) LayoutInflater.from(mActivity).inflate(R.layout.include_pin_key, pinContainer3, false);
                view.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                view.setId(i);
                view.addTextChangedListener(new MyTextWatcher(view));

                pinContainer3.addView(view);
            } else {
                EditText view = (EditText) LayoutInflater.from(mActivity).inflate(R.layout.include_pin_key, pinContainer4, false);
                view.setTransformationMethod(new AsteriskPasswordTransformationMethod());
                view.setId(i);
                view.addTextChangedListener(new MyTextWatcher(view));

                pinContainer4.addView(view);
            }*/
        }
    }

    /*View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            ((EditText)v).setSelectAllOnFocus();
        }
    };*/


    /**
     * Listener to manage clear button, when user clicks on clear button on keyboard we have to move focus to previous EditText
     */
    private static class CKeyListener implements View.OnKeyListener {

        private EditText pre_edt;

        public CKeyListener(EditText edt) {
            // TODO Auto-generated constructor stub
            this.pre_edt = edt;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            // TODO Auto-generated method stub

            if (pre_edt != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    // this is for backspace
                    String txt = ((EditText) v).getText().toString();
                    if (txt.length() <= 0) {
                        pre_edt.setText("");
                        pre_edt.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        }
    }

    class MyTextWatcher implements TextWatcher {

        EditText edt;

        MyTextWatcher(EditText edt) {
            this.edt = edt;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            //Checking if user enters text of length 1 then move focous to next EditText.
            if (edt.getId() < TOTAL_LENGTH - 1)
                links(edt.getId() + 1).requestFocus();

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }


    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {

    }

    @Override
    public boolean sendData(Bundle bnd) {
        if (isAdded()) {

            //Checking if next button is clicked then we validate and adding car information via webservice call.
            if (bnd.getString(ConstantCode.INTENT_ACTION).equalsIgnoreCase(ConstantCode.ACTION_NEXT)) {
                if (isSaved == false) {

                    //Checking the validation if all the Edittext are filled then calling the Webservice to add a car.
                    if (validate()) {
                        save();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    boolean isSaved = false;

    /**
     * Calls webservice to add car
     */
    private void save() {
        Bundle bundle = ((ActivityAddNewCarSetup) mActivity).bundle;
        if (bundle == null) {
            return;
        }

        HashMap<String, Object> metaList = new HashMap<String, Object>();
        try {
            metaList.put(ConstantCode.name, bundle.getString(ConstantCode.INTENT_NAME));
            metaList.put(ConstantCode.ln, bundle.getString(ConstantCode.INTENT_LICENSE_NO));
            metaList.put(ConstantCode.ft, bundle.getString(ConstantCode.INTENT_FUEL_TYPE));
            metaList.put(ConstantCode.model, bundle.getString(ConstantCode.INTENT_MODEL));
            metaList.put(ConstantCode.brand, bundle.getString(ConstantCode.INTENT_BRAND));
            metaList.put(ConstantCode.printedPasscode, getCode());

            metaList.put(ConstantCode.userid, Utility.getLoggedInUser().id + "");

            /*if (!TextUtils.isEmpty(carId))
                metaList.put(ConstantCode.carid, carId + "");*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, getString(R.string.msg_adding_car_wait));
        WebUtils.call(WebServiceConfig.WebService.SAVE_CAR_PROFILE, null, metaList, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                isSaved = true;

                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                showToast("Saved");
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);

                Cars car = (Cars) Utility.parseFromString(json.optString(ConstantCode.data), Cars.class);
                Log.e(TAG, "[SAVE_CAR_PROFILE] : car parameters - " + car.name);
                Log.e(TAG, "[SAVE_CAR_PROFILE] : car parameters - " + car.id);
                car.updateDeviceKeysOnly();
                Log.e(TAG, "[SAVE_CAR_PROFILE] : car parameters - added to car table on [ADDING NEW CAR FLOW]" + car.toString());

                if(!PrefManager.getInstance().contains(ConstantCode.RECENTLY_ADDED_CAR))
                {
                    PrefManager.putInt(ConstantCode.RECENTLY_ADDED_CAR, car.id);
                }
                else
                {
                    PrefManager.getInstance().edit().putInt(ConstantCode.RECENTLY_ADDED_CAR, car.id);
                }

                progressDialog.dismiss();

                ((ActivityAddNewCarSetup) mActivity).performNext();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showToast("" + values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                progressDialog.dismiss();
            }
        });
    }

    /**
     * Returns combined 16 key from 16 different Edittext
     *
     * @return
     */
    private String getCode() {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < TOTAL_LENGTH; i++) {
            EditText editText = (EditText) links(i);
            if (!Utility.isEmpty(editText)) {
                stringBuffer.append(editText.getText().toString().trim());
            }
        }
        return stringBuffer.toString();
    }

    /**
     * Used to validate all the EditText is not blank and other details that is filled on FragmentAddCarInfo.java like , nickname, license no , brand model
     *
     * @return
     */
    private boolean validate() {
        try {
            for (int i = 0; i < TOTAL_LENGTH; i++) {
                EditText editText = (EditText) links(i);
                if (Utility.isEmpty(editText)) {
                    showSnackbar(getString(R.string.msg_pin_key), MESSAGE_TYPE_ERROR);
                    return false;
                }
            }

            Bundle bundle = ((ActivityAddNewCarSetup) mActivity).bundle;
            if (bundle == null) {
                showSnackbar(getString(R.string.msg_enter_details), MESSAGE_TYPE_ERROR);
                ((ActivityAddNewCarSetup) mActivity).getViewPager().setCurrentItem(2);
                return false;
            } else if (TextUtils.isEmpty(bundle.getString(ConstantCode.INTENT_NAME))) {
                showSnackbar(getString(R.string.msg_enter_nickname), MESSAGE_TYPE_ERROR);
                ((ActivityAddNewCarSetup) mActivity).getViewPager().setCurrentItem(2);
                return false;
            } else if (TextUtils.isEmpty(bundle.getString(ConstantCode.INTENT_LICENSE_NO))) {
                showSnackbar(getString(R.string.msg_enter_license_no), MESSAGE_TYPE_ERROR);
                ((ActivityAddNewCarSetup) mActivity).getViewPager().setCurrentItem(2);
                return false;
            } else if (TextUtils.isEmpty(bundle.getString(ConstantCode.INTENT_BRAND))) {
                showSnackbar(getString(R.string.msg_enter_brand), MESSAGE_TYPE_ERROR);
                ((ActivityAddNewCarSetup) mActivity).getViewPager().setCurrentItem(2);
                return false;
            } else if (TextUtils.isEmpty(bundle.getString(ConstantCode.INTENT_MODEL))) {
                showSnackbar(getString(R.string.msg_enter_model), MESSAGE_TYPE_ERROR);
                ((ActivityAddNewCarSetup) mActivity).getViewPager().setCurrentItem(2);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}


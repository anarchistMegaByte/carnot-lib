package com.carnot.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.activity.ActivityAddNewCarSetup;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by pankaj on 1/4/16.
 * Used to save car information
 */
public class FragmentAddCarInfo extends BaseFragment {

    private EditText edtNickname, edtLicenseNo;//, edtModel;//edtBrand;
    private Spinner spnBrands, spnModels;

    ArrayList<String> brandList;
    ArrayList<String> modelList;
    boolean isAllSaved = false;

    RadioGroup radioGroupFuelType;

    public FragmentAddCarInfo() {
        setContentView(R.layout.fragment_add_new_car_setup_car_info, true);
    }

    @Override
    public void initVariable() {

    }

    @Override
    public void initView() {
        edtNickname = (EditText) links(R.id.edt_nickname);
        edtLicenseNo = (EditText) links(R.id.edt_license_no);
        radioGroupFuelType = (RadioGroup) links(R.id.radio_group_fuel_type);
        spnBrands = (Spinner) links(R.id.edt_brand);
        spnModels = (Spinner) links(R.id.edt_model);
    }


    @Override
    public void postInitView() {
        super.postInitView();
        edtLicenseNo.setHint("eg, DL-01-AK-0012");
        spnBrands.setVisibility(View.GONE);
        spnModels.setVisibility(View.GONE);

        //Adding textwatcher for masking the field as [ MH-12-AK-1234 ] format
        edtLicenseNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            boolean isBackPressed;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d("TAG", "onTextChanged() called with: " + "s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");
                if (before > count) {
                    isBackPressed = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d("TAG", "afterTextChanged() called with: " + "s = [" + s + "]");
                edtLicenseNo.removeTextChangedListener(this);

                //Managing back click event and on else adding -(dash)
                if (isBackPressed == true && (edtLicenseNo.getText().toString().length() == 2 || edtLicenseNo.getText().toString().length() == 5 || edtLicenseNo.getText().toString().length() == 8)) {
                    edtLicenseNo.setText(edtLicenseNo.getText().toString().replaceAll("[a-zA-Z0-9]{1,1}$", ""));
                } else if (isBackPressed == false && (edtLicenseNo.getText().toString().length() == 2 || edtLicenseNo.getText().toString().length() == 5 || edtLicenseNo.getText().toString().length() == 8)) {
                    edtLicenseNo.setText(edtLicenseNo.getText() + "-");
                }
                isBackPressed = false;
                edtLicenseNo.setSelection(edtLicenseNo.getText().length());
                edtLicenseNo.addTextChangedListener(this);
            }
        });

        edtLicenseNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Utility.hideKeyboard(mActivity, edtLicenseNo);
                    return true;
                }
                return false;
            }
        });
        //Adding listener to brands spinner so on selection of brands model is updated from server
        spnBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (isFirstTime == false) {

                //When brand is selected then we are loading models from server for specific brand
                getModelFromServer(spnBrands.getSelectedItem().toString());

//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spnModels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                /*
                Fragment fragment;

                PagerAdapter pagerAdapter = ((ActivityAddNewCarSetup) mActivity).getPagerAdapter();
                if (pagerAdapter instanceof ActivityAddNewCarSetup.CustomPagerAdapterWelcome) {
                    fragment = ((ActivityAddNewCarSetup.CustomPagerAdapterWelcome) pagerAdapter).getItem(3);
                } else {
                    fragment = ((ActivityAddNewCarSetup.CustomPagerAdapterCarInfo) pagerAdapter).getItem(2);
                }

//                updating next screen for fetching new video url
                if (fragment instanceof FragmentAddPlugInAdapter) {
                    Bundle bnd = new Bundle();
                    bnd.putString(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_UPDATE);
                    bnd.putString(ConstantCode.INTENT_BRAND, spnBrands.getSelectedItem().toString());
                    bnd.putString(ConstantCode.INTENT_MODEL, spnModels.getSelectedItem().toString());
                    ((FragmentAddPlugInAdapter) fragment).sendData(bnd);
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void addAdapter() {

    }

    @Override
    public void loadData() {
        getBrandsFromServer();
    }

    /**
     * Get All the brands from server
     */
    private void getBrandsFromServer() {
        WebUtils.call(WebServiceConfig.WebService.GET_BRANDS, null, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);

                Type collectionTypeDayType = new TypeToken<ArrayList<String>>() {
                }.getType();
                brandList = Utility.parseArrayFromString(jsonData.optString(ConstantCode.brands), String[].class);
                ArrayAdapter<String> brandsAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, brandList);
                brandsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnBrands.setAdapter(brandsAdapter);
                spnBrands.setVisibility(View.VISIBLE);
                /*if (selectedBrand != null && brandList != null) {
                    if (brandList.contains(selectedBrand)) {
                        if (modelList != null)
                            isFirstTime = true;
                        spnBrands.setSelection(brandList.indexOf(selectedBrand));
                    }
                }
                if (selectedModel != null && modelList != null) {
                    if (modelList.contains(selectedModel)) {
                        spnModels.setSelection(modelList.indexOf(selectedModel));
                    }
                }*/
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
//                if (counter == 0)
//                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
//                if (counter == 0)
//                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }
        });
    }

    /**
     * User to Get Model from server for specific brand
     *
     * @param brand
     */
    private void getModelFromServer(String brand) {

        try {
            brand = URLEncoder.encode(brand, "UTF-8");
            brand = brand.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        WebUtils.call(WebServiceConfig.WebService.GET_MODELS, new String[]{brand}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                JSONArray jsonArrayModel = jsonData.optJSONArray(ConstantCode.models);
                if (jsonArrayModel != null && jsonArrayModel.length() > 0) {
                    Type collectionTypeDayType = new TypeToken<ArrayList<String>>() {
                    }.getType();
                    modelList = Utility.parseArrayFromString(jsonData.optString(ConstantCode.models), String[].class);
                    ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, modelList);
                    modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnModels.setAdapter(modelAdapter);
                    spnModels.setVisibility(View.VISIBLE);
                    /*if (selectedModel != null && modelList != null) {
                        if (modelList.contains(selectedModel)) {
                            spnModels.setSelection(modelList.indexOf(selectedModel));
                        }
                    } else {
                        spnModels.setSelection(0);
                    }*/
                }
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
//                if (counter == 0)
//                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
//                if (counter == 0)
//                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }
        });
    }

    /*boolean isSaved = false;
    public String carId = "";
    public String imageUrl = "";
    public String videoUrl = "";*/

    @Override
    public boolean sendData(Bundle bnd) {
//        if (!isSaved) {
        if (isAdded()) {

            //Checking if next button is clicked then checking if user entered all the fields with validate() methods if not then show mendatory message and return true else return false, When we return false then ActivityAddNewCarSetup activity moves to next fragment
            if (bnd.getString(ConstantCode.INTENT_ACTION).equalsIgnoreCase(ConstantCode.ACTION_NEXT)) {
                if (validate()) {

                    String licenseNo = edtLicenseNo.getText().toString().trim();

                    //removing all the _ with blank in license no
                    if (licenseNo != null) {
                        licenseNo = licenseNo.replace("_", "");
                    }
                    //saving to parent activity which is gone to service at the end.
                    ActivityAddNewCarSetup activityAddNewCarSetup = ((ActivityAddNewCarSetup) mActivity);
                    activityAddNewCarSetup.bundle = new Bundle();
                    activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_NAME, edtNickname.getText().toString().trim());
                    activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_LICENSE_NO, licenseNo);

                    if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_other) {
                        activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_FUEL_TYPE, ConstantCode.FUEL_TYPE_OTHER);
                    } else if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_diesel) {
                        activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_FUEL_TYPE, ConstantCode.FUEL_TYPE_DIESEL);
                    } else if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_petrol) {
                        activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_FUEL_TYPE, ConstantCode.FUEL_TYPE_PETROL);
                    }

                    activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_BRAND, spnBrands != null ? spnBrands.getSelectedItem().toString() : "");
                    activityAddNewCarSetup.bundle.putString(ConstantCode.INTENT_MODEL, spnModels != null ? spnModels.getSelectedItem().toString() : "");


                    //=============Updating next fragment============
                    Fragment fragment;

                    PagerAdapter pagerAdapter = ((ActivityAddNewCarSetup) mActivity).getPagerAdapter();
                    if (pagerAdapter instanceof ActivityAddNewCarSetup.CustomPagerAdapterWelcome) {
                        fragment = ((ActivityAddNewCarSetup.CustomPagerAdapterWelcome) pagerAdapter).getItem(3);
                    } else {
                        fragment = ((ActivityAddNewCarSetup.CustomPagerAdapterCarInfo) pagerAdapter).getItem(2);
                    }

//                updating next screen for fetching new video url
                    if (fragment instanceof FragmentAddPlugInAdapter) {
                        Bundle bnd1 = new Bundle();
                        bnd1.putString(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_UPDATE);
                        bnd1.putString(ConstantCode.INTENT_BRAND, spnBrands.getSelectedItem().toString());
                        bnd1.putString(ConstantCode.INTENT_MODEL, spnModels.getSelectedItem().toString());
                        ((FragmentAddPlugInAdapter) fragment).sendData(bnd1);
                    }

                    return false;

                }
                return true;
            }
        }
        return false;
//        }
//        return false;
    }


    /**
     * Validates mandatory fields before moving to next screen
     *
     * @return
     */
    private boolean validate() {

        /*if (TextUtils.isEmpty(filePath)) {
            showSnackbar(getString(R.string.msg_select_photo), MESSAGE_TYPE_ERROR);
            return false;
        } else */
        if (Utility.isEmpty(edtNickname)) {
            showSnackbar(getString(R.string.msg_enter_nickname), MESSAGE_TYPE_ERROR);
            return false;
        } else if (Utility.isEmpty(edtLicenseNo)) {
            showSnackbar(getString(R.string.msg_enter_license_no), MESSAGE_TYPE_ERROR);
            return false;
        } //else if (!Utility.isValidLicenseNo(edtLicenseNo.getText().toString().trim())) {
        else if (false) {
            showSnackbar(getString(R.string.msg_enter_valid_license_no), MESSAGE_TYPE_ERROR);
            return false;
        } else if (spnBrands == null || Utility.isEmpty(spnBrands.getSelectedItem().toString())) {
            showSnackbar(getString(R.string.msg_enter_brand), MESSAGE_TYPE_ERROR);
            return false;
        } else if (spnModels == null || spnModels.getSelectedItem() == null || Utility.isEmpty(spnModels.getSelectedItem().toString())) {
            showSnackbar(getString(R.string.msg_enter_model), MESSAGE_TYPE_ERROR);
            return false;
        }

        return true;
    }


    /**
     * Saving information to server
     */
    public void save() {

//        String carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);

        HashMap<String, Object> metaList = new HashMap<String, Object>();
        try {
            metaList.put(ConstantCode.name, edtNickname.getText().toString().trim());
            metaList.put(ConstantCode.ln, edtLicenseNo.getText().toString().trim());
            metaList.put(ConstantCode.model, spnModels.getSelectedItem().toString().trim());
            metaList.put(ConstantCode.brand, spnBrands.getSelectedItem().toString().trim());
//            metaList.put(ConstantCode.ft, spnFuelType.getSelectedItem().toString().trim());
            metaList.put(ConstantCode.userid, Utility.getLoggedInUser().id + "");
            /*if (!TextUtils.isEmpty(carId))
                metaList.put(ConstantCode.carid, carId + "");*/
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject meta = new JSONObject();
        try {
            meta.put("name", edtNickname.getText().toString().trim());
            meta.put("ln", edtLicenseNo.getText().toString().trim());
            meta.put("model", spnModels.getSelectedItem().toString().trim());
            meta.put("brand", spnBrands.getSelectedItem().toString().trim());
            meta.put("userid", Utility.getLoggedInUser().id + "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*HashMap<String, String> data = new HashMap<String, String>();
        data.put(ConstantCode.dtype, "profile");
        data.put(ConstantCode.dinfo, "Car Profile");

        data.put(ConstantCode.content_type, "image/jpeg");
        data.put(ConstantCode.file, Utility.getBase64FromImage(filePath));
        data.put(ConstantCode.meta, new JSONObject(metaList).toString());


        //Uploading document

        WebUtils.call(WebServiceConfig.WebService.UPLOAD_DOCUMENTS, new String[]{carId}, data, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                showToast("Document Uploaded Successfully");

                *//*JSONObject json = (JSONObject) values;

                User user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);
                Utility.setLoggedInUser(user);

                startDashboardActivity();*//*

                if (isAllSaved == true) {
                    progressDialog.dismiss();
                    showToast("finish here");
                }
                isAllSaved = true;
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showToast("failed documents " + values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                progressDialog.dismiss();
            }
        });*/


        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Uploading, Please wait");
        isAllSaved = true;
        //Saving textual fields
        WebUtils.call(WebServiceConfig.WebService.SAVE_CAR_PROFILE, null, metaList, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                showToast("Saved");
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);

                if (isAllSaved == true) {
                    progressDialog.dismiss();

                    ((ActivityAddNewCarSetup) mActivity).performNext();
                }
                isAllSaved = true;
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showToast("failed to save " + values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                progressDialog.dismiss();
            }
        });
    }
}

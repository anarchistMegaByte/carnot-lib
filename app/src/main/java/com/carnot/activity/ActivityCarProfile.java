package com.carnot.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.carnot.R;
import com.carnot.custom_views.EditableLinearLayout;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.MediaCallback;
import com.carnot.libclasses.MediaHelper;
import com.carnot.libclasses.PermissionHelper;
import com.carnot.models.Cars;
import com.carnot.models.Document;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by javid on 31/3/16.
 * Activity to show and edit car profile
 */
public class ActivityCarProfile extends BaseActivity {

    public static boolean IS_FIRST_TIME = false;
    private MediaHelper mediaHelper;
    private ImageView ivCarPic;
    String filePath;
    private EditText edtNickname, edtLicenseNo;//, edtModel;//edtBrand;
    private TextView txtLblBrand, txtLblModel;
    private Spinner spnBrands, spnModels;
    private String selectedBrand, selectedModel, selectedFuel;
    boolean isFirstTime = false;
    ArrayList<String> brandList;
    ArrayList<String> modelList;
    boolean isAllSaved = false;
    Cars currentCars;
    RadioGroup radioGroupFuelType;
    RadioButton radioPetrol, radioDiesel, radioOther;
    private EditableLinearLayout editableLinearLayout;
    private CheckBox chkShowShortTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_profile);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.lbl_car_profile));
        getSupportActionBar().setTitle(getIntent().getExtras().getString(ConstantCode.INTENT_CAR_NAME));
        currentCars = Cars.readSpecific(getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID));
    }

    @Override
    public void initView() {
        ivCarPic = (ImageView) links(R.id.ivCarPic);
        edtNickname = (EditText) links(R.id.edt_nickname);
        edtLicenseNo = (EditText) links(R.id.edt_license_no);
        chkShowShortTrips = (CheckBox) links(R.id.chk_show_short_trips);
        txtLblBrand = (TextView) links(R.id.lbl_brand);
        txtLblModel = (TextView) links(R.id.lbl_model);

        editableLinearLayout = (EditableLinearLayout) links(R.id.editable_layout);

        radioGroupFuelType = (RadioGroup) links(R.id.radio_group_fuel_type);
        radioPetrol = (RadioButton) links(R.id.radio_petrol);
        radioDiesel = (RadioButton) links(R.id.radio_diesel);
        radioOther = (RadioButton) links(R.id.radio_other);

//        edtBrand = (EditText) links(R.id.edt_brand);
//        edtModel = (EditText) links(R.id.edt_model);
        spnBrands = (Spinner) links(R.id.edt_brand);
        spnModels = (Spinner) links(R.id.edt_model);

        mediaHelper = new MediaHelper(ActivityCarProfile.this);
    }


    @Override
    public void postInitView() {
//        edtLicenseNo.addTextChangedListener(new MaskTextWatcher(edtLicenseNo, "##/##"));

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

        spnBrands.setVisibility(View.GONE);
        spnModels.setVisibility(View.GONE);

        //Adding listener to brands spinner so on selection of brands model is updated from server
        spnBrands.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //When brand is selected then we are loading models from server for specific brand
                if (isFirstTime == false) {
                    getModelFromServer(spnBrands.getSelectedItem().toString());
                }
                isFirstTime = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Used to toggle between Viewable and Editable mode.
     *
     * @param isEdit
     */
    private void setEditableMode(boolean isEdit) {
        if (isEdit) {
            edtNickname.setFocusable(true);
            edtNickname.setFocusableInTouchMode(true);
            edtLicenseNo.setFocusable(true);
            edtLicenseNo.setFocusableInTouchMode(true);
            Utility.showKeyboard(mActivity, edtNickname);
            editableLinearLayout.setInEditMode(true);
            menuItemEdit.setVisible(false);
            menuItemUpdate.setVisible(true);
            txtLblBrand.setVisibility(View.GONE);
            txtLblModel.setVisibility(View.GONE);
            spnBrands.setVisibility(View.VISIBLE);
            spnModels.setVisibility(View.VISIBLE);
        } else {
            Utility.hideKeyboard(mActivity, edtNickname);
            editableLinearLayout.setInEditMode(false);
            menuItemEdit.setVisible(true);
            menuItemUpdate.setVisible(false);
            edtNickname.clearFocus();
            edtLicenseNo.clearFocus();
            edtNickname.setFocusable(false);
            edtNickname.setFocusableInTouchMode(false);
            edtLicenseNo.setFocusable(false);
            edtLicenseNo.setFocusableInTouchMode(false);
            txtLblBrand.setVisibility(View.VISIBLE);
            txtLblModel.setVisibility(View.VISIBLE);
            spnBrands.setVisibility(View.GONE);
            spnModels.setVisibility(View.GONE);
        }
    }

    @Override
    public void addAdapter() {

        ((ImageView) findViewById(R.id.ivCamera)).setOnClickListener(clickListener);

    }

    Document document;

    @Override
    public void loadData() {
        final String carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);
        document = Document.getCarProfile(carId);
        if (document != null) {
//            Uri imageUri = Uri.parse(document.img);
//            ivCarPic.setImageURI(imageUri);

            links(R.id.progress_image).setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(document.img)
                    .error(R.drawable.img_no_image)
                    .crossFade()
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            links(R.id.progress_image).setVisibility(View.GONE);
                            ivCarPic.setTag(null);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            links(R.id.progress_image).setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivCarPic);

            //Checking if it is first time then loading information from server
            if (IS_FIRST_TIME) {
                get();
                IS_FIRST_TIME = false;
            }
        } else {
            get();
            IS_FIRST_TIME = false;
        }

        if (currentCars != null) {
            chkShowShortTrips.setChecked(currentCars.showShortTrips);
            if (currentCars.name != null) {
                edtNickname.setText(currentCars.name + "");
            } else {
                edtNickname.setText("");
            }
            if (currentCars.ln != null)
                edtLicenseNo.setText(currentCars.ln + "");
            else {
                edtLicenseNo.setText("");
            }
            selectedFuel = currentCars.fuel;


            if (ConstantCode.FUEL_TYPE_OTHER.equalsIgnoreCase(selectedFuel)) {
                radioOther.setChecked(true);
            } else if (ConstantCode.FUEL_TYPE_DIESEL.equalsIgnoreCase(selectedFuel)) {
                radioDiesel.setChecked(true);
            } else if (ConstantCode.FUEL_TYPE_PETROL.equalsIgnoreCase(selectedFuel)) {
                radioPetrol.setChecked(true);
            }

            selectedBrand = currentCars.brand;
            selectedModel = currentCars.model;


            txtLblBrand.setText(selectedBrand);
            txtLblModel.setText(selectedModel);

            if (brandList != null) {
                if (brandList.contains(selectedBrand)) {
                    if (modelList == null) {
                        isFirstTime = true;
                    }
                    spnBrands.setSelection(brandList.indexOf(selectedBrand));
                }
            }
            if (modelList != null) {
                if (modelList.contains(selectedModel)) {
                    spnModels.setSelection(modelList.indexOf(selectedModel));
                }
            }
        }
        //Geting all the brands from server
        getBrandsFromServer();
    }


    MenuItem menuItemEdit, menuItemUpdate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_car_profile, menu);
        menuItemEdit = menu.findItem(R.id.action_edit_carprofile);
        menuItemUpdate = menu.findItem(R.id.action_update_carprofile);
        setEditableMode(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId()== R.id.action_update_carprofile){
                if (validate()) {
                    save();
                }
                return true;}

            if (item.getItemId()==  R.id.action_edit_carprofile){
                setEditableMode(true);
                return true;}

        return false;
    }

    /**
     * Validating all mendatory fields if all are correct it will return true else false
     *
     * @return
     */
    private boolean validate() {

        /*if (TextUtils.isEmpty(filePath)) {
            showSnackbar(getString(R.string.msg_select_photo), MESSAGE_TYPE_ERROR);
            return false;
        } else*/
        if (Utility.isEmpty(edtNickname)) {
            showSnackbar(getString(R.string.msg_enter_registration_no), MESSAGE_TYPE_ERROR);
            return false;
        } else if (Utility.isEmpty(edtLicenseNo)) {
            showSnackbar(getString(R.string.msg_enter_license_no), MESSAGE_TYPE_ERROR);
            return false;
        } else if (!Utility.isValidLicenseNo(edtLicenseNo.getText().toString().trim())) {
            showSnackbar(getString(R.string.msg_enter_valid_license_no), MESSAGE_TYPE_ERROR);
            return false;
        } else if (spnBrands == null || spnBrands.getSelectedItem() == null || Utility.isEmpty(spnBrands.getSelectedItem().toString())) {
            showSnackbar(getString(R.string.msg_enter_brand), MESSAGE_TYPE_ERROR);
            return false;
        } else if (spnModels == null || spnModels.getSelectedItem() == null || Utility.isEmpty(spnModels.getSelectedItem().toString())) {
            showSnackbar(getString(R.string.msg_enter_model), MESSAGE_TYPE_ERROR);
            return false;
        }

        return true;
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

                if (v.getId() == R.id.ivCamera){
                    pickDialog();}

        }
    };

    /**
     * Used to open dialog to show user a option to pick image from Camera or Gallery.
     */
    public void pickDialog() {
        try {

            final Intent intent = null;

            CharSequence colors[] = new CharSequence[]{getString(R.string.lbl_take_photo), getString(R.string.lbl_choose_from_gallery)};

            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityCarProfile.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.lbl_select_cover_photo));
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            dialog.dismiss();

                            //Asking for permission of camera for Marshmallow users
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {

                                    //Taking image from camera
                                    mediaHelper.takePictureFromCamera(null, null, new MediaCallback() {
                                        @Override
                                        public void onResult(boolean status, File file, MediaHelper.Media mediaType, Object object) {

                                            if (status) {
                                                filePath = file.getPath();
                                                showInImageView(ivCarPic);


                                            }
                                        }
                                    });
                                }

                                @Override
                                public void permissionRefused() {

                                }
                            });

                            break;
                        case 1:
                            dialog.dismiss();
                            //Asking for permission of camera for Marshmallow users
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {

                                    //Taking image from gallery
                                    mediaHelper.takePictureFromGallery(null, new MediaCallback() {
                                        @Override
                                        public void onResult(boolean status, File file, MediaHelper.Media mediaType, Object object) {

                                            if (status) {
                                                filePath = file.getPath();
                                                showInImageView(ivCarPic);
                                            }
                                        }
                                    });


                                }

                                @Override
                                public void permissionRefused() {

                                }
                            });
                            break;

                        default:
                            dialog.dismiss();
                            break;
                    }
                }
            });
            builder.show();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    /**
     * Loading profile from CarProfile and Document from server
     */
    private void get() {
        final String carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);

        //Getting car image from server
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "profile"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                document.car_id = carId;
                document.dtype = "profile";
                document.save();
//                //we are doing this because we are not getting updated url so on next time the url is fetch from server
//                IS_FIRST_TIME = true;

                links(R.id.progress_image).setVisibility(View.VISIBLE);
                Glide.with(mActivity)
                        .load(document.img)
                        .error(R.drawable.img_no_image)
                        .crossFade()
                        .listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                links(R.id.progress_image).setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                links(R.id.progress_image).setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(ivCarPic);

            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
//                if (counter == 0)
//                    uiLoadingHelper.showError(values.toString());
                links(R.id.progress_image).setVisibility(View.GONE);
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                links(R.id.progress_image).setVisibility(View.GONE);
//                if (counter == 0)
//                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                links(R.id.progress_image).setVisibility(View.GONE);
            }
        });

        //Getting car profile from server
        WebUtils.call(WebServiceConfig.WebService.GET_CAR_PROFILE, new String[]{carId}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;

                JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                edtNickname.setText(jsonData.optString(ConstantCode.name) + "");
                edtLicenseNo.setText(jsonData.optString(ConstantCode.ln) + "");
                selectedFuel = jsonData.optString(ConstantCode.fuel);
                if (ConstantCode.FUEL_TYPE_OTHER.equalsIgnoreCase(selectedFuel)) {
                    radioOther.setChecked(true);
                } else if (ConstantCode.FUEL_TYPE_DIESEL.equalsIgnoreCase(selectedFuel)) {
                    radioDiesel.setChecked(true);
                } else if (ConstantCode.FUEL_TYPE_PETROL.equalsIgnoreCase(selectedFuel)) {
                    radioPetrol.setChecked(true);
                }

                selectedBrand = jsonData.optString(ConstantCode.brand);
                selectedModel = jsonData.optString(ConstantCode.model);

                txtLblBrand.setText(selectedBrand);
                txtLblModel.setText(selectedModel);

                if (currentCars != null) {
                    currentCars.name = jsonData.optString(ConstantCode.name);
                    currentCars.ln = jsonData.optString(ConstantCode.ln);
                    currentCars.brand = jsonData.optString(ConstantCode.brand);
                    currentCars.model = jsonData.optString(ConstantCode.model);
                    currentCars.fuel = selectedFuel;
                    currentCars.saveProfileOnly();
                }

                if (brandList != null) {
                    if (brandList.contains(selectedBrand)) {
                        if (modelList == null) {
                            isFirstTime = true;
                        }
                        spnBrands.setSelection(brandList.indexOf(selectedBrand));
                    }
                }
                if (modelList != null) {
                    if (modelList.contains(selectedModel)) {
                        spnModels.setSelection(modelList.indexOf(selectedModel));
                    }
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

    /**
     * Get All the brands from server and loading list to Spinner Brands.
     */
    private void getBrandsFromServer() {
        WebUtils.call(WebServiceConfig.WebService.GET_BRANDS, null, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);

                brandList = Utility.parseArrayFromString(jsonData.optString(ConstantCode.brands), String[].class);
                ArrayAdapter<String> brandsAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, brandList);
                brandsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnBrands.setAdapter(brandsAdapter);
//                spnBrands.setVisibility(View.VISIBLE);
                if (selectedBrand != null && brandList != null) {
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
                } else {
                    getModelFromServer(selectedBrand);
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

    /**
     * Used to Get all Models for brand.
     *
     * @param brand
     */
    private void getModelFromServer(String brand) {
        try {
            brand = URLEncoder.encode(brand, "UTF-8");
            brand = brand.replace("+", "%20");
        } catch (Exception e) {
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
                    modelList = new Gson().fromJson(jsonData.optString(ConstantCode.models), collectionTypeDayType);
                    ArrayAdapter<String> modelAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_spinner_item, modelList);
                    modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spnModels.setAdapter(modelAdapter);
//                    spnModels.setVisibility(View.VISIBLE);
                    if (selectedModel != null && modelList != null) {
                        if (modelList.contains(selectedModel)) {
                            spnModels.setSelection(modelList.indexOf(selectedModel));
                        }
                    }
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


    /**
     * Updating profile and document image to server and success response updating local database also
     */
    private void save() {
        String carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);

        HashMap<String, Object> metaList = new HashMap<String, Object>();
        try {

            if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_other) {
                selectedFuel = ConstantCode.FUEL_TYPE_OTHER;
            } else if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_diesel) {
                selectedFuel = ConstantCode.FUEL_TYPE_DIESEL;
            } else if (radioGroupFuelType.getCheckedRadioButtonId() == R.id.radio_petrol) {
                selectedFuel = ConstantCode.FUEL_TYPE_PETROL;
            }

            String licenseNo = edtLicenseNo.getText().toString().trim();
            if (licenseNo != null) {
                licenseNo = licenseNo.replace(" ", "");
            }
            metaList.put(ConstantCode.name, edtNickname.getText().toString().trim());
            metaList.put(ConstantCode.ln, licenseNo);
            metaList.put(ConstantCode.ft, selectedFuel);
            metaList.put(ConstantCode.model, spnModels.getSelectedItem().toString().trim());
            metaList.put(ConstantCode.brand, spnBrands.getSelectedItem().toString().trim());
            metaList.put(ConstantCode.userid, Utility.getLoggedInUser().id + "");
            metaList.put(ConstantCode.carid, carId + "");
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
        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Saving, Please wait");
        //Checking if filepath exist then send the documents else no
        if (!TextUtils.isEmpty(filePath)) {
            HashMap<String, Object> data = new HashMap<String, Object>();
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
                    if (document != null && json.optString(ConstantCode.imgUrl) != null) {
                        document.img = json.optString(ConstantCode.imgUrl);
                        document.save();
                    }

                /*JSONObject json = (JSONObject) values;

                User user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);
                Utility.setLoggedInUser(user);

                startDashboardActivity();*/

                    if (isAllSaved == true) {
                        progressDialog.dismiss();
                        finish();
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
            });
        } else {
            isAllSaved = true;
        }
        //Saving textual fields
        WebUtils.call(WebServiceConfig.WebService.SAVE_CAR_PROFILE, null, metaList, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                showToast("Document Uploaded Successfully");
                if (currentCars != null) {
                    currentCars.name = edtNickname.getText().toString().trim();
                    currentCars.fuel = selectedFuel;
                    currentCars.ln = edtLicenseNo.getText().toString().trim();
                    currentCars.model = spnModels.getSelectedItem().toString().trim();
                    currentCars.brand = spnBrands.getSelectedItem().toString().trim();
                    currentCars.showShortTrips = chkShowShortTrips.isChecked();
                    currentCars.saveProfileOnly();
                    currentCars.updateShowShortTrip(chkShowShortTrips.isChecked());
                }
//                finish();

                /*JSONObject json = (JSONObject) values;

                User user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);
                Utility.setLoggedInUser(user);

                startDashboardActivity();*/
                if (isAllSaved == true) {
                    progressDialog.dismiss();
                    finish();
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

            @Override
            public void loadOffline() {
                super.loadOffline();
                progressDialog.dismiss();
                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mediaHelper != null)
            mediaHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

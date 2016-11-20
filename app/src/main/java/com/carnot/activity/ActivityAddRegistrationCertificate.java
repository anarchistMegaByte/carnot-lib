package com.carnot.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.MediaCallback;
import com.carnot.libclasses.MediaHelper;
import com.carnot.libclasses.PermissionHelper;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by javid on 31/3/16.
 * Activity add Document like Registration , PUC, Insurance, DL
 */
public class ActivityAddRegistrationCertificate extends BaseActivity {

    public static final String DATEPICKER_TAG = "datepicker";

    private MediaHelper mediaHelper;
    private ImageView imgCertificate, imgCameraCapture;
    private int documentType;
    String filePath;
    EditText edtDocumentNumber;
    TextView txtDate;
    Calendar calendar;
    TextView txtDocumentType;
    boolean isDateSelected = false;
    private TextView lblDocumentNo, lblDocumentDate;

    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_registration_certificate);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        documentType = getIntent().getExtras().getInt(ConstantCode.INTENT_DOCUMENT_TYPE);

        calendar = Calendar.getInstance();

        Calendar now = Calendar.getInstance();
        datePickerDialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        isDateSelected = true;
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, monthOfYear);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        txtDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                    }
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

    }

    @Override
    public void initView() {
        mediaHelper = new MediaHelper(ActivityAddRegistrationCertificate.this);

        lblDocumentNo = (TextView) links(R.id.lbl_document_no);
        lblDocumentDate = (TextView) links(R.id.lbl_document_date);
        edtDocumentNumber = (EditText) links(R.id.edt_document_number);
        txtDocumentType = (TextView) links(R.id.txt_registration_certificate);

        txtDate = (TextView) links(R.id.txt_document_date);

        imgCertificate = (ImageView) links(R.id.img_certificate);
        imgCameraCapture = (ImageView) links(R.id.img_camera_capture);

        //Checking document type and depending on that we are changing labels of the fields
        switch (documentType) {
            case ConstantCode.DOCUMENT_TYPE_REGISTRATION:
                txtDocumentType.setText(getString(R.string.lbl_registration_certificate));
                lblDocumentNo.setText(getString(R.string.lbl_registration_number));
                lblDocumentDate.setText(getString(R.string.lbl_date_of_registration));
                setTitle(getString(R.string.lbl_add_registration_certificate));
                break;
            case ConstantCode.DOCUMENT_TYPE_PUC:
                txtDocumentType.setText(getString(R.string.lbl_puc));
                lblDocumentDate.setText(getString(R.string.lbl_valid_till));
                lblDocumentNo.setVisibility(View.GONE);
                edtDocumentNumber.setVisibility(View.GONE);
                setTitle(getString(R.string.lbl_add_puc_detail));
                break;
            case ConstantCode.DOCUMENT_TYPE_INSURANCE:
                txtDocumentType.setText(getString(R.string.lbl_insurance));
                lblDocumentDate.setText(getString(R.string.lbl_valid_till));
                lblDocumentNo.setVisibility(View.GONE);
                edtDocumentNumber.setVisibility(View.GONE);
                setTitle(getString(R.string.lbl_add_insurance_detail));
                break;
            case ConstantCode.DOCUMENT_TYPE_DL:
                txtDocumentType.setText(getString(R.string.lbl_Driver_License));
                lblDocumentNo.setText(getString(R.string.lbl_dl_number));
                edtDocumentNumber.setHint(getString(R.string.lbl_dl_number));
                lblDocumentDate.setText(getString(R.string.lbl_valid_till));
                lblDocumentDate.setVisibility(View.GONE);
                txtDate.setVisibility(View.GONE);
                setTitle(getString(R.string.lbl_add_dl_detail));
                break;
        }
    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {

        imgCameraCapture.setOnClickListener(clickListener);
        txtDate.setOnClickListener(clickListener);
    }

    @Override
    public void loadData() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_documents, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_update_carprofile) {

                //Validing the fields if success then saving values
                if (validate()) {
                    save();
                }
                return true;

        }
        return false;
    }

    /**
     * Validate all the fields
     * <p/>
     * if document type is registration or dl then there must me document number
     * <p/>
     * if document type is puc insurance then date must be present
     *
     * @return
     */
    private boolean validate() {

        /*if (TextUtils.isEmpty(filePath)) {
            showSnackbar(getString(R.string.msg_select_photo), MESSAGE_TYPE_ERROR);
            return false;
        } else*/

        if (Utility.isEmpty(edtDocumentNumber) && (documentType == ConstantCode.DOCUMENT_TYPE_REGISTRATION || documentType == ConstantCode.DOCUMENT_TYPE_DL)) {
            showSnackbar(getString(R.string.msg_enter_registration_no), MESSAGE_TYPE_ERROR);
            return false;
        } else if (!isDateSelected && (documentType == ConstantCode.DOCUMENT_TYPE_PUC || documentType == ConstantCode.DOCUMENT_TYPE_INSURANCE)) {
            showSnackbar(getString(R.string.msg_enter_date_of_purchase), MESSAGE_TYPE_ERROR);
            return false;
        }

        return true;
    }

    /**
     * Used to upload document to server
     */
    private void save() {

        String carId = getIntent().getExtras().getString(ConstantCode.INTENT_CAR_ID);

        JSONObject meta = new JSONObject();
        try {
            meta.put("no", edtDocumentNumber.getText().toString().trim());
            meta.put("date", txtDate.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Object> data = new HashMap<String, Object>();

        switch (documentType) {
            case ConstantCode.DOCUMENT_TYPE_REGISTRATION:
                data.put(ConstantCode.dtype, "reg");
                data.put(ConstantCode.dinfo, "Registration Document");
                break;
            case ConstantCode.DOCUMENT_TYPE_PUC:
                data.put(ConstantCode.dtype, "puc");
                data.put(ConstantCode.dinfo, "Car PUC Document");
                break;
            case ConstantCode.DOCUMENT_TYPE_INSURANCE:
                data.put(ConstantCode.dtype, "ins");
                data.put(ConstantCode.dinfo, "Insurance Document");
                break;
            case ConstantCode.DOCUMENT_TYPE_DL:
                data.put(ConstantCode.dtype, "dl");
                data.put(ConstantCode.dinfo, "Car Service Document");
                break;
        }

        data.put(ConstantCode.content_type, "image/jpeg");
        data.put(ConstantCode.file, Utility.getBase64FromImage(filePath));
//        data.put(ConstantCode.file, "iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAYAAABV7bNHAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA4ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTExIDc5LjE1ODMyNSwgMjAxNS8wOS8xMC0wMToxMDoyMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo5ZjU0ZTc4ZS02YjlmLTQxZWQtODZhZi1lOTc4NDQ1MWE0NWYiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6MUFBMkMxODZERkNEMTFFNUI2ODU5REVFOUIwMTkxQjUiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6MUFBMkMxODVERkNEMTFFNUI2ODU5REVFOUIwMTkxQjUiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKE1hY2ludG9zaCkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDowYzE4MjFiMi00MTA2LTRkNTUtOWZiMi02MjZlODAyNWY3MzkiIHN0UmVmOmRvY3VtZW50SUQ9ImFkb2JlOmRvY2lkOnBob3Rvc2hvcDoyNWI1YzRhNC0yODEwLTExNzktYTBhZC04YmY3NDg1NmFiMmQiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz4ZwCJUAAADI0lEQVR42uzcvWsUQRgG8LkzomhAsBAjKGKjsFYWgl8BLyCJ4EeliGBQkFRBO/E/sDU2QSwMgtZKSIrgih8YsLHxIJWCNtqIogEDhvMZbxYkcrszu7Ozz87NC8+RkL3L3o/9eGdu9xqdTkeE6l3NQBCAAlAACkC8NWDtldotk6W3IN/Uz/I0eg25U+j/R7G3W1ADuY1Mhl2shkhMxyBKJLaDNB0S41mMCon1NE+DxNwHUSCxN4qVIxVrFNutC3gcRa4gqyUjicLNpFOgLs4DZJ3KuI9ITQs4si4iM//8nrdWkM9Mu1vTAo6wiPQLOc6E1DTEkSt4NQXBBtISE5IZUBTLkfcp5EXKUl4hme9iUbyMx5P9gpTvIN1HSPkbRXdIn5D3mS1AuzXJBeQGaTMyhxzOWO438oNzqFEeUoIznLHcd2QM63GfdyxmH0kX5yNyFHlah8GqLSRdnLfIIeQd72C1N1LaG5RIgynro4Mzj5xDftZxukNnSzqTsvVk4dxFTrvAKXM+SAfJtGQXfxOZUGctUWcg20grate85Xo+qOwZRRtIX5ETyCNRQbmYci2C9AE5YnlXpQPKi/RGncaXRIXlctLeBOkxIq+G+CIqLtefanQMlqO4eNIl0CbV4A1rLHsWeYZs6xcgE5ykDiKLyF7fgfLgJLUHeY0c8xWoCE5SW5EF5LxvQDZwktqgGsUbvgDZxEmqoYYa06L4B5SVAungPEnplV5mvP6Eev5gHYF0cB4il3v8TY7SxzSQZMP5HNlRJyBdnEsi/SKHZU2kA6oN2M8P1G7ZwjFF2oW8QkZ4gezjmCLJi9LnsB7jfEDl4axFWsxYbr2CIgIqHyepncjujIHtdRHFUzxA7nD2IfImjKFUHCGmeI5BfYQja8AQR3azs45w5HTH9ipxzLeg7gVU91LevFc4+XaxKO6FYANnIxNO/oP0/0g2cJJROw2O+TFoLVL3LsNRNbZaLXE9K8H5O4Vg7YsF8t+SaQfH41syKbecOgBVjsMMRIHDCkSDwwhEhcMGRIfDBESJwwJEi2O3UfS0wre/BKAAFIACEHH9EWAA6QMPi0/GNIsAAAAASUVORK5CYII=");
        data.put(ConstantCode.meta, meta);

        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Uploading, Please wait");
        WebUtils.call(WebServiceConfig.WebService.UPLOAD_DOCUMENTS, new String[]{carId}, data, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();
                JSONObject json = (JSONObject) values;
                showToast("Document Uploaded Successfully");
                setResult(RESULT_OK);
                finish();

                /*JSONObject json = (JSONObject) values;

                User user = (User) Utility.parseFromString(json.optString(ConstantCode.data), User.class);
                Utility.setLoggedInUser(user);

                startDashboardActivity();*/
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                progressDialog.dismiss();
                showToast("failed documents " + values);
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                progressDialog.dismiss();
            }
        });

//        finish();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.img_camera_capture) {
                    pickDialog();}
            if(v.getId() == R.id.txt_document_date){
                    datePickerDialog.show(getFragmentManager(), DATEPICKER_TAG);}
            }
    };


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


    /**
     * Opening dialog to take image from camera or from gallery
     */
    public void pickDialog() {
        try {
            final Intent intent = null;

            CharSequence colors[] = new CharSequence[]{getString(R.string.lbl_take_photo), getString(R.string.lbl_choose_from_gallery)};

            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityAddRegistrationCertificate.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.lbl_select_cover_photo));
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            dialog.dismiss();

                            //Requesting for permission of CAMERA Permission for Marshmallow Devices
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {
                                    mediaHelper.takePictureFromCamera(null, null, new MediaCallback() {
                                        @Override
                                        public void onResult(boolean status, File file, MediaHelper.Media mediaType, Object object) {

                                            if (status) {
                                                filePath = file.getPath();
                                                showInImageView(imgCertificate);
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
                            //Requesting for permission of READ EXTERNAL STORAGE Permission for Marshmallow Devices
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {

                                    mediaHelper.takePictureFromGallery(null, new MediaCallback() {
                                        @Override
                                        public void onResult(boolean status, File file, MediaHelper.Media mediaType, Object object) {
                                            if (status) {
                                                filePath = file.getPath();
                                                showInImageView(imgCertificate);
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
}

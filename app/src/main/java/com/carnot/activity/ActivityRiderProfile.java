package com.carnot.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.carnot.libclasses.ContactsPicker;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.libclasses.MediaCallback;
import com.carnot.libclasses.MediaHelper;
import com.carnot.libclasses.PermissionHelper;
import com.carnot.models.Contacts;
import com.carnot.models.User;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by javid on 31/3/16.
 * Activity to show Profile of Rider
 */
public class ActivityRiderProfile extends BaseActivity implements DatePickerDialog.OnDateSetListener {

    public static final String DATEPICKER_TAG = "datepicker";

    private EditText edtNickname;
    ArrayList<Contacts> list;
    String[] listNames;
    private MediaHelper mediaHelper;
    private ImageView ivCarPic;
    private EditText edtDob;

    private RadioGroup radioGroup;
    private LinearLayout llEmergencyContacts;
    String filePath;
    private ContactsPicker contactsPicker;
    private EditableLinearLayout editableLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_profile);
        PermissionHelper.init(mActivity);
    }

    @Override
    public void initVariable() {
        setToolbar(R.id.toolbar, true);
        setTitle(getString(R.string.action_my_profile));
        contactsPicker = new ContactsPicker(this);
    }


    @Override
    public void initView() {
        ivCarPic = (ImageView) links(R.id.ivCarPic);
        edtNickname = (EditText) links(R.id.edt_nickname);
        radioGroup = (RadioGroup) links(R.id.radio_group_gender);
        llEmergencyContacts = (LinearLayout) links(R.id.ll_emergency_contacts);
        editableLinearLayout = (EditableLinearLayout) links(R.id.editable_layout);
        editableLinearLayout.setInEditMode(false);
        edtDob = (EditText) links(R.id.edt_dob);
        mediaHelper = new MediaHelper(ActivityRiderProfile.this);

    }

    @Override
    public void postInitView() {
        edtNickname.clearFocus();
    }

    @Override
    public void addAdapter() {

        ((ImageView) findViewById(R.id.ivCamera)).setOnClickListener(clickListener);

        /*datePickerDialog.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                txtDob.setText(day + "/" + month + "/" + year);
                age = Calendar.getInstance().get(Calendar.YEAR) - year;
            }
        });



        txtDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.setYearRange(Calendar.getInstance().get(Calendar.YEAR) - 90, Calendar.getInstance().get(Calendar.YEAR) - 10);
                datePickerDialog.setCloseOnSingleTapDay(true);
                datePickerDialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });*/

    }

    private boolean validate() {

        if (Utility.isEmpty(edtNickname)) {
            showSnackbar(getString(R.string.msg_enter_nickname), MESSAGE_TYPE_ERROR);
            return false;
        } else if (Utility.isEmpty(edtDob)) {
            showSnackbar(getString(R.string.msg_enter_dob), MESSAGE_TYPE_ERROR);
            return false;
        }

        return true;
    }

    /**
     * Saving Rider information and document(image) to server
     */
    private void save() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        HashMap<String, Object> params = new HashMap<>();
        params.put(ConstantCode.nm, edtNickname.getText().toString().trim());
        params.put(ConstantCode.gd, radioGroup.getCheckedRadioButtonId() == R.id.radio_male ? ConstantCode.male : ConstantCode.female);
        params.put(ConstantCode.ag, edtDob.getText().toString().trim() + "");

        if (!TextUtils.isEmpty(filePath)) {
            params.put(ConstantCode.content_type, "image/jpeg");
            params.put(ConstantCode.file, Utility.getBase64FromImage(filePath));
        }

        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Saving please wait");
        WebUtils.call(WebServiceConfig.WebService.SAVE_MY_PROFILE, new String[]{id}, params, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                progressDialog.dismiss();
                JSONObject json = (JSONObject) values;

                //Saving information in our local database also
                User user = Utility.getLoggedInUser();
                user.name = edtNickname.getText().toString().trim();
                user.age = edtDob.getText().toString().trim() + "";
                user.gender = radioGroup.getCheckedRadioButtonId() == R.id.radio_male ? ConstantCode.male : ConstantCode.female;

                if (json != null) {
                    user.ph = json.optString(ConstantCode.imgUrl);
                }

                //saving information in shared preference
                Utility.setLoggedInUser(user);
                user.save();

                //making fields viewable
                setEditableMode(true);

                showToast("Profile saved successfully");
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

            @Override
            public void loadOffline() {
                super.loadOffline();
                progressDialog.dismiss();
                showSnackbar(getString(R.string.msg_internet_alert), MESSAGE_TYPE_ERROR);
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
            edtDob.setFocusable(true);
            edtDob.setFocusableInTouchMode(true);
            //Utility.showKeyboard(mActivity, edtNickname);
            editableLinearLayout.setInEditMode(true);
            menuItemEdit.setVisible(false);
            menuItemUpdate.setVisible(true);

        } else {
            Utility.hideKeyboard(mActivity, edtNickname);
            editableLinearLayout.setInEditMode(false);
            menuItemEdit.setVisible(true);
            menuItemUpdate.setVisible(false);
            edtNickname.clearFocus();
            edtDob.clearFocus();
            edtNickname.setFocusable(false);
            edtNickname.setFocusableInTouchMode(false);
            edtDob.setFocusable(false);
            edtDob.setFocusableInTouchMode(false);
        }
    }

    @Override
    public void loadData() {
        User user = Utility.getLoggedInUser();
        //checking if there are emergencycontacts are stored not stored on local database then fetching emergency contacts from server
        if (user.emergencyContacts != null && user.emergencyContacts.length() > 0) {
            try {
                //loading emergencycontacts from server
                resetEmergencyContactView(new JSONArray(user.emergencyContacts));
            } catch (JSONException e) {
                e.printStackTrace();
                loadEmergencyContactFromServer();
            }
        } else {
            loadEmergencyContactFromServer();
        }
        edtNickname.setText(user.name);
        if (!"0".equalsIgnoreCase(user.age)) {
            edtDob.setText(user.age + "");
        }
        if (ConstantCode.male.equalsIgnoreCase(user.gender))
            ((RadioButton) links(R.id.radio_male)).setChecked(true);
        else
            ((RadioButton) links(R.id.radio_female)).setChecked(true);

        if (user.ph != null) {
//                    Uri imageUri = Uri.parse("http://carnotimgs.s3.amazonaws.com/pictures/img_56_profile.jpg");
//            Uri imageUri = Uri.parse(user.ph);
//                    Uri imageUri = Uri.parse("https://carnotimgs.s3.amazonaws.com/pictures/img_ODBRefer.png");

//            ivCarPic.setImageURI(imageUri);

            links(R.id.progress_image).setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(user.ph)
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
        }

//        loadProfileFromServer();
//        loadEmergencyContactFromServer();
    }

    /*//Reading contacts from phone
    private void readAllContacts() {
        new AsyncTask<Void, Void, ArrayList<Contacts>>() {

            @Override
            protected ArrayList<Contacts> doInBackground(Void... params) {
                list = new ArrayList<Contacts>();
                Cursor phones = mActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                listNames = new String[phones.getCount() + 1];
                Contacts contact = new Contacts();
                contact.name = "Select Contact";
                contact.number = "";
                contact.email = "";
                list.add(contact);
                listNames[0] = contact.name;
                int i = 1;
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contacts contact1 = new Contacts();
                    contact1.name = name;
                    contact1.number = phoneNumber;
                    contact1.email = "";
                    list.add(contact1);
                    listNames[i] = name + "[ " + phoneNumber + " ]";
                    i++;
                }

                phones.close();
                return list;
            }

            @Override
            protected void onPostExecute(ArrayList<Contacts> list) {
                super.onPostExecute(list);
                ActivityRiderProfile.this.list = list;
                *//*ArrayAdapter<Contacts> contactsAdapter = new ArrayAdapter<Contacts>(mActivity, android.R.layout.simple_spinner_item, list);
                contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spnContact1.setAdapter(contactsAdapter);
                spnContact2.setAdapter(contactsAdapter);
                spnContact3.setAdapter(contactsAdapter);*//*
            }
        }.execute();
    }*/

    //Read emergency contacts from server
    private void loadEmergencyContactFromServer() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        //Saving textual fields
        WebUtils.call(WebServiceConfig.WebService.GET_EMERGENCY_NUMBER, new String[]{id}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                JSONObject json = (JSONObject) values;
                JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                JSONArray jsonContacts = jsonData.optJSONArray(ConstantCode.contacts);

                User user = Utility.getLoggedInUser();
                user.emergencyContacts = jsonContacts.toString();
                Utility.setLoggedInUser(user);
                user.save();

                //populating emergency contacts from the response getting from server
                resetEmergencyContactView(jsonContacts);

            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
                llEmergencyContacts.removeAllViews();
                for (int i = 0; i < 3; i++) {
                    View view = LayoutInflater.from(mActivity).inflate(R.layout.include_emergency_contact, llEmergencyContacts, false);
                    ((TextView) view.findViewById(R.id.txt_rider_name)).setText("Select Contact");
                    ((TextView) view.findViewById(R.id.tv_gender_age)).setText("");
                    view.setClickable(true);
                    view.setOnClickListener(emergencyContactsClickListener);
                    llEmergencyContacts.addView(view);
                }
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
//                progressDialog.dismiss();
            }

            @Override
            public void loadOffline() {
                super.loadOffline();
                try {
                    JSONArray jsonContacts = new JSONArray(Utility.getLoggedInUser().emergencyContacts);
                    if (jsonContacts != null) {
                        resetEmergencyContactView(jsonContacts);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * Updating UI to show emergencycontacts
     *
     * @param jsonContacts
     */
    private void resetEmergencyContactView(JSONArray jsonContacts) {

        llEmergencyContacts.removeAllViews();

        for (int i = 0; i < jsonContacts.length(); i++) {
            JSONObject jsonObject1 = jsonContacts.optJSONObject(i);

            Contacts contacts = new Contacts();
            contacts.name = jsonObject1.optString(ConstantCode.nm);
            contacts.number = jsonObject1.optString(ConstantCode.ph);

            View view = LayoutInflater.from(mActivity).inflate(R.layout.include_emergency_contact, llEmergencyContacts, false);
            ((TextView) view.findViewById(R.id.txt_rider_name)).setText(jsonObject1.optString(ConstantCode.nm));
            ((TextView) view.findViewById(R.id.tv_gender_age)).setText(jsonObject1.optString(ConstantCode.ph));

            view.setClickable(true);
            view.setOnClickListener(emergencyContactsClickListener);
            view.setTag(contacts);
            llEmergencyContacts.addView(view);
        }

        //Checking if there are less then 3 emergency contacts are there then we set remaining contacts as default "Select Contact" Placeholder so user can add more
        int remaining = 3 - llEmergencyContacts.getChildCount();
        //adding remaining fields
        for (int i = 0; i < remaining; i++) {
            View view = LayoutInflater.from(mActivity).inflate(R.layout.include_emergency_contact, llEmergencyContacts, false);
            ((TextView) view.findViewById(R.id.txt_rider_name)).setText("Select Contact");
            ((TextView) view.findViewById(R.id.tv_gender_age)).setText("");
            view.setClickable(true);
            view.setOnClickListener(emergencyContactsClickListener);
            llEmergencyContacts.addView(view);
        }
    }

    View view1;
    TextView txtRiderName;
    TextView txtRiderAge;
    AlertDialog alertDialog;
    boolean isEmergencyChange = false;
    View.OnClickListener emergencyContactsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View view) {
            view1 = view;
            txtRiderName = (TextView) view.findViewById(R.id.txt_rider_name);
            txtRiderAge = (TextView) view.findViewById(R.id.tv_gender_age);

            EasyPermissions.requestPermissions(ActivityRiderProfile.this, new EasyPermissions.PermissionCallbacks() {
                @Override
                public void onPermissionsGranted(int requestCode, List<String> perms) {
                    contactsPicker.pickContacts(new ContactsPicker.OnContactPicked() {
                        @Override
                        public void onContactPicked(String name, String number) {
                            txtRiderName.setText(name);
                            txtRiderAge.setText(number);
                        }
                    });
                }

                @Override
                public void onPermissionsDenied(int requestCode, List<String> perms) {

                }

                @Override
                public void onPermissionsPermanentlyDeclined(int requestCode, List<String> perms) {
                /*Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                mActivity.startActivity(intent);*/
                }
            }, null, 100, Manifest.permission.READ_CONTACTS);

        }
    };


    MenuItem menuItemEdit, menuItemUpdate;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_car_profile, menu);
        menuItemEdit = menu.findItem(R.id.action_edit_carprofile);
        menuItemUpdate = menu.findItem(R.id.action_update_carprofile);
        setEditableMode(true);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

            if (item.getItemId() == R.id.action_update_carprofile){
                if (validate()) {
                    save();
                }

                if (isEmergencyChange) {
                    saveEmergencyContacts();
                }
                return true;}

            if (item.getItemId() ==  R.id.action_edit_carprofile){
                setEditableMode(true);
                return true;}

        return false;
    }

    //Used to save emergency contacts to server
    private void saveEmergencyContacts() {
        final JSONArray jsonArray = new JSONArray();
        try {

            for (int i = 0; i < llEmergencyContacts.getChildCount(); i++) {
                if (llEmergencyContacts.getChildAt(i).getTag() != null) {

                    Contacts contact = (Contacts) llEmergencyContacts.getChildAt(i).getTag();

                    //removing all the characters except number from phone number
                    String number = contact.number.replaceAll("[^0-9]", "");
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(ConstantCode.nm, contact.name);
                    jsonObject.put(ConstantCode.ph, Utility.getLast10Digit(number));
                    jsonObject.put(ConstantCode.em, contact.email + "");
                    jsonArray.put(jsonObject);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (jsonArray.length() <= 0) {
            showSnackbar(getString(R.string.msg_mendatory_contact));
        }

        HashMap<String, Object> param = new HashMap<>();
        param.put(ConstantCode.contacts, jsonArray);

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Saving please wait");
        //Saving textual fields
        WebUtils.call(WebServiceConfig.WebService.ADD_EMERGENCY_NUMBER, new String[]{id}, param, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);

                User user = Utility.getLoggedInUser();

                //saving emergencycontacts to local
                user.emergencyContacts = jsonArray.toString();
                Utility.setLoggedInUser(user);
                user.save();

                progressDialog.dismiss();
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

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


                if (v.getId() == R.id.ivCamera){
                    pickDialog();}

            }
    };

    /**
     * Shows dialog to pick photo from Camera or gallery
     */
    public void pickDialog() {
        try {

            final Intent intent = null;

            CharSequence colors[] = new CharSequence[]{getString(R.string.lbl_take_photo), getString(R.string.lbl_choose_from_gallery)};

            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRiderProfile.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getString(R.string.lbl_select_cover_photo));
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            dialog.dismiss();

                            //Requesting for permission for marshmallow devices
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {
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
                            //Requesting for permission for marshmallow devices
                            PermissionHelper.askForPermission(mActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionHelper.PermissionCallback() {
                                @Override
                                public void permissionGranted() {

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
            builder.create().show();
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mediaHelper != null)
            mediaHelper.onActivityResult(requestCode, resultCode, data);
        if (contactsPicker != null)
            contactsPicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {

    }
}

package com.carnot.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.activity.ActivityAddNewCarSetup;
import com.carnot.dialogs.DialogUtil;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.ContactsPicker;
import com.carnot.libclasses.EasyPermissions;
import com.carnot.models.Contacts;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by pankaj on 1/4/16.
 */
public class FragmentAddCarSafetyFirst extends BaseFragment {

    //    Spinner spnContact1, spnContact2, spnContact3;
    TextView txtContact1, txtContact2, txtContact3;
    ContactsPicker contactsPicker;

    public FragmentAddCarSafetyFirst() {
        setContentView(R.layout.fragment_add_new_car_setup_safety);
    }

    @Override
    public void initVariable() {
        contactsPicker = new ContactsPicker(this);
    }

    @Override
    public void initView() {
        txtContact1 = (TextView) links(R.id.txt_contact1);
        txtContact2 = (TextView) links(R.id.txt_contact2);
        txtContact3 = (TextView) links(R.id.txt_contact3);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (contactsPicker != null) {
            contactsPicker.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void addAdapter() {
        txtContact1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(txtContact1);
            }
        });
        txtContact2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(txtContact2);
            }
        });
        txtContact3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact(txtContact3);
            }
        });
    }

    private void pickContact(final TextView txtName) {
        EasyPermissions.requestPermissions(this, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onPermissionsGranted(int requestCode, List<String> perms) {
                contactsPicker.pickContacts(new ContactsPicker.OnContactPicked() {
                    @Override
                    public void onContactPicked(String name, String number) {
                        Contacts contacts = new Contacts();
                        contacts.name = name;
                        contacts.number = number;
                        txtName.setText(name + " [" + number + "]");
                        txtName.setTag(contacts);
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


    @Override
    public void loadData() {
        /*EasyPermissions.requestPermissions(FragmentAddCarSafetyFirst.this, new EasyPermissions.PermissionCallbacks() {
            @Override
            public void onPermissionsGranted(int requestCode, List<String> perms) {
                readAllContacts();

            }

            @Override
            public void onPermissionsDenied(int requestCode, List<String> perms) {
                loadContactFromServer();
            }

            @Override
            public void onPermissionsPermanentlyDeclined(int requestCode, List<String> perms) {
                *//*Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mActivity.getPackageName(), null);
                intent.setData(uri);
                mActivity.startActivity(intent);*//*
            }
        }, null, 100, Manifest.permission.READ_CONTACTS);*/
        loadContactFromServer();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadContactFromServer() {

        String id = ConstantCode.DEFAULT_USER_ID;
        if (Utility.getLoggedInUser() != null)
            id = Utility.getLoggedInUser().id + "";

//        final ProgressDialog progressDialog = DialogUtil.ProgressDialog(mActivity, "Saving please wait");
        //Saving textual fields
        WebUtils.call(WebServiceConfig.WebService.GET_EMERGENCY_NUMBER, new String[]{id}, null, new NetworkCallbacks() {
                    @Override
                    public void successWithString(Object values, WebServiceConfig.WebService webService) {
                        super.successWithString(values, webService);

                        JSONObject json = (JSONObject) values;
                        JSONObject jsonData = json.optJSONObject(ConstantCode.data);
                        JSONArray jsonContacts = jsonData.optJSONArray(ConstantCode.contacts);
                        Contacts contact1 = null, contact2 = null, contact3 = null;

                        if (jsonContacts == null) {
//                            ((ActivityAddNewCarSetup) mActivity).enableButton(false);
                            return;
                        }

                        if (jsonContacts.length() > 0) {
                            isSaved = true;
//                            ((ActivityAddNewCarSetup) mActivity).enableButton(true);
                            JSONObject jsonObject1 = jsonContacts.optJSONObject(0);
                            contact1 = new Contacts();
                            contact1.name = jsonObject1.optString(ConstantCode.nm);
                            contact1.number = jsonObject1.optString(ConstantCode.ph);
                        }

                        if (jsonContacts.length() > 1) {
                            JSONObject jsonObject2 = jsonContacts.optJSONObject(1);
                            contact2 = new Contacts();
                            contact2.name = jsonObject2.optString(ConstantCode.nm);
                            contact2.number = jsonObject2.optString(ConstantCode.ph);
                        }
                        if (jsonContacts.length() > 2) {
                            JSONObject jsonObject3 = jsonContacts.optJSONObject(2);
                            contact3 = new Contacts();
                            contact3.name = jsonObject3.optString(ConstantCode.nm);
                            contact3.number = jsonObject3.optString(ConstantCode.ph);
                        }

                        if (contact1 != null) {
                            txtContact1.setText(contact1.name + " [" + contact1.number + "]");
                        }
                        if (contact2 != null) {
                            txtContact2.setText(contact2.name + " [" + contact2.number + "]");
                        }
                        if (contact3 != null) {
                            txtContact3.setText(contact3.name + " [" + contact3.number + "]");
                        }


                        /*int found1 = -1, found2 = -1, found3 = -1;
                        if (list == null) {
                            list = new ArrayList<Contacts>();
                        }

                        if (list != null && list.size() > 0) {
                            int i = 0;
                            for (Contacts contacts : list) {
                                if (contact1 != null && contact1.name != null) {
                                    if (contacts.name.equalsIgnoreCase(contact1.name)) {
                                        found1 = i;
                                    }
                                }
                                if (contact2 != null && contact2.name != null) {
                                    if (contacts.name.equalsIgnoreCase(contact2.name)) {
                                        found2 = i;
                                    }
                                }
                                if (contact3 != null && contact3.name != null) {
                                    if (contacts.name.equalsIgnoreCase(contact3.name)) {
                                        found3 = i;
                                    }
                                }
                                i++;
                            }
                        }

                        if (spnContact1.getAdapter() == null || spnContact2.getAdapter() == null || spnContact3 == null) {
                            Contacts contact = new Contacts();
                            contact.name = "Select Contact";
                            contact.number = "";
                            contact.email = "";
                            list.add(contact);
                            ArrayAdapter<Contacts> contactsAdapter = new ArrayAdapter<Contacts>(mActivity, android.R.layout.simple_spinner_item, list);
                            contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spnContact1.setAdapter(contactsAdapter);
                            spnContact2.setAdapter(contactsAdapter);
                            spnContact3.setAdapter(contactsAdapter);
                        }

                        if (contact1 != null && contact1.name != null) {
                            if (found1 == -1) {
                                if (list.size() > 1) {
                                    list.add(1, contact1);
                                    found1 = 1;
                                } else {
                                    list.add(contact1);
                                    found1 = list.size() - 1;
                                }
                            }
                            if (found1 >= 0) {
                                spnContact1.setSelection(found1);
                            }
                        }

                        if (contact2 != null && contact2.name != null) {
                            if (found2 == -1) {
                                if (list.size() > 2) {
                                    list.add(2, contact2);
                                    found2 = 2;
                                } else {
                                    list.add(contact2);
                                    found2 = list.size() - 1;
                                }
                            }
                            if (found2 >= 0) {
                                spnContact2.setSelection(found2);
                            }
                        }
                        if (contact3 != null && contact3.name != null) {
                            if (found3 == -1) {
                                if (list.size() > 3) {
                                    list.add(3, contact3);
                                    found3 = 3;
                                } else {
                                    list.add(contact3);
                                    found3 = list.size() - 1;
                                }
                            }
                            if (found3 >= 0) {
                                spnContact3.setSelection(found3);
                            }
                        }*/


//                showToast("Contacts " + json);

//                progressDialog.dismiss();
//                showToast("finish here");
                    }

                    @Override
                    public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                        super.failedWithMessage(values, webService);
//                progressDialog.dismiss();
//                        showToast("failed to get" + values.toString());
//                        ((ActivityAddNewCarSetup) mActivity).enableButton(false);
                    }

                    @Override
                    public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                        super.failedForNetwork(values, webService);
//                        ((ActivityAddNewCarSetup) mActivity).enableButton(false);
//                progressDialog.dismiss();
                    }
                }

        );
    }


    /*ArrayList<Contacts> list;

    private void readAllContacts() {
        new AsyncTask<Void, Void, ArrayList<Contacts>>() {

            @Override
            protected ArrayList<Contacts> doInBackground(Void... params) {
                ArrayList<Contacts> list = new ArrayList<Contacts>();
                Cursor phones = mActivity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
                while (phones.moveToNext()) {
                    String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Contacts contact = new Contacts();
                    contact.name = name;
                    contact.number = phoneNumber;
                    contact.email = "";
                    list.add(contact);
                }
                Contacts contact = new Contacts();
                contact.name = "Select Contact";
                contact.number = "";
                contact.email = "";
                list.add(0, contact);
                phones.close();
                return list;
            }

            @Override
            protected void onPostExecute(ArrayList<Contacts> list) {
                super.onPostExecute(list);
                FragmentAddCarSafetyFirst.this.list = list;
                ArrayAdapter<Contacts> contactsAdapter = new ArrayAdapter<Contacts>(mActivity, android.R.layout.simple_spinner_item, list);
                contactsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spnContact1.setAdapter(contactsAdapter);
                spnContact2.setAdapter(contactsAdapter);
                spnContact3.setAdapter(contactsAdapter);
            }
        }.execute();
    }*/

    boolean isSaved = false;

    private boolean validate() {
        if (txtContact1.getTag() == null && txtContact2.getTag() == null && txtContact3.getTag() == null) {
            showSnackbar(getString(R.string.msg_mendatory_contact));
            return false;
        }
        return true;
    }

    @Override
    public boolean sendData(Bundle bnd) {
        if (bnd.getString(ConstantCode.INTENT_ACTION).equalsIgnoreCase(ConstantCode.ACTION_NEXT)) {
            if (!isSaved) {
                if (validate()) {
                    save();
                }
                return true;
            }
        }
        return false;
    }


    private void save() {


        JSONArray jsonArray = new JSONArray();
        try {
            if (txtContact1.getTag() != null) {
                Contacts contact = (Contacts) txtContact1.getTag();
                String number = contact.number.replaceAll("[^0-9]", "");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ConstantCode.nm, contact.name);
                jsonObject.put(ConstantCode.ph, Utility.getLast10Digit(number));
                jsonObject.put(ConstantCode.em, contact.email + "");
                jsonArray.put(jsonObject);
            }
            if (txtContact2.getTag() != null) {
                Contacts contact = (Contacts) txtContact2.getTag();
                String number = contact.number.replaceAll("[^0-9]", "");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ConstantCode.nm, contact.name);
                jsonObject.put(ConstantCode.ph, Utility.getLast10Digit(number));
                jsonObject.put(ConstantCode.em, contact.email + "");
                jsonArray.put(jsonObject);
            }
            if (txtContact3.getTag() != null) {
                Contacts contact = (Contacts) txtContact3.getTag();
                String number = contact.number.replaceAll("[^0-9]", "");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(ConstantCode.nm, contact.name);
                jsonObject.put(ConstantCode.ph, Utility.getLast10Digit(number));
                jsonObject.put(ConstantCode.em, contact.email + "");
                jsonArray.put(jsonObject);
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

                JSONObject json = (JSONObject) values;
                showToast("Saved");

                progressDialog.dismiss();
//                showToast("finish here");
                isSaved = true;
                ((ActivityAddNewCarSetup) mActivity).performNext();
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

package com.carnot.libclasses;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by pankaj on 7/12/16.
 */
public class ContactsPicker {
    private Activity activity;
    private Fragment fragment;
    private final int PICK_CONTACT = 123;

    public ContactsPicker(Activity activity) {
        this.activity = activity;
    }

    public ContactsPicker(Fragment fragment) {
        this.fragment = fragment;
    }

    OnContactPicked callback;

    public void pickContacts(OnContactPicked contactPicked) {
        try {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            contactPickerIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE); // Show user only contacts w/ phone numbers
            this.callback = contactPicked;
            if (activity != null)
                activity.startActivityForResult(contactPickerIntent, PICK_CONTACT);
            else {
                fragment.startActivityForResult(contactPickerIntent, PICK_CONTACT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("in on ActivityResult");

                    Uri contactUri = data.getData();
                    Cursor cursor;
                    if (activity != null) {
                        cursor = activity.getContentResolver().query(contactUri, null, null, null, null);
                    } else {
                        cursor = fragment.getActivity().getContentResolver().query(contactUri, null, null, null, null);
                    }
                    cursor.moveToFirst();

                    Log.i("Contact", "===============================");
                    for (String s : cursor.getColumnNames()) {
                        Log.i("Contact", s + " = " + cursor.getString(cursor.getColumnIndex(s)));
                    }
                    Log.i("Contact", "===============================");

                    // Retrieve the phone number from the NUMBER column
                    String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    //((TextView) findViewById(R.id.txt_name_number)).setText(name + "[" + number + "]");
                    // Do something with the phone number...
                    cursor.close();

                    if (callback != null) {
                        callback.onContactPicked(name, number);
                    }
                }
                break;
        }
    }

    public interface OnContactPicked {
        public void onContactPicked(String name, String number);
    }


}

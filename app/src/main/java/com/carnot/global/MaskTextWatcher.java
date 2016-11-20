package com.carnot.global;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.ParseException;


public class MaskTextWatcher implements TextWatcher {

    private String mMask;
    String mResult = "";
    private EditText editText;

    public MaskTextWatcher(EditText editText, String mask) {
        mMask = mask;
        this.editText = editText;
    }

    @Override
    public void afterTextChanged(Editable s) {


    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start,
                              int before, int count) {

        /*String mask = mMask;
        String value = s.toString();
        String marks = mMask.replaceAll("#", "");
        value = value.replaceAll(marks, "");

        if (value.equals(mResult))
            return;
        editText.removeTextChangedListener(this);
        int in = editText.getSelectionEnd();
        editText.setText(FormatWithMask(value, mMask));
        editText.setSelection(in);
        mResult = value;
        editText.addTextChangedListener(this);*/


        String mask = mMask;
        String value = s.toString();

        if (value.equals(mResult))
            return;

        try {

            // prepare the formatter
            MaskFormatter formatter = new MaskFormatter(mask);
            formatter.setValueContainsLiteralCharacters(false);
            formatter.setPlaceholderCharacter((char) 1);

            // get a string with applied mask and placeholder chars
            value = formatter.valueToString(value);

            try {

                // find first placeholder
                value = value.substring(0, value.indexOf((char) 1));

                //process a mask char
                if (value.charAt(value.length() - 1) ==
                        mask.charAt(value.length() - 1)) {
                    value = value.substring(0, value.length() - 1);
                }

            } catch (Exception e) {
            }

            mResult = value;

            editText.removeTextChangedListener(this);
//            s.replace(0, s.length(), value);
            editText.setText(mResult);
            editText.setSelection(editText.getText().toString().length());
            editText.addTextChangedListener(this);

        } catch (ParseException e) {

            //the entered value does not match a mask
            int offset = e.getErrorOffset();
            value = removeCharAt(value, offset);
            editText.removeTextChangedListener(this);
//            s.replace(0, s.length(), value);
            editText.addTextChangedListener(this);
        }


    }

    public String FormatWithMask(String input, String mask) {
        if (TextUtils.isEmpty(input))
            return input;

        String output = "";
        int index = 0;
        for (char m : mask.toCharArray()) {
            if (m == '#') {
                if (index < input.length()) {
                    output += input.charAt(index);
                    index++;
                } else {
                    break;
                }
            } else
                output += m;
        }
        return output;
    }


    public static String removeCharAt(String s, int pos) {

        StringBuffer buffer = new StringBuffer(s.length() - 1);
        buffer.append(s.substring(0, pos)).append(s.substring(pos + 1));
        return buffer.toString();

    }

}


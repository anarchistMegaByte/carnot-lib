package com.carnot.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.carnot.R;
import com.carnot.custom_views.LinePagerIndicator;
import com.carnot.fragment.FragmentAddBluetoothSettings;
import com.carnot.fragment.FragmentAddCarInfo;
import com.carnot.fragment.FragmentAddCarIntro;
import com.carnot.fragment.FragmentAddCarSafetyFirst;
import com.carnot.fragment.FragmentAddPlugInAdapter;
import com.carnot.fragment.FragmentAddSecurityCode;
import com.carnot.global.ConstantCode;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.BaseFragment;
import com.carnot.libclasses.NonSwipableViewPager;
import com.carnot.models.User;

/**
 * Created by pankaj on 31/3/16.
 * Making the activity as dialog, in this activity we will load other fragment in viewpager
 */
public class ActivityAddNewCarSetup extends BaseActivity {

    public static boolean isActivityStarted = false;
    public Bundle bundle;
    LinePagerIndicator line_pager_indicator;

    public ViewPager getViewPager() {
        return viewPager;
    }

    //We are taking nonswipableview pager because we have to remove the swiping of view pager
    NonSwipableViewPager viewPager;


    //    CustomPagerAdapter pagerAdapter;
    TextView txtTitle;
    Button btnPrev, btnNext;
    ImageView imgClose;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isActivityStarted = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActivityStarted = true;
        bundle = new Bundle();
        setContentView(R.layout.activity_add_new_car_setup);

        int[] widthHeight = Utility.getDeviceWidthHeight(mActivity);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = widthHeight[0] - (int) Utility.convertDpToPixel(50, mActivity);
        params.height = widthHeight[1] - 100;

        this.getWindow().setAttributes(params);
        this.setFinishOnTouchOutside(false);
    }

    @Override
    public void onBackPressed() {
        //Checking if it is comming from signup page then compulsary add car
        if (getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_ACTION))) {
            return;
        }
        super.onBackPressed();
    }

    public PagerAdapter getPagerAdapter() {
        if (customPagerAdapterWelcome != null)
            return customPagerAdapterWelcome;
        else
            return customPagerAdapterCarInfo;
    }


    @Override
    public void initVariable() {

    }

    boolean skipEmergencyContact = false;

    /*public void enableButton(boolean skipEmergencyContact) {
        this.skipEmergencyContact = skipEmergencyContact;
        links(R.id.progress_bar).setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }*/

    @Override
    public void initView() {
        txtTitle = (TextView) links(R.id.txt_title);
        line_pager_indicator = (LinePagerIndicator) links(R.id.line_pager_indicator);
        viewPager = (NonSwipableViewPager) links(R.id.view_pager);
        btnPrev = (Button) links(R.id.btn_prev);
        btnNext = (Button) links(R.id.btn_next);
        imgClose = (ImageView) links(R.id.img_close);
    }

    @Override
    public void postInitView() {
        viewPager.setPagingEnabled(false);
        btnPrev.setVisibility(View.GONE);
        viewPager.setOffscreenPageLimit(3);

        /*//We are showing this once emergenycontact loading is finished and updated via enableButton(boolean) from FragmentAddCarSafetyFirst.java
        if (!(getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_ACTION)))) {
            links(R.id.progress_bar).setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
        }*/
    }

    @Override
    public void addAdapter() {

        //Checking is this activity opened for adding first car, if it is open for adding first car then we need to show the welcome screen with Emergency contacts, else without emergency contacts screen
        if (getIntent() != null && getIntent().hasExtra(ConstantCode.INTENT_ACTION) && ConstantCode.ACTION_REGISTER_CAR.equalsIgnoreCase(getIntent().getStringExtra(ConstantCode.INTENT_ACTION))) {
            setUpAdapterWelcome();
            line_pager_indicator.setUp(viewPager, 1);
        } else {
            setUpAdapterCarInfo();
            line_pager_indicator.setUp(viewPager, 1);
        }

        btnPrev.setOnClickListener(clickListener);
        btnNext.setOnClickListener(clickListener);
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void loadData() {
//        txtTitle.setText(getString(R.string.lbl_welcome_x, Utility.getLoggedInUser().email));
    }

    public void performNext() {
        clickListener.onClick(btnNext);
    }

    public void performPrev() {
        clickListener.onClick(btnPrev);
    }

    /**
     * Managing click listener of next and previous button and checking whether to go to next screen depending of validation perform of specific Fragment and it is determinded by sendDataMethod
     * If sendData() returns from fragment is true then it means fragment will handle the click event(means there is some time or validation fails so we remain user to current fragment only).
     * else we will move to next fragment
     */
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            BaseFragment fragment;
            if (customPagerAdapterWelcome != null) {
                fragment = (BaseFragment) customPagerAdapterWelcome.getItem(viewPager.getCurrentItem());
            } else {
                fragment = (BaseFragment) customPagerAdapterCarInfo.getItem(viewPager.getCurrentItem());
            }
            Bundle bnd = new Bundle();

            //If Next button is clicked
            if (v.getId() == btnNext.getId()) {
                bnd.putString(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_NEXT);

                //Checking if fragment will handle this or we simply move to next screen
                if (!fragment.sendData(bnd)) {
                    if (btnNext.getText().toString().trim().equalsIgnoreCase(getString(R.string.lbl_get_started))) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + (skipEmergencyContact ? 2 : 1), true);
                    } else if (btnNext.getText().toString().trim().equalsIgnoreCase(getString(R.string.lbl_finish))) {
                        //if next button has the label then finish this activity with success RESULT_OK so FragmentHome will call garage api and populate all the markers
                        User user = Utility.getLoggedInUser();
                        Utility.setLoggedInUser(user);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                    }
                }
            } else if (v.getId() == btnPrev.getId()) {
                bnd.putString(ConstantCode.INTENT_ACTION, ConstantCode.ACTION_PREV);

                //Checking if fragment is handling this previous button click event then no need to show next fragment else simply show next fragment
                if (!fragment.sendData(bnd)) {
                    if (btnPrev.getText().toString().trim().equalsIgnoreCase(getString(R.string.lbl_previous))) {
                        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                    }
                }
            }
        }
    };

    /**
     * =======================WELCOME SCREEN FLOW, WELCOME SCREEN -> EMERGENCY CONTACT -> CAR INFO -> PLUG IN ADAPTER -> BLUETOOTH -> SECURITY CODE
     */
    CustomPagerAdapterWelcome customPagerAdapterWelcome;

    /**
     * Setting pager adapter for Adding first car with Emergency contacts
     */
    private void setUpAdapterWelcome() {
        customPagerAdapterWelcome = new CustomPagerAdapterWelcome(getSupportFragmentManager());
        viewPager.setAdapter(customPagerAdapterWelcome);
        txtTitle.setText(getString(customPagerAdapterWelcome.getTitle(), Utility.getLoggedInUser().name));
        viewPager.addOnPageChangeListener(pageChangeListenerWelcome);
        btnNext.setText(getString(R.string.lbl_get_started));
    }

    /**
     * ViewPagerAdapter that adds welcomeScreenFragment, safetyFragment, carInfoFragment, plugInAdapterFragment, bluetoothFragment, securityCodeFragment fragments
     */
    public class CustomPagerAdapterWelcome extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        int[] titles;
        Fragment[] fragments;//welcomeScreenFragment, safetyFragment, carInfoFragment, plugInAdapterFragment, bluetoothFragment, securityCodeFragment;

        public CustomPagerAdapterWelcome(FragmentManager fm) {
            super(fm);
            titles = new int[]{R.string.lbl_welcome_x, R.string.lbl_safety_first, R.string.lbl_car_info, R.string.lbl_plugin_adapter, R.string.lbl_bluetooth_settings, R.string.lbl_security_code};
            fragments = new Fragment[titles.length];
            this.mNumOfTabs = titles.length;
        }

        public int getTitle() {
            return titles[viewPager.getCurrentItem()];
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddCarIntro();
                    return fragments[position];
                case 1:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddCarSafetyFirst();
                    return fragments[position];
                case 2:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddCarInfo();
                    return fragments[position];
                case 3:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddPlugInAdapter();
                    return fragments[position];
                case 4:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddBluetoothSettings();
                    return fragments[position];
                case 5:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddSecurityCode();
                    return fragments[position];

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    /**
     * Listener to manage the button labels
     */
    ViewPager.OnPageChangeListener pageChangeListenerWelcome = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            line_pager_indicator.onPageChange(position - 1);
            txtTitle.setText(getString(customPagerAdapterWelcome.getTitle(), Utility.getLoggedInUser().name));
            if (position == 0) {
//                    btnPrev.setText(getString(R.string.lbl_get_started));
//                    btnNext.setVisibility(View.GONE);

                btnNext.setText(getString(R.string.lbl_get_started));
                btnPrev.setVisibility(View.GONE);
            } else if (position == viewPager.getAdapter().getCount() - 1) {
//                    btnPrev.setText(getString(R.string.lbl_finish));
//                    btnNext.setVisibility(View.GONE);
                btnNext.setText(getString(R.string.lbl_finish));
                btnPrev.setVisibility(View.GONE);
            } else {
                btnPrev.setText(getString(R.string.lbl_previous));
                btnNext.setText(getString(R.string.lbl_next));
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * =======================ADD CARINFO FLOW, CAR INFO -> PLUG IN ADAPTER -> BLUETOOTH -> SECURITY CODE
     */
    CustomPagerAdapterCarInfo customPagerAdapterCarInfo;

    /**
     * Setting pager adapter for Adding first car with Emergency contacts
     */
    private void setUpAdapterCarInfo() {
        customPagerAdapterCarInfo = new CustomPagerAdapterCarInfo(getSupportFragmentManager());
        viewPager.setAdapter(customPagerAdapterCarInfo);
        txtTitle.setText(getString(customPagerAdapterCarInfo.getTitle(), Utility.getLoggedInUser().name));
        viewPager.addOnPageChangeListener(pageChangeListenerCarInfo);
        btnNext.setText(getString(R.string.lbl_next));
    }

    /**
     * ViewPagerAdapter that adds welcomeScreenFragment, carInfoFragment, plugInAdapterFragment, bluetoothFragment, securityCodeFragment fragments
     */
    public class CustomPagerAdapterCarInfo extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        int[] titles;
        Fragment[] fragments;//welcomeScreenFragment, carInfoFragment, plugInAdapterFragment, bluetoothFragment, securityCodeFragment;

        public CustomPagerAdapterCarInfo(FragmentManager fm) {
            super(fm);
            titles = new int[]{R.string.lbl_welcome_x, R.string.lbl_car_info, R.string.lbl_plugin_adapter, R.string.lbl_bluetooth_settings, R.string.lbl_security_code};
            fragments = new Fragment[titles.length];
            this.mNumOfTabs = titles.length;
        }

        public int getTitle() {
            return titles[viewPager.getCurrentItem()];
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddCarIntro();
                    return fragments[position];
                case 1:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddCarInfo();
                    return fragments[position];
                case 2:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddPlugInAdapter();
                    return fragments[position];
                case 3:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddBluetoothSettings();
                    return fragments[position];
                case 4:
                    if (fragments[position] == null)
                        fragments[position] = new FragmentAddSecurityCode();
                    return fragments[position];
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    /**
     * Listener to manage the button labels
     */
    ViewPager.OnPageChangeListener pageChangeListenerCarInfo = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            line_pager_indicator.onPageChange(position - 1);
            txtTitle.setText(getString(customPagerAdapterCarInfo.getTitle(), Utility.getLoggedInUser().name));
            if (position == 0) {
//                    btnPrev.setText(getString(R.string.lbl_get_started));
//                    btnNext.setVisibility(View.GONE);

                btnNext.setText(getString(R.string.lbl_next));
                btnPrev.setVisibility(View.GONE);
            } else if (position == viewPager.getAdapter().getCount() - 1) {
//                    btnPrev.setText(getString(R.string.lbl_finish));
//                    btnNext.setVisibility(View.GONE);
                btnNext.setText(getString(R.string.lbl_finish));
                btnPrev.setVisibility(View.GONE);
            } else {
                btnPrev.setText(getString(R.string.lbl_previous));
                btnNext.setText(getString(R.string.lbl_next));
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}

package com.carnot.global;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.carnot.App;
import com.carnot.BuildConfig;
import com.carnot.R;
import com.carnot.activity.ActivitySplash;
import com.carnot.libclasses.CircleTransform;
import com.carnot.models.GraphData;
import com.carnot.models.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Utility {

    public final static Class exludeAnimActivities[] = new Class[]{ActivitySplash.class};
    public final static long COUNTDOWN_TIME_FOR_ACCEIDENT = 3 * 60 * 1000;
    /*public static DisplayImageOptions OPTIONS_FOR_LOCATION = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.ic_launcher)
			.showImageOnFail(R.drawable.ic_launcher)
			.showImageOnLoading(R.drawable.ic_launcher)
			.resetViewBeforeLoading(true).cacheOnDisk(true)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300)).build();

	public static ImageLoader imageLoader;*/

    public final static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public final static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public final static int roundToInt(double doubleVar) {
        return (int) Math.round(doubleVar);
    }

    public final static String trim(EditText edt) {
        if (!isBlank(edt)) {
            return edt.getText().toString().trim();
        }
        return "";
    }

    public final static String trim(String str) {
        if (str != null) {
            return str.trim();
        }
        return "";
    }

    public static Bitmap getBitmap(int res, Activity activity) {
        View view = LayoutInflater.from(activity).inflate(res, null, false);
        return getBitmap(view, activity);
    }

    public static Bitmap getBitmap(View view, Activity activity) {

        view.setDrawingCacheEnabled(true);

        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        view.buildDrawingCache(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false); // clear drawing cache

        return bitmap;
    }

    public static final int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(id, null);
        } else {
            return context.getResources().getColor(id);
        }
    }

    public static String getMinsSeconds(long secondsCompleted) {
//        long hours = secondsCompleted / 3600;
        long minutes = (secondsCompleted) / 60;
//        long minutes = (secondsCompleted) / 60;
        long seconds = secondsCompleted % 60;

//            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static String getHrsMinsSec(double secondsCompleted, String format) {
        long hours = (long) (secondsCompleted / 3600);
        long minutes = (long) ((secondsCompleted % 3600) / 60);
        long seconds = (long) (secondsCompleted % 60);

        format = format.replaceAll("hh", String.format("%02d", hours));
        format = format.replaceAll("mm", String.format("%02d", minutes));
        format = format.replaceAll("ss", String.format("%02d", seconds));

        format = format.replaceAll("h", String.format("%01d", hours));
        format = format.replaceAll("m", String.format("%01d", minutes));
        format = format.replaceAll("s", String.format("%01d", seconds));

        return format;

//        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static File getPublicDir(Activity activity) {
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/Carnot/");
        dir.mkdirs();
        return dir;
    }

    public static Bitmap screenShot(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static File saveBitmap(Activity activity, Bitmap bitmap) {
        try {
            Calendar calendar = Calendar.getInstance();
            String now = calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND);
            String mPath = getPublicDir(activity).getPath() + now + ".jpg";
            File imageFile = new File(mPath);
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;

            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File takeScreenshot(Activity activity) {
        Calendar calendar = Calendar.getInstance();
        String now = calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.YEAR) + "_" + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND);
        String mPath = "";
        try {

            // image naming and path  to include sd card  appending name you choose for file
            mPath = getPublicDir(activity).getPath() + now + ".jpg";

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            Bitmap bitmap = screenShot(v1);

            /*v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);*/

            File imageFile = new File(mPath);
            if (!imageFile.exists()) {
                imageFile.createNewFile();
            }

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            return imageFile;
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            showLogE(Utility.class, mPath + "");
            e.printStackTrace();

        }
        return null;
    }

    public static void shareImage(Activity activity, File file) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        activity.startActivity(Intent.createChooser(share, "Share Image"));
    }


    /**
     * Checking for all possible internet providers
     * *
     */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }


    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    public static Drawable getTintedDrawable(Resources res, int drawableResId,
                                             int colorResId) {
        Drawable drawable = res.getDrawable(drawableResId);
        int color = res.getColor(colorResId);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        return drawable;
    }

    public final static boolean isBlank(EditText edt) {
        if (edt == null
                || (edt != null && TextUtils.isEmpty(edt.getText().toString()
                .trim()))) {
            return true;
        }
        return false;
    }


    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static int[] getDeviceWidthHeight(Activity activity) {

        int size[] = new int[2];

        if (isAndroidAPILevelGreaterThenEqual(android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)) {
            DisplayMetrics displayMetrics = activity.getResources()
                    .getDisplayMetrics();
            size[0] = displayMetrics.widthPixels;
            size[1] = displayMetrics.heightPixels;
        } else {
            Display mDisplay = activity.getWindowManager().getDefaultDisplay();
            size[0] = mDisplay.getWidth();
            size[1] = mDisplay.getHeight();
        }
        return size;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
//            return true;
        }
    }

    // android.os.Build.VERSION_CODES.FROYO
    public static boolean isAndroidAPILevelGreaterThenEqual(int apiLevel) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= apiLevel) {
            return true;
        } else {
            return false;
        }
    }


    public static boolean isInternetConnected(Context activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            View view = activity.getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void hideKeyboard(Activity activity, EditText edt) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                inputManager.hideSoftInputFromWindow(edt.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public static void showKeyboard(Activity activity, EditText edt) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);

            // check if no view has focus:
            if (edt != null) {
                edt.requestFocus();
                inputManager.showSoftInput(edt,
                        InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public final static String getIMEINumber(Activity activity) {
        return ((TelephonyManager) activity
                .getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
    }

    public final static void showLogE(Class cl, String msg) {
        if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE))
            Log.e(cl.getSimpleName(), msg);
    }

    public final static void showLog(Class cl, String msg) {
        if ("debug".equalsIgnoreCase(BuildConfig.BUILD_TYPE))
            Log.i(cl.getSimpleName(), msg);
    }

    public static final void showToast(Activity activity, String msg) {
        if (!TextUtils.isEmpty(msg))
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }


    /*public static Object parseFromString(String jsonData, Class modelClass) {
        return new Gson().fromJson(jsonData, modelClass);
    }

    public static String getJsonString(Object modelClass) {

        return new Gson().toJson(modelClass);
    }*/


    public static String roundOfDoublePoints(double value) {
        // it will give 20.05, 20.5, 20
//        DecimalFormat df = new DecimalFormat("###.##");
//        return df.format(value);
        // it will give 20.05,20.50,20.00
        return String.format("%.2f", value);
    }

    public static double round(double value, int places) {
        // TODO Auto-generated method stub

        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;

    }


    public static String getCurrentTimeZoneOffSet() {
        // TODO Auto-generated method stub
        TimeZone timezone = Calendar.getInstance().getTimeZone();
        int mins = (timezone.getRawOffset() / 1000) / 60;
        int hr = mins / 60;
        mins = Math.abs(mins) % 60;

        // way1
        String timeZoneStr1 = TimeUnit.HOURS.convert(timezone.getRawOffset(),
                TimeUnit.MILLISECONDS) + "";

        // way2
        String timeZoneStr = (hr >= 0 ? "+" : "") + hr + ":"
                + (mins < 10 ? "0" : "") + mins;
        // return Calendar.getInstance().getTimeZone().getDisplayName();
        return timeZoneStr;
    }

    public static String getCountryName(Context context) {
        Locale loc = new Locale("", getCountryCode(context));
        return loc.getDisplayCountry();
    }

    public static String getCountryCode(Context context) {
        return context.getResources().getConfiguration().locale.getCountry();

    }

    // public static void saveUserInPref(Activity activity, UserModel user) {
    //
    // ApplicationSession session = ApplicationSession.getSession(activity);
    //
    // Gson gson = new Gson();
    // String jsonString = gson.toJson(user);
    // session.putString(ConstantCode.PREF_USER, jsonString);
    //
    // }

    // public static UserModel currentUser;
    //
    // public static UserModel getUserFromPref(Context activity) {
    //
    // if (currentUser == null) {
    // ApplicationSession session = ApplicationSession
    // .getSession(activity);
    // Gson gson = new Gson();
    // currentUser = (UserModel) gson.fromJson(
    // session.getString(ConstantCode.PREF_USER), UserModel.class);
    // }
    // return currentUser;
    // }

    // ======DATE SETTINGS=====
    public static String getFormatedDate(Calendar calendar, String format,
                                         boolean isConvertToUTC) {

        if (isConvertToUTC)
            calendar.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (calendar != null) {
            return DateFormat.format(format, calendar).toString();
        }
        return "";
    }

    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public static String getCurrentDate() {
        SimpleDateFormat df = new SimpleDateFormat(ConstantCode.DATE_FORMAT_DD_MM_YY);
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public static String getCurrentDate(String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);
        String date = df.format(Calendar.getInstance().getTime());
        return date;
    }

    public static String getDateForDisplay(Calendar calendar) {

        if (calendar != null) {
            return DateFormat.format("dd MMMM yyyy", calendar).toString();
        }
        return "";
    }

    public static String getDateForDisplayInLabel1(Calendar calendar) {

        if (calendar != null) {
            return DateFormat.format("dd MMMM, HH:mm a", calendar).toString();
        }
        return "";
    }

    public static Date getDateFromString(String date) {
        Date parsed = null;
        SimpleDateFormat sdf = new SimpleDateFormat(ConstantCode.DATE_FORMAT_DD_MM_YY);
        try {
            parsed = sdf.parse(date);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return parsed;
    }


    public static Date getTimeFromString(String date) {
        Date parsed = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            parsed = sdf.parse(date);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return parsed;
    }

  /*  public static Calendar getDate(String date, String time) {
        return getDate(date + " " + time, ConstantCode.DATE_FORMAT_SERVER_DATE_TIME, false, true);
    }
*/

    public static Calendar getDate(String date, String formatOfThisDate, boolean isThisTimeUTC,
                                   boolean isReturnTimeZoneLocal) {

        try {
            if (!TextUtils.isEmpty(date)) {
                Calendar c;

                if (isThisTimeUTC) {
                    c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                } else {
                    c = Calendar.getInstance(TimeZone.getDefault());
                }

                SimpleDateFormat sdf;
                sdf = new SimpleDateFormat(formatOfThisDate);

                if (isThisTimeUTC) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                } else {
                    sdf.setTimeZone(TimeZone.getDefault());
                }

                Date dt = sdf.parse(date);

                if (isReturnTimeZoneLocal) {
                    c.setTimeZone(TimeZone.getDefault());
                    sdf.setTimeZone(TimeZone.getDefault());

                } else {
                    c.setTimeZone(TimeZone.getTimeZone("UTC"));
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                c.setTime(dt);
                return c;
            }
        } catch (Exception e) {

        }
        return null;
    }

    public static String[] months = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN",
            "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};

    public static String getMonth(int i) {
        // TODO Auto-generated method stub
        return months[i];
    }


    public final static int getRandomeNumber(int upper, int lower) {
        int r = (int) (Math.random() * (upper - lower)) + lower;
        return r;
    }

    public static void createFolders() {
        // TODO Auto-generated method stub
        File file = new File(ConstantCode.IMAGE_FOLDER);
        if (!file.exists())
            file.mkdirs();

        file = new File(ConstantCode.VIDEO_FOLDER);

        if (!file.exists())
            file.mkdirs();
    }

    public static File createNewImageFile() {

        return new File(ConstantCode.IMAGE_FOLDER + File.separator + "myFile_"
                + System.currentTimeMillis() + ".jpg");
    }

    public static File createNewVideoFile() {
        return new File(ConstantCode.VIDEO_FOLDER + File.separator + "myFile_"
                + System.currentTimeMillis() + ".mp4");
    }


    public static String getDateForWebService(Calendar calendar) {
        // TODO Auto-generated method stub
        if (calendar != null) {
            return DateFormat.format("yyyy-MM-dd", calendar).toString();
        }
        return "";
    }

    public static Drawable getAndroidDrawable(Context context,
                                              String pDrawableName) {
        int resourceId = context.getResources().getIdentifier(pDrawableName,
                "drawable", context.getPackageName());
        if (resourceId == 0) {
            return null;
        } else {
            return context.getResources().getDrawable(resourceId);
        }
    }

    public static boolean isEmpty(String text) {
        // TODO Auto-generated method stub
        if (text == null || (text != null && TextUtils.isEmpty(text.trim()))) {
            return true;
        }
        return false;
    }


    public static String getVersion(Context context) {
        // TODO Auto-generated method stub
        try {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getNetworkClass(Context context) {
        // TODO Auto-generated method stub
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return "-"; // not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return "WIFI";
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: // api<8 : replace by 11
                    return "2G";
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD: // api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP: // api<13 : replace by 15
                    return "3G";
                case TelephonyManager.NETWORK_TYPE_LTE: // api<11 : replace by 13
                    return "4G";
                default:
                    return "?";
            }
        }
        return "?";
    }

    public static void activityStartAnim(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_in_left);
    }

    public static void activityStopAnim(Activity activity) {
        activity.overridePendingTransition(R.anim.grow_out_left, R.anim.slide_out_right);
    }

    public static boolean isEmpty(EditText edt) {
        if (edt != null && edt.getText().toString().trim().length() > 0) return false;
        return true;
    }

    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .serializeNulls()
                    .create();
        }
        return gson;
    }

    public static <T> ArrayList<T> parseArrayFromString(String jsonData, Class modelClass) {
        if (gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .serializeNulls()
                    .create();
        }

        return new ArrayList<T>(Arrays.asList((T[]) gson.fromJson(jsonData, modelClass)));

    }

    public static Object parseFromString(String jsonData, Class modelClass) {
        if (gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .serializeNulls()
                    .create();
        }
        return gson.fromJson(jsonData, modelClass);
    }

    public static String getJsonString(Object modelClass) {
        if (gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                    .serializeNulls()
                    .create();
        }
        return gson.toJson(modelClass);
    }

    public static String getLast10Digit(String number) {
//        Matcher m = Pattern.compile("[\\d]{0,10}$").matcher(number);
//        while (m.find()) {
//            return m.group(0);
//        }
        return number;
//        return "";
    }

    public static void showCircularImageView(Context context, ImageView img, String url, int placeholder) {
        /*Glide
                .with(context)
                .load(url)
                .placeholder(placeholder)
                .centerCrop()
                .crossFade()
                .into(img);*/

        Glide
                .with(context)
                .load(url)
                .transform(new CircleTransform(context))
                .placeholder(R.drawable.ic_profile_circle_black)
//                    .centerCrop()
                .error(R.drawable.ic_profile_circle_black)
                .crossFade()
                .into(img);
    }


    /**
     * Get logged in user from preference
     *
     * @return
     */
    public static User user;

    public static User getLoggedInUser() {
        if (user == null)
            user = (User) parseFromString(PrefManager.getString(ConstantCode.PREF_LOGGED_IN_USER_DETAILS), User.class);
        return user;
    }

    /**
     * Setting Logged in user to preference
     *
     * @param user
     */
    public static void setLoggedInUser(User user) {
        Utility.user = user;

        //Saving user id in general pref and resetting userwise preference
        PrefManager.getGeneralPref(App.getContext()).edit().putString(ConstantCode.PREF_LOGGED_IN_USER_ID, user.id + "").commit();
        PrefManager.init(App.getContext());
        PrefManager.putString(ConstantCode.PREF_LOGGED_IN_USER_DETAILS, getJsonString(user));
        PrefManager.putString(ConstantCode.PREF_LOGGED_IN_USER_ID, user.id + "");
        PrefManager.putString(ConstantCode.PREF_LOGGED_IN_USER_NAME, "TestUser");
    }

    public static void logoutExistingUser() {
        PrefManager.getGeneralPref(App.getContext()).edit().clear().commit();
        PrefManager.init(App.getContext());
        user = null;
    }


    /**
     * Return HybridCar CarbonFootpring in grams
     *
     * @param distanceInKm
     * @return
     */
    public static int getCarbonFootPrintHybridCar(int distanceInKm) {
        int carbonFootPrint = 70 * distanceInKm;
        return carbonFootPrint / 1000;//convert to kg
//        return 2756;
    }

    /**
     * Return PublicTransport CarbonFootpring in grams
     *
     * @param distanceInKm
     * @return
     */
    public static int getCarbonFootPrintPublicTransportAvg(int distanceInKm) {
        int carbonFootPrint = 45 * distanceInKm;
        return carbonFootPrint / 1000;//convert to kg
//        return 1000;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static String getBase64FromImage(String filePath) {
        try {

            /*BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, bounds);
            bounds.inSampleSize = calculateInSampleSize(bounds, 100, 100);
            bounds.inJustDecodeBounds = false;*/
            Bitmap bm = BitmapFactory.decodeFile(filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();

            String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            return encodedImage;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isValidLicenseNo(String trim) {
        if (trim != null) {
            trim = trim.replaceAll(" ", "");
        }
        if (trim.matches("[a-zA-Z]{2,2}-[0-9]{1,2}-[a-zA-Z]{1,2}-[0-9]{1,4}")) {
            return true;
        }
        return false;
    }

    public static int getStatusBackground(String category) {
        if (ConstantCode.NOTIFICATION_CATEGORY_SECURITY.equalsIgnoreCase(category)) {
            return R.drawable.status_red_gradient;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_PASSPORT.equalsIgnoreCase(category)) {
            return R.drawable.status_yellow_gradient;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_DIAGNOSTICS.equalsIgnoreCase(category)) {
            return R.drawable.status_red_gradient;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_ONBOARDING.equalsIgnoreCase(category)) {
            return R.drawable.status_black_gradient;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_INSIGHT.equalsIgnoreCase(category)) {
            return R.drawable.status_black_gradient;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_CUSTOM.equalsIgnoreCase(category)) {
            return R.drawable.status_black_gradient;
        }
        return R.drawable.status_black_gradient;
    }

    public static int getStatusTextColor(String category) {
        if (ConstantCode.NOTIFICATION_CATEGORY_SECURITY.equalsIgnoreCase(category)) {
            return R.color.white;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_PASSPORT.equalsIgnoreCase(category)) {
            return R.color.white;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_DIAGNOSTICS.equalsIgnoreCase(category)) {
            return R.color.white;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_ONBOARDING.equalsIgnoreCase(category)) {
            return R.color.green_text_color;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_INSIGHT.equalsIgnoreCase(category)) {
            return R.color.green_text_color;
        } else if (ConstantCode.NOTIFICATION_CATEGORY_CUSTOM.equalsIgnoreCase(category)) {
            return R.color.white;
        }
        return R.color.white;
    }

    public static boolean isValidMobile(String trim) {
        if (trim.matches("[0-9]{10}"))
            return true;
        return false;
    }

    /*public static Object parseArrayFromString(String jsonData, final Class<Object> modelClass) {
        Type collectionTypeDayType = new TypeToken<ArrayList<modelClass>>() {}.getType();
        ArrayList<modelClass> dayTypeList = new Gson().fromJson(jsonData, collectionTypeDayType);
        return dayTypeList;
    }*/

    public static void insertIntoGraphDataTable(int carID)
    {

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2016,0,1);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(2017,11,31);

        Calendar iterator = Calendar.getInstance();
        iterator.setTimeInMillis(startCalendar.getTimeInMillis());
        Log.e("Utility", "---------STARTED-----------");
        while(iterator.getTimeInMillis() <= endCalendar.getTimeInMillis())
        {
            GraphData gd = new GraphData();
            gd.car_id = carID;
            gd.dayOfTheYear = iterator.get(Calendar.DAY_OF_YEAR);
            gd.dayOfTheMonth = iterator.get(Calendar.DAY_OF_MONTH);
            gd.day = iterator.get(Calendar.DAY_OF_MONTH);
            gd.month = iterator.get(Calendar.MONTH);
            gd.year = iterator.get(Calendar.YEAR);
            gd.save();
            iterator.add(Calendar.DAY_OF_YEAR,1);
        }
        Log.e("Utility", "--------FINISHED---------------");
    }
}

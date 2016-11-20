package com.carnot.libclasses;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility to request and check System permissions for apps targeting Android M (API >= 23).
 * <p/>
 * Changed by Pankaj Sharma (27-5-2016)
 * Changes now you can add Permission group, and if user accept all permission then you will get Granted Notice.
 */
public class EasyPermissions {

    private static final String TAG = "EasyPermissions";

    public interface PermissionCallbacks {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

        void onPermissionsPermanentlyDeclined(int requestCode, List<String> perms);

    }

    /**
     * Check if the calling context has a set of permissions.
     *
     * @param context the calling context.
     * @param perms   one ore more permissions, such as {@code android.Manifest.permission.CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission
     * is not yet granted.
     */
    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions 
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }

        return true;
    }

    private static PermissionCallbacks callbacks;

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param object      Activity or Fragment requesting permissions. Should implement
     *                    {@link ActivityCompat.OnRequestPermissionsResultCallback}
     *                    or
     *                    {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
     * @param rationale   a message explaining why the application needs this set of permissions, will
     *                    be displayed if the user rejects the request the first time.
     * @param requestCode request code to track this request, must be < 256.
     * @param perms       a set of permissions to be requested.
     */
    public static void requestPermissions(final Object object, PermissionCallbacks callback, String rationale,
                                          final int requestCode, final String... perms) {
        requestPermissions(object, callback, rationale,
                android.R.string.ok,
                android.R.string.cancel,
                requestCode, perms);
    }

    static long timeWhenRequestingStart;
    static Object object;

    /**
     * Request a set of permissions, showing rationale if the system requests it.
     *
     * @param obj            Activity or Fragment requesting permissions. Should implement
     *                       {@link ActivityCompat.OnRequestPermissionsResultCallback}
     *                       or
     *                       {@link android.support.v13.app.FragmentCompat.OnRequestPermissionsResultCallback}
     * @param rationale      a message explaining why the application needs this set of permissions, will
     *                       be displayed if the user rejects the request the first time.
     * @param positiveButton custom text for positive button
     * @param negativeButton custom text for negative button
     * @param requestCode    request code to track this request, must be < 256.
     * @param permission     a set of permissions or permission.group to be requested.
     */
    public static void requestPermissions(final Object obj, final PermissionCallbacks callback, String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          final int requestCode, final String... permission) {

        callbacks = callback;
        object = obj;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // only for lower of M
//            PermissionCallbacks callbacks = (PermissionCallbacks) object;
            callbacks.onPermissionsGranted(requestCode, new ArrayList<String>(Arrays.asList(permission)));
            return;
        }


        checkCallingObjectSuitability(object);
//        final PermissionCallbacks callbacks = (PermissionCallbacks) object;
        final String[] perms = getActualPermissions(object,
                permission);

       /* boolean hasPermission = true;
        for (String perm : perms) {
            if (!EasyPermissions.hasPermissions(getActivity(object), perm)) {
                hasPermission = false;
                break;
            }
        }*/

        if (perms.length <= 0) {
            callbacks.onPermissionsGranted(requestCode, new ArrayList<String>(Arrays.asList(permission)));
            return;
        }

        boolean shouldShowRationale = false;
        for (String perm : perms) {
            shouldShowRationale = shouldShowRationale || shouldShowRequestPermissionRationale(object, perm);
        }

        if (shouldShowRationale) {
            if (!TextUtils.isEmpty(rationale)) {
                AlertDialog dialog = new AlertDialog.Builder(getActivity(object))
                        .setMessage(rationale)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                executePermissionsRequest(object, perms, requestCode);
                            }
                        })
                        .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // act as if the permissions were denied
                                callbacks.onPermissionsDenied(requestCode, Arrays.asList(perms));
                            }
                        }).create();
                dialog.show();
            } else {
                executePermissionsRequest(object, perms, requestCode);
            }
        } else {
            timeWhenRequestingStart = System.currentTimeMillis();
            executePermissionsRequest(object, perms, requestCode);
        }
    }

    private static String[] getActualPermissions(Object object, String[] permission) {
        initPermissionGroups();
        ArrayList<String> permissionList = new ArrayList<String>();
        for (String indiPerm : permission) {
            if (permissionGroups.containsKey(indiPerm)) {
                String[] arr = permissionGroups.get(indiPerm);
                for (String s : arr) {
                    if (!EasyPermissions.hasPermissions(getActivity(object), s)) {
                        permissionList.add(s);
                    }
                }
            } else {
                if (!EasyPermissions.hasPermissions(getActivity(object), indiPerm)) {
                    permissionList.add(indiPerm);
                }
            }
        }

        Set<String> set = new LinkedHashSet<String>(permissionList);

        return set.toArray(new String[set.size()]);
    }

    private static void initPermissionGroups() {
        if (permissionGroups == null) {
            permissionGroups = new HashMap<String, String[]>();
            permissionGroups.put(Manifest.permission_group.CALENDAR, new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR});
            permissionGroups.put(Manifest.permission_group.CAMERA, new String[]{Manifest.permission.CAMERA});
            permissionGroups.put(Manifest.permission_group.CONTACTS, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS, Manifest.permission.GET_ACCOUNTS});
            permissionGroups.put(Manifest.permission_group.LOCATION, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            permissionGroups.put(Manifest.permission_group.MICROPHONE, new String[]{Manifest.permission.RECORD_AUDIO});
            permissionGroups.put(Manifest.permission_group.PHONE, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CALL_LOG, Manifest.permission.WRITE_CALL_LOG, Manifest.permission.ADD_VOICEMAIL, Manifest.permission.USE_SIP, Manifest.permission.PROCESS_OUTGOING_CALLS});
            permissionGroups.put(Manifest.permission_group.SENSORS, new String[]{Manifest.permission.BODY_SENSORS});
            permissionGroups.put(Manifest.permission_group.SMS, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_WAP_PUSH, Manifest.permission.RECEIVE_MMS});
            permissionGroups.put(Manifest.permission_group.STORAGE, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    private static HashMap<String, String[]> permissionGroups;

    /**
     * Handle the result of a permission request, should be called from the calling Activity's
     * {@link ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}
     * method.
     * <p/>
     * If any permissions were granted or denied, the Activity will receive the appropriate
     * callbacks through {@link PermissionCallbacks} and methods annotated with
     *
     * @param requestCode  requestCode argument to permission result callback.
     * @param permissions  permissions argument to permission result callback.
     * @param grantResults grantResults argument to permission result callback.
     * @throws IllegalArgumentException if the calling Activity does not implement
     *                                  {@link PermissionCallbacks}.
     */
    public static void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                  int[] grantResults) {

//        checkCallingObjectSuitability(object);
//        PermissionCallbacks callbacks = (PermissionCallbacks) object;

        // Make a collection of granted and denied permissions from the request.
        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                boolean showRationale = shouldShowRequestPermissionRationale(object, perm);
                if (!showRationale) {
                    timeWhenRequestingStart = System.currentTimeMillis() - 2;
                }
                denied.add(perm);
            }
        }
        boolean isPermenantlyDisabled = false;
        // Report granted permissions, if any.
        if (!granted.isEmpty() && denied.isEmpty()) {
            // Notify callbacks
            callbacks.onPermissionsGranted(requestCode, granted);
        }

        // Report denied permissions, if any.
        /*if (!denied.isEmpty()) {
            callbacks.onPermissionsDenied(requestCode, denied);
        }*/

        //if 100% fail then check for whether timing
        else if (granted.isEmpty() && !denied.isEmpty()) {
            long diff = System.currentTimeMillis() - timeWhenRequestingStart;
            if (diff < 150) {
                //means it is permenantly disabled
                isPermenantlyDisabled = true;
                if (callbacks != null) {
                    callbacks.onPermissionsPermanentlyDeclined(requestCode, denied);
                }
            }
            Log.i("TAG", diff + "");
        }

        // Report denied permissions, if any.
        if (!denied.isEmpty() && !isPermenantlyDisabled) {
            callbacks.onPermissionsDenied(requestCode, denied);
        }

        /*// If 100% successful, call annotated methods
        if (!granted.isEmpty() && denied.isEmpty()) {
            runAnnotatedMethods(object, requestCode);
        }*/
    }

    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    @TargetApi(23)
    private static void executePermissionsRequest(Object object, String[] perms, int requestCode) {
        checkCallingObjectSuitability(object);

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }

    @TargetApi(11)
    private static Activity getActivity(Object object) {
        if (object instanceof Activity) {
            return ((Activity) object);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).getActivity();
        } else {
            return null;
        }
    }

    /*private static void runAnnotatedMethods(Object object, int requestCode) {
        Class clazz = object.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AfterPermissionGranted.class)) {
                // Check for annotated methods with matching request code.
                AfterPermissionGranted ann = method.getAnnotation(AfterPermissionGranted.class);
                if (ann.value() == requestCode) {
                    // Method must be void so that we can invoke it
                    if (method.getParameterTypes().length > 0) {
                        throw new RuntimeException(
                                "Cannot execute non-void method " + method.getName());
                    }

                    try {
                        // Make method accessible if private
                        if (!method.isAccessible()) {
                            method.setAccessible(true);
                        }
                        method.invoke(object);
                    } catch (IllegalAccessException e) {
                        Log.e(TAG, "runDefaultMethod:IllegalAccessException", e);
                    } catch (InvocationTargetException e) {
                        Log.e(TAG, "runDefaultMethod:InvocationTargetException", e);
                    }
                }
            }
        }
    }*/

    private static void checkCallingObjectSuitability(Object object) {
        // Make sure Object is an Activity or Fragment
        boolean isActivity = object instanceof Activity;
        boolean isSupportFragment = object instanceof Fragment;
        boolean isAppFragment = object instanceof android.app.Fragment;
        boolean isMinSdkM = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;

        if (!(isSupportFragment || isActivity || (isAppFragment && isMinSdkM))) {
            if (isAppFragment) {
                throw new IllegalArgumentException(
                        "Target SDK needs to be greater than 23 if caller is android.app.Fragment");
            } else {
                throw new IllegalArgumentException("Caller must be an Activity or a Fragment.");
            }
        }

        /*// Make sure Object implements callbacks
        if (!(object instanceof PermissionCallbacks)) {
            throw new IllegalArgumentException("Caller must implement PermissionCallbacks.");
        }*/
    }
}

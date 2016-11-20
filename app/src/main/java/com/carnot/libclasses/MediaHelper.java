package com.carnot.libclasses;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Media Helper : Helps developer to take picture from gallery, camera, video from gallery and load them in image view If it is video thumbnail is shown on it.
 *
 * @author Pankaj Sharma
 */
public class MediaHelper {

    public int current_code_to_send = 100;
    public static final String IMAGE_FILE_EXT = ".jpg";
    public static final String VIDEO_FILE_EXT = ".mp4";
    public static final String IMAGE_STORAGE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "MediaHelper" + File.separator + "IMAGE";
    public static final String VIDEO_STORAGE_DIR = Environment.getExternalStorageDirectory().getPath() + File.separator + "MediaHelper" + File.separator + "VIDEO";

    private HashMap<Integer, MediaCallback> mediaHolderList;

    public enum Media {
        IMAGE, VIDEO
    }

    ;

    private Media mMediaType;
    private Activity mActivity;
    private Fragment mFragment;

    public MediaHelper(Activity activity) {
        // TODO Auto-generated constructor stub
        mActivity = activity;
        mediaHolderList = new HashMap<Integer, MediaCallback>();
        File file = new File(IMAGE_STORAGE_DIR);
        if (!file.exists())
            file.mkdirs();

        file = new File(VIDEO_STORAGE_DIR);
        if (!file.exists())
            file.mkdirs();
    }

    public MediaHelper(Fragment fragment) {
        // TODO Auto-generated constructor stub
        mFragment = fragment;
        mediaHolderList = new HashMap<Integer, MediaCallback>();
        File file = new File(IMAGE_STORAGE_DIR);
        if (!file.exists())
            file.mkdirs();

        file = new File(VIDEO_STORAGE_DIR);
        if (!file.exists())
            file.mkdirs();
    }

    private File getNewFile(Media type) {
        try {
            File file = new File(type == Media.IMAGE ? IMAGE_STORAGE_DIR + File.separator + "img" + System.currentTimeMillis() + IMAGE_FILE_EXT : VIDEO_STORAGE_DIR + File.separator + "img" + System.currentTimeMillis() + VIDEO_FILE_EXT);
            if (!file.exists()) {
                file.createNewFile();
                // file.deleteOnExit();
            }
            return file;
        } catch (Exception e) {
            return null;
        }
    }

    public void takePictureFromCamera(File file, Object object, MediaCallback callback) {
        mMediaType = Media.IMAGE;
        File currentFile = null;
        if (file != null) {
            currentFile = file;
        } else {
            currentFile = getNewFile(mMediaType);
        }

        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        Intent imageSelectIntent = new Intent();
        imageSelectIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentFile));
        imageSelectIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

        current_code_to_send = current_code_to_send + 1;

        callback.mediaType = mMediaType;
        callback.object = object;
        callback.file = currentFile;
        mediaHolderList.put(current_code_to_send, callback);

        if (mActivity != null)
            mActivity.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Picture"), current_code_to_send);
        else if (mFragment != null)
            mFragment.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Picture"), current_code_to_send);
    }

    public void takePictureFromGallery(Object object, MediaCallback callback) {
        mMediaType = Media.IMAGE;
        File currentFile = null;

        Intent imageSelectIntent = new Intent();
        imageSelectIntent.setAction(Intent.ACTION_PICK);
        imageSelectIntent.setType("image/*");

        current_code_to_send = current_code_to_send + 1;

        callback.mediaType = mMediaType;
        callback.object = object;
        callback.file = currentFile;
        mediaHolderList.put(current_code_to_send, callback);
        if (mActivity != null)
            mActivity.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Picture"), current_code_to_send);
        else if (mFragment != null)
            mFragment.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Picture"), current_code_to_send);

    }

    public void takeVideoFromGallery(Object object, MediaCallback callback) {
        mMediaType = Media.VIDEO;
        File currentFile = null;

        Intent imageSelectIntent = new Intent();
        imageSelectIntent.setType("video/mp4");
        imageSelectIntent.setAction(Intent.ACTION_GET_CONTENT);
        current_code_to_send = current_code_to_send + 1;

        callback.mediaType = mMediaType;
        callback.object = object;
        callback.file = currentFile;
        mediaHolderList.put(current_code_to_send, callback);

        if (mActivity != null)
            mActivity.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Video"), current_code_to_send);
        else if (mFragment != null)
            mFragment.startActivityForResult(Intent.createChooser(imageSelectIntent, "Select Video"), current_code_to_send);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            if (mediaHolderList.containsKey(requestCode)) {
                MediaCallback mediaHolder = mediaHolderList.get(requestCode);
                if (mediaHolder != null) {
                    if (mediaHolder.file == null) {// means file not have so get from intent
                        try {
                            mediaHolder.file = getNewFile(mediaHolder.mediaType);

                            InputStream inputStream = null;

                            if (mActivity != null) {
                                inputStream = mActivity.getContentResolver().openInputStream(data.getData());
                            } else if (mFragment != null) {
                                inputStream = mFragment.getActivity().getContentResolver().openInputStream(data.getData());
                            }

                            if (inputStream != null) {
                                FileOutputStream fileOutputStream = new FileOutputStream(mediaHolder.file);
                                copyStream(inputStream, fileOutputStream);
                                fileOutputStream.close();
                                inputStream.close();

                                mediaHolder.onResult(true, mediaHolder.file, mediaHolder.mediaType, mediaHolder.object);
                            }
                        } catch (Exception e) {

                            // mediaHolder.onResult(false, null, mediaHolder.mediaType, mediaHolder.object);
                            mediaHolder.onMessageCallback(e.getMessage(), e);

                        }

                        mediaHolderList.remove(requestCode);
                    } else {
                        mediaHolder.onResult(true, mediaHolder.file, mediaHolder.mediaType, mediaHolder.object);
                        mediaHolderList.remove(requestCode);
                    }
                }
            }
        }
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }
}

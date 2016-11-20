package com.carnot.libclasses;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.widget.ImageView;


import java.io.File;


public abstract class MediaCallback {
    private static final int IMAGE_MAX_SIZE = 700;

    public abstract void onResult(boolean status, File file, MediaHelper.Media mediaType, Object object);

    public Object object;
    public File file;
    public MediaHelper.Media mediaType;
    public Bitmap bitmap, thumbnailBitmap;

    /**
     * Called to show original image
     *
     * @param imageView
     */
    public void showInImageView(ImageView imageView) {

        if (file.exists() && mediaType == MediaHelper.Media.IMAGE) {

            if (bitmap == null)
                bitmap = createBitmapFromFile(file);


            imageView.setImageBitmap(bitmap);

        } else if (file.exists() && mediaType == MediaHelper.Media.VIDEO) {
            showThumbnailInImageView(imageView);
        }
    }

 /*   public Bitmap createBitmapFromFile(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inSampleSize = 2;

            int scale = 1;

            if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
                                / (double) Math
                                .max(options.outHeight, options.outWidth))
                                / Math.log(0.5)));
            }

            options.inSampleSize = scale;

            Bitmap bm = BitmapFactory.decodeFile(file.getPath(), options);

            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif
                    .getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix m = new Matrix();
            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bm;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bm;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
                return bm;
            }


            return bm;

        } catch (Exception e) {
            return null;
        }
    }*/

    public Bitmap createBitmapFromFile(File file) {

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bmOptions); //it will fill bmOptions with image height & width.
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions, IMAGE_MAX_SIZE,
                IMAGE_MAX_SIZE);

        // Decode bitmap with inSampleSize set
        bmOptions.inJustDecodeBounds = false;
        Bitmap profilePic = BitmapFactory.decodeFile(file.getPath(), bmOptions);

        try {
            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

            Matrix m = new Matrix();
            if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
                m.postRotate(180);

            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                m.postRotate(90);

            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                m.postRotate(270);
            }

            profilePic = Bitmap.createBitmap(profilePic, 0, 0, profilePic.getWidth(),
                    profilePic.getHeight(), m, true);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return profilePic;
    }



    public static int calculateInSampleSize(BitmapFactory.Options ourOption,
                                            int imageWidth, int imageHeight) {
        final int height = ourOption.outHeight;
        final int width = ourOption.outWidth;
        int inSampleSize = 1;
        if (height > imageHeight || width > imageWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) imageHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) imageWidth);
            }
        }
        return inSampleSize;
    }

    /**
     * Called to show thumbnail image
     *
     * @param imageView
     */
    public void showThumbnailInImageView(ImageView imageView) {
        if (file.exists() && mediaType == MediaHelper.Media.IMAGE) {

            imageView.setImageBitmap(getThumbnailBitmap());

        } else if (file.exists() && mediaType == MediaHelper.Media.VIDEO) {

            imageView.setImageBitmap(getThumbnailBitmap());

        }
    }

    public Bitmap getThumbnailBitmap() {
        if (thumbnailBitmap == null) {
            if (file.exists() && mediaType == MediaHelper.Media.IMAGE) {

                if (bitmap == null)
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                int[] size = getThumbnailWidthHeight();
                thumbnailBitmap = ThumbnailUtils.extractThumbnail(bitmap, size[0], size[1]);

            } else if (file.exists() && mediaType == MediaHelper.Media.VIDEO) {

                thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MICRO_KIND);
                if (thumbnailBitmap == null) {
                    thumbnailBitmap = createThumbnailAtTime(file.getPath(), 150);
                }

                if (thumbnailBitmap == null) {
                    onMessageCallback("Cannot generate thumbnail", null);
                }
            }
        }
        return thumbnailBitmap;
    }

    private Bitmap createThumbnailAtTime(String filePath, int timeInSeconds) {
        try {
            MediaMetadataRetriever mMMR = new MediaMetadataRetriever();
            mMMR.setDataSource(filePath);
            // api time unit is microseconds
            return mMMR.getFrameAtTime(timeInSeconds * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        } catch (Exception e) {
            e.printStackTrace();
            onMessageCallback(e.getMessage(), e);
        }
        return null;
    }

    public int[] getThumbnailWidthHeight() {
        return new int[]{512, 384};
    }

    public void onMessageCallback(String message, Exception exception) {

    }
}
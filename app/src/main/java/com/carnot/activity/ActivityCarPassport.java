package com.carnot.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.carnot.R;
import com.carnot.adapter.DocumentsAdapter;
import com.carnot.adapter.ServiceHistoryAdapter;
import com.carnot.global.ConstantCode;
import com.carnot.global.UILoadingHelper;
import com.carnot.global.Utility;
import com.carnot.libclasses.BaseActivity;
import com.carnot.libclasses.VerticalSpaceItemDecoration;
import com.carnot.models.Document;
import com.carnot.network.NetworkCallbacks;
import com.carnot.network.WebServiceConfig;
import com.carnot.network.WebUtils;


import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pankaj on 31/3/16.
 * Activity Car Documents to show added documents(Registration , puc, insurance, dl) and Service Detials
 */
public class ActivityCarPassport extends BaseActivity {

    FloatingActionButton fab;
    BottomSheetBehavior mBottomSheetBehavior;
    NestedScrollView nestedScrollView;
    private GridLayoutManager lLayout;
    private RecyclerView recyclerViewDocuments;
    private List<Document> listDocuments = new ArrayList<>(4);
    private RecyclerView recyclerViewServiceHistory;
    private List<Document> listServicesHistory = new ArrayList<>(4);
    private String carId;
    private UILoadingHelper uiLoadingHelper;
    private DocumentsAdapter adapter;
    ServiceHistoryAdapter serviceHistoryAdapter;
    ImageView fullScreenImage;
    FrameLayout containerFullScreen;
    ImageView imgTranslucent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_passport);
    }

    @Override
    public void initVariable() {

        setToolbar(R.id.toolbar, true);
        setTitle(R.string.lbl_yellow_submarine);
        getSupportActionBar().setTitle(R.string.lbl_yellow_submarine);
//        getSupportActionBar().setSubtitle("UP15K0060");

        carId = getIntent().getStringExtra(ConstantCode.INTENT_CAR_ID);
        String carName = getIntent().getStringExtra(ConstantCode.INTENT_CAR_NAME);

        getSupportActionBar().setTitle(carName);

        uiLoadingHelper = new UILoadingHelper();
        uiLoadingHelper.set(links(R.id.txt_empty), links(R.id.progress_bar), links(R.id.data_container));

        Document blog = new Document();
        blog.info = getString(R.string.lbl_registration_certificate);
        blog.sDesc = "UP15K0060";
        blog.isAlert = false;

        listDocuments.add(blog);
        blog = new Document();
        blog.info = "Insurance Validity";
        blog.sDesc = "Septermber 2017";
        blog.isAlert = false;

        listDocuments.add(blog);
        blog = new Document();
        blog.info = "PUC Validity";
        blog.sDesc = "Septermber 2017";
        blog.isAlert = true;

        listDocuments.add(blog);

        blog = new Document();
        blog.info = "Driver's  License";
        blog.sDesc = "Jonathan Donut";
        blog.isAlert = false;

        listDocuments.add(blog);

        //dummy data for service History

        blog = new Document();
        blog.info = "Chevrolet Service Center";
        blog.sDesc = "18 January,2015";
        blog.isAlert = false;

        listServicesHistory.add(blog);
        blog = new Document();
        blog.info = "Hond Service Center";
        blog.sDesc = "27 October,2015";
        blog.isAlert = false;

        listServicesHistory.add(blog);
        blog = new Document();
        blog.info = "BMW Validity";
        blog.sDesc = "9 January 2016";
        blog.isAlert = true;

        listServicesHistory.add(blog);


    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (containerFullScreen.getVisibility() == View.VISIBLE) {
            containerFullScreen.callOnClick();
        } else {
            super.onBackPressed();
        }
    }

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            Document document = adapter.getItem(position);
//            fullScreenImage.setImageURI(Uri.parse(document.img));

            fullScreenImage.setVisibility(View.VISIBLE);
            Glide.with(mActivity)
                    .load(document.img)
                    .error(R.drawable.img_no_image)
                    .crossFade()
                    .into(fullScreenImage);

            ((TextView) links(R.id.tv_document_name1)).setText(document.info);
            ((TextView) links(R.id.tv_document_title1)).setText(!TextUtils.isEmpty(document.no) ? document.no : document.date);

            zoomImageFromThumb(view.findViewById(R.id.iv_document_pic), "");


//            links(R.id.layout_full_screen).setVisibility(View.VISIBLE);
            /*links(R.id.img_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    links(R.id.layout_full_screen).setVisibility(View.GONE);

                }
            });*/
        }
    };

    AnimatorSet mCurrentAnimator;

    private void zoomImageFromThumb(final View thumbView, String image) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        // Load the high-resolution "zoomed-in" image.

        // imgZoom.setImageResource(imageResId);
//        if (!image.startsWith("file:///"))
//            image = "file://" + image;
        // Utility.getImageLoader().displayImage(image, imgZoom, Utility.OPTIONS_FOR_ORDER_IMAGES);
//        final Bitmap bitmap = ((BitmapDrawable) ((ImageView) thumbView).getDrawable()).getBitmap();
//        if (bitmap != null)
//            fullScreenImage.setImageBitmap(bitmap);

        // imgZoom.setImageBitmap(ChatUtils.decodeBase64(image));

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        // thumbView.setAlpha(0f);
        containerFullScreen.setVisibility(View.VISIBLE);
        imgTranslucent.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        containerFullScreen.setPivotX(0f);
        containerFullScreen.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(containerFullScreen, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(containerFullScreen, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(containerFullScreen, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(containerFullScreen, View.SCALE_Y, startScale, 1f))
                .with(ObjectAnimator.ofFloat(imgTranslucent, "alpha", 0.0f, 1.0f));
        set.setDuration(100);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        final float startScaleFinal = startScale;
        containerFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                // Animate the four positioning/sizing properties in parallel,
                // back to their original values.
                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator.ofFloat(containerFullScreen, View.X, startBounds.left))
                        .with(ObjectAnimator.ofFloat(containerFullScreen, View.Y, startBounds.top))
                        .with(ObjectAnimator.ofFloat(containerFullScreen, View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(containerFullScreen, View.SCALE_Y, startScaleFinal))
                        .with(ObjectAnimator.ofFloat(imgTranslucent, "alpha", 1.0f, 0.0f));

                set.setDuration(100);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        imgTranslucent.setVisibility(View.GONE);
//                        fullScreenImage.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                        containerFullScreen.setVisibility(View.GONE);
                        // bitmap.recycle();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        onAnimationEnd(animation);
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });

        // bitmap.recycle();
    }

    @Override
    public void initView() {
        containerFullScreen = (FrameLayout) links(R.id.layout_full_screen);

        fullScreenImage = ((ImageView) links(R.id.img_full_screen));
        imgTranslucent = (ImageView) links(R.id.imgTranslucent);

        nestedScrollView = (NestedScrollView) links(R.id.bottom_sheet);

        mBottomSheetBehavior = BottomSheetBehavior.from(nestedScrollView);

        fab = (FloatingActionButton) findViewById(R.id.fab);


        lLayout = new GridLayoutManager(ActivityCarPassport.this, 2);
        recyclerViewDocuments = (RecyclerView) links(R.id.recycler_view_document);
        recyclerViewDocuments.setHasFixedSize(false);
        recyclerViewDocuments.setLayoutManager(lLayout);

        recyclerViewServiceHistory = (RecyclerView) findViewById(R.id.recycler_view_service_history);
        recyclerViewServiceHistory.setHasFixedSize(true);
        VerticalSpaceItemDecoration decoration = new VerticalSpaceItemDecoration((int) Utility.convertDpToPixel(getResources().getDimension(R.dimen.scale_1dp), mActivity));
        recyclerViewServiceHistory.addItemDecoration(decoration);
        recyclerViewServiceHistory.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void postInitView() {

    }

    @Override
    public void addAdapter() {
        ((TextView) links(R.id.tv_action_registration_certificate)).setOnClickListener(clickListener);
        ((TextView) links(R.id.tv_action_puc)).setOnClickListener(clickListener);
        ((TextView) links(R.id.tv_action_insurance)).setOnClickListener(clickListener);
        ((TextView) links(R.id.tv_action_derive_licence)).setOnClickListener(clickListener);
        ((TextView) links(R.id.tv_action_service_details)).setOnClickListener(clickListener);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mBottomSheetBehavior.setPeekHeight(0);
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {

            }
        });
        adapter = new DocumentsAdapter();
        adapter.setOnItemClickListener(itemClickListener);
        recyclerViewDocuments.setAdapter(adapter);


    }


    @Override
    public void loadData() {

        loadFromDatabase();
        /*loadRegistrationDocs();
        loadPUC();
        loadInsurance();
        loadDL();*/
    }

    private void loadFromDatabase() {
        uiLoadingHelper.startProgress();
        ArrayList<Document> list = Document.readDocument(carId);
//        ArrayList<Document> list = null;
        if (list != null && list.size() > 0) {
            adapter.setItems(list);
            adapter.setOnItemClickListener(itemClickListener);
            uiLoadingHelper.showContent();
        } else {
            loadRegistrationDocs();
            loadPUC();
            loadInsurance();
            loadDL();
        }

        ArrayList<Document> listServiceDetail = Document.readServiceDetails(carId);
        if (listServiceDetail != null && listServiceDetail.size() > 0) {
            serviceHistoryAdapter = new ServiceHistoryAdapter(listServiceDetail);
            recyclerViewServiceHistory.setAdapter(serviceHistoryAdapter);
        } else {
            loadServiceDetail();
        }
//        loadServiceDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    boolean someDataLoaded = false;
    int totalThreads = 0;

    private void loadDL() {
        uiLoadingHelper.startProgress();
        totalThreads++;
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "dl"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                totalThreads--;
                someDataLoaded = true;
                JSONObject json = (JSONObject) values;
                Document document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                addMeta(document, json);
                document.dtype = "dl";
                document.car_id = carId;
                document.save();

//                try {
////                    JSONObject meta = document.meta;
//                    JSONObject meta = new JSONObject(document.meta);
//                    document.sDesc = meta.optString("date");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                adapter.addItem(document);
//                showToast("Document Get Successfully " + document.info);

                uiLoadingHelper.showContent();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }
        });
    }

    private void loadServiceDetail() {
//        uiLoadingHelper.startProgress();
//        totalThreads++;
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "ser"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
//                totalThreads--;
//                someDataLoaded = true;
                JSONObject json = (JSONObject) values;
                Document document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                addMeta(document, json);
                document.dtype = "ser";
                document.car_id = carId;
                document.save();
                ArrayList<Document> docList = new ArrayList<Document>();
                docList.add(document);

                serviceHistoryAdapter = new ServiceHistoryAdapter(docList);
                recyclerViewServiceHistory.setAdapter(serviceHistoryAdapter);

//                uiLoadingHelper.showContent();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
                totalThreads--;
                /*if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(values.toString());*/
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                totalThreads--;
                /*if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(((Exception) values).getMessage());*/
            }
        });
    }

    private void addMeta(Document document, JSONObject json) {
        JSONObject jsonData = json.optJSONObject(ConstantCode.data);
        JSONObject jsonMeta = jsonData.optJSONObject(ConstantCode.meta);
        if (document != null && jsonMeta != null) {
            document.date = jsonMeta.optString("date");
            document.no = jsonMeta.optString("no");
            document.regular_service = jsonMeta.optString("regular_service");
            document.battery_jumpstart = jsonMeta.optString("battery_jumpstart");
            document.battery_recharging = jsonMeta.optString("battery_recharging");
            document.polishing = jsonMeta.optString("polishing");
            document.wheel_balanching = jsonMeta.optString("wheel_balanching");
            document.wheel_aliging = jsonMeta.optString("wheel_aliging");
        }
    }

    private void loadInsurance() {
        totalThreads++;
        uiLoadingHelper.startProgress();
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "ins"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                totalThreads--;
                someDataLoaded = true;
                JSONObject json = (JSONObject) values;
                Document document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                addMeta(document, json);
                document.dtype = "ins";
                document.car_id = carId;
                document.save();
//                try {
////                    JSONObject meta = document.meta;
//                    JSONObject meta = new JSONObject(document.meta);
//                    document.sDesc = meta.optString("date");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }


                adapter.addItem(document);
//                showToast("Document Get Successfully " + document.info);

                uiLoadingHelper.showContent();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(((Exception) values).toString());
            }
        });
    }

    private void loadPUC() {
        totalThreads++;
        uiLoadingHelper.startProgress();
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "puc"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                totalThreads--;
                someDataLoaded = true;
                JSONObject json = (JSONObject) values;
                Document document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                addMeta(document, json);
                document.dtype = "puc";
                document.car_id = carId;
                document.save();
//                try {
////                    JSONObject meta = document.meta;
//                    JSONObject meta = new JSONObject(document.meta);
//                    document.sDesc = meta.optString("date");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

                adapter.addItem(document);
//                showToast("Document Get Successfully " + document.info);

                uiLoadingHelper.showContent();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }
        });
    }

    private void loadRegistrationDocs() {
//        uiLoadingHelper.showError(getString(R.string.lbl_empty_msg_car_passport));
        totalThreads++;
        uiLoadingHelper.startProgress();
        WebUtils.call(WebServiceConfig.WebService.GET_DOCUMENTS, new String[]{carId, "reg"}, null, new NetworkCallbacks() {
            @Override
            public void successWithString(Object values, WebServiceConfig.WebService webService) {
                super.successWithString(values, webService);
                totalThreads--;
                someDataLoaded = true;
                JSONObject json = (JSONObject) values;
                Document document = (Document) Utility.parseFromString(json.optString(ConstantCode.data), Document.class);
                addMeta(document, json);
                document.dtype = "reg";
                document.car_id = carId;
                document.save();

                adapter.addItem(document);
//                showToast("Document Get Successfully " + document.info);
                uiLoadingHelper.showContent();
            }

            @Override
            public void failedWithMessage(Object values, WebServiceConfig.WebService webService) {
                super.failedWithMessage(values, webService);
//                showToast("failed documents " + values.toString());
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(values.toString());
            }

            @Override
            public void failedForNetwork(Object values, WebServiceConfig.WebService webService) {
                super.failedForNetwork(values, webService);
                totalThreads--;
                if (!someDataLoaded && totalThreads == 0)
                    uiLoadingHelper.showError(((Exception) values).getMessage());
            }
        });
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            int type;
            if (v.getId() ==  R.id.tv_action_registration_certificate){
                type = ConstantCode.DOCUMENT_TYPE_REGISTRATION;
                openAddDocumentActivity(type);}
            else if (v.getId() ==  R.id.tv_action_puc){
                type = ConstantCode.DOCUMENT_TYPE_PUC;
                openAddDocumentActivity(type);}
            else if (v.getId() ==  R.id.tv_action_insurance){
                type = ConstantCode.DOCUMENT_TYPE_INSURANCE;
                openAddDocumentActivity(type);}
            else if (v.getId() ==  R.id.tv_action_derive_licence){
                type = ConstantCode.DOCUMENT_TYPE_DL;
                openAddDocumentActivity(type);}

            else if (v.getId() == R.id.tv_action_service_details){
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                intent = new Intent(ActivityCarPassport.this, ActivityAddServiceDetail.class);
                intent.putExtras(getIntent().getExtras());
                startActivityForResult(intent, ADD_SERVICE_DETAIL);}

        }
    };

    private int ADD_DOCUMENT = 100;
    private int ADD_SERVICE_DETAIL = 101;

    private void openAddDocumentActivity(int documentType) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        Intent intent = new Intent(ActivityCarPassport.this, ActivityAddRegistrationCertificate.class);
        Bundle bnd = getIntent().getExtras();
        bnd.putInt(ConstantCode.INTENT_DOCUMENT_TYPE, documentType);
        intent.putExtras(bnd);
        startActivityForResult(intent, ADD_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ADD_DOCUMENT) {
            if (adapter == null) {
                adapter = new DocumentsAdapter();
                adapter.setOnItemClickListener(itemClickListener);
                recyclerViewDocuments.setAdapter(adapter);
            } else {
                adapter.clear();
            }

            loadRegistrationDocs();
            loadPUC();
            loadInsurance();
            loadDL();

        } else if (resultCode == RESULT_OK && requestCode == ADD_SERVICE_DETAIL) {
            if (serviceHistoryAdapter == null) {
                serviceHistoryAdapter = new ServiceHistoryAdapter();
                recyclerViewServiceHistory.setAdapter(serviceHistoryAdapter);
            } else {
                serviceHistoryAdapter.clear();
            }
            loadServiceDetail();
        }
    }
}

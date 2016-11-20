package com.carnot.network;

/**
 * Created by sumeet on 14/12/15.
 */

public class PhotoMultipartRequest {

   /* private String FILE_PART_NAME = "image";

    private MultipartEntityBuilder mBuilder = MultipartEntityBuilder.create();
    private final Listener<T> mListener;
    //private final File mImageFile;
    protected Map<String, String> headers;
    private Map<String, String> stringData;
    private Context ctxt;
//    private final Gson gson = new Gson();
    private Class<T> clazz;
    private ProgressDialog progressDialog;
//    private CustomErrorListener customErrorListener;
    private ArrayList<File> mFilePart;

    public PhotoMultipartRequest(String url,
                                 ErrorListener errorListener,
                                 Listener<T> listener,
                                 File imageFile) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        //mImageFile = imageFile;

        buildMultipartEntity();
    }

    public PhotoMultipartRequest(Context ctxt, String url,
                                 ErrorListener errorListener,
                                 Listener<T> listener,
                                 Map<String, String> stringData, ArrayList<File> files,
                                 String file_param_name, Class clazz, boolean showDialog) {
        super(Method.POST, url, errorListener);

        this.ctxt = ctxt;
        mListener = listener;
        //mImageFile = imageFile;
        this.stringData = stringData;
        this.clazz = clazz;

        mFilePart = files;
        this.FILE_PART_NAME = file_param_name;
//        customErrorListener = (CustomErrorListener) errorListener;

        if (showDialog == true) {
            //showProgressBarDialog();
            showProgressDialog();
        }

        buildMultipartEntity();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        *//*Map<String, String> headers = super.getHeaders();

        if (headers == null
                || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }

        headers.put("Accept", "application/json");

        return headers;*//*
        return headers != null ? headers : VolleyGlobal.getInstance(ctxt).getHeaders();
    }

    private void buildMultipartEntity() {
        if (null != mFilePart) {
            for (File file : mFilePart) {
                mBuilder.addBinaryBody(FILE_PART_NAME, file, ContentType.create("image/jpeg"), file.getName());
            }
        }


        mBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        mBuilder.setLaxMode().setBoundary("xx").setCharset(Charset.forName("UTF-8"));

        if (null != stringData) {
            try {
                for (Map.Entry<String, String> entry : stringData.entrySet()) {
                    //entity.addPart(postEntityModel.getName(), new StringBody(postEntityModel.getValue(), ContentType.TEXT_PLAIN));
                    mBuilder.addTextBody(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                Logcat.showLog("PhotoMultipartRequest", e.getMessage());
            }
        }

    }

    @Override
    public String getBodyContentType() {
        String contentTypeHeader = mBuilder.build().getContentType().getValue();
        return contentTypeHeader;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mBuilder.build().writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream bos, building the multipart request.");
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            Logcat.showLog("PhotoMultipartRequest", json);
            T myPojo = gson.fromJson(json, clazz);
            return Response.success(myPojo, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
       *//* T result = null;
        return Response.success(result, HttpHeaderParser.parseCacheHeaders(response));*//*
    }

    @Override
    protected void deliverResponse(T response) {
        mListener.onResponse(response);
        dismissProgressDialog();
    }

    public void showProgressDialog() {
        progressDialog = ProgressDialog.show(ctxt, "Please wait", "Processing...", true, false);
        customErrorListener.setProgressBarDialog(progressDialog);
    }

    public void dismissProgressDialog() {
        if (null != progressDialog) {
            progressDialog.dismiss();
        }
    }*/
}

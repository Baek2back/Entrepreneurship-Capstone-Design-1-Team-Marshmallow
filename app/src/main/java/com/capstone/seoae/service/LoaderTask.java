package com.capstone.seoae.service;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.AsyncTask;

import com.capstone.seoae.model.Object3DData;


import java.io.InputStream;
import java.util.List;

public abstract class LoaderTask extends AsyncTask<Void, Integer, List<Object3DData>> {
    protected final AssetManager assetManager;
    private final Callback callback;
    private final ProgressDialog dialog;

    public LoaderTask(Activity parent, AssetManager assetManager, Callback callback) {
        this.assetManager = assetManager;
        this.dialog = new ProgressDialog(parent);
        this.callback = callback; }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.dialog.setMessage("Loading...");
        this.dialog.setCancelable(false);
        this.dialog.show();
    }


    @Override
    protected List<Object3DData> doInBackground(Void... params) {
        try {
            callback.onStart();
            List<Object3DData> data = build();
            build(data);
            callback.onLoadComplete(data);
            return  data;
        } catch (Exception ex) {
            callback.onLoadError(ex);
            return null;
        }
    }

    protected abstract List<Object3DData> build() throws Exception;

    protected abstract void build(List<Object3DData> data) throws Exception;

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        switch (values[0]) {
            case 0:
                this.dialog.setMessage("Analyzing model...");
                break;
            case 1:
                this.dialog.setMessage("Allocating memory...");
                break;
            case 2:
                this.dialog.setMessage("Loading data...");
                break;
            case 3:
                this.dialog.setMessage("Scaling object...");
                break;
            case 4:
                this.dialog.setMessage("Building 3D model...");
                break;
            case 5:
                break;
        }
    }

    @Override
    protected void onPostExecute(List<Object3DData> data) {
        super.onPostExecute(data);
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public interface Callback {

        void onStart();

        void onLoadError(Exception ex);

        void onLoadComplete(List<Object3DData> data);
    }
}

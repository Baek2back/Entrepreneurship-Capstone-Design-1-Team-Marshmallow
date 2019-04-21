package com.capstone.seoae.service.collada;

import android.app.Activity;
import android.content.res.AssetManager;
import android.net.Uri;

import com.capstone.seoae.model.Object3DData;
import com.capstone.seoae.service.LoaderTask;
import com.capstone.seoae.service.collada.entities.AnimatedModelData;
import com.capstone.seoae.service.collada.loader.ColladaLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ColladaLoaderTask extends LoaderTask {
    AnimatedModelData modelData;
    public ColladaLoaderTask(Activity parent, AssetManager assetManager, Callback callback) {
        super(parent, assetManager, callback);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<Object3DData> build() throws IOException {
        // Parse STL
        Object[] ret = ColladaLoader.buildAnimatedModel(assetManager.open("cowboy.dae"));
        List<Object3DData> datas = (List<Object3DData>) ret[1];
        modelData = (AnimatedModelData) ret[0];
        return datas;
    }

    @Override
    protected void build(List<Object3DData> datas) throws Exception {
        ColladaLoader.populateAnimatedModel(assetManager.open("cowboy.dae"), datas, modelData);
        if (datas.size() == 1) {
            datas.get(0).centerAndScale(5, new float[]{0, 0, 0});
        } else {
            Object3DData.centerAndScale(datas, 5, new float[]{0, 0, 0});
        }
    }
}

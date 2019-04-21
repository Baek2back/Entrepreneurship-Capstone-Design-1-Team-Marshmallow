package com.capstone.seoae.service.collada.entities;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class JointData {
    public final int index;
    public final String nameId;
    public final float[] bindLocalTransform;
    public final float[] inverseBindTransform;
    public String meshId;

    public final List<JointData> children = new ArrayList<>();

    public JointData(int index, String nameId, float[] bindLocalTransform, float[] inverseBindTransform) {
        this.index = index;
        this.nameId = nameId;
        this.bindLocalTransform = bindLocalTransform;
        this.inverseBindTransform = inverseBindTransform;
    }

    public void addChild(JointData child) {
        children.add(child);
    }

    public JointData setMeshId(String meshId) {
        this.meshId = meshId;
        return this;
    }
}
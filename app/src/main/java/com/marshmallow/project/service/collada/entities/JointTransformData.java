package com.marshmallow.project.service.collada.entities;

public class JointTransformData {
    public final String jointNameId;
    public final float[] jointLocalTransform;

    public JointTransformData(String jointNameId, float[] jointLocalTransform) {
        this.jointNameId = jointNameId;
        this.jointLocalTransform = jointLocalTransform;
    }
}

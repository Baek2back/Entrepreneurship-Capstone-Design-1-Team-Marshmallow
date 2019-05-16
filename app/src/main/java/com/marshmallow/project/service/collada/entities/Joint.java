package com.marshmallow.project.service.collada.entities;

import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Joint {
    private final int index;// ID
    private final String name;
    private final float[] bindLocalTransform;
    private final List<Joint> children = new ArrayList<>();

    private float[] inverseBindTransform;

    private final float[] animatedTransform = new float[16];

    /**
     * @param index                - the joint's index (ID).
     * @param name                 - the name of the joint. This is how the joint is named in the
     *                             collada file, and so is used to identify which joint a joint
     *                             transform in an animation keyframe refers to.
     * @param bindLocalTransform   local space transform
     * @param inverseBindTransform global space inverse transform
     */
    public Joint(int index, String name, float[] bindLocalTransform, float[] inverseBindTransform) {
        this.index = index;
        this.name = name;
        this.bindLocalTransform = bindLocalTransform;
        this.inverseBindTransform = inverseBindTransform;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public List<Joint> getChildren() {
        return children;
    }

    public float[] getBindLocalTransform() {
        return bindLocalTransform;
    }

    /**
     * Adds a child joint to this joint. Used during the creation of the joint
     * hierarchy. Joints can have multiple children, which is why they are
     * stored in a list (e.g. a "hand" joint may have multiple "finger" children
     * joints).
     *
     * @param child - the new child joint of this joint.
     */
    public void addChild(Joint child) {
        this.children.add(child);
    }

    /**
     * The animated transform is the transform that gets loaded up to the shader
     * and is used to deform the vertices of the "skin". It represents the
     * transformation from the joint's bind position (original position in
     * model-space) to the joint's desired animation pose (also in model-space).
     * This matrix is calculated by taking the desired model-space transform of
     * the joint and multiplying it by the inverse of the starting model-space
     * transform of the joint.
     *
     * @return The transformation matrix of the joint which is used to deform
     * associated vertices of the skin in the shaders.
     */
    public float[] getAnimatedTransform() {
        return animatedTransform;
    }

    /**
     * This returns the inverted model-space bind transform. The bind transform
     * is the original model-space transform of the joint (when no animation is
     * applied). This returns the inverse of that, which is used to calculate
     * the animated transform matrix which gets used to transform vertices in
     * the shader.
     *
     * @return The inverse of the joint's bind transform (in model-space).
     */
    public float[] getInverseBindTransform() {
        return inverseBindTransform;
    }

    /**
     * This is called during set-up, after the joints hierarchy has been
     * created. This calculates the model-space bind transform of this joint
     * like so: </br>
     * </br>
     * {@code bindTransform = parentBindTransform * bindLocalTransform}</br>
     * </br>
     * where "bindTransform" is the model-space bind transform of this joint,
     * "parentBindTransform" is the model-space bind transform of the parent
     * joint, and "bindLocalTransform" is the bone-space bind transform of this
     * joint. It then calculates and stores the inverse of this model-space bind
     * transform, for use when calculating the final animation transform each
     * frame. It then recursively calls the method for all of the children
     * joints, so that they too calculate and store their inverse bind-pose
     * transform.
     *
     * @param parentBindTransform - the model-space bind transform of the parent joint.
     */
    public void calcInverseBindTransform(float[] parentBindTransform, boolean override) {

        float[] bindTransform = new float[16];
        Matrix.multiplyMM(bindTransform, 0, parentBindTransform, 0, bindLocalTransform, 0);
        if (index >= 0 && (override || this.inverseBindTransform == null)) {
            // when model has inverse bind transforms available, don't overwrite it
            // this way we calculate only the joints with no animations which has no inverse bind transform available
            this.inverseBindTransform = new float[16];
            if (!Matrix.invertM(inverseBindTransform, 0, bindTransform, 0)) {
                Log.w("Joint", "Couldn't calculate inverse matrix for " + name);
            }
        }
        for (Joint child : children) {
            child.calcInverseBindTransform(bindTransform, override);
        }
    }

    @Override
    public Joint clone() {
        final Joint ret = new Joint(this.index, this.name, this.bindLocalTransform.clone(), this.inverseBindTransform !=
                null? this.inverseBindTransform.clone() : null);
        for (final Joint child : this.children){
            ret.addChild(child.clone());
        }
        return ret;
    }
}

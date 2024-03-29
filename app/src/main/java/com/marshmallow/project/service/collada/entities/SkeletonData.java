package com.marshmallow.project.service.collada.entities;

public class SkeletonData {
    public final int jointCount;
    public final JointData headJoint;
    public final int boneCount;

    public SkeletonData(int jointCount, int boneCount, JointData headJoint) {
        this.jointCount = jointCount;
        this.boneCount = boneCount;
        this.headJoint = headJoint;
    }
}

package com.marshmallow.project.service.collada.entities;

import java.util.List;

public class AnimatedModelData {
    private final SkeletonData joints;
    private final List<MeshData> mesh;

    public AnimatedModelData(List<MeshData> mesh, SkeletonData joints){
        this.joints = joints;
        this.mesh = mesh;
    }

    public SkeletonData getJointsData(){
        return joints;
    }

    public List<MeshData> getMeshData(){
        return mesh;
    }
}

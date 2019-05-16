package com.marshmallow.project.service.collada.entities;

public class AnimationData {
    public final float lengthSeconds;
    public final KeyFrameData[] keyFrames;

    public AnimationData(float lengthSeconds, KeyFrameData[] keyFrames) {
        this.lengthSeconds = lengthSeconds;
        this.keyFrames = keyFrames;
    }
}

package com.marshmallow.project.service.collada.entities;

public class Vector2f {
    public float x, y;

    public Vector2f(float x, float y) {
        this.x = x;
        this.y = y;

    }

    /* (non-Javadoc)
     * @see org.lwjgl.util.vector.WritableVector3f#set(float, float, float)
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
}

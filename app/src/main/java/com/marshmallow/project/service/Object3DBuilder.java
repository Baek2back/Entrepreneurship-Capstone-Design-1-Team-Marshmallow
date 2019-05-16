package com.marshmallow.project.service;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.marshmallow.project.model.AnimatedModel;
import com.marshmallow.project.model.Object3DData;
import com.marshmallow.project.service.collada.entities.Joint;
import com.marshmallow.project.service.wavefront.WavefrontLoader;
import com.marshmallow.project.util.Math3DUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Object3DBuilder {
    private static final int COORDS_PER_VERTEX = 3;
    /**
     * Default vertices colors
     */
    private static float[] DEFAULT_COLOR = {1.0f, 1.0f, 0, 1.0f};

    final static float[] axisVertexLinesData = new float[]{
            //@formatter:off
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, // right
            0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, // left
            0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, // up
            0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, // down
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, // z+
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, // z-

            0.95f, 0.05f, 0, 1, 0, 0, 0.95f, -0.05f, 0, 1, 0f, 0f, // Arrow X (>)
            -0.95f, 0.05f, 0, -1, 0, 0, -0.95f, -0.05f, 0, -1, 0f, 0f, // Arrow X (<)
            -0.05f, 0.95f, 0, 0, 1, 0, 0.05f, 0.95f, 0, 0, 1f, 0f, // Arrox Y (^)
            -0.05f, 0, 0.95f, 0, 0, 1, 0.05f, 0, 0.95f, 0, 0, 1, // Arrox z (v)

            1.05F, 0.05F, 0, 1.10F, -0.05F, 0, 1.05F, -0.05F, 0, 1.10F, 0.05F, 0, // Letter X
            -0.05F, 1.05F, 0, 0.05F, 1.10F, 0, -0.05F, 1.10F, 0, 0.0F, 1.075F, 0, // Letter Y
            -0.05F, 0.05F, 1.05F, 0.05F, 0.05F, 1.05F, 0.05F, 0.05F, 1.05F, -0.05F, -0.05F, 1.05F, -0.05F, -0.05F,
            1.05F, 0.05F, -0.05F, 1.05F // letter z
            //@formatter:on
    };

    final static float[] squarePositionData = new float[]{
            // @formatter:off
            -0.5f, 0.5f, 0.5f, // top left front
            -0.5f, -0.5f, 0.5f, // bottom left front
            0.5f, -0.5f, 0.5f, // bottom right front
            0.5f, 0.5f, 0.5f, // upper right front
            -0.5f, 0.5f, -0.5f, // top left back
            -0.5f, -0.5f, -0.5f, // bottom left back
            0.5f, -0.5f, -0.5f, // bottom right back
            0.5f, 0.5f, -0.5f // upper right back
            // @formatter:on
    };

    final static int[] squareDrawOrderData = new int[]{
            // @formatter:off
            // front
            0, 1, 2,
            0, 2, 3,
            // back
            7, 6, 5,
            4, 7, 5,
            // up
            4, 0, 3,
            7, 4, 3,
            // bottom
            1, 5, 6,
            2, 1, 6,
            // left
            4, 5, 1,
            0, 4, 1,
            // right
            3, 2, 6,
            7, 3, 6
            // @formatter:on
    };

    final static float[] cubePositionData = {
            //@formatter:off
            // Front face
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,

            // Right face
            1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            1.0f, -1.0f, -1.0f,
            1.0f, 1.0f, -1.0f,

            // Back face
            1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,
            1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, -1.0f,

            // Left face
            -1.0f, 1.0f, -1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, 1.0f, 1.0f,

            // Top face
            -1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,
            -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, -1.0f,

            // Bottom face
            1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f,
            1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, 1.0f,
            -1.0f, -1.0f, -1.0f
    };

    final static float[] cubeColorData = {

            // Front face (red)
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,

            // Right face (green)
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f,

            // Back face (blue)
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,

            // Left face (yellow)
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f,

            // Top face (cyan)
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f, 1.0f,

            // Bottom face (magenta)
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f
    };

    final static float[] cubeNormalData =
            {
                    // Front face
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,

                    // Right face
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,
                    1.0f, 0.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,
                    0.0f, 0.0f, -1.0f,

                    // Left face
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,
                    -1.0f, 0.0f, 0.0f,

                    // Top face
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,
                    0.0f, 1.0f, 0.0f,

                    // Bottom face
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f,
                    0.0f, -1.0f, 0.0f
            };


    final static float[] cubeTextureCoordinateData =
            {
                    // Front face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Right face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Back face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Left face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Top face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f,

                    // Bottom face
                    0.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 0.0f,
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    1.0f, 0.0f
            };
    //@formatter:on

    public static Object3DData buildPoint(float[] point) {
        return new Object3DData(createNativeByteBuffer(point.length * 4).asFloatBuffer().put(point))
                .setDrawMode(GLES20.GL_POINTS).setId("Point");
    }

    public static Object3DData buildLine(float[] line) {
        return new Object3DData(createNativeByteBuffer(line.length * 4).asFloatBuffer().put(line))
                .setDrawMode(GLES20.GL_LINES).setId("Line").setFaces(new WavefrontLoader.Faces(0));
    }

    public static Object3DData buildAxis() {
        return new Object3DData(
                createNativeByteBuffer(axisVertexLinesData.length * 4).asFloatBuffer().put(axisVertexLinesData))
                .setDrawMode(GLES20.GL_LINES).setFaces(new WavefrontLoader.Faces(0));
    }

    public static Object3DData buildCubeV1() {
        return new Object3DData(
                createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData))
                .setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV1").centerAndScale(1.0f).setFaces(new WavefrontLoader.Faces(8));
    }

    public static Object3DData buildCubeV1_with_normals() {
        return new Object3DData(
                createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData))
                .setVertexColorsArrayBuffer(
                        createNativeByteBuffer(cubeColorData.length * 4).asFloatBuffer().put(cubeColorData))
                .setVertexNormalsArrayBuffer(
                        createNativeByteBuffer(cubeNormalData.length * 4).asFloatBuffer().put(cubeNormalData))
                .setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV1_light").centerAndScale(1.0f).setFaces(new WavefrontLoader.Faces(8));
    }

    public static Object3DData buildSquareV2() {
        IntBuffer drawBuffer = createNativeByteBuffer(squareDrawOrderData.length * 4).asIntBuffer().put(squareDrawOrderData);
        FloatBuffer vertexBuffer = createNativeByteBuffer(squarePositionData.length * 4).asFloatBuffer().put(squarePositionData);
        return new Object3DData(vertexBuffer,drawBuffer.asReadOnlyBuffer()).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV2")
                .centerAndScale(1.0f).setFaces(new WavefrontLoader.Faces(8)).setDrawOrder(drawBuffer).setVertexArrayBuffer(vertexBuffer);
    }

    public static Object3DData buildCubeV3(byte[] textureData) {
        return new Object3DData(
                createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData),
                createNativeByteBuffer(cubeTextureCoordinateData.length * 4).asFloatBuffer()
                        .put(cubeTextureCoordinateData).asReadOnlyBuffer(),
                textureData).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV3").centerAndScale(1.0f).setFaces(new WavefrontLoader.Faces(8));
    }

    public static Object3DData buildCubeV4(byte[] textureData) {
        return new Object3DData(
                createNativeByteBuffer(cubePositionData.length * 4).asFloatBuffer().put(cubePositionData),
                createNativeByteBuffer(cubeColorData.length * 4).asFloatBuffer().put(cubeColorData).asReadOnlyBuffer(),
                createNativeByteBuffer(cubeTextureCoordinateData.length * 4).asFloatBuffer()
                        .put(cubeTextureCoordinateData).asReadOnlyBuffer(),
                textureData).setDrawMode(GLES20.GL_TRIANGLES).setId("cubeV4").centerAndScale(1.0f).setFaces(new WavefrontLoader.Faces(8));
    }

    /**
     * Generate a new object that contains all the line normals for all the faces for the specified object
     * <p>
     * TODO: This only works for objects made of triangles. Make it useful for any kind of polygonal face
     *
     * @param obj the object to which we calculate the normals.
     * @return the model with all the normal lines
     */
    public static Object3DData buildFaceNormals(Object3DData obj) {
        if (obj.getDrawMode() != GLES20.GL_TRIANGLES) {
            return null;
        }

        FloatBuffer vertexBuffer = obj.getVertexArrayBuffer() != null ? obj.getVertexArrayBuffer()
                : obj.getVertexBuffer();
        if (vertexBuffer == null) {
            Log.v("Builder", "Generating face normals for '" + obj.getId() + "' I found that there is no vertex data");
            return null;
        }

        FloatBuffer normalsLines;
        IntBuffer drawBuffer = obj.getDrawOrder();
        if (drawBuffer != null) {
            Log.v("Builder", "Generating face normals for '" + obj.getId() + "' using indices...");
            int size = /* 2 points */ 2 * 3 * /* 3 points per face */ (drawBuffer.capacity() / 3)
                    * /* bytes per float */4;
            normalsLines = createNativeByteBuffer(size).asFloatBuffer();
            drawBuffer.position(0);
            for (int i = 0; i < drawBuffer.capacity(); i += 3) {
                int v1 = drawBuffer.get() * COORDS_PER_VERTEX;
                int v2 = drawBuffer.get() * COORDS_PER_VERTEX;
                int v3 = drawBuffer.get() * COORDS_PER_VERTEX;
                float[][] normalLine = Math3DUtils.calculateFaceNormal(
                        new float[]{vertexBuffer.get(v1), vertexBuffer.get(v1 + 1), vertexBuffer.get(v1 + 2)},
                        new float[]{vertexBuffer.get(v2), vertexBuffer.get(v2 + 1), vertexBuffer.get(v2 + 2)},
                        new float[]{vertexBuffer.get(v3), vertexBuffer.get(v3 + 1), vertexBuffer.get(v3 + 2)});
                normalsLines.put(normalLine[0]).put(normalLine[1]);
            }
        } else {
            if (vertexBuffer.capacity() % (/* COORDS_PER_VERTEX */3 * /* VERTEX_PER_FACE */ 3) != 0) {
                // something in the data is wrong
                Log.v("Builder", "Generating face normals for '" + obj.getId()
                        + "' I found that vertices are not multiple of 9 (3*3): " + vertexBuffer.capacity());
                return null;
            }

            Log.v("Builder", "Generating face normals for '" + obj.getId() + "'...");
            normalsLines = createNativeByteBuffer(6 * vertexBuffer.capacity() / 9 * 4).asFloatBuffer();
            vertexBuffer.position(0);
            for (int i = 0; i < vertexBuffer.capacity() / /* COORDS_PER_VERTEX */ 3 / /* VERTEX_PER_FACE */3; i++) {
                float[][] normalLine = Math3DUtils.calculateFaceNormal(
                        new float[]{vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()},
                        new float[]{vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()},
                        new float[]{vertexBuffer.get(), vertexBuffer.get(), vertexBuffer.get()});
                normalsLines.put(normalLine[0]).put(normalLine[1]);

                // debug
                @SuppressWarnings("unused")
                String normal = new StringBuilder().append(normalLine[0][0]).append(",").append(normalLine[0][1])
                        .append(",").append(normalLine[0][2]).append("-").append(normalLine[1][0]).append(",")
                        .append(normalLine[1][1]).append(",").append(normalLine[1][2]).toString();
                // Log.v("Builder", "fNormal[" + i + "]:(" + normal + ")");
            }
        }

        return new Object3DData(normalsLines).setDrawMode(GLES20.GL_LINES).setColor(obj.getColor())
                .setPosition(obj.getPosition()).setVersion(1);
    }

    public static AnimatedModel buildSkeleton(AnimatedModel animatedModel){
        float[] identity = new float[16];
        Matrix.setIdentityM(identity,0);

        AnimatedModel skeleton = new AnimatedModel(createNativeByteBuffer(animatedModel.getJointCount()*3*3*4)
                .asFloatBuffer());
        skeleton.setVertexNormalsArrayBuffer(createNativeByteBuffer(animatedModel.getJointCount()*3*3*4)
                .asFloatBuffer());
        skeleton.setDrawMode(GLES20.GL_TRIANGLES);
        skeleton.setRootJoint(animatedModel.getRootJoint().clone(), animatedModel.getJointCount(), animatedModel
                .getBoneCount(), true);
        skeleton.setJointIds(createNativeByteBuffer(skeleton.getJointCount()*3*3*4).asFloatBuffer());
        skeleton.doAnimation(animatedModel.getAnimation());
        skeleton.setVertexWeights(createNativeByteBuffer(skeleton.getJointCount()*3*3*4).asFloatBuffer());
        skeleton.setPosition(animatedModel.getPosition());
        skeleton.setScale(animatedModel.getScale());

        Log.i("Object3DBuilder","Building "+skeleton.getJointCount()+" bones...");
        buildBones(skeleton, skeleton.getRootJoint(), identity, new float[]{0,0,0}, -1, animatedModel.getVertexBuffer());

        skeleton.setId(animatedModel.getId()+"-skeleton");

        return skeleton;
    }

    private static void buildBones(AnimatedModel animatedModel, Joint joint, float[] parentTransform, float[]
            parentPoint, int parentJoinIndex, FloatBuffer vertexBuffer){

        float[] point = new float[4];
        float[] transform = new float[16];
        Matrix.multiplyMM(transform,0,parentTransform,0,joint.getBindLocalTransform(),0);
        Matrix.multiplyMV(point,0,transform,0,new float[]{0,0,0,1},0);

        float[] v = Math3DUtils.substract(point,parentPoint);
        float[] point1 = new float[]{point[0],point[1],point[2]-Matrix.length(v[0],v[1],v[2])*0.05f};
        float[] point2 = new float[]{point[0],point[1],point[2]+Matrix.length(v[0],v[1],v[2])*0.05f};

        float[] normal = Math3DUtils.calculateFaceNormal2(parentPoint, point1, point2);

        // TODO: remove this
        /*parentPoint = new float[]{vertexBuffer.get((int)(100* Math.random())),vertexBuffer.get((int)(100* Math.random
                ())),vertexBuffer.get((int)(100* Math.random()))};*/

        animatedModel.getVertexArrayBuffer().put(parentPoint[0]);
        animatedModel.getVertexArrayBuffer().put(parentPoint[1]);
        animatedModel.getVertexArrayBuffer().put(parentPoint[2]);
        animatedModel.getVertexArrayBuffer().put(point1[0]);
        animatedModel.getVertexArrayBuffer().put(point1[1]);
        animatedModel.getVertexArrayBuffer().put(point1[2]);
        animatedModel.getVertexArrayBuffer().put(point2[0]);
        animatedModel.getVertexArrayBuffer().put(point2[1]);
        animatedModel.getVertexArrayBuffer().put(point2[2]);

        animatedModel.getVertexNormalsArrayBuffer().put(normal);
        animatedModel.getVertexNormalsArrayBuffer().put(normal);
        animatedModel.getVertexNormalsArrayBuffer().put(normal);

        animatedModel.getJointIds().put(parentJoinIndex);
        animatedModel.getJointIds().put(parentJoinIndex);
        animatedModel.getJointIds().put(parentJoinIndex);
        for (int i=3; i<9; i++) {
            animatedModel.getJointIds().put(joint.getIndex());
        }
        for (int i=0; i<9; i+=3) {
            animatedModel.getVertexWeights().put(parentJoinIndex >= 0?1:0);
            animatedModel.getVertexWeights().put(0);
            animatedModel.getVertexWeights().put(0);
        }

        for (Joint child : joint.getChildren()){
            buildBones(animatedModel,child,transform, point, joint.getIndex(), vertexBuffer);
        }
    }

    private static ByteBuffer createNativeByteBuffer(int length) {
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(length);
        // use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder());
        return bb;
    }
}

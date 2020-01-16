package net.caustix.computergraphics.android;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Николай on 17.09.2015.
 */

public class Vertex {

    public FloatBuffer vertexBuffer;
    public ShortBuffer indexBuffer;
    public int numVertices;
    public int numIndeces;

    public Vertex (float[] vertex, int coordsPerVertex)
    {
        this.setVertices(vertex, coordsPerVertex);
    }

    public Vertex (float[] vertex, short[] indices, int coordsPerVertex) {
        this.setVertices(vertex, coordsPerVertex);
        this.setIndices(indices);
    }

    private void setVertices(float vertex[], int coordsPerVertex)
    {
        //Инициализируем байт буффер (byte buffer) вершин для координат фигуры
        ByteBuffer bb = ByteBuffer.allocateDirect(vertex.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertex);
        vertexBuffer.position(0);
        numVertices = vertex.length / coordsPerVertex;
    }

    protected void setIndices(short[] indices) {
        ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
        numIndeces = indices.length;
    }
}

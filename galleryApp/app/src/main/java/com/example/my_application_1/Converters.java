package com.example.my_application_1;

import androidx.room.TypeConverter;
import java.nio.ByteBuffer;

public class Converters {
    @TypeConverter
    public static float[] fromByteArray(byte[] byteArray) {
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        float[] floats = new float[byteArray.length / 4];
        buffer.asFloatBuffer().get(floats);
        return floats;
    }

    @TypeConverter
    public static byte[] fromFloatArray(float[] floatArray) {
        ByteBuffer buffer = ByteBuffer.allocate(floatArray.length * 4);
        buffer.asFloatBuffer().put(floatArray);
        return buffer.array();
    }
}


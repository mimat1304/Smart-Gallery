package com.example.my_application_1;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FaceNetEmbeddings {
    private Interpreter tflite;

    public FaceNetEmbeddings(Context context, String modelPath) throws IOException{
        tflite= new Interpreter(loadModelFile(context.getAssets(),modelPath));
    }
    private MappedByteBuffer loadModelFile(AssetManager assetManager,String modelPath)throws IOException{
        AssetFileDescriptor fileDescriptor=assetManager.openFd("facenet.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }
    public float[][] getEmbeddings(ArrayList<Bitmap> faceBitmap){
        int imageSize=160;// facenet requires size of input images to be 160
        float [][]embeddings= new float[faceBitmap.size()][512]; //dimensions of embeddings is 512

        for(int i=0;i<faceBitmap.size();i++){
            float [][][][]input= preprocessBitmap(faceBitmap.get(i),imageSize);
            float [][]output=new float[1][512];
            tflite.run(input,output);
            embeddings[i]=output[0];
        }
        return embeddings;
    }
    private float[][][][] preprocessBitmap(Bitmap bitmap, int size) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, size, size, true);
        int[] Pixels = new int[size * size];
        resizedBitmap.getPixels(Pixels, 0, resizedBitmap.getWidth(), 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        float[][][][] input = new float[1][size][size][3];
        int pixel = 0;
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                final int val = Pixels[pixel++];
                input[0][i][j][0] = (Color.red(val) - 127.5f) / 128.0f;
                input[0][i][j][1] = (Color.green(val) - 127.5f) / 128.0f;
                input[0][i][j][2] = (Color.blue(val) - 127.5f) / 128.0f;
            }
        }
        return input;
    }
}
package com.example.my_application_1;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.io.IOException;
import java.util.List;

public class GalleryAdapter extends BaseAdapter {
    private Context context;
    private List<String> imagePaths;
    private LayoutInflater inflater;
    private OnImageClickListener onImageClickListener;
    private OnImageLongClickListener onImageLongClickListener;


    public GalleryAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.inflater = LayoutInflater.from(context);
        this.onImageClickListener = (OnImageClickListener) context;
        this.onImageLongClickListener = (OnImageLongClickListener) context;
    }

    public interface OnImageClickListener {
        void onImageClick(String imagePath,ImageView imv);
    }
    public interface OnImageLongClickListener{
        boolean onImageLongClick(String imagePath, ImageView imV);
    }


    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.imageView);
        String imagePath = imagePaths.get(position);
//        ExifInterface exif = null;
//        try {
//            exif = new ExifInterface(imagePath);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//        int rotationAngle = 0;
//        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
//            rotationAngle = 90;
//        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
//            rotationAngle = 180;
//        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
//            rotationAngle = 270;
//        }
        Glide.with(context)
                .load(imagePath)
                .into(imageView);
        imageView.setOnClickListener(v -> onImageClickListener.onImageClick(imagePath, imageView));
        imageView.setOnLongClickListener(v -> onImageLongClickListener.onImageLongClick(imagePath, imageView));
//        imageView.setRotation(rotationAngle);
        return convertView;
    }
}


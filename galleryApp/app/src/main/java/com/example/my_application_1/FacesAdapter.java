package com.example.my_application_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class FacesAdapter extends ArrayAdapter<Bitmap> {

    public FacesAdapter(@NonNull Context context, @NonNull ArrayList<Bitmap> faces) {
        super(context, 0, faces);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Bitmap face = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cropped_faces, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.Face);
        imageView.setImageBitmap(face);

        return convertView;
    }
}

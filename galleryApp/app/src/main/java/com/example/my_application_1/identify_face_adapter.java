package com.example.my_application_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class identify_face_adapter extends ArrayAdapter<identification_variables> {
    int resource;
    Context context;
    List<identification_variables>items;
    public identify_face_adapter(@NonNull Context context, int resource, List<identification_variables>items){
        super(context, resource, items);
        this.items=items;
        this.context=context;
        this.resource=resource;
    }

    @Override
    public View getView(int position, @Nullable View convertView,@NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.detected_face);
        Bitmap currentItem = items.get(position).getBitmap();
        imageView.setImageBitmap(currentItem);
        EditText editText = convertView.findViewById(R.id.name);
        editText.setHint(items.get(position).getName());
        return convertView;
    }
}

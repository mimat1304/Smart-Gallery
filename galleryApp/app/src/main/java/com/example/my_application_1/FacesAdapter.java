package com.example.my_application_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class FacesAdapter extends ArrayAdapter<embeddings_list_item> {

    private Context context;
    private int resource;
    private List<embeddings_list_item> items;

    public FacesAdapter(@NonNull Context context, int resource, @NonNull List<embeddings_list_item> items) {
        super(context, resource, items);
        this.context=context;
        this.resource=resource;
        this.items=items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.Face);
        TextView textView = convertView.findViewById(R.id.embeddings);

        embeddings_list_item currentItem = items.get(position);
        imageView.setImageBitmap(currentItem.getBitmap());
        textView.setText(currentItem.getEmbeddings());

        return convertView;
    }
}

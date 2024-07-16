package com.example.my_application_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DialogCustomAdapter extends BaseAdapter {
    private Context context;
    private List<Item> items;
    private LayoutInflater inflater;

    public DialogCustomAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.dialog_list_item, parent, false);
        }

        ImageView itemImage = convertView.findViewById(R.id.item_image);
        TextView itemName = convertView.findViewById(R.id.item_name);

        Item item = items.get(position);
        itemImage.setImageBitmap(item.getBitmap());
        itemName.setText(item.getName());

        return convertView;
    }
}

class Item {
    private Bitmap image;
    private String name;

    public Item(Bitmap image, String name) {
        this.image = image;
        this.name = name;
    }

    public Bitmap getBitmap() {
        return image;
    }

    public String getName() {
        return name;
    }
}


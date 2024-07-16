package com.example.my_application_1;

import android.content.Context; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.ArrayAdapter; import android.widget.TextView;

import androidx.annotation.NonNull; import androidx.annotation.Nullable;

import java.util.List;

public class SuggestionsAdapter extends ArrayAdapter {

    private Context context;
    private int resource;
    private List<User> items;

    public SuggestionsAdapter(@NonNull Context context, int resource, @NonNull List<User> items) {
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

        TextView textView = convertView.findViewById(R.id.name);

        User currentItem = items.get(position);
        String nameStr = currentItem.name;
        textView.setText(nameStr);
        return convertView;
    }
}

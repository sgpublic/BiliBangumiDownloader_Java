package com.sgpublic.bilidownload.UIHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sgpublic.bilidownload.R;

import java.util.List;

public class ExploreFolderAdapter extends ArrayAdapter<ExploreFolderItem> {
    private final int resource;

    public ExploreFolderAdapter(@NonNull Context context, int resource, @NonNull List<ExploreFolderItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ExploreFolderItem item = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        TextView item_explore_title = view.findViewById(R.id.item_explore_title);
        TextView item_explore_content = view.findViewById(R.id.item_explore_content);
        item_explore_title.setText(item.getFoldName());
        item_explore_content.setText(item.getFoldDescription());
        return view;
    }
}

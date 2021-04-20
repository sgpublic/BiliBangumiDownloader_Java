package com.sgpublic.bilidownload.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sgpublic.bilidownload.R;

import java.util.List;

public class LicenseListAdapter extends ArrayAdapter<LicenseListItem> {
    private final int resource;
    private final Context context;

    public LicenseListAdapter(@NonNull Context context, int resource, @NonNull List<LicenseListItem> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LicenseListItem item = getItem(position);
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        TextView item_license_title = view.findViewById(R.id.item_license_title);
        item_license_title.setText(item.getProjectTitle());

        TextView item_license_author = view.findViewById(R.id.item_license_author);
        item_license_author.setText(item.getProjectAuthor());

        TextView item_license_about = view.findViewById(R.id.item_license_about);
        item_license_about.setText(item.getProjectAbout());

        view.findViewById(R.id.item_license_base).setOnClickListener((v) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(item.getProjectUrl()));
            context.startActivity(intent);
        });

        return view;
    }
}

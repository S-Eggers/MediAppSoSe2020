package com.sebastian_eggers.MediApp.Helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sebastian_eggers.MediApp.R;

import java.time.LocalTime;
import java.util.ArrayList;

public class TimeAdapter extends BaseAdapter {
    private ArrayList<LocalTime> data;
    private static LayoutInflater inflater = null;

    public TimeAdapter(Context context, ArrayList<LocalTime> data) {
        this.data = data;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.row_time, null);

        TextView text = view.findViewById(R.id.row_text);
        text.setText(getItem(position).toString());
        text.setPadding(0,10,0,10);
        return view;
    }
}

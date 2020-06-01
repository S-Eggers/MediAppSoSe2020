package com.sebastian_eggers.MediApp.Helper;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sebastian_eggers.MediApp.BuildConfig;
import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;

public class DrugAdapter extends BaseAdapter {
    private ArrayList<Drug> data;
    private static LayoutInflater inflater = null;
    private Context context;

    public DrugAdapter(Context context, ArrayList<Drug> data) {
        this.data = data;
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.data.size();
    }

    @Override
    public Drug getItem(int position) {
        return this.data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return this.data.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.row_drug, null);

        TextView title = view.findViewById(R.id.row_text);
        title.setText(getItem(position).getName());

        buildDescriptionTextView(view, position);
        buildDoseTextView(view, position);
        buildDaysTextView(view, position);
        buildTimesTextView(view, position);

        return view;
    }

    private void buildDescriptionTextView(View view, int position) {
        TextView description = view.findViewById(R.id.row_description);
        String desc = getItem(position).getDescription();
        description.setText(desc);
        if(desc.length() == 0) {
            description.setHeight(0);
        }
    }

    private void buildDoseTextView(View view, int position) {
        TextView dose = view.findViewById(R.id.row_dose);
        StringBuilder doseBuilder = new StringBuilder();
        int dosePerIntake = getItem(position).getDosePerIntake();
        doseBuilder.append(dosePerIntake);
        doseBuilder.append(getItem(position).getDoseUnit());
        doseBuilder.append(" ");
        Resources r = context.getResources();
        String drugForm = getItem(position).getForm();
        int id = r.getIdentifier(drugForm, "string", BuildConfig.APPLICATION_ID);
        doseBuilder.append(r.getString(id));
        if(dosePerIntake == 1 && drugForm.equals("pill"))
            doseBuilder.replace(doseBuilder.length() - 1, doseBuilder.length(), "");
        dose.setText(doseBuilder);
    }

    private void buildDaysTextView(View view, int position) {
        TextView days = view.findViewById(R.id.row_days);
        StringBuilder s = new StringBuilder();
        ArrayList<DayOfWeek> dayOfWeekArrayList = getItem(position).getDays();
        if(dayOfWeekArrayList.size() < 7) {
            for (DayOfWeek day : getItem(position).getDays()) {
                s.append(day.toString().charAt(0));
                s.append(day.toString().substring(1).toLowerCase());
                s.append(", ");
            }
            s.replace(s.length() - 2, s.length(), "");
        }
        else {
            s.append(context.getResources().getString(R.string.every_day));
        }
        days.setText(s);
    }

    private void buildTimesTextView(View view, int position) {
        TextView times = view.findViewById(R.id.row_time);
        StringBuilder s = new StringBuilder();
        ArrayList<LocalTime> timeArrayList = getItem(position).getIntake();
        Collections.sort(timeArrayList);
        for(LocalTime time:timeArrayList) {
            s.append(time);
            s.append(", ");
        }
        if(s.length() > 2)
            s.replace(s.length() - 2, s.length(), "");
        times.setText(s);

    }
}

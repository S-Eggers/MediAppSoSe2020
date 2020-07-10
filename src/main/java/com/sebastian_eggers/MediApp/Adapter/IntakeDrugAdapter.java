package com.sebastian_eggers.MediApp.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sebastian_eggers.MediApp.Models.Drug;
import com.sebastian_eggers.MediApp.R;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;

public class IntakeDrugAdapter extends DrugAdapter {

    public IntakeDrugAdapter(Context context, ArrayList<Drug> data) {
        super(context, data);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = inflater.inflate(R.layout.row_drug_with_intake_reminder, null);

        TextView title = view.findViewById(R.id.row_text);
        title.setText(getItem(position).getName());

        buildDescriptionTextView(view, position);
        buildDoseTextView(view, position);
        buildDaysTextView(view, position);
        buildTimesTextView(view, position);
        buildIntakeTextView(view, position);

        return view;
    }

    /**
     * Fill intake notification with content
     *
     * @param view Row view
     * @param position Drug position in ArrayList
     */
    private void buildIntakeTextView(View view, int position) {
        Calendar now = Calendar.getInstance();
        DayOfWeek nowDoW = DayOfWeek.of(now.get(Calendar.DAY_OF_WEEK));

        Calendar lastIntake = Calendar.getInstance();
        lastIntake.setTimeInMillis(getItem(position).getLastIntake());
        DayOfWeek lastIntakeDoW = DayOfWeek.of(lastIntake.get(Calendar.DAY_OF_WEEK));

        long diff = now.getTimeInMillis() - lastIntake.getTimeInMillis();

        TextView textView = view.findViewById(R.id.row_intake_today);
        String text;
        if(nowDoW.compareTo(lastIntakeDoW) == 0 && diff < 86400001) {
            text = context.getResources().getString(R.string.intake_already_done);
            textView.setTextColor(context.getResources().getColor(R.color.color_primary_dark));
        }
        else {
            text = context.getResources().getString(R.string.intake_not_already_done);
        }
        textView.setText(text);
    }
}

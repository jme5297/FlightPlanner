package com.jme5297.flightplanner;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class FlightCardAdapter extends ArrayAdapter<FlightCard> {
    private final Context context;
    private final ArrayList<FlightCard> values;
    private Boolean globallyEditable = false;


    public FlightCardAdapter(Context context, ArrayList<FlightCard> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    public void setEditable(Boolean b) {
        globallyEditable = b;
    }

    public Boolean isEditable() {
        return globallyEditable;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.flight_card_wind_table, null);
        }

        // Add data
        TextView title  = view.findViewById(R.id.flight_card_title);
        title.setText(getItem(position).title);

        if(globallyEditable)
        {
            view.setBackgroundColor(context.getResources().getColor(R.color.fc_edit));
        }else{
            view.setBackgroundColor(Color.parseColor("#FAFAFA"));
        }

        TextView date       = view.findViewById(R.id.flight_card_date);
        DateFormat formatter = new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US);
        date.setText(formatter.format(getItem(position).date));

        switch(values.get(position).fc_type){
            case WINDCHART:
                FlightCard_WindChart fc_w = (FlightCard_WindChart)values.get(position);
                TextView wt_spd = view.findViewById(R.id.fc_wt_spd);
                TextView wt_dir = view.findViewById(R.id.fc_wt_dir);
                TextView wt_tas = view.findViewById(R.id.fc_wt_tas);
                TextView wt_var = view.findViewById(R.id.fc_wt_var);

                wt_spd.setText(String.format("%.0f", fc_w.windSpeed));
                wt_dir.setText(String.format("%.0f", fc_w.windDir_head));
                wt_tas.setText(String.format("%.0f", fc_w.trueAirspeed));
                wt_var.setText(String.format("%.0f", fc_w.magVar));
        }

        return view;
    }
}

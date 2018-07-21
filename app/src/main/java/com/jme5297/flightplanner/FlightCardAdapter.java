package com.jme5297.flightplanner;

import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

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

    // Set/Get for the deletable option
    public void setDeletable(Boolean b) {
        globallyEditable = b;
    }
    public Boolean isDeletable() {
        return globallyEditable;
    }

    /* Main Get view command */
    // TODO: Look more into how ArrayAdapters work
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Most of these are used commonly throughout so we hold on to them.
        View view = convertView;
        LayoutInflater inflater;
        DateFormat formatter = new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US);;

        // Populate the views with the flight card data that was saved
        switch(values.get(position).fc_type){

            case WINDCHART:

                // TODO: determine where this view == null check is feasable
                //if(view == null){
                inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.flight_card_wind_table, null);
                //}

                // Grab the current flight card data
                FlightCard_WindChart fc_wt = (FlightCard_WindChart)values.get(position);

                // Reference all view in the layout
                TextView wt_title = view.findViewById(R.id.fc_wt_title);
                TextView wt_date = view.findViewById(R.id.fc_wt_date);
                TextView wt_spd = view.findViewById(R.id.fc_wt_spd);
                TextView wt_dir = view.findViewById(R.id.fc_wt_dir);
                TextView wt_tas = view.findViewById(R.id.fc_wt_tas);
                TextView wt_var = view.findViewById(R.id.fc_wt_var);

                // Populate the data based on the flight card content
                wt_title.setText(fc_wt.title);
                wt_date.setText(formatter.format(fc_wt.date));
                wt_spd.setText(String.format(Locale.US, "%.0f", fc_wt.windSpeed));
                wt_dir.setText(String.format(Locale.US,"%.0f", fc_wt.windDir_head));
                wt_tas.setText(String.format(Locale.US,"%.0f", fc_wt.trueAirspeed));
                wt_var.setText(String.format(Locale.US,"%.0f", fc_wt.magVar));

                break;

            case METARTAF:

                //if(view == null){
                inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.flight_card_metartaf, null);
                //}

                FlightCard_METARTAF fc_mt = (FlightCard_METARTAF)getItem(position);

                // Reference all views in the layout
                TextView mt_title = view.findViewById(R.id.fc_mt_icao);
                TextView mt_date = view.findViewById(R.id.fc_mt_date);
                TextView mt_spd = view.findViewById(R.id.fc_mt_winds);
                TextView mt_clds = view.findViewById(R.id.fc_mt_clouds);
                TextView mt_alt = view.findViewById(R.id.fc_mt_alt);

                // Set cloud information
                if(fc_mt.clouds.size() == 0){
                    mt_clds.setText("None");
                }else{
                    // Only display the first layer of clouds
                    Pair<Double,String> cloudData = fc_mt.clouds.get(0);
                    String mt_clds_height = String.format(Locale.US, "%.0f", cloudData.first);
                    String mt_clds_type = cloudData.second;
                    String cloudsText = mt_clds_height + " " + mt_clds_type;
                    mt_clds.setText(cloudsText);
                }

                // Set the text for all objects
                mt_alt.setText(String.format(Locale.US,"%2.2f", fc_mt.altimeter));
                mt_title.setText(fc_mt.station);
                mt_date.setText(formatter.format(fc_mt.date));
                String spd = String.valueOf(fc_mt.windSpeed);
                String dir = String.valueOf(fc_mt.windDir);
                String windTxt = dir + " @ " + spd;
                mt_spd.setText(windTxt);

                break;
        }

        // If the object is in "delete" mode, change the background color to a red scary color
        if(globallyEditable)
        {
            view.setBackgroundColor(context.getResources().getColor(R.color.fc_edit));
        }else{
            view.setBackgroundColor(Color.parseColor("#FAFAFA"));
        }

        return view;
    }
}

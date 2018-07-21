package com.jme5297.flightplanner;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Date;

public class FlightCard_METARTAF extends FlightCard {

    public String station = "";
    public Double altimeter = 0.0;
    public ArrayList<Pair<Double,String>> clouds = new ArrayList<>();
    public Double windDir = 0.0;
    public Double windSpeed = 0.0;

    public FlightCard_METARTAF(){
        fc_type = FlightCardType.METARTAF;
        title = "METARTAF";
        title_type = "METARTAF";
        date = new Date();
    }

}

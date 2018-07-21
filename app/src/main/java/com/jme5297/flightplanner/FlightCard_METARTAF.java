package com.jme5297.flightplanner;

import java.util.Date;

public class FlightCard_METARTAF extends FlightCard {

    String station = "";

    public FlightCard_METARTAF(){
        fc_type = FlightCardType.WINDCHART;
        title = "METAR";
        date = new Date();
    }

}

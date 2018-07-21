package com.jme5297.flightplanner;

import java.time.LocalDateTime;
import java.util.Date;

enum FlightCardType
{
    WINDCHART,
    METAR,
    TAF
}

public class FlightCard {

    protected static Integer id_counter;
    protected Integer my_id;
    protected FlightCardType fc_type;
    public String title = "";
    public String title_type = "";
    public Date date;

    public FlightCard(){
        if(id_counter == null){
            id_counter = 0;
            my_id = 0;
        }
        else{
            id_counter++;
            my_id = id_counter;
        }
    }

    public Integer getID(){
        return my_id;
    }
    public FlightCardType getFlightCardType(){ return fc_type; }
}

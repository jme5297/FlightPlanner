package com.jme5297.flightplanner;

import java.time.LocalDateTime;
import java.util.Date;

public class FlightCard_WindChart extends FlightCard {

    public Double   windSpeed       = 0.0;
    public Double   windDir_head    = 0.0;
    public Double   trueAirspeed    = 0.0;
    public Double   magVar          = 0.0;
    public Integer  showEvery       = 0;

    public FlightCard_WindChart(){
        fc_type = FlightCardType.WINDCHART;
        title_type = "Wind Table";
        date = new Date();
    }
}

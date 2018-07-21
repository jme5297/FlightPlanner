package com.jme5297.flightplanner;

import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DataController {

    // List of currently active flight cards
    static ArrayList<FlightCard> activeFlightCards = new ArrayList<>();

    // Helper functions for flight cards
    public static void RemoveFlightCardAtPosition(int i)
    {
        activeFlightCards.remove(i);
    }
    public static void ChangeFlightTitleAtPosition(int i, String title) { activeFlightCards.get(i).title = title; }
    public static ArrayList<FlightCard> getActiveFlightCards(){ return activeFlightCards; }

    // Add a new flight card to the stack
    public static void addFlightCard(Context context, FlightCard fc){
        activeFlightCards.add(fc);

        // Save the new flight card to the main data file.
        try {
            SaveFlightCards(context);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Load flight cards from the data file
    public static void LoadFlightCards(Context context){

        // Get reference to the file. If the file doesn't exist, then no need to load anything.
        File path = context.getFilesDir();
        File file = new File(path, "savedData.xml");
        if(!file.exists()){ return; }

        // Load in the flight data
        Document doc;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        // If no flight cards have been saved, don't worry about this
        if(!doc.hasChildNodes()){ return; }

        // Each time data file is loaded in, we start with a clean slate.
        activeFlightCards.clear();

        // Iterate through all of the flight cards in the file
        NodeList flightCards = doc.getElementsByTagName("flight_card");
        for(int ii = 0; ii < flightCards.getLength(); ii++){

            // Get the specific flight card element
            Element flightCard = (Element)flightCards.item(ii);
            String fc_type = flightCard.getAttributes().getNamedItem("type").getNodeValue();

            // Grab the date parameter for the flight card.
            // NOTE: all flight cards have a date on them. They may mean different things.
            // TODO: Work out date specifics for METAR/TAFs
            Date fc_date = new Date();
            try {
                fc_date = new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US).parse(flightCard.getAttributes().getNamedItem("date_time").getNodeValue());
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Load in the flight cards to the flight card array based on type
            switch(fc_type){

                // Wind correction table
                case "wind_table":

                    String wt_title = flightCard.getAttributes().getNamedItem("title").getNodeValue();
                    Double wt_windDir = Double.parseDouble(flightCard.getElementsByTagName("wind_dir").item(0).getTextContent());
                    Double wt_windSpeed = Double.parseDouble(flightCard.getElementsByTagName("wind_speed").item(0).getTextContent());
                    Double wt_trueAirspeed = Double.parseDouble(flightCard.getElementsByTagName("true_airspeed").item(0).getTextContent());
                    Double wt_magVar = Double.parseDouble(flightCard.getElementsByTagName("magnetic_variation").item(0).getTextContent());
                    Integer wt_showEvery = Integer.parseInt(flightCard.getElementsByTagName("show_every").item(0).getTextContent());

                    // Construct new flight card, populate with XML data
                    FlightCard_WindChart fc_wt = new FlightCard_WindChart();
                    fc_wt.windDir_head = wt_windDir;
                    fc_wt.windSpeed = wt_windSpeed;
                    fc_wt.trueAirspeed = wt_trueAirspeed;
                    fc_wt.showEvery = wt_showEvery;
                    fc_wt.magVar = wt_magVar;
                    fc_wt.title = wt_title;
                    fc_wt.date = fc_date;

                    // Add this newly created flight card to the active flight cards
                    activeFlightCards.add(fc_wt);
                    break;

                case "metar_taf":

                    // Construct flight card, populate with XML data
                    // TODO: implement actual code to handle this
                    FlightCard_METARTAF fc_mt = new FlightCard_METARTAF();
                    fc_mt.altimeter = 29.92;
                    fc_mt.clouds.add(new Pair<>(3500.0, "SCT"));
                    fc_mt.windDir = 250.0;
                    fc_mt.windSpeed = 15.0;
                    fc_mt.station = "KMDQ";

                    // Push flight card to active flight card stack
                    activeFlightCards.add(fc_mt);
                    break;
            } // switch(type)
        } // for(flight cards)
    } // LoadFlightCards

    public static void SaveFlightCards(Context context){

        // If file exists, delete the file to prep for a re-write.
        // If file does not exist, create file.
        File path = context.getFilesDir();
        File file = new File(path, "savedData.xml");
        if(file.exists()){
            file.delete();
        }else{
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Open a new XLM document and add the root
        Document doc;
        Element root;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            root = doc.createElement("active_flight_cards");
            doc.appendChild(root);
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        // If there's no flight cards to save, then don't run save logic
        if(activeFlightCards.size() == 0) { return; }

        // Iterate through all active flight cards
        // These elements are generic use elements for constructing other elements
        Element el1 = null;
        Element el2 = null;
        Element el3 = null;
        for(int ii = 0; ii < activeFlightCards.size(); ii++) {

            switch(activeFlightCards.get(ii).fc_type){

                case WINDCHART:

                    // Cast active flight card to wind table
                    FlightCard_WindChart fc_wt = (FlightCard_WindChart)activeFlightCards.get(ii);

                    // Create a flight card, set basic attributes
                    el1 = doc.createElement("flight_card");
                    el1.setAttribute("type", "wind_table");
                    el1.setAttribute("title", fc_wt.title);
                    el1.setAttribute("date_time", new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US).format(fc_wt.date));

                    // Add all required elements to the file
                    el2 = doc.createElement("wind_speed");
                    el2.setTextContent(fc_wt.windSpeed.toString());
                    el1.appendChild(el2);
                    el2 = doc.createElement("wind_dir");
                    el2.setTextContent(fc_wt.windDir_head.toString());
                    el1.appendChild(el2);
                    el2 = doc.createElement("show_every");
                    el2.setTextContent(fc_wt.showEvery.toString());
                    el1.appendChild(el2);
                    el2 = doc.createElement("true_airspeed");
                    el2.setTextContent(fc_wt.trueAirspeed.toString());
                    el1.appendChild(el2);
                    el2 = doc.createElement("magnetic_variation");
                    el2.setTextContent(fc_wt.magVar.toString());
                    el1.appendChild(el2);

                    // Add to the root document
                    root.appendChild(el1);

                    break;

                case METARTAF:

                    // Cast current flight card to a metar/taf flight card
                    FlightCard_METARTAF fc_mt = (FlightCard_METARTAF)activeFlightCards.get(ii);

                    el1 = doc.createElement("flight_card");
                    el1.setAttribute("type", "metar_taf");
                    el1.setAttribute("title", fc_mt.title);
                    el1.setAttribute("date_time", new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US).format(fc_mt.date));

                    // TODO: Add code to take fc data and put into xml


                    root.appendChild(el1);
                    break;
            } // switch(type)
        } // for(all flight cards)

        // Write to output file
        try {
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            tr.transform(
                    new DOMSource(doc),
                    new StreamResult(new FileOutputStream(file.toString()))
            );
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

    } // SaveFlightCards
} // Class DataController

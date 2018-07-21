package com.jme5297.flightplanner;

import android.content.Context;
import android.content.Intent;

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

    static ArrayList<FlightCard> activeFlightCards = new ArrayList<>();

    public static void RemoveFlightCardAtPosition(int i)
    {
        activeFlightCards.remove(i);
    }
    public static void ChangeFlightTitleAtPosition(int i, String title)
    {
        activeFlightCards.get(i).title = title;
    }

    public static void DeleteDataFile(Context context)
    {
        File path = context.getFilesDir();
        File file = new File(path, "savedData.xml");
        if(!file.exists()){
            return;
        }
        file.delete();
    }

    public static void LoadFlightCards(Context context) throws
            IOException,
            ParserConfigurationException,
            SAXException {
        File path = context.getFilesDir();
        File file = new File(path, "savedData.xml");
        if(!file.exists()){
            return;
        }

        // Load in the flight data
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        if(!doc.hasChildNodes()){
            return;
        }

        activeFlightCards.clear();

        doc.getDocumentElement().normalize();
        NodeList flightCards = doc.getElementsByTagName("flight_card");
        for(int ii = 0; ii < flightCards.getLength(); ii++){
            Element flightCard = (Element)flightCards.item(ii);
            String fc_type = flightCard.getAttributes().getNamedItem("type").getNodeValue();
            String fc_title = flightCard.getAttributes().getNamedItem("title").getNodeValue();
            Date fc_date = new Date();
            try {
                fc_date = new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US).parse(flightCard.getAttributes().getNamedItem("date_time").getNodeValue());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            switch(fc_type){
                case "wind_table":
                    Double windDir = Double.parseDouble(
                            flightCard
                                    .getElementsByTagName("wind_dir")
                                    .item(0)
                                    .getTextContent());
                    Double windSpeed = Double.parseDouble(
                            flightCard
                                    .getElementsByTagName("wind_speed")
                                    .item(0)
                                    .getTextContent());
                    Double trueAirspeed = Double.parseDouble(
                            flightCard
                                    .getElementsByTagName("true_airspeed")
                                    .item(0)
                                    .getTextContent());
                    Double magVar = Double.parseDouble(
                            flightCard
                                    .getElementsByTagName("magnetic_variation")
                                    .item(0)
                                    .getTextContent());
                    Integer showEvery = Integer.parseInt(
                            flightCard
                                    .getElementsByTagName("show_every")
                                    .item(0)
                                    .getTextContent());

                    FlightCard_WindChart fc = new FlightCard_WindChart();
                    fc.windDir_head = windDir;
                    fc.windSpeed = windSpeed;
                    fc.trueAirspeed = trueAirspeed;
                    fc.showEvery = showEvery;
                    fc.magVar = magVar;
                    fc.title = fc_title;
                    fc.date = fc_date;
                    activeFlightCards.add(fc);
            }
        }
    }

    public static void SaveFlightCards(Context context) throws
            FileNotFoundException,
            TransformerException,
            ParserConfigurationException {

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
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElement("active_flight_cards");
        doc.appendChild(root);

        if(activeFlightCards.size() == 0) { return; }

        for(int ii = 0; ii < activeFlightCards.size(); ii++) {
            switch(activeFlightCards.get(ii).fc_type){

                case WINDCHART:
                    FlightCard_WindChart fc = (FlightCard_WindChart)activeFlightCards.get(ii);
                    Element e = null;
                    Element f = null;

                    e = doc.createElement("flight_card");
                    e.setAttribute("type", "wind_table");
                    e.setAttribute("title", fc.title);
                    e.setAttribute("date_time", new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.US).format(fc.date));

                    f = doc.createElement("wind_speed");
                    f.setTextContent(fc.windSpeed.toString());
                    e.appendChild(f);
                    f = doc.createElement("wind_dir");
                    f.setTextContent(fc.windDir_head.toString());
                    e.appendChild(f);
                    f = doc.createElement("show_every");
                    f.setTextContent(fc.showEvery.toString());
                    e.appendChild(f);
                    f = doc.createElement("true_airspeed");
                    f.setTextContent(fc.trueAirspeed.toString());
                    e.appendChild(f);
                    f = doc.createElement("magnetic_variation");
                    f.setTextContent(fc.magVar.toString());
                    e.appendChild(f);

                    // Add to the document
                    root.appendChild(e);

                    break;
            }
        }

        // Write to output file
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        tr.transform(
                new DOMSource(doc),
                new StreamResult(
                        new FileOutputStream(file.toString())
                )
        );
    }
    public static ArrayList<FlightCard> getActiveFlightCards(){ return activeFlightCards; }
    public static void addFlightCard(Context context, FlightCard fc) throws
            FileNotFoundException,
            TransformerException,
            ParserConfigurationException {

        activeFlightCards.add(fc);
        SaveFlightCards(context);

    }
}

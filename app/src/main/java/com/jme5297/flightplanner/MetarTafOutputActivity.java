package com.jme5297.flightplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.sax.Element;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MetarTafOutputActivity extends AppCompatActivity {

    Boolean saved = false;
    String icaostation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metar_taf_output);

        // Test XML DATA
        String url = "https://www.aviationweather.gov/adds/dataserver_current/httpparam?dataSource=metars&requestType=retrieve&format=xml&stationString=KMDQ&hoursBeforeNow=1";
        Document doc;
        try {
            URL urlurl = new URL(url);
            InputStream is = urlurl.openStream();
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(urlurl.openStream());
            doc.getDocumentElement().normalize();
            String raw_data = doc.getElementsByTagName("METAR").item(0).getFirstChild().getTextContent();
            TextView text = findViewById(R.id.mto_output);
            text.setText(raw_data);

        }catch (Exception e){
            e.printStackTrace();
        }

        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mt_main, menu);
        MenuItem item = menu.findItem(R.id.action_mt_save);

        icaostation = getIntent().getStringExtra("mt_ICAO_Station");
        saved = getIntent().getBooleanExtra("mt_saved", true);

        if(saved) {
            Drawable resIcon = getResources().getDrawable(R.drawable.ic_v_save_white);
            resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            item.setEnabled(false);
            item.setIcon(resIcon);
        }else {
            item.setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Save button is pressed
        if (id == R.id.action_mt_save) {

            // Create a flight card
            FlightCard_METARTAF fc = new FlightCard_METARTAF();
            fc.altimeter = 29.92;
            fc.clouds.add(new Pair<>(3500.0, "SCT"));
            fc.windDir = 250.0;
            fc.windSpeed = 15.0;
            fc.station = icaostation;

            DataController.addFlightCard(getApplicationContext(), fc);

            MenuItem save_item = item;
            Snackbar
                    .make(findViewById(R.id.mt_main), "Flight Card created!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null)
                    .show();
            save_item.setEnabled(false);
            Drawable resIcon = getResources().getDrawable(R.drawable.ic_v_save_white);
            resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            save_item.setIcon(resIcon);

        } else if (id == R.id.action_mt_home) {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }

        return super.onOptionsItemSelected(item);
    }
}
package com.jme5297.flightplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import org.xml.sax.SAXException;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener{

    /*
     * Declarations
     */
    ListView flightCardList;                    // List View for all flight cards
    FlightCardAdapter flightCardArrayAdapter;   // Adapter for the List View (custom)
    Boolean isEditingItem = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            DataController.LoadFlightCards(this);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        // Set the new toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get reference to the flight card table
        flightCardList = findViewById(R.id.flightCardList);

        // Implement the flight card adapter for the list
        flightCardArrayAdapter = new FlightCardAdapter(
                getApplicationContext(),
                DataController.getActiveFlightCards());
        flightCardList.setAdapter(flightCardArrayAdapter);
        flightCardList.setOnItemClickListener(this);
        flightCardList.setOnItemLongClickListener(this);

        // Update the flight cards.
        flightCardArrayAdapter.notifyDataSetChanged();

        //TranslateAnimation anim = new TranslateAnimation();

        // Tests for implementing edit/delete by long clicking
        /*
        registerForContextMenu(flightCardList);
        flightCardList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        */

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.email_me);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Email feedback support coming soon.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "jme5297@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Flight Planner - Feedback");
                startActivity(Intent.createChooser(intent,""));
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
            return true;
        }
        if (id == R.id.action_delete) {
            if(flightCardArrayAdapter.isEditable()){
                flightCardArrayAdapter.setEditable(false);
                item.setIcon(getResources().getDrawable(R.drawable.ic_v_delete_white));
            }
            else {
                flightCardArrayAdapter.setEditable(true);
                item.setIcon(getResources().getDrawable(R.drawable.ic_v_check_white));
            }

            flightCardArrayAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle wind_tool_menu view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_winds) {
            Intent intent = new Intent(this, WindToolActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_metar_taf) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {

        if(isEditingItem){ return; }

        if(flightCardArrayAdapter.isEditable()){
            DataController.RemoveFlightCardAtPosition(i);
            flightCardArrayAdapter.notifyDataSetChanged();

            try {
                DataController.SaveFlightCards(this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            return;
        }

        FlightCard fc = (FlightCard)adapterView.getItemAtPosition(i);
        switch(fc.fc_type)
        {
            case WINDCHART:
                FlightCard_WindChart fc_wc = (FlightCard_WindChart)adapterView.getItemAtPosition(i);

                // Start up the wind tool output activity
                Intent intent = new Intent(this, WindToolOutputActivity.class);
                intent.putExtra(WindToolActivity.EXTRA_SHOWEVERY, fc_wc.showEvery);
                intent.putExtra(WindToolActivity.EXTRA_WIND_DIR, fc_wc.windDir_head);
                intent.putExtra(WindToolActivity.EXTRA_WIND_SPD, fc_wc.windSpeed);
                intent.putExtra(WindToolActivity.EXTRA_MAG_VAR, fc_wc.magVar);
                intent.putExtra(WindToolActivity.EXTRA_TAS, fc_wc.trueAirspeed);
                intent.putExtra(WindToolActivity.EXTRA_SAVED, true);
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        // Create a dialog to give wind card a title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Flight Card Title");
        builder.setMessage("Rename the title of this flight card:");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(DataController.getActiveFlightCards().get(i).title);
        builder.setView(input);
        final AdapterView<?> adapterViewIn = adapterView;
        final int ii = i;
        final Context myContext = this;
        final View myView = view;
        isEditingItem = true;

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(input.getText().toString().isEmpty()){
                    dialog.cancel();
                    isEditingItem = false;
                    return;
                }

                DataController.ChangeFlightTitleAtPosition(ii, input.getText().toString());
                flightCardArrayAdapter.notifyDataSetChanged();

                try {
                    DataController.SaveFlightCards(myContext);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                }

                Snackbar
                        .make(myView, "Flight Card Renamed!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();

                isEditingItem = false;
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isEditingItem = false;
                dialog.cancel();
            }
        });
        builder.show();

        return false;
    }
}

package com.jme5297.flightplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener{

    /*
     * Class-Wide declarations
     */
    ListView flightCardListView;                // List View for all flight cards
    FlightCardAdapter flightCardArrayAdapter;   // Adapter for the List View (custom)
    Boolean isEditingItem = false;              // Currently changing name of flight card

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load in the flight cards from the savedData.xml file.
        DataController.LoadFlightCards(this);

        // Set the new toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        // Get reference to the flight card table, and set the adapter.
        flightCardListView = findViewById(R.id.flightCardList);
        flightCardArrayAdapter = new FlightCardAdapter(getApplicationContext(), DataController.getActiveFlightCards());
        flightCardListView.setAdapter(flightCardArrayAdapter);
        flightCardListView.setOnItemClickListener(this);
        flightCardListView.setOnItemLongClickListener(this);

        // Update the flight cards with the new data.
        flightCardArrayAdapter.notifyDataSetChanged();

        // Set up the e-mail floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.email_me);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Email feedback support coming soon.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                /*
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ "jme5297@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Flight Planner - Feedback");
                startActivity(Intent.createChooser(intent,""));
                */
            }
        });

        // Set up the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Set up the navigation view
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    /* Part of Navigation listener. */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /* Part of the toolbar that this class extends from. */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /* When an item on the Options menu is selected */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Settings page
        if (id == R.id.action_main_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
            return true;
        }

        // Delete mode - enable
        if (id == R.id.action_main_delete) {
            if(flightCardArrayAdapter.isDeletable()){
                flightCardArrayAdapter.setDeletable(false);
                item.setIcon(getResources().getDrawable(R.drawable.ic_v_delete_white));
            }
            else {
                flightCardArrayAdapter.setDeletable(true);
                item.setIcon(getResources().getDrawable(R.drawable.ic_v_check_white));
            }

            // This needs to be called to update background colors
            flightCardArrayAdapter.notifyDataSetChanged();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* One of the navigation items from the draw is selected */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle wind_tool_menu view item clicks here.
        int id = item.getItemId();

        // Wind Table
        if (id == R.id.nav_winds) {
            Intent intent = new Intent(this, WindToolActivity.class);
            startActivity(intent);
        }

        // METAR & TAF tool
        else if (id == R.id.nav_metar_taf) {

            // Testing with data by sending directly to the output activity
            Intent intent  = new Intent(this, MetarTafOutputActivity.class);
            intent.putExtra("mt_ICAO_Station", "KMDQ");
            intent.putExtra("mt_saved", false);
            startActivity(intent);

        }

        // Share options
        else if (id == R.id.nav_share) {

        }

        // Send and export options
        else if (id == R.id.nav_send) {

        }

        // Close the navigation drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /* Comes from ListView.OnItemClickListener */
    @Override
    public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {

        // If we're currently editing an item, no need to open anything.
        // TODO: This really should be changed to some other feature to change titles (if at all).
        if(isEditingItem){ return; }

        // If the flight careds are in deletable mode, then a click will delete!
        if(flightCardArrayAdapter.isDeletable()){

            // Data parameters to send into the dialog
            final Integer posToDelete = i;
            final Context myContext = this;

            // Confirm with the user that this should be deleted
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setMessage("Are you sure?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DataController.RemoveFlightCardAtPosition(posToDelete);
                            flightCardArrayAdapter.notifyDataSetChanged();
                            DataController.SaveFlightCards(myContext);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isEditingItem = false;
                            dialog.cancel();
                        }
                    })
                    .show();

            // Aferwords, we don't need the rest of the function, so return now
            return;
        }

        // Get a hold of the current flight card being looked at.
        FlightCard fc = (FlightCard)adapterView.getItemAtPosition(i);
        Intent intent;

        // Based on the type of flight card that was clicked, open new activity
        switch(fc.fc_type)
        {
            case WINDCHART:
                // Cast to the wind-chart flight card
                FlightCard_WindChart fc_wc = (FlightCard_WindChart)fc;

                // Start up the wind tool output activity
                // TODO: Should look up a way to send the entire flight card
                intent = new Intent(this, WindToolOutputActivity.class);
                intent.putExtra(WindToolActivity.EXTRA_SHOWEVERY, fc_wc.showEvery);
                intent.putExtra(WindToolActivity.EXTRA_WIND_DIR, fc_wc.windDir_head);
                intent.putExtra(WindToolActivity.EXTRA_WIND_SPD, fc_wc.windSpeed);
                intent.putExtra(WindToolActivity.EXTRA_MAG_VAR, fc_wc.magVar);
                intent.putExtra(WindToolActivity.EXTRA_TAS, fc_wc.trueAirspeed);
                intent.putExtra(WindToolActivity.EXTRA_SAVED, true);
                startActivity(intent);

                break;

            case METARTAF:
                // Cast to the metar/taf flight card
                FlightCard_METARTAF fc_mt = (FlightCard_METARTAF)fc;

                // Start up the metar/taf activity
                // TODO: This is just an example, but it should be expanded
                intent  = new Intent(this, MetarTafOutputActivity.class);
                intent.putExtra("mt_ICAO_Station", "KMDQ");
                intent.putExtra("mt_saved", true);
                startActivity(intent);

                break;

        } // switch(fc.fc_type)
    } // onItemClick

    /* Callback used for editing titles */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

        // Titles cannot be changed for metar cards, because ICAO data is displayed here.
        if(DataController.getActiveFlightCards().get(i).fc_type == FlightCardType.METARTAF) {
            return false;
        }

        // Create a dialog to give wind card a title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Flight Card Title");
        builder.setMessage("Rename the title of this flight card:");

        // Data to be accessed in the positive/negative button listeners
        final AdapterView<?> adapterViewIn = adapterView;
        final int ii = i;
        final Context myContext = this;
        final View myView = view;
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(DataController.getActiveFlightCards().get(i).title);
        builder.setView(input);

        // Enter editing mode (cannot open other activity windows)
        isEditingItem = true;

        // Positive button (change name)
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // If user didn't input anything, don't change the title
                if(input.getText().toString().isEmpty()){
                    dialog.cancel();
                    isEditingItem = false;
                    return;
                }

                // Change title of the flight card, and update the flight card set.
                DataController.ChangeFlightTitleAtPosition(ii, input.getText().toString());
                flightCardArrayAdapter.notifyDataSetChanged();

                // Save the new flight card data
                DataController.SaveFlightCards(myContext);

                // Notify the user
                Snackbar.make(myView, "Flight Card Renamed!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();

                // No longer editing
                isEditingItem = false;
            }
        });

        // Negative (cancel)
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // No longer editing
                isEditingItem = false;
                dialog.cancel();
            }
        });

        builder.show();

        return false;
    }
}

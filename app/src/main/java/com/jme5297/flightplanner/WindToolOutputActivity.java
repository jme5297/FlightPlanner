package com.jme5297.flightplanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileNotFoundException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class WindToolOutputActivity extends AppCompatActivity {

    Double windSpeed;
    Double windDir_head;
    Double trueAirspeed;
    Double magVar;
    Integer showEvery;
    Boolean saved;

    Integer numDataRows;
    Double[] TC;
    Double[] GS;
    Double[] WCA;
    Double[] MH;

    TableLayout windChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind_tool_output);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        windChart = findViewById(R.id.wto_windChart);

        // Get values passed through intent
        Intent intent = getIntent();
        windSpeed       = intent.getDoubleExtra (WindToolActivity.EXTRA_WIND_SPD,   0.0);
        windDir_head    = intent.getDoubleExtra (WindToolActivity.EXTRA_WIND_DIR,   0.0);
        trueAirspeed    = intent.getDoubleExtra (WindToolActivity.EXTRA_TAS,        0.0);
        magVar          = intent.getDoubleExtra (WindToolActivity.EXTRA_MAG_VAR,    0.0);
        showEvery       = intent.getIntExtra    (WindToolActivity.EXTRA_SHOWEVERY,  90);
        saved           = intent.getBooleanExtra(WindToolActivity.EXTRA_SAVED,      false);
        numDataRows = 0;

        // Calculate required chart data
        CalculateChartData();

        // Generate the chart
        GenerateChart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.wto_main, menu);
        MenuItem item = menu.findItem(R.id.action_wto_save);

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
        if (id == R.id.action_wto_save) {

            // Create a dialog to give wind card a title
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Flight Card - Title");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            final MenuItem save_item = item;

            // Set up the buttons
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Save data to active flight cards
                    FlightCard_WindChart fc = new FlightCard_WindChart();
                    fc.title = input.getText().toString();
                    if(input.getText().toString().isEmpty()){
                        fc.title = fc.title_type;
                    }
                    fc.windSpeed = windSpeed;
                    fc.windDir_head = windDir_head;
                    fc.magVar = magVar;
                    fc.showEvery = showEvery;
                    fc.trueAirspeed = trueAirspeed;

                    try {
                        DataController.addFlightCard(getApplicationContext(), fc);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    }

                    Snackbar
                            .make(findViewById(R.id.wto_main), "Flight Card created!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null)
                            .show();

                    save_item.setEnabled(false);
                    Drawable resIcon = getResources().getDrawable(R.drawable.ic_v_save_white);
                    resIcon.mutate().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
                    save_item.setIcon(resIcon);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

        }else if(id == R.id.action_wto_home) {
            Intent in = new Intent(this, MainActivity.class);
            startActivity(in);
        }

        return super.onOptionsItemSelected(item);
    }

    void CalculateChartData(){
        numDataRows = (360/showEvery) + 1;

        TC  = new Double[numDataRows];
        GS  = new Double[numDataRows];
        WCA = new Double[numDataRows];
        MH  = new Double[numDataRows];

        Double wd_ang = 90.0-(windDir_head+180.0);
        Double wd_ang_mod = wd_ang % 360.0;
        Double wd_x = windSpeed*Math.cos(wd_ang_mod*Math.PI/180.0);
        Double wd_y = windSpeed*Math.sin(wd_ang_mod*Math.PI/180.0);

        // Perform the calculations
        for(int ii = 0; ii < numDataRows; ii++){
            double gc_naut = ii*showEvery;
            TC[ii] = gc_naut;
            double gc_ang_mod = ((90.0-gc_naut)+360.0) % 360.0;
            double wd_x_T = wd_x*Math.cos(gc_ang_mod*Math.PI/180.0)+wd_y*Math.sin(gc_ang_mod*Math.PI/180.0);
            double wd_y_T = -wd_x*Math.sin(gc_ang_mod*Math.PI/180.0)+wd_y*Math.cos(gc_ang_mod*Math.PI/180.0);
            double sgn = (Math.abs(wd_y_T) < 0.00001) ? Math.signum(wd_x_T) : Math.signum(wd_y_T);
            double alpha = sgn*Math.acos(wd_x_T/windSpeed);
            double alpha_mod = (alpha + 2.0*Math.PI) % (2.0*Math.PI);
            double beta = Math.asin(-windSpeed*Math.sin(alpha_mod)/trueAirspeed);
            GS[ii] = trueAirspeed*Math.cos(-beta)+windSpeed*Math.cos(alpha_mod);
            WCA[ii] = -1.0*beta*180.0/Math.PI;
            double th_ang = gc_ang_mod + beta*180.0/Math.PI;
            double th_naut = ((90.0 - th_ang) + 360.0) % 360.0;
            MH[ii] = th_naut + magVar;
        }
    }

    void GenerateChart(){

        // Set initial header data and layout parameters
        TableRow r0 = new TableRow(this);
        r0.setLayoutParams( new TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                0.3f ) );
        r0.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
        TableRow.LayoutParams trlp = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT,
                1.0f );
        trlp.setMargins(
                (int)getResources().getDimension(R.dimen.wind_chart_margin),
                (int)getResources().getDimension(R.dimen.wind_chart_margin),
                (int)getResources().getDimension(R.dimen.wind_chart_margin),
                (int)getResources().getDimension(R.dimen.wind_chart_margin) );

        // Set title row
        String[] titles = {"TC", "GS", "WCA", "MH"};
        for(int j = 0; j < titles.length; j++){
            TextView txt = new TextView(this);
            txt.setLayoutParams(trlp);
            txt.setBackgroundColor( getResources().getColor(R.color.colorPrimary) );
            txt.setText(titles[j]);
            txt.setTextColor(Color.WHITE);
            txt.setTypeface(null, Typeface.BOLD);
            txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
            txt.setTextSize(getResources().getDimension(R.dimen.wind_chart_table));
            r0.addView(txt);
        }
        windChart.addView(r0);

        // Add all of the values.
        for(int i = 0; i < numDataRows; i++) {
            TableRow r = new TableRow(this);
            r.setLayoutParams( new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.MATCH_PARENT,
                    1.0f ) );
            r.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);

            double[] printedVals = {TC[i], GS[i], WCA[i], MH[i]};
            int[] colors = {
                    Color.BLACK,
                    getResources().getColor(R.color.wc_green),
                    getResources().getColor(R.color.wc_red),
                    getResources().getColor(R.color.wc_blue) };

            for(int j = 0; j < 4; j++){
                TextView txt = new TextView(this);
                txt.setLayoutParams(trlp);
                txt.setBackgroundColor( getResources().getColor(R.color.colorBright) );
                txt.setText(String.format("%.0f",printedVals[j]));
                txt.setTypeface(null, Typeface.BOLD);
                txt.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER);
                txt.setTextSize(getResources().getDimension(R.dimen.wind_chart_table));
                txt.setTextColor(colors[j]);
                r.addView(txt);
            }
            windChart.addView(r);
        }
    }
}

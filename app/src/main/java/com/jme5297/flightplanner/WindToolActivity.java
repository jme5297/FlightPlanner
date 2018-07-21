package com.jme5297.flightplanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class WindToolActivity extends AppCompatActivity {

    public static final String EXTRA_WIND_DIR = "com.jme5297.flightplanner.wt_wind_speed";
    public static final String EXTRA_WIND_SPD = "com.jme5297.flightplanner.wt_wind_direction";
    public static final String EXTRA_TAS = "com.jme5297.flightplanner.wt_true_airspeed";
    public static final String EXTRA_SHOWEVERY = "com.jme5297.flightplanner.wt_show_every";
    public static final String EXTRA_MAG_VAR = "com.jme5297.flightplanner.wt_mag_var";
    public static final String EXTRA_SAVED = "com.jme5297.flightplanner.wt_saved";

    static Double windDirDeg;
    static Double windSpeedKts;
    static Double trueAirspeedKts;
    static Integer showEveryDeg;
    static Integer showEveryDegPos;
    static Double magVarDeg;

    EditText et_magVar;
    EditText et_windDir;
    EditText et_windSpeed;
    EditText et_trueAirspeed;
    Spinner  sp_displayEvery;
    Button   btnGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wind_tool);

        // Find the objects.
        et_windDir = findViewById(R.id.wt_wind_direction);
        et_windSpeed = findViewById(R.id.wt_wind_speed);
        et_trueAirspeed = findViewById(R.id.wt_tas);
        sp_displayEvery = findViewById(R.id.wt_showevery);
        et_magVar = findViewById(R.id.wt_mag_var);
        btnGenerate = findViewById(R.id.wt_btnGenerate);

        addEventListeners();

        // Set values if not empty
        setInitialValues();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    void setInitialValues(){
        if(windDirDeg != null){
            et_windDir.setText(windDirDeg.toString());
        }
        if(windSpeedKts != null){
            et_windSpeed.setText(windSpeedKts.toString());
        }
        if(trueAirspeedKts != null){
            et_trueAirspeed.setText(trueAirspeedKts.toString());
        }
        if(showEveryDeg != null){
            sp_displayEvery.setSelection(showEveryDegPos);
        }
        if(magVarDeg != null){
            et_magVar.setText(magVarDeg.toString());
        }
    }

    void addEventListeners() {

        // Add event listeners
        et_windDir.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!et_windDir.getText().toString().matches("")){
                    windDirDeg = Double.parseDouble(et_windDir.getText().toString());
                }

            }
        });
        et_windSpeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!et_windSpeed.getText().toString().matches("")){
                    windSpeedKts = Double.parseDouble(et_windSpeed.getText().toString());
                }

            }
        });
        et_trueAirspeed.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!et_trueAirspeed.getText().toString().matches("")){
                    trueAirspeedKts = Double.parseDouble(et_trueAirspeed.getText().toString());
                }

            }
        });
        et_magVar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!et_magVar.getText().toString().matches("")){
                    magVarDeg = Double.parseDouble(et_magVar.getText().toString());
                }

            }
        });
        sp_displayEvery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                showEveryDeg = Integer.parseInt(sp_displayEvery.getSelectedItem().toString());
                showEveryDegPos = sp_displayEvery.getSelectedItemPosition();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                showEveryDeg = 90;
                showEveryDegPos = 0;
            }
        });
    }

    /** Pass data onto the window correction chart generator */
    public void PassGeneratedData (View view) {
        if(windDirDeg == null || windSpeedKts == null || trueAirspeedKts == null ) {
            Snackbar
                    .make(view, "All values above must be set.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }
        Intent intent = new Intent(this, WindToolOutputActivity.class);
        intent.putExtra(EXTRA_SHOWEVERY, showEveryDeg);
        intent.putExtra(EXTRA_WIND_DIR, windDirDeg);
        intent.putExtra(EXTRA_WIND_SPD, windSpeedKts);
        intent.putExtra(EXTRA_MAG_VAR, magVarDeg);
        intent.putExtra(EXTRA_TAS, trueAirspeedKts);
        intent.putExtra(EXTRA_SAVED, false);
        startActivity(intent);
    }
}

package xyz.navinda.opensinhaladictionary;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(NullPointerException e){
            Log.e("SearchActivity Toolbar", "You have got a NULL POINTER EXCEPTION");
        }

        // Get the widget references from XML layout
        final Button saveBtn = findViewById(R.id.btnSave);
        final EditText txtSuggestionsLimit = findViewById(R.id.txtSuggestionsLimit);
        final EditText txtFontSize = findViewById(R.id.txtFontSize);

        //get existing settings
        HandleSettings settings = new HandleSettings(getSharedPreferences("Settings", MODE_PRIVATE));
        txtFontSize.setText(String.valueOf(settings.getFontSize()));
        txtSuggestionsLimit.setText(String.valueOf(settings.getSuggestionsLimit()));

        //when click save button
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //save settings in shared preferences
                SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();

                editor.putString("suggestionsLimit",txtSuggestionsLimit.getText().toString());
                editor.putString("fontSize", txtFontSize.getText().toString());
                editor.apply();
                //end activity
                finish();
            }
        });

    }

}

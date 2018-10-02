package xyz.navinda.opensinhaladictionary;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import java.util.HashMap;
import java.util.Map;

public class HandleSettings extends AppCompatActivity{
    private  SharedPreferences settings;

    public HandleSettings(SharedPreferences settings) {
        this.settings = settings;
    }

    public int getFontSize() {
        return(Integer.parseInt(getSettings().get("fontSize")));
    }

    public int getSuggestionsLimit() {
        return(Integer.parseInt(getSettings().get("suggestionsLimit")));
    }

    //check custom settings exist
    public boolean checkSettingsExist() {
        if (settings.contains("fontSize")) {
            return true;
        } else {
            return false;
        }
    }

    //get settings from shared preferences & return them as key:value array
    private Map<String,String> getSettings() {
        Map<String,String> array = new HashMap<String,String>();
        String fontSize,suggestionsLimit;

        if (checkSettingsExist()) {
            fontSize = settings.getString("fontSize", null);
            suggestionsLimit = settings.getString("suggestionsLimit",null);
        } else {
            //default settings
            fontSize = "24";
            suggestionsLimit = "8";
        }

        array.put("fontSize",fontSize);
        array.put("suggestionsLimit",suggestionsLimit);

        return (array);
    }

}

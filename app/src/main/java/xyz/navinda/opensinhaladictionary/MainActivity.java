package xyz.navinda.opensinhaladictionary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import android.support.v7.widget.SearchView;

public class MainActivity extends AppCompatActivity

        implements NavigationView.OnNavigationItemSelectedListener {
    private Typeface mTypeface;
    private List<String> meanings_list;
    private ArrayAdapter<String> arrayAdapter;
    private Uri url;
    private SearchView txtInput;
    private boolean suggest;
    private String inputWord;
    private Map<String, String> DBen2sn = new TreeMap<String, String>();
    private Map<String, String> DBsn2en = new TreeMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Load db to array
        loadDB();

        //Close keyboard when drawer open
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                closeKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //Called when a drawer has settled in a completely open state.
                //The drawer is interactive at this point.
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Called when a drawer has settled in a completely closed state.
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Called when the drawer motion state changes. The new state will be one of STATE_IDLE, STATE_DRAGGING or STATE_SETTLING.
            }
        });

        // Get the application context
        Context mContext = getApplicationContext();
        Activity mActivity = MainActivity.this;

        // Get the widget reference from XML layout
        ListView mListView = (ListView) findViewById(R.id.listOutput);

        // Initialize a typeface (custom font)
        mTypeface  = Typeface.createFromAsset(getAssets(),"font/malithi_web.ttf");

        //Array for meanings
        String[] meanings = new String[]{};
        meanings_list = new ArrayList<String>(Arrays.asList(meanings));

        //Create array adapter
        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, meanings_list) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent){
                // Cast the list view each item as text view
                TextView item = (TextView) super.getView(position,convertView,parent);

                // Set the typeface/font for the current item
                item.setTypeface(mTypeface);

                // Set the list view item's text color
                item.setTextColor(Color.parseColor("#000000"));

                // Set the item text style to bold
                //item.setTypeface(item.getTypeface(), Typeface.BOLD);

                // Change the item text size
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP,24);

                // return the view
                return item;
            }
        };

        //Set array adapter to list view
        mListView.setAdapter(arrayAdapter);

        //Listview onclick
        suggest=false;
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // Get the selected item text from ListView
                String selectedWord = (String) parent.getItemAtPosition(position);

                //if user select a suggestion
                if (suggest) {
                    inputWord=selectedWord;
                    txtInput.setQuery(inputWord, false);
                    closeKeyboard();
                    readyInput();
                    doSearch();
                    suggest=false;
                }
            }
        });

        //Get buttons and input text reference
        txtInput = findViewById(R.id.txtInput);
        txtInput.setIconified(false);
        txtInput.clearFocus();

        //Lister for search view
        txtInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //when user hits return key
                readyInput();
                if (checkInput()) {
                    doSearch();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                clearMeanings();
                readyInput();
                if (!isEmptyOrNull(inputWord)) {
                    suggestWords();
                }
                return false;
            }
        });
    }

    private void loadDB(){
        //load db to maps
        readDB("db/en2sn.txt");
        readDB("db/sn2en.txt");
    }

    private void readDB(String db) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(db), "UTF-8"));
            String line = reader.readLine();

            while (line != null) {
                String[] record = line.split("#");
                if (db.equals("db/en2sn.txt")) {
                    DBen2sn.put(record[0],record[1]);
                } else {
                    DBsn2en.put(record[0],record[1]);
                }
                line = reader.readLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void suggestWords(){
        suggest=true;

        if (checkLang()) { //if input is English
            addSuggestions(DBen2sn);
        } else { //if input is Sinhala
            addSuggestions(DBsn2en);
        }
    }

    private void addSuggestions(Map<String, String> DB){
        //add suggestions to listview
        String suggestion="";
        int suggestions=0;
        int suggestionsLimit=8;
        for (String key : DB.keySet()) {
            if (key.startsWith(inputWord)) {
                suggestions+=1;
                suggestion=key;
                meanings_list.add(suggestion);
                arrayAdapter.notifyDataSetChanged();
                if (suggestions==suggestionsLimit) {break;}
            }
        }
    }

    private void readyInput() {
        //get input word
        inputWord=txtInput.getQuery().toString();

        //Remove spaces in input word and change case to lower
        inputWord=inputWord.trim();
        inputWord=inputWord.toLowerCase();
    }

    private void doSearch(){
        String foundMeanings="";
        Boolean found = false;
        if (checkLang()) { //If input English
            if (DBen2sn.containsKey(inputWord)) {
                foundMeanings=DBen2sn.get(inputWord);
                found =true;
            } else {
                found =false;
            }
        } else { //if input is Sinhala
            if (DBsn2en.containsKey(inputWord)) {
                foundMeanings=DBsn2en.get(inputWord);
                found =true;
            } else {
                found =false;
            }
        }

        if (found) {
            showMeanings(foundMeanings);
        } else {
            String notFoundMsg;
            notFoundMsg="Definition for '" + inputWord + "' is not included in our database at the moment.";
            clearMeanings();
            meanings_list.add(notFoundMsg);
            arrayAdapter.notifyDataSetChanged();
            showAlertDialogBox("Sorry!",notFoundMsg,"Ok",Gravity.NO_GRAVITY);
        }
    }

    private boolean checkInput() {
        if (isEmptyOrNull(inputWord)) {
            clearMeanings();
            meanings_list.add("Please enter a word to search.");
            arrayAdapter.notifyDataSetChanged();
            showAlertDialogBox("Sorry!","Please enter a word to search.","Ok",Gravity.NO_GRAVITY);
            return false;
        } else {
            return true;
        }
    }

    private boolean checkLang() {
        //check input language
        final String IS_ENGLISH_REGEX = "[a-zA-Z0-9\\-#\\.\\(\\)\\/%&\\s]{0,19}";
        return inputWord.matches(IS_ENGLISH_REGEX);
        //English - True, Sinhala-False
    }

    private boolean isEmptyOrNull(String input){
        if (inputWord == null || inputWord.isEmpty()){
            return true;
        } else {
            return false;
        }
    }

    private void showMeanings(String resultWords){
        resultWords=resultWords.trim(); //remove spaces
        String[] result=resultWords.split("@"); //seperate meanings by @
        clearMeanings(); //clear output list box
        //add meanings one by one
        meanings_list.addAll(Arrays.asList(result));
        arrayAdapter.notifyDataSetChanged();
    }

    private void clearMeanings(){
        //clear outputbox
        meanings_list.clear();
        arrayAdapter.notifyDataSetChanged();
    }

    private void showAlertDialogBox(final String titleTxt, String msg, String btnTxt, int TxtAlign) {

        AlertDialog ad = new AlertDialog.Builder(this).create();
        ad.setCancelable(false); // This blocks the 'BACK' button
        ad.setInverseBackgroundForced(true);
        ad.setMessage(msg);


        //Custom title for alert box
        final TextView title = new TextView(this);
        title.setText(titleTxt);
        title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        ad.setCustomTitle(title);
        ad.setCanceledOnTouchOutside(true);

        //Action for button
        ad.setButton(btnTxt, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (titleTxt.equals("About")) {
                    url = Uri.parse("https://www.navinda.xyz/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, url);
                    startActivity(intent);
                }
                if (titleTxt.equals("Report Bugs")) {
                    sendEmail();
                }
                dialog.dismiss();
            }
        });

        ad.show();

        //Alignment for alert box body msg
        TextView messageView = ad.findViewById(android.R.id.message);
        messageView.setGravity(TxtAlign);

        //Center alert box ok button
        Button customButton = ad.getButton(AlertDialog.BUTTON_POSITIVE);
        customButton.setText(btnTxt);
        LinearLayout parent = (LinearLayout) customButton.getParent();
        parent.setGravity(Gravity.CENTER_HORIZONTAL);
        View leftSpacer = parent.getChildAt(1);
        leftSpacer.setVisibility(View.GONE);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_online) {
            url = Uri.parse("https://www.zigiriweb.navinda.xyz/");
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            startActivity(intent);
        } else if (id == R.id.nav_help) {
            showAlertDialogBox("Help","Just type the word you want to search & press the search button. \n\nNote : OSD will detect your input language automatically.","Close",Gravity.NO_GRAVITY);
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_report) {
            showAlertDialogBox("Report Bugs","This built-in feature is still under construction.\n\nIf you find any bugs, please be kind enough to inform me by sending an email to navilk@zoho.com.","Report",Gravity.NO_GRAVITY);
        } else if (id == R.id.nav_source) {
            url = Uri.parse("https://github.com/ipman98/OpenSinhalaDictionary");
            Intent intent = new Intent(Intent.ACTION_VIEW, url);
            startActivity(intent);
        } else {
            showAlertDialogBox("About","Developed By Navinda Dissanayake.\n\nwww.navinda.xyz","Visit My Site", Gravity.CENTER);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void sendEmail() {
        //Send mail to bug report
        String[] TO = {"navilk@zoho.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "OSD-Bug Report");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Bug : \n Steps to trigger : ");

        try {
            startActivity(Intent.createChooser(emailIntent, "Report Bugs via E-mail"));
            finish();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void closeKeyboard(){
        try {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private void shareApp(){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Try out Open Sinhala Dictionary!. It's an ad-free, open-source English-Sinhala dictionary for Android & Windows.");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
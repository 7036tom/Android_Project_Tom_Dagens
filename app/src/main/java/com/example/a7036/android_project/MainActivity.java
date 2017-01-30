package com.example.a7036.android_project;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView.OnItemClickListener;

import android.location.Location;


public class MainActivity extends AppCompatActivity{

    private String TAG = MainActivity.class.getSimpleName();
    private ListView lv;
    ArrayList<HashMap<String, String>> dataList;
    private Menu m = null;
    static boolean Poi = true;
    static boolean Dest = true;
    static boolean Parc = true;

    String latitude;
    String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GPSTracker gps = new GPSTracker(this);

        if(gps.canGetLocation()) {
            // gps enabled} // return boolean true/false
            latitude = String.valueOf(gps.getLatitude()); // returns latitude
            longitude = String.valueOf(gps.getLongitude()); // returns longitude
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // On met le layout.
        dataList = new ArrayList<>();
        lv = (ListView) findViewById(R.id.list);

        new GetDisplay().execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private class GetDisplay extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Json Data is downloading", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String url = "http://voyage2.corellis.eu/api/v2/homev2?lat="+latitude+"&lon="+longitude+"&offset=0";
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray data = jsonObj.getJSONArray("data");

                    // looping through All destinations
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject c = data.getJSONObject(i);

                        if (c.getString("type").equals("POI") || c.getString("type").equals("CITY") || c.getString("type").equals("ADMIN") || c.get("type").equals("PARCOURS")){ // On n'affiche que ce que l'on veut.
                            if (Poi || (!Poi && !c.getString("type").equals("POI"))) {
                                if (Dest || ((!Dest && !c.getString("type").equals("CITY")) && (!Dest && !c.getString("type").equals("ADMIN"))))
                                {
                                    if (Parc || ((!Parc && !c.getString("type").equals("PARCOURS")))){
                                        String type = c.getString("type");
                                        String id = c.getString("id");
                                        String display = c.getString("display");
                                        // Hasmap pour destination unique.
                                        HashMap<String, String> dat = new HashMap<>();

                                        // On complete dat.
                                        dat.put("type", type);
                                        dat.put("display", display);
                                        dat.put("id", id);

                                        dataList.add(dat);
                                    }
                                }
                            }
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }

            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Gestion de l'adapteur.
            ListAdapter adapter = new SimpleAdapter(MainActivity.this, dataList,
                    R.layout.list_item, new String[]{"type", "display", "id"},
                    new int[]{R.id.type, R.id.display, R.id.id});
            lv.setAdapter(adapter);

            // Gestion des clics sur listview.
            lv.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String type = ((TextView) view.findViewById(R.id.type)).getText().toString();
                    String idIntent = ((TextView) view.findViewById(R.id.id)).getText().toString();

                    // Reste Parcours et destination a faire.
                    if (type.equals("POI")) {
                        Intent adapterPOI = new Intent(MainActivity.this, POI_Adapter.class);
                        adapterPOI.putExtra("id", idIntent);
                        startActivity(adapterPOI);

                    }

                    if (type.equals("PARCOURS")) {
                        Intent adapterPARCOURS = new Intent(MainActivity.this, PARCOURS_Adapter.class);
                        adapterPARCOURS.putExtra("id", idIntent);
                        startActivity(adapterPARCOURS);

                    }

                    if (type.equals("ADMIN") || type.equals("CITY")) {
                        Intent DESTINATION_Adapter = new Intent(MainActivity.this, DESTINATION_Adapter.class);
                        DESTINATION_Adapter.putExtra("id", idIntent);
                        startActivity(DESTINATION_Adapter);

                    }



                }
            });


        }
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        if (item.isChecked())
        {
            item.setChecked(false);
        }
        else
        {
            item.setChecked(true);
        }

        if (item.getTitle().equals("Poi") && Poi) {
            Poi = false;
            recreate();
        }

        else if (item.getTitle().equals("Poi") && !Poi) {
            Poi = true;
            recreate();
        }

        if (item.getTitle().equals("Parcours") && Parc) {
            Parc = false;
            recreate();
        }

        else if (item.getTitle().equals("Parcours") && !Parc) {
            Parc = true;
            recreate();
        }

        if (item.getTitle().equals("Destination") && Dest) {
            Dest = false;
            recreate();
        }

        else if (item.getTitle().equals("Destination") && !Dest) {
            Dest = true;
            recreate();
        }



        return super.onOptionsItemSelected(item);
    }

}

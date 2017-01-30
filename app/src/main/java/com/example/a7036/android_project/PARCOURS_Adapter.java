package com.example.a7036.android_project;

import android.app.Activity;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.squareup.picasso.Picasso;

public class PARCOURS_Adapter extends Activity{

    private ImageView img1;
    private TextView Titre;
    private TextView Description;
    private String TAG_PARCOURS = PARCOURS_Adapter.class.getSimpleName();
    String id_PARCOURS;
    String url_photo;
    String nom;
    String description;
    int width;

    private class GetDisplay extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(PARCOURS_Adapter.this, "Json Data is downloading", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response

            String url_PARCOURS = "http://voyage2.corellis.eu/api/v2/parcours?id="+id_PARCOURS; // On veut le json du Parcours choisi.

            String jsonStr = sh.makeServiceCall(url_PARCOURS);

            Log.e(TAG_PARCOURS, "Response from url: " + jsonStr);
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray data = jsonObj.getJSONArray("data");
                    JSONObject c = data.getJSONObject(0);

                    nom = c.getString("title");
                    description = c.getString("description");

                    JSONArray rep_photo = c.getJSONArray("medias"); // Contient au moins une image.
                    for (int i = 0; i < 1; i++) {
                        JSONObject photo = rep_photo.getJSONObject(i);
                        url_photo = photo.getString("url");
                    }

                } catch (final JSONException e) {
                    Log.e(TAG_PARCOURS, "Json parsing error: " + e.getMessage());
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
                Log.e(TAG_PARCOURS, "Couldn't get json from server.");
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

            // On s'occupe de l'image.
            Picasso.with(getApplicationContext()).load(url_photo).resize(width, width).centerCrop().into(img1);

            // On s'occupe du texte.
            Titre.setText(nom);
            Description.setText(Html.fromHtml(description));



        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.poi_layout); // On installe le layout.

        // Déclaration des élements du layout.
        img1 = (ImageView) findViewById(R.id.imageView);
        Titre = (TextView) findViewById(R.id.Titre);
        Description = (TextView) findViewById(R.id.Description);

        // On recupere l'id du POI(resp parcours..)
        id_PARCOURS = getIntent().getStringExtra("id");

        // On recupere la largeur de l'ecran pour l'installation future de l'image.
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;

        new GetDisplay().execute();

    }

}

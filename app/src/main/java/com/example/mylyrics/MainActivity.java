package com.example.mylyrics;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

public class MainActivity extends AppCompatActivity {

    EditText edit_artist, edt_name;
    TextView text_lyrics;

    //Change here with your Spotify Developer Client_ID and Redirect_URI
    private static final String CLIENT_ID = "Your_Client_ID";
    private static final String REDIRECT_URI = "Your_Redirect_URI";
    private SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_artist = findViewById(R.id.edit_artist);
        edt_name = findViewById(R.id.edit_song);

        text_lyrics = findViewById(R.id.textView);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v){
                getLyrics();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");
                        // Now you can start interacting with App Remote
                        connected();

                    }

                    public void onFailure(Throwable throwable) {
                        Log.e("MyActivity", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    private void connected() {
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        edit_artist.setText(track.artist.name);
                        edt_name.setText(track.name);
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });

    }

    private void getLyrics(){
        String url = "https://api.lyrics.ovh/v1/" + edit_artist.getText().toString()+"/"+ edt_name.getText().toString();
        url = url.replace(" ","%20");
        Toast.makeText(getApplicationContext(),url,Toast.LENGTH_LONG).show();


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    text_lyrics.setText(response.getString("lyrics"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public  void onErrorResponse(VolleyError error){
                //Toast.makeText(getApplicationContext(), error.getMessage(),Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "Lyrics Didn't Found",Toast.LENGTH_SHORT).show();

            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}

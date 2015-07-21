package juanmeanwhile.org.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;


public class PlayerActivity extends ActionBarActivity {

    private static final String ARG_TRACK_ID= "track_id";
    private static final String ARG_TRACK_NAME = "track_name";
    private static final String ARG_ARTIST_NAME = "artist_name";
    private static final String ARG_ALBUM_NAME = "album_name";
    private static final String ARG_TRACK_IMG = "img";

    public static Intent newIntent(Context context, Track track){
        Intent intent = new Intent(context, PlayerActivity.class);

        Bundle args = new Bundle();
        args.putString(ARG_TRACK_ID, track.id);
        args.putString(ARG_TRACK_NAME, track.name);
        args.putString(ARG_ALBUM_NAME, track.album.name);

        ArrayList<String> artists = new ArrayList<String>();
        for (ArtistSimple artist : track.artists)
            artists.add(artist.name);

        args.putStringArrayList(ARG_ARTIST_NAME, artists);

        if (track.album.images.size() > 0)
            args.putString(ARG_TRACK_IMG, track.album.images.get(0).url);

        intent.putExtras(args);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //TODO launch dialog or load fragment according if we are on tablet or not
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, PlayerActivityFragment.newInstance(extras.getString(ARG_TRACK_ID),
                    extras.getString(ARG_TRACK_NAME), extras.getString(ARG_ARTIST_NAME), extras.getString(ARG_ALBUM_NAME), extras.getString(ARG_TRACK_IMG))).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

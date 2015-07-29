package juanmeanwhile.org.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import juanmeanwhile.org.spotifystreamer.data.ParcelableTrack;
import juanmeanwhile.org.spotifystreamer.fragment.PlayerFragment;
import kaaes.spotify.webapi.android.models.Track;


public class PlayerActivity extends ActionBarActivity {

    private static final String ARG_TRACK= "track";
    private static final String ARG_TRACK_LIST = "tracks";

    public static Intent newIntent(Context context, Track track, List<Track> topTenTracks){
        Intent intent = new Intent(context, PlayerActivity.class);

        Bundle args = new Bundle();
        args.putParcelable(ARG_TRACK, new ParcelableTrack(track));

        ArrayList<String> topTen = new ArrayList<String>();
        for (Track t : topTenTracks) {
            topTen.add(t.id);
        }
        intent.putExtra(ARG_TRACK_LIST, topTen);

        intent.putExtras(args);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        ArrayList<String> topTenTracksId = getIntent().getStringArrayListExtra(ARG_TRACK_LIST);

        //TODO launch dialog or load fragment according if we are on tablet or not
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, PlayerFragment.newInstance((ParcelableTrack)extras.getParcelable(ARG_TRACK), topTenTracksId)).commit();
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

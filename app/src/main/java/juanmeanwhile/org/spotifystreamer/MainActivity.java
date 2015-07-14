package juanmeanwhile.org.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import juanmeanwhile.org.spotifystreamer.fragment.ArtistFragment;
import juanmeanwhile.org.spotifystreamer.fragment.SearchFragment;
import kaaes.spotify.webapi.android.models.Artist;


public class MainActivity extends AppCompatActivity implements SearchFragment.OnSearchFragmentInteractonListener {

    private static final String TAG = "MainActivity";

    private boolean mTwoPane;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.fragment_artist_container)  != null) {
            mTwoPane = true;
        } else {
            mTwoPane = false;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onArtistSelected(Artist artist) {
        if (mTwoPane) {
            //load fragment into container
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_artist_container, ArtistFragment.newInstance(artist)).setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).addToBackStack(null).commit();

        } else {
            startActivity(ArtistActivity.newIntent(this, artist));
        }
    }
}

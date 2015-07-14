package juanmeanwhile.org.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import juanmeanwhile.org.spotifystreamer.fragment.ArtistFragment;
import kaaes.spotify.webapi.android.models.Artist;


public class ArtistActivity extends AppCompatActivity {

    private static final String TAG = "ArtistActivity";
    private static final String ARG_ARTIST_ID = "artistId";
    private static final String ARG_ARTIST_NAME = "artistName";


    private String mArtistId;
    private String mArtistName;

    public static Intent newIntent(Context context, Artist artist) {
        Intent intent = new Intent(context, ArtistActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString(ARG_ARTIST_ID, artist.id);
        bundle.putString(ARG_ARTIST_NAME, artist.name);

        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        //read args
        mArtistId = getIntent().getStringExtra(ARG_ARTIST_ID);
        mArtistName = getIntent().getStringExtra(ARG_ARTIST_NAME);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.artist_activity_title);
        actionBar.setSubtitle(mArtistName);
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            Artist artist = new Artist();
            artist.name = mArtistName;
            artist.id = mArtistId;
            getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.fragment_artist_container, ArtistFragment.newInstance(artist)).addToBackStack(null).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

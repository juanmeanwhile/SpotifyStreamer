package juanmeanwhile.org.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.android.MainThreadExecutor;
import retrofit.client.Response;


public class ArtistActivity extends AppCompatActivity {

    private static final String TAG = "ArtistActivity";
    private static final String ARG_ARTIST_ID = "artistId";
    private static final String ARG_ARTIST_NAME = "artistName";

    private static final String SAVED_NAMES = "saved_names";
    private static final String SAVED_IMGS = "saved_imgs";
    private static final String SAVED_ALBUMS = "saved_albums";

    private static final String COUNTRY = "SE";

    private String mArtistId;
    private String mArtistName;
    private RecyclerView mRecyclerView;
    private TextView mEmptyHint;
    private TrackAdapter mAdapter;

    protected SpotifyApi mApi;
    protected SpotifyService mSpotify;
    private ArrayList<Track> mTrackList;
    private String mCurrentSearch = "";

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

        //Init spotify API
        mApi = new SpotifyApi(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        mSpotify = mApi.getService();

        mRecyclerView = (RecyclerView) findViewById(R.id.song_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.list_divider));

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mEmptyHint = (TextView) findViewById(R.id.empty_hint);
        mEmptyHint.setVisibility(View.VISIBLE);

        searchTracks(mArtistId);
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

    private void searchTracks(String artistId) {
        mEmptyHint.setText(R.string.artist_tracks_getting);

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("country", COUNTRY);

        mSpotify.getArtistTopTrack(artistId, paramMap, new Callback<Tracks>() {
            @Override
            public void success(Tracks tracks, Response response) {
                setResults(tracks.tracks);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.error_default, Toast.LENGTH_SHORT).show();
                mEmptyHint.setText(R.string.get_tracks_error);
                mEmptyHint.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void setResults(List<Track> tracks) {
        if ((tracks == null || tracks.size() == 0)) {
            //show empty hint in case there is no returned results
            mEmptyHint.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyHint.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mAdapter = new TrackAdapter(tracks);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        if (mTrackList == null)
            return;

        //save name, id and image url as it is the only we need
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> imgs = new ArrayList<String>();
        ArrayList<String> albums = new ArrayList<String>();

        for (Track track : mTrackList) {
            names.add(track.name);
            imgs.add(track.album.images.size()>0?track.album.images.get(0).url:null);
            albums.add(track.album.name);
        }

        outState.putStringArrayList(SAVED_NAMES, names);
        outState.putStringArrayList(SAVED_IMGS, imgs);
        outState.putStringArrayList(SAVED_ALBUMS, albums);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        ArrayList<String> names = savedInstanceState.getStringArrayList(SAVED_NAMES);
        ArrayList<String> imgs = savedInstanceState.getStringArrayList(SAVED_IMGS);
        ArrayList<String> albums = savedInstanceState.getStringArrayList(SAVED_ALBUMS);

        if (names == null)
            return;

        mTrackList = new ArrayList<Track>();
        for (int i = 0; i < names.size(); i++) {
            Track track = new Track();
            track.name = names.get(i);

            //Album
            Album album = new Album();
            album.name = albums.get(0);

            //image
            album.images = new ArrayList<Image>();
            if (imgs.get(i) != null) {
                Image image = new Image();
                image.url = imgs.get(i);
                album.images.add(image);
            }
            track.album = album;

            mTrackList.add(track);
        }

        setResults(mTrackList);
    }

    public class TrackAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Track> mDataset;

        // Provide a suitable constructor (depends on the kind of dataset)
        public TrackAdapter(List<Track> tracks) {
            mDataset = tracks;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_track, parent, false);

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Track track = mDataset.get(position);
            holder.mName.setText(track.name);
            holder.mAlbum.setText(track.album.name);
            if (track.album.images.size() > 0)
                Picasso.with(ArtistActivity.this).load(track.album.images.get(0).url).placeholder(R.drawable.placeholder).into(holder.mPic);
        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }

    /**
     * Provides data model for each artist item
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mName;
        public TextView mAlbum;
        public ImageView mPic;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(R.id.name);
            mAlbum = (TextView) v.findViewById(R.id.album);
            mPic = (ImageView) v.findViewById(R.id.pic);
        }
    }

}

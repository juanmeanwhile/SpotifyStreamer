package juanmeanwhile.org.spotifystreamer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Image;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.android.MainThreadExecutor;
import retrofit.client.Response;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String SAVED_NAMES = "saved_names";
    private static final String SAVED_IMGS = "saved_imgs";
    private static final String SAVED_IDS = "saved_ids";
    private static final String SAVED_SEARCH = "saved_search";

    private RecyclerView mRecyclerView;
    private TextView mEmptyHint;
    private EditText mSearchField;

    protected SpotifyApi mApi;
    protected SpotifyService mSpotify;

    private ArtistAdapter mAdapter;
    private List<Artist> mArtistList;
    private String mCurrentSearch = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init spotify API
        mApi = new SpotifyApi(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        mSpotify = mApi.getService();

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Dont do anything
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0)
                    searchArtist(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                //don't do anything
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.artist_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, R.drawable.list_divider));

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mEmptyHint = (TextView) findViewById(R.id.empty_hint);
        mEmptyHint.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);

        if (mArtistList == null)
            return;

        //save name, id and image url as it is the only we need
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> imgs = new ArrayList<String>();
        ArrayList<String> ids = new ArrayList<String>();

        for (Artist artist : mArtistList) {
            names.add(artist.name);
            imgs.add(artist.images.size()>0?artist.images.get(0).url:null);
            ids.add(artist.id);
        }

        outState.putStringArrayList(SAVED_NAMES, names);
        outState.putStringArrayList(SAVED_IMGS, imgs);
        outState.putStringArrayList(SAVED_IDS, ids);
        outState.putString(SAVED_SEARCH, mCurrentSearch);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurrentSearch = savedInstanceState.getString(SAVED_SEARCH);
        ArrayList<String> names = savedInstanceState.getStringArrayList(SAVED_NAMES);
        ArrayList<String> imgs = savedInstanceState.getStringArrayList(SAVED_IMGS);
        ArrayList<String> ids = savedInstanceState.getStringArrayList(SAVED_IDS);

        if (names == null)
            return;

        mArtistList = new ArrayList<Artist>();
        for (int i = 0; i < names.size(); i++) {
            Artist artist = new Artist();
            artist.id = ids.get(i);
            artist.name = names.get(i);

            //image
            artist.images = new ArrayList<Image>();
            if (imgs.get(i) != null) {
                Image image = new Image();
                image.url = imgs.get(i);
                artist.images.add(image);
            }

            mArtistList.add(artist);
        }

        mSearchField.setText(mCurrentSearch);
        setResults(mArtistList);
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

    private void searchArtist(String artist) {
        mCurrentSearch = artist;

        mSpotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(ArtistsPager artistsPager, Response response) {
                setResults(artistsPager.artists.items);
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(getApplicationContext(), R.string.error_default, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setResults(List<Artist> artists) {
        mArtistList = artists;

        //show empty hint in case there is no returned results
        if (artists.size() == 0) {

            Toast.makeText(MainActivity.this, R.string.artist_search_error_not_found, Toast.LENGTH_SHORT).show();
            mEmptyHint.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyHint.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            mAdapter = new ArtistAdapter(artists);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    public class ArtistAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<Artist> mDataset;

        private View.OnClickListener mListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(ArtistActivity.newIntent(MainActivity.this, ((Artist) view.getTag())));
            }
        };

        // Provide a suitable constructor (depends on the kind of dataset)
        public ArtistAdapter(List<Artist> artist) {
            mDataset = artist;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_artist, parent, false);

            v.setOnClickListener(mListener);
            ViewHolder vh = new ViewHolder(v);

            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Artist artist = mDataset.get(position);
            holder.itemView.setTag(artist);
            holder.mName.setText(artist.name);
            if (artist.images.size() > 0)
                Picasso.with(MainActivity.this).load(artist.images.get(0).url).into(holder.mPic);
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
        public ImageView mPic;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(R.id.name);
            mPic = (ImageView) v.findViewById(R.id.pic);
        }
    }
}

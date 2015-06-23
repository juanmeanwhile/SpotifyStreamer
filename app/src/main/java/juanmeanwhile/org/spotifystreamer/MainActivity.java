package juanmeanwhile.org.spotifystreamer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private TextView mEmptyHint;
    private EditText mSearchField;

    protected SpotifyApi mApi;
    protected SpotifyService mSpotify;
    private SearchArtistTask mSearchTask;

    private ArtistAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init spotify API
        mApi = new SpotifyApi();
        mSpotify = mApi.getService();

        mSearchField = (EditText) findViewById(R.id.search_field);
        mSearchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Dont do anything
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 3) {
                    searchArtist(charSequence.toString());
                }
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
        if (mSearchTask != null)
            mSearchTask.cancel(true);

        mSearchTask = new SearchArtistTask();
        mSearchTask.execute(artist);
    }

    private void setResults(ArtistsPager artistPager) {
        //show empty hint in case there is no returned results
        if ((artistPager == null || artistPager.artists.items.size() == 0)) {
            mEmptyHint.setVisibility(View.VISIBLE);
        } else {
            mEmptyHint.setVisibility(View.GONE);

            mAdapter = new ArtistAdapter(artistPager.artists.items);
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

    private class SearchArtistTask extends AsyncTask<String, Void, ArtistsPager> {

        @Override
        protected ArtistsPager doInBackground(String... strings) {
            Log.d(TAG, "Searching for artist: " + strings[0]);
            return mSpotify.searchArtists(strings[0]);
        }

        @Override
        protected void onPostExecute(ArtistsPager artistsPager) {
            Log.d(TAG, "Success");
            setResults(artistsPager);
        }
    }

}

package juanmeanwhile.org.spotifystreamer.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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

import juanmeanwhile.org.spotifystreamer.DividerItemDecoration;
import juanmeanwhile.org.spotifystreamer.PlayerActivity;
import juanmeanwhile.org.spotifystreamer.R;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArtistFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArtistFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArtistFragment extends Fragment {


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

    private OnFragmentInteractionListener mListener;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param artist artist to show top tracks of.
     * @return A new instance of fragment ArtistFragment.
     */
    public static ArtistFragment newInstance(Artist artist) {
        ArtistFragment fragment = new ArtistFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ARTIST_ID, artist.id);
        args.putString(ARG_ARTIST_NAME, artist.name);

        fragment.setArguments(args);

        return fragment;
    }

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //read args
            mArtistId = getArguments().getString(ARG_ARTIST_ID);
            mArtistName = getArguments().getString(ARG_ARTIST_NAME);
        }

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);

        }

        //Init spotify API
        mApi = new SpotifyApi(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        mSpotify = mApi.getService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_artist, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.song_list);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.drawable.list_divider));

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mEmptyHint = (TextView) v.findViewById(R.id.empty_hint);
        mEmptyHint.setVisibility(View.VISIBLE);

        searchTracks(mArtistId);

        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                Toast.makeText(getActivity(), R.string.error_default, Toast.LENGTH_SHORT).show();
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
    public void onSaveInstanceState (Bundle outState){
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

    private void restoreInstanceState(Bundle savedInstanceState) {
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

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(PlayerActivity.newIntent(getActivity(), (Track)view.getTag()));
            }
        };

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

            v.setOnClickListener(onClickListener);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Track track = mDataset.get(position);
            holder.itemView.setTag(track);
            holder.mName.setText(track.name);
            holder.mAlbum.setText(track.album.name);
            if (track.album.images.size() > 0)
                Picasso.with(getActivity()).load(track.album.images.get(0).url).placeholder(R.drawable.placeholder).into(holder.mPic);
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


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}

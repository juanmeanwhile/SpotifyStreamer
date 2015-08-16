package juanmeanwhile.org.spotifystreamer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import juanmeanwhile.org.spotifystreamer.PlayerService;
import juanmeanwhile.org.spotifystreamer.R;
import juanmeanwhile.org.spotifystreamer.Utils;
import juanmeanwhile.org.spotifystreamer.data.ParcelableTrack;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import retrofit.android.MainThreadExecutor;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerFragment extends DialogFragment {

    private static final String TAG = "PlayerFragment";

    private static final String ARG_TRACK= "track";
    private static final String ARG_TRACK_LIST = "tracks";

    private static final String SAVED_TRACK = "track";
    private static final String SAVED_POSITION = "track";
    private static final String SAVED_PLAYING = "track";


    private TextView mArtist;
    private TextView mTrackName;
    private TextView mAlbum;
    private ImageView mPic;
    private SeekBar mSeekBar;
    private TextView mStartTime;
    private TextView mEndTime;
    private ImageButton mPrevButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;

    private int mPlayingIndex = 0;
    private ParcelableTrack mTrack;
    private boolean mIsPlaying = false;

    protected SpotifyApi mApi;
    protected SpotifyService mSpotify;
    private GetTrackAsyncTask mGetTask;


    //List of ids of the top ten tracks for this artist
    private ArrayList<String> mTopTenTracksId;

    //downloaded tracks will be stored here so we don't have to download them again
    private HashMap<String, Track> mTopTenTracks;

    public static DialogFragment newInstance(ParcelableTrack track, ArrayList<String> topten) {
        PlayerFragment fr = new PlayerFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_TRACK, track);
        args.putStringArrayList(ARG_TRACK_LIST, topten);

        fr.setArguments(args);
        return fr;
    }

    public PlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        //Init spotify API
        mApi = new SpotifyApi(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        mSpotify = mApi.getService();

        if (savedInstanceBundle == null)
            mTrack = getArguments().getParcelable(ARG_TRACK);
        else
            mTrack = savedInstanceBundle.getParcelable(ARG_TRACK);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);

        mArtist = (TextView) v.findViewById(R.id.artist);
        StringBuilder sb = new StringBuilder();
        if (mTrack.artists != null && mTrack.artists.size() > 0) {
            sb.append(mTrack.artists.get(0));
            for (int i = 1; i < mTrack.artists.size(); i++) {
                sb.append(",");
                sb.append(mTrack.artists.get(i));
            }
        }

        mTopTenTracksId = getArguments().getStringArrayList(ARG_TRACK_LIST);
        mTopTenTracks = new HashMap<>(10);

        //get current index of the given track in the list
        for (int i = 0; i < mTopTenTracksId.size(); i++){
            if (mTopTenTracksId.get(i).equals(mTrack.id)){
                mPlayingIndex = i;
            }
        }

        mArtist.setText(sb.toString());
        mTrackName = (TextView) v.findViewById(R.id.title);
        mAlbum = (TextView) v.findViewById(R.id.album);
        mPic = (ImageView) v.findViewById(R.id.pic);
        mSeekBar = (SeekBar) v.findViewById(R.id.seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    PlayerService.startService(getActivity(), PlayerService.Action.SEEK_TO, null, progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mStartTime = (TextView) v.findViewById(R.id.current_time);
        mEndTime = (TextView) v.findViewById(R.id.end_time);

        mPrevButton = (ImageButton) v.findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPreviousClick(view);
            }
        });

        mPlayButton = (ImageButton) v.findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlayClick(view);
            }
        });

        mNextButton = (ImageButton)v.findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextClick(view);
            }
        });

        setTrackData(mTrack);

        //Listen to PlayerService events
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerService.BROADCAST_PLAYING);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, intentFilter);

        if (savedInstanceState == null) {
            loadSong(mTrack.preview_url);
        } else {
            //Recreating fragment. Service has been already started, so ask for status and adapt player status (playing or not)
            //according to the result
            PlayerService.startService(getActivity(), PlayerService.Action.STATUS, null, 0);
        }
        return v;
    }

    @Override
    public void onStart(){
        super.onStart();


    }

    @Override
    public void onStop(){
        super.onStop();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int position = intent.getIntExtra(PlayerService.ARG_POSITION, 0);
            int duration = intent.getIntExtra(PlayerService.ARG_DURATION, 0);
            mIsPlaying = intent.getBooleanExtra(PlayerService.ARG_IS_PLAYING, true);

            setControls(duration, mIsPlaying);

            updateTime(position);

        }
    };

    private void loadTrack(String trackId) {

        if (mTopTenTracks.containsKey(trackId)){
            Track track = mTopTenTracks.get(trackId);
            setTrackData(track);

            loadSong(track.preview_url);
        } else {
            setControlsIndeterminate(true);

            //mark text as loading
            mArtist.setText("...");
            mTrackName.setText(R.string.load_track_loading);
            mAlbum.setText("...");
            mPic.setImageResource(R.drawable.placeholder);

            //get track info
            if (mGetTask != null)
                mGetTask.cancel(true);

            mGetTask = new GetTrackAsyncTask();
            mGetTask.execute(trackId);
        }
    }

    private void setTrackData(Track track) {
        mTrack = new ParcelableTrack(track);

        StringBuilder sb = new StringBuilder();
        sb.append(track.artists.get(0).name);
        for (int i = 1; i < track.artists.size(); i++){
            sb.append(", ");
            sb.append(track.artists.get(i).name);
        }
        mArtist.setText(sb.toString());
        mTrackName.setText(track.name);
        mAlbum.setText(track.album.name);
        if (track.album.images != null && track.album.images.size() > 0) {
            Picasso.with(getActivity()).load(track.album.images.get(0).url).placeholder(R.drawable.placeholder).into(mPic);
        }
    }

    private void setControls(int duration, boolean playing){
        setControlsIndeterminate(false);
        mPlayButton.setImageResource(playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);

        mSeekBar.setMax(duration);
        mEndTime.setText(duration / 60000 + ":" + duration / 1000);
    }

    private void setControlsIndeterminate(boolean indeterminate){
        Log.d(TAG, "Setting controls to: " + indeterminate);

        mSeekBar.setIndeterminate(indeterminate);
        mEndTime.setText(indeterminate ? getString(R.string.player_time_loading) : "00:00");
        mStartTime.setText(indeterminate ? getString(R.string.player_time_loading) : "00:00");

        //mPlayButton.setEnabled(!indeterminate);
        //mNextButton.setEnabled(!indeterminate);
        //mPrevButton.setEnabled(!indeterminate);
    }

    private void updateTime(long currentTime) {

        // Displaying time completed playing
        mStartTime.setText("" + Utils.milliSecondsToTimer(currentTime));

        // Updating progress bar
        mSeekBar.setProgress((int) currentTime);

        if (mSeekBar.isIndeterminate())
            mSeekBar.setIndeterminate(false);
    }


    private void loadSong(String url){
        setControlsIndeterminate(true);

        //Send load song to service
        Log.d(TAG, "Ask service to load a new song");
        PlayerService.startService(getActivity(), PlayerService.Action.PLAY, url, 0);
    }

    public void onPreviousClick(View v){
        mIsPlaying = false;

        if (mPlayingIndex > 0)
            mPlayingIndex--;
        else
            mPlayingIndex = mTopTenTracksId.size()-1;

        loadTrack(mTopTenTracksId.get(mPlayingIndex));
    }

    public void onPlayClick(View v){
        if ( mIsPlaying){
            //stop playing
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);

            //send pause to service
            PlayerService.startService(getActivity(), PlayerService.Action.PAUSE, null, 0);

            mIsPlaying = false;

        } else {
            //resume playing
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);

            //send resume to service
            PlayerService.startService(getActivity(), PlayerService.Action.RESUME, null, 0);
        }
    }

    public void onNextClick(View v){
        mIsPlaying = false;

        if (mPlayingIndex < mTopTenTracksId.size())
            mPlayingIndex++;
        else
            mPlayingIndex = 0;

        loadTrack(mTopTenTracksId.get(mPlayingIndex));
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(SAVED_TRACK, mTrack);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mGetTask != null)
            mGetTask.cancel(true);
    }

    /**
     * Task to download a track that can be canceled
     */
    public class GetTrackAsyncTask extends AsyncTask<String, Void, Track>{

        @Override
        protected Track doInBackground(String... strings) {
            try {
                return mSpotify.getTrack(strings[0]);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.toString());
            }

            return null;
        }

        @Override
        public void onPostExecute(Track track) {
            if (track != null) {
                //save track data for later
                mTopTenTracks.put(track.id, track);

                setTrackData(track);
                loadSong(track.preview_url);
            } else {
                Toast.makeText(getActivity(), R.string.load_track_error, Toast.LENGTH_LONG).show();
            }
        }
    }

}

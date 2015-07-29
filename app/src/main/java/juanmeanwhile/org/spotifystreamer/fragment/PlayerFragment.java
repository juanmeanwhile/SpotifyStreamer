package juanmeanwhile.org.spotifystreamer.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

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

    private TextView mArtist;
    private TextView mTrackName;
    private TextView mAlbum;
    private ImageView mPic;
    private ProgressBar mProgress;
    private TextView mStartTime;
    private TextView mEndTime;
    private ImageButton mPrevButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;

    MediaPlayer mMediaPlayer;
    Handler mHandler;
    private int mPlayingIndex = 0;
    private ParcelableTrack mTrack;

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
        mHandler = new Handler();
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
        mProgress = (ProgressBar) v.findViewById(R.id.progress_bar);
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
        loadSong(mTrack.preview_url);

        return v;
    }

    private void loadTrack(String trackId) {

        if (mTopTenTracks.containsKey(trackId)){
            Track track = mTopTenTracks.get(trackId);
            setTrackData(track);

            loadSong(track.preview_url);
        } else {
            //mark as loading
            mProgress.setIndeterminate(true);
            mEndTime.setText(R.string.player_time_loading);
            mStartTime.setText(R.string.player_time_loading);
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

    private void loadSong(String url){
        mProgress.setIndeterminate(true);
        mEndTime.setText(R.string.player_time_loading);
        mStartTime.setText(R.string.player_time_loading);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                Log.d(TAG, "onPrepared()");
                mProgress.setIndeterminate(false);
                mProgress.setMax(mMediaPlayer.getDuration());
                mEndTime.setText(mMediaPlayer.getDuration() / 60000 + ":" + mMediaPlayer.getDuration() / 1000);
                mPlayButton.setEnabled(true);

                onPlayClick(null);
            }
        });

        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.media_prepare_error, Toast.LENGTH_LONG).show();
        }
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                long totalDuration = mMediaPlayer.getDuration();
                long currentDuration = mMediaPlayer.getCurrentPosition();

                // Displaying time completed playing
                mStartTime.setText("" + Utils.milliSecondsToTimer(currentDuration));

                // Updating progress bar
                mProgress.setProgress((int) currentDuration);

                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
            }
        }
    };

    public void onPreviousClick(View v){
        mMediaPlayer.stop();

        if (mPlayingIndex > 0)
            mPlayingIndex--;
        else
            mPlayingIndex = mTopTenTracksId.size()-1;

        loadTrack(mTopTenTracksId.get(mPlayingIndex));
    }

    public void onPlayClick(View v){
        if ( mMediaPlayer.isPlaying()){
            //stop playing
            mPlayButton.setImageResource(android.R.drawable.ic_media_play);
            mMediaPlayer.pause();

        } else {
            //resume playing
            mPlayButton.setImageResource(android.R.drawable.ic_media_pause);
            mMediaPlayer.start();

            mUpdateTimeTask.run();
        }
    }

    public void onNextClick(View v){
        mMediaPlayer.stop();

        if (mPlayingIndex < mTopTenTracksId.size())
            mPlayingIndex++;
        else
            mPlayingIndex = 0;

        loadTrack(mTopTenTracksId.get(mPlayingIndex));
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(ARG_TRACK, mTrack);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mGetTask != null)
            mGetTask.cancel(true);

        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /**
     * Task to download a track that can be canceled
     */
    public class GetTrackAsyncTask extends AsyncTask<String, Void, Track>{

        @Override
        protected Track doInBackground(String... strings) {
            return mSpotify.getTrack(strings[0]);
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

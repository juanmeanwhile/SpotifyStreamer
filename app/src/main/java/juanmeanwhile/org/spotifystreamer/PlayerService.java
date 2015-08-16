package juanmeanwhile.org.spotifystreamer;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener {

    public enum Action {
        PLAY, PAUSE, SEEK_TO, RESUME, STATUS
    }

    private static final String TAG = "PlayerService";
    private static final String ACTION_PLAY = "play";
    private static final String ACTION_PAUSE = "pause";
    private static final String ACTION_SEEK_TO = "seekTo";
    private static final String ACTION_RESUME = "resume";
    private static final String ACTION_STATUS = "status";

    public static final String BROADCAST_PLAYING = "playing";

    //Used in startService
    private static final String ARG_URL = "url";

    //Used in both startService and broadcast
    public static final String ARG_POSITION = "position";

    //Used to broadcast
    public static final String ARG_DURATION = "duration";
    public static final String ARG_IS_PLAYING = "isPlaying";

    private Handler mHandler;
    MediaPlayer mMediaPlayer;
    private boolean mPrepared = false;

    /**
     * Commincate with the Service indicating an action to be realized by the Service. Starts the service if needed
     * @param context Application context
     * @param action kind of action to be performed
     * @param url Url of the tracks, only needed when requesting a play action
     * @param trackPosition positon of the track in millis, required when requesting a seek to action
     */
    public static void startService(Context context, Action action, String url, int trackPosition) {
        Intent intent = new Intent(context, PlayerService.class);
        switch (action){
            case PLAY:
                intent.setAction(ACTION_PLAY);
                intent.putExtra(ARG_URL, url);
                break;
            case RESUME:
                intent.setAction(ACTION_RESUME);
                break;
            case PAUSE:
                intent.setAction(ACTION_PAUSE);
                break;
            case SEEK_TO:
                intent.setAction(ACTION_SEEK_TO);
                intent.putExtra(ARG_POSITION, trackPosition);
                break;
            case STATUS:
                intent.setAction(ACTION_STATUS);
                break;
        }
        context.startService(intent);

    }

    public PlayerService() {

        mHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        if (intent.getAction().equals(ACTION_PLAY)) {
            String url = intent.getStringExtra(ARG_URL);
            loadSong(url);

        } else if (intent.getAction().equals(ACTION_PAUSE)){
            mMediaPlayer.pause();

        } else if (intent.getAction().equals(ACTION_RESUME)) {
            mMediaPlayer.start();

        }  else if (intent.getAction().equals(ACTION_SEEK_TO)) {
            int pos = intent.getIntExtra(ARG_POSITION, 0);
            mMediaPlayer.seekTo(pos);

        } else if (intent.getAction().equals(ACTION_STATUS)) {
            //send broadcast with status
            Intent brIntent = new Intent();
            brIntent.setAction(BROADCAST_PLAYING);
            brIntent.putExtra(ARG_DURATION, mMediaPlayer.getDuration());
            brIntent.putExtra(ARG_POSITION, mMediaPlayer.getCurrentPosition());
            brIntent.putExtra(ARG_IS_PLAYING, mMediaPlayer.isPlaying());

            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(brIntent);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        onHandleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void loadSong(String url) {
        if (mMediaPlayer != null)
            mMediaPlayer.stop();

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);

        try {
            mPrepared = false;
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.media_prepare_error, Toast.LENGTH_LONG).show();
        }
    }

    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "onPrepared()");

        mPrepared = true;

        mMediaPlayer.start();

        mUpdateTimeTask.run();
    }

    private Runnable mUpdateTimeTask = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                long currentDuration = mMediaPlayer.getCurrentPosition();

                //Broadcast duration and current position
                Intent intent = new Intent();
                intent.setAction(BROADCAST_PLAYING);
                intent.putExtra(ARG_DURATION, mMediaPlayer.getDuration());
                intent.putExtra(ARG_POSITION, mMediaPlayer.getCurrentPosition());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 200);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();

        if (mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
    }
}
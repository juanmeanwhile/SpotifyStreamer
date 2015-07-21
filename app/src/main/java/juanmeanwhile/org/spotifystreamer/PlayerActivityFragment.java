package juanmeanwhile.org.spotifystreamer;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;


/**
 * A placeholder fragment containing a simple view.
 */
public class PlayerActivityFragment extends Fragment {


    private static final String ARG_TRACK_ID= "track_id";
    private static final String ARG_TRACK_NAME = "track_name";
    private static final String ARG_ARTIST_NAME = "artist_name";
    private static final String ARG_ALBUM_NAME = "album_name";
    private static final String ARG_TRACK_IMG = "img";



    private TextView mArtist;
    private TextView mTrack;
    private TextView mAlbum;
    private ImageView mPic;
    private ProgressBar mProgress;
    private TextView mStartTime;
    private TextView mEndTime;
    private ImageButton mPrevButton;
    private ImageButton mPlayButton;
    private ImageButton mNextButton;



    public static Fragment newInstance(String trackId, String trackName, String artistName, String albumName, String imgUrl) {
        PlayerActivityFragment fr = new PlayerActivityFragment();

        Bundle args = new Bundle();
        args.putString(ARG_TRACK_ID, trackId);
        args.putString(ARG_TRACK_NAME, trackName);
        args.putString(ARG_ALBUM_NAME, albumName);

        args.putString(ARG_ARTIST_NAME, artistName);
        args.putString(ARG_TRACK_IMG, imgUrl);

        fr.setArguments(args);
        return fr;
    }

    public PlayerActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_player, container, false);

        mArtist = (TextView) v.findViewById(R.id.artist);
        StringBuilder sb = new StringBuilder(getArguments().getStringArrayList(ARG_ARTIST_NAME).get(0));
        for (int i= 1; i < getArguments().getStringArrayList(ARG_ARTIST_NAME).size(); i++) {
            sb.append(",");
            sb.append(getArguments().getStringArrayList(ARG_ARTIST_NAME).get(i));
        }
        mArtist.setText(sb.toString());

        mTrack = (TextView) v.findViewById(R.id.title);
        mTrack.setText(getArguments().getString(ARG_TRACK_NAME));

        mAlbum = (TextView) v.findViewById(R.id.album);
        mAlbum.setText(getArguments().getString(ARG_ALBUM_NAME));

        mPic = (ImageView) v.findViewById(R.id.pic);
        String url = getArguments().getString(ARG_TRACK_IMG);
        if (url != null) {
            Picasso.with(getActivity()).load(url).placeholder(R.drawable.placeholder).into(mPic);
        }

        mProgress = (ProgressBar) v.findViewById(R.id.progress_bar);

        mStartTime = (TextView) v.findViewById(R.id.current_time);

        mEndTime = (TextView) v.findViewById(R.id.end_time);

        mPrevButton = (ImageButton) v.findViewById(R.id.prev_button);
        mPlayButton = (ImageButton) v.findViewById(R.id.play_button);
        mNextButton = (ImageButton)v.findViewById(R.id.next_button);

        return v;
    }
}

package juanmeanwhile.org.spotifystreamer.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.concurrent.Executors;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import retrofit.android.MainThreadExecutor;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    protected SpotifyApi mApi;
    protected SpotifyService mSpotify;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        //Init spotify API
        mApi = new SpotifyApi(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        mSpotify = mApi.getService();
    }
}

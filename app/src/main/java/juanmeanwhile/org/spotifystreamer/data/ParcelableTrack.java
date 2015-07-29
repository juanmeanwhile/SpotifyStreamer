package juanmeanwhile.org.spotifystreamer.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Juan on 29/07/2015.
 */
public class ParcelableTrack extends Track implements Parcelable{

    public ParcelableTrack(Track track) {
        id = track.id;
        name = track.name;
        preview_url = track.preview_url;
        album = track.album;
        artists = track.artists;
    }

    public static final Parcelable.Creator<ParcelableTrack> CREATOR
            = new Parcelable.Creator<ParcelableTrack>() {
        public ParcelableTrack createFromParcel(Parcel in) {
            return new ParcelableTrack(in);
        }

        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    private ParcelableTrack(Parcel in) {
        id = in.readString();
        name = in.readString();
        preview_url = in.readString();

        album = new Album();
        album.name = in.readString();

        album.images = new ArrayList<Image>();
        Image img = new Image();
        img.url = in.readString();
        if (!img.url.equals(""))
            album.images.add(img);


        List<String> artistList = new ArrayList<String>();
        in.readStringList(artistList);

        artists = new ArrayList<ArtistSimple>(artistList.size());
        for (String a : artistList){
            ArtistSimple as = new ArtistSimple();
            as.name = a;
            artists.add(as);
        }


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(preview_url);
        parcel.writeString(album.name);
        if (album.images.size() > 0)
            parcel.writeString(album.images.get(0).url);
        else
            parcel.writeString("");

        ArrayList<String> artistList = new ArrayList<String>();
        for (ArtistSimple artist : artists) {
            artistList.add(artist.name);
        }
        parcel.writeStringList(artistList);
    }

}

package violentapplications.asterixobelixsoundboard;

import android.net.Uri;

/**
 * Created by Sebastian on 2017-09-19.
 */

public class Sound {
    private String name;
    private Uri uri;

    public Sound(String name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }
}

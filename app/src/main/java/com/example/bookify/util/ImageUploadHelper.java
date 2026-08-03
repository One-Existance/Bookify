package com.example.bookify.util;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

/**
 * Event images are picked as a local content:// URI, which is meaningless off the
 * device that picked it. This uploads that file to Firebase Storage and returns a
 * durable https:// download URL that any device can load (via Glide - plain
 * ImageView.setImageURI only resolves local URIs, not remote ones).
 */
public class ImageUploadHelper {

    private static final String STORAGE_FOLDER = "event_images";

    private ImageUploadHelper() {}

    /**
     * Resolves whatever the event form is currently holding into a final URL to save.
     * Empty, or already-uploaded (http/https) values are returned as-is with no network
     * call - this only uploads when it's a fresh local pick, so re-saving an event
     * without touching its image never re-uploads.
     */
    public static Task<String> resolveImageUrl(Context context, String localOrRemote) {
        if (TextUtils.isEmpty(localOrRemote) || localOrRemote.startsWith("http")) {
            return Tasks.forResult(localOrRemote);
        }

        Uri localUri = Uri.parse(localOrRemote);
        String mimeType = context.getContentResolver().getType(localUri);
        String extension = mimeType != null ? MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) : null;
        if (extension == null) extension = "jpg";

        StorageReference ref = FirebaseStorage.getInstance().getReference()
                .child(STORAGE_FOLDER + "/" + UUID.randomUUID() + "." + extension);

        return ref.putFile(localUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) throw task.getException();
                    return ref.getDownloadUrl();
                })
                .continueWith(task -> task.getResult().toString());
    }
}

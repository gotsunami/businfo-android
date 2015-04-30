package com.monnerville.transports.herault.core;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.monnerville.transports.herault.R;
import static com.monnerville.transports.herault.core.Application.TAG;
import java.io.File;

/**
 *
 * @author matm
 */
public class UpdateManager {
	// Schedules download id through download manager
	private final String ARCHIVE_NAME = "update.tar.bz2";
	private final Context mContext;
    private final ProgressDialog mDialog;

	public UpdateManager(Context ctx) {
		mContext = ctx;

        mDialog = new ProgressDialog(ctx);
        mDialog.setMessage(mContext.getString(R.string.download_title));
        mDialog.setIndeterminate(false);
        mDialog.setMax(100);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(true);
	}

	private void extractArchive() {
		// TODO
	}

	public void download() {
        new DownloadFileTask().execute(mContext.getString(R.string.network_update_uri));
	}

    // Async download task
    private class DownloadFileTask extends AsyncTask<String, String, Exception> {
        @Override
        protected void onPreExecute() {
            // UI thread
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected Exception doInBackground(String... url) {
            try {
                // Synchronous download
                Ion.with(mContext).load(url[0])
                    .progress(new ProgressCallback() {
                        @Override
                        public void onProgress(long downloaded, long total) {
                            publishProgress(""+(int)((total*100)/downloaded));
                        }
                    })
                    .write(new File(mContext.getFilesDir(), ARCHIVE_NAME))
                    .get();
            } catch (Exception e) {
                return e;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            // UI thread
            // setting progress percentage
            mDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(Exception e) {
            // UI thread
            mDialog.dismiss();
            if (e != null) {
                // TODO: show an error dialog
                Log.e(TAG, "AN ERROR OCCURRED DURING DOWNLOAD: " + e);
            }
        }
    }
}

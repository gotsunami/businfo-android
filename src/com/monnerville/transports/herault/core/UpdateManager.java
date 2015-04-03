package com.monnerville.transports.herault.core;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import static android.content.Context.DOWNLOAD_SERVICE;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.monnerville.transports.herault.R;
import static com.monnerville.transports.herault.core.Application.TAG;
import java.io.File;

/**
 *
 * @author matm
 */
public class UpdateManager {
	// Schedules download id through download manager
	private long mEnqueue;
	private DownloadManager dm;
	private final BroadcastReceiver mReceiver;

	private final String ARCHIVE_NAME = "update.tar.bz2";
	private final Context mContext;
	private String mArchiveLocalPath;

	public UpdateManager(Context ctx) {
		mContext = ctx;
		mReceiver = newReceiver();
	}

	private BroadcastReceiver newReceiver() {
		return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    Query query = new Query();
                    query.setFilterById(mEnqueue);
					Cursor c = dm.query(query);
					if (c.moveToFirst()) {
                        int idx = c .getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c .getInt(idx)) {
                            mArchiveLocalPath = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        }
                    }
                }
            }
        };
	}

	private void extractArchive() {
		// TODO
	}

	public void download() {
		Uri u = Uri.parse(mContext.getString(R.string.network_update_uri));
		DownloadManager.Request m = new DownloadManager.Request(u);
		m.setTitle("Downloading new schedules");
		m.setDescription("Yeah man!");
		m.setVisibleInDownloadsUi(true);
		File f = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), ARCHIVE_NAME);
		m.setDestinationUri(Uri.fromFile(f));
		dm = (DownloadManager)mContext.getSystemService(DOWNLOAD_SERVICE);
		mEnqueue = dm.enqueue(m);
	}

	public BroadcastReceiver receiver() {
		return mReceiver;
	}
}

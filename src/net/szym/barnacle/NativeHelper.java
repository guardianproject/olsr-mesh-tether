package net.szym.barnacle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class NativeHelper {
	public static final String TAG = "NativeHelper";

	public static File app_bin;
	public static File app_log;

    static String SU_C;
    static String RUN;
    static String OLSRD;
    static String WIFI;
    static String SET_NET_DNS1;

	public static void setup(Context context) {
		app_bin = context.getDir("bin", Context.MODE_PRIVATE).getAbsoluteFile();
		app_log = context.getDir("log", Context.MODE_PRIVATE).getAbsoluteFile();
		SU_C = new File(app_bin, "su_c").getAbsolutePath();
		RUN = new File(app_bin, "run").getAbsolutePath();
		OLSRD = new File(app_bin, "olsrd").getAbsolutePath();
		WIFI = new File(app_bin, "wifi").getAbsolutePath();
		SET_NET_DNS1 = new File(app_bin, "set_net_dns1").getAbsolutePath();
	}

	public static boolean unzipAssets(Context context) {
		boolean result = true;
		try {
			AssetManager am = context.getAssets();
			final String[] assetList = am.list("");

			for (String asset : assetList) {
				if (asset.equals("images") || asset.equals("sounds")
						|| asset.equals("webkit"))
					continue;

				int BUFFER = 2048;
				final File file = new File(NativeHelper.app_bin, asset);
				final InputStream assetIS = am.open(asset);

				if (file.exists()) {
					file.delete();
					Log.i(BarnacleApp.TAG, "DebiHelper.unzipDebiFiles() deleting "
							+ file.getAbsolutePath());
				}

				FileOutputStream fos = new FileOutputStream(file);
				BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);

				int count;
				byte[] data = new byte[BUFFER];

				while ((count = assetIS.read(data, 0, BUFFER)) != -1) {
					dest.write(data, 0, count);
				}

				dest.flush();
				dest.close();

				assetIS.close();
			}
		} catch (IOException e) {
			result = false;
			Log.e(BarnacleApp.TAG, "Can't unzip", e);
		}
		chmod("0750", new File(SU_C));
		chmod("0750", new File(RUN));
		chmod("0750", new File(OLSRD));
		chmod("0750", new File(WIFI));
		chmod("0750", new File(SET_NET_DNS1));
		chmod("0750", new File(app_bin, "script_aria"));
		chmod("0750", new File(app_bin, "script_hero"));
		chmod("0750", new File(app_bin, "script_samsung"));
		return result;
	}

	public static void chmod(String modestr, File path) {
		Log.i(TAG, "chmod " + modestr + " " + path.getAbsolutePath());
		try {
			Class<?> fileUtils = Class.forName("android.os.FileUtils");
			Method setPermissions = fileUtils.getMethod("setPermissions", String.class,
					int.class, int.class, int.class);
			int mode = Integer.parseInt(modestr, 8);
			int a = (Integer) setPermissions.invoke(null, path.getAbsolutePath(), mode,
					-1, -1);
			if (a != 0) {
				Log.i(TAG, "ERROR: android.os.FileUtils.setPermissions() returned " + a
						+ " for '" + path + "'");
			}
		} catch (ClassNotFoundException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (IllegalAccessException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (InvocationTargetException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		} catch (NoSuchMethodException e) {
			Log.i(TAG, "android.os.FileUtils.setPermissions() failed:", e);
		}
	}
}

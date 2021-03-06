package nl.giovanniterlingen.whatsapp;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class MessageService extends Service {

	public static final String ACTION_SEND_MSG = "send_msg";
	public static final String ACTION_START_COMPOSING = "start_composing";
	public static final String ACTION_STOP_COMPOSING = "stop_composing";
	public static final String ACTION_SHOW_ONLINE = "show_online";
	public static final String ACTION_SHOW_OFFLINE = "show_offline";
	public static final String ACTION_SET_STATUS = "set_status";
	public static final String ACTION_GET_LAST_SEEN = "get_last_seen";
	public static final String ACTION_SEND_READ = "send_read";
	public static final String ACTION_SEND_IMAGE = "send_image";
	public static final String ACTION_GET_AVATAR = "get_avatar";
	private WhatsApi wa;

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == ACTION_SEND_MSG) {
				try {
					wa.sendMessage(intent.getStringExtra("to"),
							intent.getStringExtra("msg"));
				} catch (WhatsAppException | InvalidKeyException
						| NoSuchAlgorithmException | IOException
						| IncompleteMessageException | InvalidMessageException
						| InvalidTokenException | JSONException
						| DecodeException e) {
					Toast.makeText(MessageService.this,
							"Caught exception: " + e.getMessage(),
							Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}

			if (intent.getAction() == ACTION_START_COMPOSING) {
				try {
					wa.sendMessageComposing(intent.getStringExtra("to"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_STOP_COMPOSING) {
				try {
					wa.sendMessagePaused(intent.getStringExtra("to"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SHOW_ONLINE) {
				try {
					wa.sendActiveStatus();
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SHOW_OFFLINE) {
				try {
					wa.sendOfflineStatus();
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SET_STATUS) {
				try {
					wa.sendStatusUpdate(intent.getStringExtra("status"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_GET_LAST_SEEN) {
				try {
					wa.sendPresenceSubscription(intent.getStringExtra("to"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SEND_READ) {
				try {
					wa.sendMessageRead(intent.getStringExtra("to"),
							intent.getStringExtra("id"));
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_SEND_IMAGE) {
				try {
					File image = new File(intent.getStringExtra("path"));
					File preview = BitmapHelper.createIcon(image.getPath(),
							MessageService.this);
					wa.sendMessageImage(intent.getStringExtra("to"), image,
							preview, "");
				} catch (WhatsAppException | IOException e) {
					e.printStackTrace();
				}
			}
			if (intent.getAction() == ACTION_GET_AVATAR) {
				try {
					wa.sendGetProfilePicture(intent.getStringExtra("to"), false);
				} catch (WhatsAppException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SEND_MSG);
		filter.addAction(ACTION_START_COMPOSING);
		filter.addAction(ACTION_STOP_COMPOSING);
		filter.addAction(ACTION_SHOW_ONLINE);
		filter.addAction(ACTION_SHOW_OFFLINE);
		filter.addAction(ACTION_SET_STATUS);
		filter.addAction(ACTION_GET_LAST_SEEN);
		filter.addAction(ACTION_SEND_READ);
		filter.addAction(ACTION_SEND_IMAGE);
		filter.addAction(ACTION_GET_AVATAR);
		registerReceiver(broadcastReceiver, filter);
		startService();
		return START_STICKY;
	}

	private void startService() {

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(MessageService.this);
		try {
			wa = new WhatsApi(MessageService.this, preferences.getString(
					"number", ""), preferences.getString("username", ""));

			wa.connect();
			wa.loginWithPassword(preferences.getString("pw", ""));
			wa.sendOfflineStatus();

		} catch (Exception e) {
			Toast.makeText(MessageService.this,
					"Caught exception: " + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
			e.printStackTrace();
			wa.disconnect();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
package nl.giovanniterlingen.whatsapp;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.MediaColumns;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.LayoutParams;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Conversations extends AppCompatActivity {

	public static final String SET_NOTIFY = "set_notify";
	public static final String SET_LAST_SEEN = "set_last_seen";
	public static final String SET_PROFILE_PICTURE = "set_profile_picture";
	public static final IntentFilter INTENT_FILTER = createIntentFilter();

	private setNotifyReceiver setNotifyReceiver = new setNotifyReceiver();
	private SQLiteDatabase newDB;
	ImageButton sButton, attachmentButton, attachPhoto, attachImage,
			attachVideo, attachAudio, attachPosition, attachContact;

	String nEdit;
	String contact;
	EditText mEdit;
	HorizontalScrollView attachmentPicker;
	private static int RESULT_LOAD_IMAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);
		registerReceiver(setNotifyReceiver, INTENT_FILTER);

		RelativeLayout relativelayout = (RelativeLayout) findViewById(R.id.relativelayout);
		Drawable drawable = getResources().getDrawable(R.drawable.background);

		relativelayout.setBackground(drawable);

		LayoutParams layout = new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		View mView = getLayoutInflater().inflate(R.layout.conversation_header,
				null);

		final ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setCustomView(mView, layout);
		}

		sButton = (ImageButton) findViewById(R.id.send_button);
		mEdit = (EditText) findViewById(R.id.message_text);
		attachmentButton = (ImageButton) findViewById(R.id.attachment_button);
		attachmentPicker = (HorizontalScrollView) findViewById(R.id.attachment_picker);

		attachPhoto = (ImageButton) findViewById(R.id.attach_photo);
		attachPhoto.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
				startActivity(intent);
			}
		});
		attachImage = (ImageButton) findViewById(R.id.attach_image);
		attachImage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(
						Intent.ACTION_PICK,
						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, RESULT_LOAD_IMAGE);
			}
		});
		attachVideo = (ImageButton) findViewById(R.id.attach_video);
		attachVideo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent("android.media.action.VIDEO_CAPTURE");
				startActivity(intent);
			}
		});
		attachAudio = (ImageButton) findViewById(R.id.attach_audio);
		attachAudio.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		attachPosition = (ImageButton) findViewById(R.id.attach_position);
		attachPosition.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://maps.google.com/maps?"));
				startActivity(intent);
			}
		});
		attachContact = (ImageButton) findViewById(R.id.attach_contact);
		attachContact.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});

		Intent intent = getIntent();
		if (intent.hasExtra("numberpass")) {
			String number = intent.getExtras().getString("numberpass");
			nEdit = number;
		}

		if (!nEdit.contains("@")) {
			// check if group message
			if (nEdit.contains("-")) {
				// to group
				contact = nEdit + "@g.us";
			} else {
				// to normal user
				contact = nEdit + "@s.whatsapp.net";
			}

			File file = new File(Conversations.this.getFilesDir().getParent()
					+ File.separator + "Avatars" + File.separator + contact
					+ ".jpg");

			Intent i1 = new Intent();
			i1.setAction(MessageService.ACTION_GET_AVATAR);
			i1.putExtra("to", nEdit);
			sendBroadcast(i1);

			if (file.exists()) {

				Bitmap avatar = BitmapHelper.getRoundedBitmap(BitmapFactory
						.decodeFile(file.getAbsolutePath()));
				ImageView image = (ImageView) findViewById(R.id.contact_photo);
				image.setImageBitmap(avatar);

			}

			// get last seen
			Intent i = new Intent();
			i.setAction(MessageService.ACTION_GET_LAST_SEEN);
			i.putExtra("to", nEdit);
			sendBroadcast(i);

			String contactname = ContactsHelper.getContactName(
					Conversations.this, nEdit);
			TextView contact_name = (TextView) findViewById(R.id.contact_name);

			if (contactname != null) {
				contact_name.setText(contactname);
			} else {
				contact_name.setText(nEdit);
			}

			getMessages();

			sButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					String to = nEdit.toString();
					String message = mEdit.getText().toString();

					if (message.isEmpty()) {
						return;

					} else {
						Intent i = new Intent();
						i.setAction(MessageService.ACTION_SEND_MSG);
						i.putExtra("to", to);
						i.putExtra("msg", message);
						sendBroadcast(i);
						mEdit.setText("");
					}
				}
			});

			attachmentButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (attachmentPicker.isShown()) {
						attachmentButton
								.setImageDrawable(getDrawable(R.drawable.ic_attachment));
						attachmentPicker.setVisibility(View.GONE);
						sButton.setVisibility(View.VISIBLE);

					} else {
						attachmentButton
								.setImageDrawable(getDrawable(R.drawable.ic_close));
						attachmentPicker.setVisibility(View.VISIBLE);
						sButton.setVisibility(View.GONE);
					}
				}
			});

			mEdit.addTextChangedListener(new TextWatcher() {

				String to = nEdit.toString();

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {

					Intent i = new Intent();
					i.setAction(MessageService.ACTION_START_COMPOSING);
					i.putExtra("to", to);
					sendBroadcast(i);
					if (mEdit.getText().toString().isEmpty()) {
						sButton.setBackground(getDrawable(R.drawable.send_button_disabled));
						sButton.setImageDrawable(getDrawable(R.drawable.ic_send_grey));
						sButton.animate().scaleX((float) 0.75);
						sButton.animate().scaleY((float) 0.75);
					} else {
						sButton.setBackground(getDrawable(R.drawable.send_button_enabled));
						sButton.setImageDrawable(getDrawable(R.drawable.ic_send));
						sButton.animate().scaleX(1);
						sButton.animate().scaleY(1);
					}

				}

				@Override
				public void afterTextChanged(Editable s) {

					Intent i = new Intent();
					i.setAction(MessageService.ACTION_STOP_COMPOSING);
					i.putExtra("to", to);
					sendBroadcast(i);
				}

			});
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; goto parent activity.
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	public void getMessages() {

		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();

		ChatAdapter adapter = new ChatAdapter(Conversations.this,
				DatabaseHelper.getMessages(newDB, nEdit), 0);

		ListView lv = (ListView) findViewById(R.id.listview);

		lv.setDivider(null);

		lv.setAdapter(adapter);

	}

	private static IntentFilter createIntentFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SET_NOTIFY);
		filter.addAction(SET_LAST_SEEN);
		filter.addAction(SET_PROFILE_PICTURE);
		return filter;
	}

	public class setNotifyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(SET_NOTIFY)) {
				getMessages();
			}
			if (intent.getAction().equals(SET_LAST_SEEN)) {

				// make sure we got the right last seen here
				if (intent.getStringExtra("from").equals(
						nEdit + "@s.whatsapp.net")
						&& !intent.getStringExtra("sec").equals("none")
						&& !intent.getStringExtra("sec").equals("deny")) {
					Calendar cal = Calendar.getInstance();
					TimeZone tz = cal.getTimeZone();
					SimpleDateFormat sdf = new SimpleDateFormat(
							"dd-MM-yyyy, HH:mm");
					sdf.setTimeZone(tz);
					long timestamp = Long.parseLong(intent
							.getStringExtra("sec"));
					String localTime = sdf.format(new Date(timestamp * 1000));

					try {
						TextView last_seen = (TextView) findViewById(R.id.contact_last_seen);
						last_seen.setText("Last seen: "
								+ parseSeconds(localTime));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (intent.getAction().equals(SET_PROFILE_PICTURE)) {

				if (intent.getStringExtra("from").equals(contact)) {

					File file = new File(Conversations.this.getFilesDir()
							.getParent()
							+ File.separator
							+ "Avatars"
							+ File.separator + contact + ".jpg");

					Bitmap avatar = BitmapHelper.getRoundedBitmap(BitmapFactory
							.decodeFile(file.getAbsolutePath()));
					ImageView image = (ImageView) findViewById(R.id.contact_photo);
					image.setImageBitmap(avatar);

				}
			}
		}
	}

	public static String parseSeconds(String date) throws ParseException {
		Date dateTime = new SimpleDateFormat("dd-MM-yyyy, HH:mm").parse(date);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dateTime);
		Calendar today = Calendar.getInstance();
		Calendar yesterday = Calendar.getInstance();
		yesterday.add(Calendar.DATE, -1);
		DateFormat timeFormatter = new SimpleDateFormat("HH:mm");

		if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == today
						.get(Calendar.DAY_OF_YEAR)) {
			return "today at " + timeFormatter.format(dateTime);
		} else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
				&& calendar.get(Calendar.DAY_OF_YEAR) == yesterday
						.get(Calendar.DAY_OF_YEAR)) {
			return "yesterday at " + timeFormatter.format(dateTime);
		} else {
			return date;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaColumns.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			Intent i = new Intent();
			i.setAction(MessageService.ACTION_SEND_IMAGE);
			i.putExtra("path", picturePath);
			i.putExtra("to", nEdit);
			sendBroadcast(i);

			Toast.makeText(Conversations.this, "IMAGE SENT :D",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_ONLINE);
		sendBroadcast(i);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_OFFLINE);
		sendBroadcast(i);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

}

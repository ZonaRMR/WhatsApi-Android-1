package nl.giovanniterlingen.whatsapp;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;
import nl.giovanniterlingen.whatsapp.MessageProcessor;
import nl.giovanniterlingen.whatsapp.ProtocolNode;
import nl.giovanniterlingen.whatsapp.message.Message;
import nl.giovanniterlingen.whatsapp.message.TextMessage;

public class MessageProcessing implements MessageProcessor {

	private Context context;

	public MessageProcessing(Context context) {
		this.context = context;
	}

	public void processMessage(ProtocolNode message) {
		String from = message.getAttribute("from");
		if (message.getAttribute("type").equals("text")) {
			ProtocolNode body = message.getChild("body");
			String hex = new String(body.getData());
			String participant = message.getAttribute("participant");
			if (participant != null && !participant.isEmpty()) {
				// Group message
				System.out.println(participant + "(" + from + ") ::: " + hex);
			} else {
				// Private message
				System.out.println(from + " ::: " + hex);
			}
		}
	}

	public void processMessage(Message message) {
		// TODO add all supported message types
		switch (message.getType()) {
		case TEXT:
			final TextMessage msg = (TextMessage) message;
			if (msg.getGroupId() != null && !msg.getGroupId().isEmpty()) {
				// Group message
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(
								context,
								msg.getFrom() + "(" + msg.getGroupId() + "): "
										+ msg.getText(), Toast.LENGTH_SHORT)
								.show();
					}
				});
			} else {
				// Private message
				((Activity) context).runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(context,
								msg.getFrom() + " : " + msg.getText(),
								Toast.LENGTH_SHORT).show();
					}
				});
			}
			break;
		default:
			processMessage(message.getProtocolNode());
		}
	}

}
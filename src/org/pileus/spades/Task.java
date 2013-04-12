package org.pileus.spades;

import java.io.*;
import java.net.*;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

public class Task extends Service implements Runnable
{
	/* Commands */
	public static final int REGISTER = 0;
	public static final int MESSAGE  = 1;

	/* Configuration */
	private String    server    = "irc.freenode.net";
	private String    nickname  = "andydroid";
	private String    channel   = "#rhnoise";
	private int       port      = 6667;

	/* Private data */
	private Messenger messenger = null;
	private Thread    thread    = null;
	private Socket    socket    = null;
	private Client    client    = null;

	/* Private methods */
	private void setup()
	{
		Os.debug("Task: setup");
		try {
			this.socket = new Socket(server, port);
			this.client = new Client(server, nickname, channel);
			Os.debug("Task: Socket and client created");
		} catch(Exception e) {
			Os.debug("Task: Failed to create socket: " + e);
			return;
		}

		try {
			BufferedReader input  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter    output = new PrintWriter(socket.getOutputStream());
			this.client.connect(input, output);
			Os.debug("Task: Client connected");
		} catch (Exception e) {
			Os.debug("Task: Failed to create readers writers: " + e);
			return;
		}
	}

	private void command(int cmd, Object value)
	{
		try {
			android.os.Message msg = android.os.Message.obtain();
			msg.what = cmd;
			msg.obj  = value;
			this.messenger.send(msg);
		} catch (Exception e) {
			Os.debug("Task: error sending message");
		}
	}

	/* Public methods */
	public Message send(String txt)
	{
		if (this.client == null && !this.client.running)
			return null;
		return this.client.send(txt);
	}

	/* Runnable methods */
	@Override
	public void run()
	{
		Os.debug("Task: thread run");
		setup();
		while (client.running)
			this.command(MESSAGE, client.recv());
		Os.debug("Task: thread exit");
	}

	/* Service Methods */
	@Override
	public void onCreate()
	{
		Os.debug("Task: onCreate");
		super.onCreate();

		/* Setup notification bar */
		Notification  note   = new Notification(android.R.drawable.presence_online, null, 0);
		Intent        intent = new Intent(this, Main.class);
		PendingIntent pend   = PendingIntent.getActivity(this, 0, intent, 0);

		note.setLatestEventInfo(this, "Spades Title", "Spades Message", pend);
		startForeground(1, note);

		/* Start client thread */
		thread = new Thread(this);
		thread.start();
	}
        
	@Override
	public void onDestroy()
	{
		Os.debug("Task: onDestroy");
		super.onDestroy();
	}
        
	@Override
	public void onStart(Intent intent, int startId)
	{
		Os.debug("Task: onStart");
		super.onStart(intent, startId);
		this.messenger = (Messenger)intent.getExtras().get("Messenger");
		this.command(REGISTER, this);
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		Os.debug("Task: onBind");
		return null;
	}
}

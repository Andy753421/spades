package org.pileus.spades;

public class Spades
{
	/* Properties */
	public  Task    task;
	public  Cards   cards;
	public  String  admin;
	public  boolean look;

	/* Static methods */
	private static String[] getCards(String msg, String regex)
	{
		String cards = msg
			.replaceAll(regex, "$1")
			.replaceAll(",", " ")
			.replaceAll("♠", "s")
			.replaceAll("♥", "h")
			.replaceAll("♦", "d")
			.replaceAll("♣", "c");
		Os.debug("Cards: getCards - [" + cards + "]:" + Os.base64(cards));
		return cards.split("\\s+");
	}

	/* Widget callback functions */
	public Spades(String admin)
	{
		this.admin = admin;
	}

	/* IRC Callbacks */
	public void onMessage(Message msg)
	{
		Os.debug("Spades: onMessage - " + msg.msg);
		if (msg.type != Message.Type.PRIVMSG)
			return;
		if (!msg.from.equals(this.admin))
			return;

		String txt = msg.txt;
		if (txt.matches(".*turn!.*") && this.look) {
			this.send(this.admin, ".look");
			this.look = false;
		}
		if (txt.startsWith("You have: ")) {
			this.cards.hand = Spades.getCards(txt, "You have: (.*)");
			this.cards.requestRender();
			this.look = false;
		}
		if (txt.matches(".*turn!.*")) {
			this.cards.pile = Spades.getCards(txt, ".*turn! \\((.*)\\)");
			this.cards.requestRender();
		}
		if (txt.startsWith("It is")) {
			this.cards.turn  = txt.replaceAll("It is (\\w+)'s (\\w+)!.*", "$1");
			this.cards.state = txt.replaceAll("It is (\\w+)'s (\\w+)!.*", "$2");
			this.cards.requestRender();
		}
		if (txt.startsWith("it is your") && msg.to != "") {
			this.cards.turn  = msg.to;
			this.cards.state = txt.replaceAll("it is your (\\w+)!", "$1");
			this.cards.requestRender();
		}
	}

	/* UI Callbacks */
	public boolean onConnect()
	{
		Os.debug("Spades: onConnect");
		this.send(this.admin, ".status");
		this.send(this.admin, ".turn");
		this.look = true;
		return true;
	}

	public boolean onBid(int bid)
	{
		Os.debug("Spades: onBid - " + bid);
		return this.send(".bid " + bid);
	}

	public boolean onPass(String card)
	{
		Os.debug("Spades: onPass - " + card);
		return this.send(this.admin, ".pass " + card);
	}

	public boolean onLook()
	{
		Os.debug("Spades: onLook");
		return this.send(".look");
	}

	public boolean onPlay(String card)
	{
		Os.debug("Spades: onPlay - " + card);
		return this.send(".play " + card);
	}

	public boolean onTurn()
	{
		Os.debug("Spades: onTurn");
		return this.send(".turn");
	}

	/* Helper functions */
	private boolean send(String dst, String msg)
	{
		if (this.task == null)
			return false;
		this.task.send(dst, msg);
		return true;
	}
	private boolean send(String msg)
	{
		if (this.task == null)
			return false;
		this.task.send(msg);
		return true;
	}
}

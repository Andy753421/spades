package org.pileus.spades;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Message
{
	/* Enumerations */
	enum Type {
		OTHER,
		PRIVMSG,
		TOPIC,
		NAMES,
		ERROR,
		CAP,
		AUTH,
		AUTHOK,
		AUTHFAIL,
	};

	/* Constnats */
	private final  String  reMsg  = "(:([^ ]+) +)?(([A-Z0-9]+) +)(([^ ]+)[= ]+)?(([^: ]+) *)?(:(.*))?";
	private final  String  reFrom = "([^! ]+)!.*";
	private final  String  reTo   = "(([^ :,]*)[:,] *)?(.*)";

	private static Pattern ptMsg  = null;
	private static Pattern ptFrom = null;
	private static Pattern ptTo   = null;

	/* Public data */
	public String  line = "";

	public String  src  = "";
	public String  cmd  = "";
	public String  dst  = "";
	public String  arg  = "";
	public String  msg  = "";

	public Type    type = Type.OTHER;
	public String  from = "";
	public String  to   = "";
	public String  txt  = "";

	/* Private methods */
	private String notnull(String string)
	{
		return string == null ? "" : string;
	}

	/* Public Methods */
	public Message(String dst, String from, String msg)
	{
		this.cmd  = "PRIVMSG";
		this.dst  = dst;
		this.from = from;
		this.msg  = msg;
		this.line = this.cmd + " " + this.dst + " :" + this.msg;
	}

	public Message(String line)
	{
		// Initialize regexes
		if (ptMsg  == null) ptMsg  = Pattern.compile(reMsg);
		if (ptFrom == null) ptFrom = Pattern.compile(reFrom);
		if (ptTo   == null) ptTo   = Pattern.compile(reTo);

		// Cleanup line
		line = line.replaceAll("\\s+",       " ");
		line = line.replaceAll("^ | $",      "");
		line = line.replaceAll("\003[0-9]*", "");
		this.line = line;

		// Split line into parts
		Matcher mrMsg = ptMsg.matcher(line);
		if (mrMsg.matches()) {
			this.src  = notnull(mrMsg.group(2));
			this.cmd  = notnull(mrMsg.group(4));
			this.dst  = notnull(mrMsg.group(6));
			this.arg  = notnull(mrMsg.group(8));
			this.msg  = notnull(mrMsg.group(10));
		}

		// Determine friendly parts
		Matcher mrFrom = ptFrom.matcher(this.src);
		if (mrFrom.matches())
			this.from = notnull(mrFrom.group(1));

		Matcher mrTo = ptTo.matcher(this.msg);
		if (mrTo.matches())
			this.to   = notnull(mrTo.group(2));

		if (this.to.equals(""))
			this.txt  = notnull(this.msg);
		else
			this.txt  = notnull(mrTo.group(3));

		// Parse commands names
		if      (this.cmd.equals("PRIVMSG"))       this.type = Type.PRIVMSG;
		else if (this.cmd.equals("332"))           this.type = Type.TOPIC;
		else if (this.cmd.equals("353"))           this.type = Type.NAMES;
		else if (this.cmd.equals("ERROR"))         this.type = Type.ERROR;
		else if (this.cmd.equals("CAP"))           this.type = Type.CAP;
		else if (this.cmd.equals("AUTHENTICATE"))  this.type = Type.AUTH;
		else if (this.cmd.equals("903"))           this.type = Type.AUTHOK;
		else if (this.cmd.equals("904"))           this.type = Type.AUTHFAIL;
		else if (this.cmd.equals("905"))           this.type = Type.AUTHFAIL;
		else if (this.cmd.equals("906"))           this.type = Type.AUTHFAIL;
		else if (this.cmd.equals("907"))           this.type = Type.AUTHFAIL;
	}

	public void debug()
	{
		Os.debug("---------------------");
		Os.debug("line = [" + line + "]");
		Os.debug("src  = " + this.src);
		Os.debug("cmd  = " + this.cmd);
		Os.debug("dst  = " + this.dst);
		Os.debug("arg  = " + this.arg);
		Os.debug("msg  = " + this.msg);
		Os.debug("from = " + this.from);
		Os.debug("to   = " + this.to);
		Os.debug("txt  = " + this.txt);
		Os.debug("---------------------");
	}

	public String toString()
	{
		return this.from + ": " + this.txt;
	}
}

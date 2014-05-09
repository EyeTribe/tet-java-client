package com.theeyetribe.client.reply;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;

public class ReplyFailed extends ReplyBase
{
	public class Values
	{
		@SerializedName(Protocol.KEY_STATUSMESSAGE)
		public String statusMessage;
	}

	public Values values = new Values();
}

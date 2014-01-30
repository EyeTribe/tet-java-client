package com.theeyetribe.client.reply;

import com.theeyetribe.client.data.CalibrationResult;

public class CalibrationPointEndReply extends ReplyBase 
{
	public class Values
	{
		public CalibrationResult calibresult;
	}
	
	public Values values = new Values();
}

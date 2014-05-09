package com.theeyetribe.client.request;

import com.theeyetribe.client.Protocol;

public class TrackerGetRequest extends RequestBase 
{
	public String[] values;

	public TrackerGetRequest()
	{
		this.category = Protocol.CATEGORY_TRACKER;
		this.request = Protocol.TRACKER_REQUEST_GET;
	}
}

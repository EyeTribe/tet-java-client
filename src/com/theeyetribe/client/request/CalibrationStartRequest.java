package com.theeyetribe.client.request;

import com.theeyetribe.client.Protocol;

public class CalibrationStartRequest extends RequestBase
{
	public class Values
	{
		public Integer pointcount;
	}

	public Values values = new Values();

	public CalibrationStartRequest()
	{
		this.category = Protocol.CATEGORY_CALIBRATION;
		this.request = Protocol.CALIBRATION_REQUEST_START;
	}
}

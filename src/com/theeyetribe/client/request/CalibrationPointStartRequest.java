package com.theeyetribe.client.request;

import com.theeyetribe.client.Protocol;

public class CalibrationPointStartRequest extends RequestBase
{
	public class Values
	{
		public Integer x;
		public Integer y;
	}

	public Values values = new Values();

	public CalibrationPointStartRequest()
	{
		this.category = Protocol.CATEGORY_CALIBRATION;
		this.request = Protocol.CALIBRATION_REQUEST_POINTSTART;
	}
}

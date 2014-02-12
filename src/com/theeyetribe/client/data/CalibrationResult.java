package com.theeyetribe.client.data;

import com.google.gson.annotations.SerializedName;
import com.theeyetribe.client.Protocol;

/**
 * CalibrationResult holds outcome of a calibration procedure. It defines if
 * calibration was successful or if certain calibration points needs resampling.
 */
public class CalibrationResult
{
	/*
	 * Was the calibration sucessful?
	 */
	public Boolean result = false;

	/*
	 * Average error in degrees
	 */
	@SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_DEGREES)
	public Double averageErrorDegree = 0d;

	/*
	 * Average error in degs, left eye
	 */
	@SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_LEFT_DEGREES)
	public Double averageErrorDegreeLeft = 0d;

	/*
	 * Average error in degs, right eye
	 */
	@SerializedName(Protocol.CALIBRESULT_AVERAGE_ERROR_RIGHT_DEGREES)
	public Double averageErrorDegreeRight = 0d;

	/*
	 * Complete list of calibrationpoints
	 */
	public CalibrationPoint[] calibpoints;

	public class CalibrationPoint
	{
		/*
		 * State defines that no data is available for calibration point
		 */
		public static final int STATE_NO_DATA = 0;
		/*
		 * State defines that calibration point should be resampled
		 */
		public static final int STATE_RESAMPLE = 1;
		/*
		 * State defines that calibration point was successfully sampled
		 */
		public static final int STATE_OK = 2;

		/*
		 * State of calib point
		 */
		public Integer state = 0;

		/*
		 * Coordinates in pixels
		 */
		@SerializedName(Protocol.CALIBRESULT_COORDINATES)
		public Point2D coordinates = new Point2D();

		/*
		 * Mean estimated coordinates
		 */
		@SerializedName(Protocol.CALIBRESULT_MEAN_ESTIMATED_COORDINATES)
		public Point2D meanEstimatedCoords = new Point2D();

		@SerializedName(Protocol.CALIBRESULT_ACCURACIES_DEGREES)
		public Accuracy accuracy = new Accuracy();

		@SerializedName(Protocol.CALIBRESULT_MEAN_ERRORS_PIXELS)
		public MeanError meanError = new MeanError();

		@SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_PIXELS)
		public StandardDeviation standardDeviation = new StandardDeviation();
	}

	public class Accuracy
	{
		/*
		 * Accuracy in degrees
		 */
		@SerializedName(Protocol.CALIBRESULT_ACCURACY_AVERAGE_DEGREES)
		public Double accuracyDegrees = 0d;

		/*
		 * Accuracy in degrees, left eye
		 */
		@SerializedName(Protocol.CALIBRESULT_ACCURACY_LEFT_DEGREES)
		public Double accuracyDegreesLeft = 0d;

		/*
		 * Accuracy in degrees, right eye
		 */
		@SerializedName(Protocol.CALIBRESULT_ACCURACY_RIGHT_DEGREES)
		public Double accuracyDegreesRight = 0d;
	}

	public class MeanError
	{
		/*
		 * Mean error in pixels
		 */
		@SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_AVERAGE_PIXELS)
		public Double meanErrorPixels = 0d;

		/*
		 * Mean error in pixels, left eye
		 */
		@SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_LEFT_PIXELS)
		public Double meanErrorPixelsLeft = 0d;

		/*
		 * Mean error in pixels, right eye
		 */
		@SerializedName(Protocol.CALIBRESULT_MEAN_ERROR_RIGHT_PIXELS)
		public Double meanErrorPixelsRight = 0d;
	}

	public class StandardDeviation
	{
		/*
		 * Average std deviation in pixels
		 */
		@SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_AVERAGE_PIXELS)
		public Double averageStandardDeviationPixels = 0d;

		/*
		 * Average std deviation in pixels, left eye
		 */
		@SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_LEFT_PIXELS)
		public Double averageStandardDeviationPixelsLeft = 0d;

		/*
		 * Average std deviation in pixels, right eye
		 */
		@SerializedName(Protocol.CALIBRESULT_STANDARD_DEVIATION_RIGHT_PIXELS)
		public Double averageStandardDeviationPixelsRight = 0d;
	}
}

package com.theeyetribe.client;

import com.theeyetribe.client.data.GazeData;

/**
 * Callback interface with methods associated to Gaze Tracking.
 * This interface should be implemented by classes that are to receive live GazeData stream.
 */
public interface IGazeListener 
{
	/**
	 * A notification call back indicating that a new GazeData frame is available. 
	 * Implementing classes should update themselves accordingly if needed.
	 * Register for updates through GazeManager.AddGazeListener().
	 * 
	 * @param gazeData
	 */
	public void onGazeUpdate(GazeData gazeData);
}

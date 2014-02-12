package com.theeyetribe.client;

/**
 * Callback interface with methods associated to the state of the physical Tracker device.
 * This interface should be implemented by classes that are to receive changes if the state of 
 * Tracker and handle these accordingly. This could be a class in the 'View' layer telling the user that a 
 * Tracker has disconnected.
 */
public interface ITrackerStateListener 
{
	/**
	 * A notification call back indicating that state of connected Tracker device has changed.
	 * Use this to detect if a tracker has been connected or disconnected.
	 * Implementing classes should update themselves accordingly if needed.
	 * Register for updates through GazeManager.AddTrackerStateListener().
	 * 
	 * @param trackerState the current state of the physical Tracker device
	 */
	public void onTrackerStateChanged(int trackerState);

	/**
	 * A notification call back indicating that main screen index has changed. 
	 * This is only relevant for multi-screen setups. Implementing classes should 
	 * update themselves accordingly if needed.
	 * Register for updates through GazeManager.AddTrackerStateListener().
	 * 
	 * @param screenIndex the currently valid screen index
	 * @param screenResolutionWidth screen resolution width in pixels
	 * @param screenResolutionHeight screen resolution height in pixels
	 * @param screenPhysicalWidth physical screen width in meters
	 * @param screenPhysicalHeight physical screen height in meters
	 */
	void OnScreenStatesChanged(int screenIndex, int screenResolutionWidth, int screenResolutionHeight, float screenPhysicalWidth, float screenPhysicalHeight);
}

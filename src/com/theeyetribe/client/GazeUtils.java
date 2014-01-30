package com.theeyetribe.client;

import com.theeyetribe.client.data.GazeData;
import com.theeyetribe.client.data.Point2D;

/**
 * Utility methods common to gaze control routines.
 */
public class GazeUtils
{
	/**
	 * Find average pupil center of two eyes.
	 * 
	 * @param gazeData gaze data frame to base calculation upon
	 * @return the average center point in normalized values
	 */
	public static Point2D getEyesCenterNormalized(GazeData gazeData)
	{
		Point2D eyeCenter = new Point2D();
		
		Point2D left = gazeData.leftEye.pupilCenterCoordinates;
		Point2D right = gazeData.rightEye.pupilCenterCoordinates;
		
		if(null != left && null != right)
		{
			eyeCenter.x = (left.x + right.x) / 2;
			eyeCenter.y = (left.y + right.y) / 2;
		}
		else
		if(null != left)
		{
			eyeCenter = left;
		}
		else
		if( null != right)
		{
			eyeCenter = right;
		}		
		
		return eyeCenter;
	}
	
	/**
	 * Find average pupil center of two eyes.
	 * 
	 * @param gazeData gaze data frame to base calculation upon
	 * @param screenWidth
	 * @param screenHeight
	 * @return the average center point in pixels
	 */
    public static Point2D getEyesCenterPixels(GazeData gazeData, int screenWidth, int screenHeight)
    {
        Point2D center = getEyesCenterNormalized(gazeData);

        return getRelativeToScreenSpace(center, screenWidth, screenHeight);
    }	
    
    private static double _MinimumEyesDistance = 0.1f;
    private static double _MaximumEyesDistance = 0.3f;
    
	/**
	 * Calculates distance between pupil centers based on previously 
	 * recorded min and max values.
	 * 
	 * @param gazeData gaze data frame to base calculation upon
	 * @return a normalized value [0..1]
	 */
	public static double getEyesDistanceNormalized(GazeData gazeData)
	{
        double dist = Math.abs(getDistancePoint2D(gazeData.leftEye.pupilCenterCoordinates, gazeData.rightEye.pupilCenterCoordinates));

        if (dist < _MinimumEyesDistance)
            _MinimumEyesDistance = dist;

        if (dist > _MaximumEyesDistance)
            _MaximumEyesDistance = dist;

        //return normalized
        return dist / (_MaximumEyesDistance - _MinimumEyesDistance);
	}
	
	/**
	 * Calculates distance between two points.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
    public static double getDistancePoint2D(Point2D a, Point2D b) 
    {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }
    
    /**
     * Converts a relative point to screen point in pixels.
     * 
     * @param point
     * @param screenWidth
     * @param screenHeight
     * @return
     */
    public static Point2D getRelativeToScreenSpace(Point2D point, int screenWidth, int screenHeight)
    {
        Point2D screen = new Point2D(point);
        screen.x = Math.round(screen.x * screenWidth);
        screen.y = Math.round(screen.y * screenHeight);

        return screen;
    }    
	
	/**
	 * Normalizes a point on screen in screen dims
	 * 
	 * @param point
	 * @param screenWidth
	 * @param screenHeight
	 * @return
	 */
	public static Point2D getNormalizedCoords(Point2D point, int screenWidth, int screenHeight)
	{
        Point2D norm = new Point2D(point);
        norm.x /= screenWidth;
        norm.y /= screenHeight;
        return norm;
	}
	
	/**
	 * Maps eye position of gaze coords within normalize space [x: -1:1 , y: -1:1]
	 * 
	 * @param point
	 * @param screenWidth
	 * @param screenHeight
	 * @return
	 */
	public static Point2D getNormalizedMapping(Point2D point, int screenWidth, int screenHeight)
	{
        Point2D normMap = getNormalizedCoords(point, screenWidth, screenHeight);

        //scale up and shift
        normMap.x *= 2f;
        normMap.x -= 1f;
        normMap.y *= 2f;
        normMap.y -= 1f;

        return normMap;
	}
}

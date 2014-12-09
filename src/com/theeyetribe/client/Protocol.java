/*
 * Copyright (c) 2013-present, The Eye Tribe. 
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the LICENSE file in the root directory of this source tree. 
 *
 */

package com.theeyetribe.client;

/**
 * Tracker API constants
 */
public class Protocol
{
    public static final int STATUSCODE_CALIBRATION_UPDATE = 800;
    public static final int STATUSCODE_SCREEN_UPDATE = 801;
    public static final int STATUSCODE_TRACKER_UPDATE = 802;

    public static final String KEY_CATEGORY = "category";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_VALUES = "values";
    public static final String KEY_STATUSCODE = "statuscode";
    public static final String KEY_STATUSMESSAGE = "statusmessage";

    public static final String CATEGORY_TRACKER = "tracker";
    public static final String CATEGORY_CALIBRATION = "calibration";
    public static final String CATEGORY_HEARTBEAT = "heartbeat";

    public static final String TRACKER_REQUEST_SET = "set";
    public static final String TRACKER_REQUEST_GET = "get";
    public static final String TRACKER_MODE_PUSH = "push";
    public static final String TRACKER_HEARTBEATINTERVAL = "heartbeatinterval";
    public static final String TRACKER_VERSION = "version";
    public static final String TRACKER_ISCALIBRATED = "iscalibrated";
    public static final String TRACKER_ISCALIBRATING = "iscalibrating";
    public static final String TRACKER_TRACKERSTATE = "trackerstate";
    public static final String TRACKER_CALIBRATIONRESULT = "calibresult";
    public static final String TRACKER_FRAMERATE = "framerate";
    public static final String TRACKER_FRAME = "frame";
    public static final String TRACKER_SCREEN_INDEX = "screenindex";
    public static final String TRACKER_SCREEN_RESOLUTION_WIDTH = "screenresw";
    public static final String TRACKER_SCREEN_RESOLUTION_HEIGHT = "screenresh";
    public static final String TRACKER_SCREEN_PHYSICAL_WIDTH = "screenpsyw";
    public static final String TRACKER_SCREEN_PHYSICAL_HEIGHT = "screenpsyh";

    public static final String CALIBRATION_REQUEST_START = "start";
    public static final String CALIBRATION_REQUEST_ABORT = "abort";
    public static final String CALIBRATION_REQUEST_POINTSTART = "pointstart";
    public static final String CALIBRATION_REQUEST_POINTEND = "pointend";
    public static final String CALIBRATION_REQUEST_CLEAR = "clear";
    public static final String CALIBRATION_CALIBRESULT = "calibresult";
    public static final String CALIBRATION_CALIBPOINTS = "calibpoints";
    public static final String CALIBRATION_POINT_COUNT = "pointcount";
    public static final String CALIBRATION_X = "x";
    public static final String CALIBRATION_Y = "y";

    public static final String FRAME_TIME = "time";
    public static final String FRAME_TIMESTAMP = "timestamp";
    public static final String FRAME_FIXATION = "fix";
    public static final String FRAME_STATE = "state";
    public static final String FRAME_RAW_COORDINATES = "raw";
    public static final String FRAME_AVERAGE_COORDINATES = "avg";
    public static final String FRAME_X = "x";
    public static final String FRAME_Y = "y";
    public static final String FRAME_LEFT_EYE = "lefteye";
    public static final String FRAME_RIGHT_EYE = "righteye";
    public static final String FRAME_EYE_PUPIL_SIZE = "psize";
    public static final String FRAME_EYE_PUPIL_CENTER = "pcenter";

    public static final String CALIBRESULT_RESULT = "result";
    public static final String CALIBRESULT_AVERAGE_ERROR_DEGREES = "deg";
    public static final String CALIBRESULT_AVERAGE_ERROR_LEFT_DEGREES = "degl";
    public static final String CALIBRESULT_AVERAGE_ERROR_RIGHT_DEGREES = "degr";
    public static final String CALIBRESULT_CALIBRATION_POINTS = "calibpoints";
    public static final String CALIBRESULT_STATE = "state";
    public static final String CALIBRESULT_COORDINATES = "cp";
    public static final String CALIBRESULT_X = "x";
    public static final String CALIBRESULT_Y = "y";
    public static final String CALIBRESULT_MEAN_ESTIMATED_COORDINATES = "mecp";
    public static final String CALIBRESULT_ACCURACIES_DEGREES = "acd";
    public static final String CALIBRESULT_ACCURACY_AVERAGE_DEGREES = "ad";
    public static final String CALIBRESULT_ACCURACY_LEFT_DEGREES = "adl";
    public static final String CALIBRESULT_ACCURACY_RIGHT_DEGREES = "adr";
    public static final String CALIBRESULT_MEAN_ERRORS_PIXELS = "mepix";
    public static final String CALIBRESULT_MEAN_ERROR_AVERAGE_PIXELS = "mep";
    public static final String CALIBRESULT_MEAN_ERROR_LEFT_PIXELS = "mepl";
    public static final String CALIBRESULT_MEAN_ERROR_RIGHT_PIXELS = "mepr";
    public static final String CALIBRESULT_STANDARD_DEVIATION_PIXELS = "asdp";
    public static final String CALIBRESULT_STANDARD_DEVIATION_AVERAGE_PIXELS = "asd";
    public static final String CALIBRESULT_STANDARD_DEVIATION_LEFT_PIXELS = "asdl";
    public static final String CALIBRESULT_STANDARD_DEVIATION_RIGHT_PIXELS = "asdr";
}
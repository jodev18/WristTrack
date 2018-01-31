package com.khsoftsolutions.wristtrack.core;

/**
 * Created by myxroft on 03/10/2017.
 */

public class Globals {

    public class ParentInformation{

        public static final String OBJ_NAME = "parent_info_obj";

        public static final String FIRST_NAME = "first_name";
        public static final String MIDDLE_NAME = "middle_name";
        public static final String LAST_NAME = "last_name";
        public static final String PARENT_AGE = "parent_age";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String ADDRESS = "home_address";
        public static final String PARENT_OBJ_ID = "parent_obj_id";
    }

    public class ChildInformation{

        public static final String OBJ_NAME = "child_info_obj";

        public static final String FIRST_NAME = "first_name";
        public static final String MIDDLE_NAME = "middle_name";
        public static final String LAST_NAME = "last_name";
        public static final String CHILD_AGE = "child_age";
        public static final String ADDRESS = "child_home_address";
        public static final String GENDER = "child_gender";

        public static final String DEVICE_MAC = "child_device_mac";

        public static final String PARENT_OBJ_ID = "parent_obj_id";
    }

    public class TrackInformation{

        public static final String OBJ_NAME = "track_obj";

        public static final String CHILD_OBJ_ID = "track_child_id";
        public static final String TRACK_TIMESTAMP = "track_child_timestamp";
        public static final String GEO_POINT = "track_geopoint";
    }

    public static final String COORDINATES_LAT_KEY = "track_lat";
    public static final String COORDINATES_LONG_LEY = "track_long";

    public static final String INTERVAL_PREFERENCE_KEY = "pref_set_interval";

}

/*
 ** Copyright 2017, Team LTT
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package com.teamltt.carcare.database.contract;

public class ResponseContract {

    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + ResponseEntry.TABLE_NAME + " (" +
            // trip_id INTEGER REFERENCES trips(trip_id)
            ResponseEntry.COLUMN_NAME_TRIP_ID + " INTEGER REFERENCES " +
            TripContract.TripEntry.TABLE_NAME + "(" + TripContract.TripEntry.COLUMN_NAME_ID + ")," +
            // this will automatically give each row the current timestamp upon insert
            ResponseEntry.COLUMN_NAME_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP," +
            ResponseEntry.COLUMN_NAME_NAME + " TEXT," +
            ResponseEntry.COLUMN_NAME_PID + " TEXT," +
            ResponseEntry.COLUMN_NAME_VALUE + " TEXT" +
            ");";

    public static final String SQL_DROP_ENTRIES = "DROP TABLE IF EXISTS " + ResponseEntry.TABLE_NAME;

    // HACK: private to prevent someone from accidentally instantiating a contract
    private ResponseContract() {
    }

    public static class ResponseEntry {
        public static final String TABLE_NAME = "responses";
        public static final String COLUMN_NAME_TRIP_ID = TripContract.TripEntry.COLUMN_NAME_ID;
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_PID = "pid";
        public static final String COLUMN_NAME_VALUE = "value";
    }
}

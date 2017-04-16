/*
 *
 *  * Copyright 2017, Team LTT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.teamltt.carcare.model;

public class Reminder {
    private int comparison, value;
    private String featureId;
    private String name, date;
    private long vehicleId;
    private long reminderId;
    private boolean archived;


    public Reminder(long reminderId, long vehicleId, String name, String featureId, int comparison, int value, String date, boolean archived) {
        this.name = name;
        this.reminderId = reminderId;
        this.featureId = featureId;
        this.comparison = comparison;
        this.value = value;
        this.date = date;
        this.archived = archived;
    }

    public long getReminderId() { return reminderId; }

    public long getVehicleId() { return vehicleId; }

    public String getName() { return name; }

    public String getFeatureId() { return featureId; }

    public int getComparisonType() { return comparison; }

    public int getComparisonValue() { return value; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

    public boolean isArchived() { return archived; }

    public boolean setArchived(boolean status) {
        this.archived = status;
        return this.archived;
    }

}

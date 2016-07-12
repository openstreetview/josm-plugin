/*
 *  Copyright 2016 Telenav, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.openstreetmap.josm.plugins.openstreetview.entity;

import org.openstreetmap.josm.data.coor.LatLon;
import com.telenav.josm.common.util.EntityUtil;


/**
 * Defines the photo business entity.
 *
 * @author Beata
 * @version $Revision$
 */
public class Photo {

    private final Long id;
    private final Long sequenceId;
    private final Long sequenceIndex;
    private final LatLon location;
    private final String name;
    private final String largeThumbnailName;
    private final String thumbnailName;
    private final Long timestamp;
    private final Double heading;
    private final String username;


    public Photo(final Long id, final Long sequenceId, final Long sequenceIndex, final Double latitude,
            final Double longitude, final String name, final String largeThumbnailName, final String thumbnailName,
            final Long timestamp, final Double heading, final String username) {
        this.id = id;
        this.sequenceId = sequenceId;
        this.sequenceIndex = sequenceIndex;
        this.location = new LatLon(latitude, longitude);
        this.name = name;
        this.largeThumbnailName = largeThumbnailName;
        this.thumbnailName = thumbnailName;
        this.timestamp = timestamp;
        this.heading = heading;
        this.username = username;
    }


    public Long getId() {
        return id;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public Long getSequenceIndex() {
        return sequenceIndex;
    }

    public LatLon getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getLargeThumbnailName() {
        return largeThumbnailName;
    }

    public String getThumbnailName() {
        return thumbnailName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Double getHeading() {
        return heading;
    }

    public String getUsername() {
        return username;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + EntityUtil.hashCode(id);
        return result;
    }


    @Override
    public boolean equals(final Object obj) {
        boolean result = false;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Photo) {
            final Photo other = (Photo) obj;
            result = EntityUtil.bothNullOrEqual(id, other.getId());
        }
        return result;
    }
}
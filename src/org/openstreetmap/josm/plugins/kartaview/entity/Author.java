/*
 * Copyright 2019 Grabtaxi Holdings PTE LTE (GRAB), All rights reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file.
 *
 */
package org.openstreetmap.josm.plugins.kartaview.entity;


/**
 * Defines the author of a detection or contribution.
 *
 * @author ioanao
 * @version $Revision$
 */
public class Author {

    private final String externalId;
    private final String userName;
    private final String type = "OSM";


    public Author(final String externalId, final String userName) {
        this.externalId = externalId;
        this.userName = userName;
    }

    public Author(final String externalId){
        this.externalId = externalId;
        this.userName = null;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUserName() {
        return userName;
    }

    public String getType() {
        return type;
    }
}
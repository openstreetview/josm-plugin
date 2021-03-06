/*
 * Copyright 2019 Grabtaxi Holdings PTE LTE (GRAB), All rights reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be found in the LICENSE file.
 *
 */
package org.openstreetmap.josm.plugins.kartaview.entity;

import org.openstreetmap.josm.data.osm.Node;


/**
 * Entity that matches a downloaded Node Element to the OsmElement.
 *
 * @author laurad
 */
public class DownloadedNode extends OsmElement {

    private final Node matchedNode;

    public DownloadedNode(final OsmElement element, final Node matchedNode) {
        super(element.getOsmId(), element.getType(), element.getMembers(), element.getFromId(), element.getOsmId(),
                element.getTag());
        this.matchedNode = matchedNode;
    }

    public Node getMatchedNode() {
        return matchedNode;
    }
}
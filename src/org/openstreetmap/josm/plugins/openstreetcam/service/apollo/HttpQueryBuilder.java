/*
 * The code is licensed under the LGPL Version 3 license http://www.gnu.org/licenses/lgpl-3.0.en.html.
 * The collected imagery is protected & available under the CC BY-SA version 4 International license.
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode.
 *
 * Copyright (c)2017, Telenav, Inc. All Rights Reserved
 */
package org.openstreetmap.josm.plugins.openstreetcam.service.apollo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openstreetmap.josm.plugins.openstreetcam.entity.DetectionMode;
import org.openstreetmap.josm.plugins.openstreetcam.entity.EditStatus;
import org.openstreetmap.josm.plugins.openstreetcam.entity.OsmComparison;
import org.openstreetmap.josm.plugins.openstreetcam.entity.SignType;
import org.openstreetmap.josm.plugins.openstreetcam.util.cnf.ApolloServiceConfig;
import com.telenav.josm.common.argument.BoundingBox;
import com.telenav.josm.common.http.HttpUtil;


/**
 *
 * @author beataj
 * @version $Revision$
 */
class HttpQueryBuilder {

    private static final char QUESTIONM = '?';
    private static final char EQ = '=';
    private static final char AND = '&';
    private static final String DATE_FORMAT = "YYYY-MM-dd";

    private final StringBuilder query;


    HttpQueryBuilder() {
        query = new StringBuilder();
    }

    String buildCommentQuery() {
        query.append(RequestConstants.UPDATE_DETECTION);
        query.append(QUESTIONM);
        appendFormatFilter(query);
        return build();
    }

    String buildSearchDetectionsQuery(final BoundingBox area, final Date date, final Long osmUserId,
            final DetectionFilter filter) {
        return buildSearchQuery(RequestConstants.SEARCH_DETECTIONS, area, date, osmUserId, filter, true);
    }

    String buildSearchClustersQuery(final BoundingBox area, final Date date, final DetectionFilter filter) {
        return buildSearchQuery(RequestConstants.SEARCH_CLUSTERS, area, date, null, filter, false);
    }

    private String buildSearchQuery(final String method, final BoundingBox area, final Date date, final Long osmUserId,
            final DetectionFilter filter, final boolean excludeSignTypes) {
        query.append(method);
        query.append(QUESTIONM);

        appendFormatFilter(query);
        appendBoundingBoxFilter(query, area);
        appendDateFilter(date);
        appendUserFilter(osmUserId);
        if (filter != null) {
            appendOsmComparisonFilter(filter.getOsmComparisons());
            appendEditStatusFilter(filter.getEditStatuses());
            appendSignTypeFilter(filter.getSignTypes());
            appendDetectionModeFilter(filter.getModes());
        }
        if (excludeSignTypes) {
            appendExcludedSignTypeFitler();
        }
        return build();
    }

    String buildRetrieveSequenceDetectionsQuery(final Long sequenceId) {
        query.append(RequestConstants.RETRIEVE_SEQUENCE_DETECTIONS);
        query.append(QUESTIONM);
        query.append(RequestConstants.SEQUENCE_ID).append(EQ).append(sequenceId);
        appendExcludedSignTypeFitler();
        return build();
    }

    String buildRetrievePhotoDetectionsQuery(final Long sequenceId, final Integer sequenceIndex) {
        query.append(RequestConstants.RETRIEVE_PHOTO_DETECTIONS);
        query.append(QUESTIONM);
        query.append(RequestConstants.SEQUENCE_ID).append(EQ).append(sequenceId);
        query.append(AND).append(RequestConstants.SEQUENCE_INDEX).append(EQ).append(sequenceIndex);
        appendExcludedSignTypeFitler();
        return build();
    }

    String buildRetrieveDetectionQuery(final Long id) {
        return buildRetrieveByIdQuery(RequestConstants.RETRIVE_DETECTION, id);
    }

    String buildRetrieveClusterQuery(final Long id) {
        return buildRetrieveByIdQuery(RequestConstants.RETRIEVE_CLUSTER, id);
    }

    String buildRetrieveClusterDetectionsQuery(final Long id) {
        return buildRetrieveByIdQuery(RequestConstants.RETRIEVE_CLUSTER_DETECTIONS, id);
    }

    String buildRetrieveClusterPhotosQuery(final Long id) {
        return buildRetrieveByIdQuery(RequestConstants.RETRIEVE_CLUSTER_PHOTOS, id);
    }

    private String buildRetrieveByIdQuery(final String method, final Long id) {
        query.append(method);
        query.append(QUESTIONM);
        query.append(RequestConstants.ID).append(EQ).append(id);
        return build();
    }

    private void appendUserFilter(final Long osmUserId) {
        if (osmUserId != null) {
            query.append(AND).append(RequestConstants.EXTERNAL_ID).append(EQ).append(osmUserId);
            query.append(AND).append(RequestConstants.AUTHOR_TYPE).append(EQ).append(RequestConstants.AUTHOR_TYPE_VAL);
        }
    }

    private void appendOsmComparisonFilter(final List<OsmComparison> osmComparisons) {
        if (osmComparisons != null && !osmComparisons.isEmpty()) {
            query.append(AND).append(RequestConstants.OSM_COMPARISONS).append(EQ)
            .append(HttpUtil.utf8Encode(new HashSet<>(osmComparisons)));
        }
    }

    private void appendEditStatusFilter(final List<EditStatus> editStatuses) {
        if (editStatuses != null && !editStatuses.isEmpty()) {
            final Set<String> editStatusValues = new HashSet<>();
            for (final EditStatus editStatus : editStatuses) {
                if (editStatus.equals(EditStatus.MAPPED)) {
                    editStatusValues.add(RequestConstants.EDIT_STATUS_FIXED);
                    editStatusValues.add(RequestConstants.EDIT_STATUS_ALREADY_FIXED);
                } else {
                    editStatusValues.add(editStatus.name());
                }
            }
            query.append(AND).append(RequestConstants.EDIT_STATUSES).append(EQ)
            .append(HttpUtil.utf8Encode(editStatusValues));
        }

    }

    private void appendSignTypeFilter(final List<SignType> signTypes) {
        if (signTypes != null && !signTypes.isEmpty()) {
            query.append(AND).append(RequestConstants.INCLUDED_SIGN_TYPES).append(EQ)
            .append(HttpUtil.utf8Encode(new HashSet<>(signTypes)));
        }
    }

    private void appendDetectionModeFilter(final List<DetectionMode> modes) {
        if (modes != null && !modes.isEmpty()) {
            query.append(AND).append(RequestConstants.MODES).append(EQ)
            .append(HttpUtil.utf8Encode(new HashSet<>(modes)));
        }
    }

    private void appendDateFilter(final Date date) {
        if (date != null) {
            query.append(AND).append(RequestConstants.DATE).append(EQ)
            .append(new SimpleDateFormat(DATE_FORMAT).format(date));
        }
    }

    private void appendExcludedSignTypeFitler() {
        query.append(AND).append(RequestConstants.EXCLUDED_SIGN_TYPES);
        query.append(EQ).append(HttpUtil.utf8Encode(RequestConstants.BLURRING_TYPE));
    }

    private void appendBoundingBoxFilter(final StringBuilder query, final BoundingBox area) {
        query.append(AND).append(RequestConstants.NORTH).append(EQ).append(area.getNorth());
        query.append(AND).append(RequestConstants.SOUTH).append(EQ).append(area.getSouth());
        query.append(AND).append(RequestConstants.EAST).append(EQ).append(area.getEast());
        query.append(AND).append(RequestConstants.WEST).append(EQ).append(area.getWest());
    }

    private void appendFormatFilter(final StringBuilder query) {
        query.append(RequestConstants.FORMAT).append(EQ).append(RequestConstants.FORMAT_VAL);
    }

    private String build() {
        return query.insert(0, ApolloServiceConfig.getInstance().getServiceUrl()).toString();
    }
}
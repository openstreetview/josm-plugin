/*
 * The code is licensed under the LGPL Version 3 license http://www.gnu.org/licenses/lgpl-3.0.en.html.
 * The collected imagery is protected & available under the CC BY-SA version 4 International license.
 * https://creativecommons.org/licenses/by-sa/4.0/legalcode.
 *
 * Copyright (c)2018, Telenav, Inc. All Rights Reserved
 */
package org.openstreetmap.josm.plugins.openstreetcam.handler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.openstreetcam.argument.DataType;
import org.openstreetmap.josm.plugins.openstreetcam.argument.SearchFilter;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Cluster;
import org.openstreetmap.josm.plugins.openstreetcam.entity.Detection;
import org.openstreetmap.josm.plugins.openstreetcam.entity.HighZoomResultSet;
import org.openstreetmap.josm.plugins.openstreetcam.entity.PhotoDataSet;
import org.openstreetmap.josm.plugins.openstreetcam.service.ServiceException;
import org.openstreetmap.josm.plugins.openstreetcam.service.apollo.ApolloService;
import org.openstreetmap.josm.plugins.openstreetcam.service.apollo.DetectionFilter;
import org.openstreetmap.josm.plugins.openstreetcam.service.photo.OpenStreetCamService;
import org.openstreetmap.josm.plugins.openstreetcam.service.photo.Paging;
import org.openstreetmap.josm.plugins.openstreetcam.util.cnf.GuiConfig;
import org.openstreetmap.josm.plugins.openstreetcam.util.pref.PreferenceManager;
import com.telenav.josm.common.argument.BoundingBox;


/**
 * Executes the service search operations 
 *
 * @author beataj
 * @version $Revision$
 */
class SearchServiceHandler {

    protected final OpenStreetCamService openStreetCamService;
    protected final ApolloService apolloService;


    SearchServiceHandler() {
        openStreetCamService = new OpenStreetCamService();
        apolloService = new ApolloService();
    }

    /**
     * Searches for data high zoom levels. For high zoom levels depending on the selected filter the following
     * data types are displayed: photo locations, detections and clusters (aggregated detections).
     * 
     * @param area a {@code BoundingBox} represents the search area.
     * @param filter a {@code SearchFilter} represents the currently selected search filters.
     * @return a {@code HighZoomResultSet} containing the result
     */
    HighZoomResultSet searchHighZoomData(final BoundingBox area, final SearchFilter filter) {
        final ExecutorService executorService = Executors.newFixedThreadPool(filter.getDataTypes().size());
        final Future<PhotoDataSet> futurePhotoDataSet = filter.getDataTypes().contains(DataType.PHOTO)
                ? executorService.submit(() -> listNearbyPhotos(area, filter, Paging.NEARBY_PHOTOS_DEAFULT)) : null;
        final Future<List<Detection>> futureDetections = filter.getDataTypes().contains(DataType.DETECTION)
                ? executorService.submit(() -> searchDetections(area, filter)) : null;
        final Future<List<Cluster>> futureClusters = filter.getDataTypes().contains(DataType.CLUSTER)
                ? executorService.submit(() -> searchClusters(area, filter)) : null;

        PhotoDataSet photoDataSet = null;
        try {
            photoDataSet = futurePhotoDataSet != null ? futurePhotoDataSet.get() : null;
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadPhotosSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoListText());
                PreferenceManager.getInstance().savePhotosSearchErrorSuppressFlag(flag);
            }
        }

        List<Detection> detections = null;
        try {
            detections = futureDetections != null ? futureDetections.get() : null;
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadDetectionsSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorDetectionRetrieveText());
                PreferenceManager.getInstance().saveDetectionsSearchErrorSuppressFlag(flag);
            }
        }

        List<Cluster> clusters = null;
        try {
            clusters = futureClusters != null ? futureClusters.get() : null;
        } catch (final Exception ex) {
            if (!PreferenceManager.getInstance().loadClustersSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorClusterRetrieveText());
                PreferenceManager.getInstance().saveClustersSearchErrorSuppressFlag(flag);
            }
        }
        if (detections != null && clusters != null) {
            // remove detections that belongs to a cluster
            detections = filterClusterDetections(clusters, detections);
        }
        executorService.shutdown();
        return new HighZoomResultSet(photoDataSet, detections, clusters);
    }

    private List<Detection> filterClusterDetections(final List<Cluster> clusters, final List<Detection> detections) {
        final List<Detection> result = new ArrayList<>();
        final List<Long> clusterDetectionIds =
                clusters.stream().flatMap(cluster -> cluster.getDetectionIds().stream()).collect(Collectors.toList());
        for (final Detection detection : detections) {
            if (!clusterDetectionIds.contains(detection.getId())) {
                result.add(detection);
            }
        }
        return result;
    }

    /**
     * Lists the photos from the current area based on the given filters.
     *
     * @param area a {@code Circle} representing the search areas.
     * @param filter a {@code Filter} represents the user's search filters. Null values are ignored.
     * @param paging a {@code Paging} representing the pagination
     * @return a list of {@code Photo}s
     */
    public PhotoDataSet listNearbyPhotos(final BoundingBox area, final SearchFilter filter, final Paging paging) {
        Long osmUserId = null;
        Date date = null;
        if (filter != null) {
            osmUserId = filter.getOsmUserId();
            date = filter.getDate();
        }
        PhotoDataSet result = new PhotoDataSet();
        try {
            result = openStreetCamService.listNearbyPhotos(area, date, osmUserId, paging);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadPhotosSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoListText());
                PreferenceManager.getInstance().savePhotosSearchErrorSuppressFlag(flag);
            }
        }
        return result;
    }

    private List<Detection> searchDetections(final BoundingBox area, final SearchFilter filter) {
        Long osmUserId = null;
        Date date = null;
        DetectionFilter detectionFilter = null;
        if (filter != null) {
            osmUserId = filter.getOsmUserId();
            date = filter.getDate();
            detectionFilter = filter.getDetectionFilter();
        }
        List<Detection> result = null;
        try {
            result = apolloService.searchDetections(area, date, osmUserId, detectionFilter);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadDetectionsSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoListText());
                PreferenceManager.getInstance().saveDetectionsSearchErrorSuppressFlag(flag);
            }
        }
        return result;
    }

    private List<Cluster> searchClusters(final BoundingBox area, final SearchFilter filter) {
        Date date = null;
        DetectionFilter detectionFilter = null;
        if (filter != null) {
            date = filter.getDate();
            detectionFilter = filter.getDetectionFilter();
        }
        List<Cluster> result = null;
        try {
            result = apolloService.searchClusters(area, date, detectionFilter);
        } catch (final ServiceException e) {
            if (!PreferenceManager.getInstance().loadClustersSearchErrorSuppressFlag()) {
                final boolean flag = handleException(GuiConfig.getInstance().getErrorPhotoListText());
                PreferenceManager.getInstance().saveClustersSearchErrorSuppressFlag(flag);
            }
        }
        return result;

    }

    boolean handleException(final String message) {
        final int val = JOptionPane.showOptionDialog(MainApplication.getMap().mapView, message,
                GuiConfig.getInstance().getErrorTitle(), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                null, null);
        return val == JOptionPane.YES_OPTION;
    }
}
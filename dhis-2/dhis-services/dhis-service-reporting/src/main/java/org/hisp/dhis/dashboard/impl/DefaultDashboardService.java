/*
 * Copyright (c) 2004-2022, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.dashboard.impl;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.hisp.dhis.common.IdentifiableObjectUtils.getUids;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.appmanager.App;
import org.hisp.dhis.appmanager.AppManager;
import org.hisp.dhis.common.IdentifiableObjectManager;
import org.hisp.dhis.common.hibernate.HibernateIdentifiableObjectStore;
import org.hisp.dhis.commons.util.TextUtils;
import org.hisp.dhis.dashboard.Dashboard;
import org.hisp.dhis.dashboard.DashboardItem;
import org.hisp.dhis.dashboard.DashboardItemStore;
import org.hisp.dhis.dashboard.DashboardItemType;
import org.hisp.dhis.dashboard.DashboardSearchResult;
import org.hisp.dhis.dashboard.DashboardService;
import org.hisp.dhis.document.Document;
import org.hisp.dhis.eventchart.EventChart;
import org.hisp.dhis.eventreport.EventReport;
import org.hisp.dhis.eventvisualization.EventVisualization;
import org.hisp.dhis.eventvisualization.EventVisualizationStore;
import org.hisp.dhis.eventvisualization.SimpleEventVisualizationView;
import org.hisp.dhis.mapping.Map;
import org.hisp.dhis.report.Report;
import org.hisp.dhis.user.User;
import org.hisp.dhis.user.UserService;
import org.hisp.dhis.util.ObjectUtils;
import org.hisp.dhis.visualization.SimpleVisualizationView;
import org.hisp.dhis.visualization.Visualization;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Note: The remove associations methods must be altered if caching is introduced.
 *
 * @author Lars Helge Overland
 */
@RequiredArgsConstructor
@Service("org.hisp.dhis.dashboard.DashboardService")
public class DefaultDashboardService implements DashboardService {
  private static final int HITS_PER_OBJECT = 6;

  private static final int MAX_HITS_PER_OBJECT = 25;

  @Qualifier("org.hisp.dhis.dashboard.DashboardStore")
  private final HibernateIdentifiableObjectStore<Dashboard> dashboardStore;

  private final IdentifiableObjectManager objectManager;

  private final UserService userService;

  private final DashboardItemStore dashboardItemStore;

  private final AppManager appManager;

  private final EventVisualizationStore eventVisualizationStore;

  @Override
  @Transactional(readOnly = true)
  public DashboardSearchResult search(String query) {
    return search(query, new HashSet<>(), null, null);
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSearchResult search(
      String query, Set<DashboardItemType> maxTypes, Integer count, Integer maxCount) {
    Set<String> words = Sets.newHashSet(query.split(TextUtils.SPACE));

    List<App> dashboardApps =
        appManager.getDashboardPlugins(
            null, getMax(DashboardItemType.APP, maxTypes, count, maxCount), true);

    DashboardSearchResult result = new DashboardSearchResult();

    result.setUsers(
        userService.getAllUsersBetweenByName(
            query, 0, getMax(DashboardItemType.USERS, maxTypes, count, maxCount)));
    result.setVisualizations(
        convertFromVisualization(
            objectManager.getBetweenLikeName(
                Visualization.class,
                words,
                0,
                getMax(DashboardItemType.VISUALIZATION, maxTypes, count, maxCount))));
    result.setEventVisualizations(
        convertFromEventVisualization(
            eventVisualizationStore.getLineListsLikeName(
                words,
                0,
                getMax(DashboardItemType.EVENT_VISUALIZATION, maxTypes, count, maxCount))));
    result.setEventCharts(
        eventVisualizationStore.getChartsLikeName(
            words, 0, getMax(DashboardItemType.EVENT_CHART, maxTypes, count, maxCount)));
    result.setMaps(
        objectManager.getBetweenLikeName(
            Map.class, words, 0, getMax(DashboardItemType.MAP, maxTypes, count, maxCount)));
    result.setEventReports(
        eventVisualizationStore.getReportsLikeName(
            words, 0, getMax(DashboardItemType.EVENT_REPORT, maxTypes, count, maxCount)));
    result.setReports(
        objectManager.getBetweenLikeName(
            Report.class, words, 0, getMax(DashboardItemType.REPORTS, maxTypes, count, maxCount)));
    result.setResources(
        objectManager.getBetweenLikeName(
            Document.class,
            words,
            0,
            getMax(DashboardItemType.RESOURCES, maxTypes, count, maxCount)));
    result.setApps(AppManager.filterAppsByName(query, dashboardApps, "ilike"));

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSearchResult search(
      Set<DashboardItemType> maxTypes, Integer count, Integer maxCount) {
    DashboardSearchResult result = new DashboardSearchResult();

    result.setVisualizations(
        convertFromVisualization(
            objectManager.getBetweenSorted(
                Visualization.class,
                0,
                getMax(DashboardItemType.VISUALIZATION, maxTypes, count, maxCount))));
    result.setEventVisualizations(
        convertFromEventVisualization(
            eventVisualizationStore.getLineLists(
                0, getMax(DashboardItemType.EVENT_VISUALIZATION, maxTypes, count, maxCount))));
    result.setEventCharts(
        eventVisualizationStore.getCharts(
            0, getMax(DashboardItemType.EVENT_CHART, maxTypes, count, maxCount)));
    result.setMaps(
        objectManager.getBetweenSorted(
            Map.class, 0, getMax(DashboardItemType.MAP, maxTypes, count, maxCount)));
    result.setEventReports(
        eventVisualizationStore.getReports(
            0, getMax(DashboardItemType.EVENT_REPORT, maxTypes, count, maxCount)));
    result.setReports(
        objectManager.getBetweenSorted(
            Report.class, 0, getMax(DashboardItemType.REPORTS, maxTypes, count, maxCount)));
    result.setResources(
        objectManager.getBetweenSorted(
            Document.class, 0, getMax(DashboardItemType.RESOURCES, maxTypes, count, maxCount)));
    result.setApps(
        appManager.getDashboardPlugins(
            null, getMax(DashboardItemType.APP, maxTypes, count, maxCount), true));

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSearchResult search(String query, Set<DashboardItemType> maxTypes) {
    return search(query, maxTypes, null, null);
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardSearchResult search(Set<DashboardItemType> maxTypes) {
    return search(maxTypes, null, null);
  }

  @Override
  @Transactional(readOnly = true)
  public void mergeDashboard(Dashboard dashboard) {
    if (dashboard.getItems() != null) {
      for (DashboardItem item : dashboard.getItems()) {
        mergeDashboardItem(item);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void mergeDashboardItem(DashboardItem item) {
    if (item.getVisualization() != null) {
      item.setVisualization(
          objectManager.get(Visualization.class, item.getVisualization().getUid()));
    }

    if (item.getEventVisualization() != null) {
      item.setEventVisualization(
          objectManager.get(EventVisualization.class, item.getEventVisualization().getUid()));
    }

    if (item.getEventChart() != null) {
      item.setEventChart(objectManager.get(EventChart.class, item.getEventChart().getUid()));
    }

    if (item.getMap() != null) {
      item.setMap(objectManager.get(Map.class, item.getMap().getUid()));
    }

    if (item.getEventReport() != null) {
      item.setEventReport(objectManager.get(EventReport.class, item.getEventReport().getUid()));
    }

    if (item.getUsers() != null) {
      item.setUsers(objectManager.getByUid(User.class, getUids(item.getUsers())));
    }

    if (item.getReports() != null) {
      item.setReports(objectManager.getByUid(Report.class, getUids(item.getReports())));
    }

    if (item.getResources() != null) {
      item.setResources(objectManager.getByUid(Document.class, getUids(item.getResources())));
    }

    if (item.getAppKey() != null) {
      item.setAppKey(item.getAppKey());
    }
  }

  @Override
  @Transactional
  public long saveDashboard(Dashboard dashboard) {
    dashboardStore.save(dashboard);

    return dashboard.getId();
  }

  @Override
  @Transactional
  public void updateDashboard(Dashboard dashboard) {
    dashboardStore.update(dashboard);
  }

  @Override
  @Transactional
  public void deleteDashboard(Dashboard dashboard) {
    dashboardStore.delete(dashboard);
  }

  @Override
  @Transactional(readOnly = true)
  public Dashboard getDashboard(long id) {
    return dashboardStore.get(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Dashboard getDashboard(String uid) {
    return dashboardStore.getByUid(uid);
  }

  // -------------------------------------------------------------------------
  // DashboardItem
  // -------------------------------------------------------------------------

  @Override
  @Transactional
  public void updateDashboardItem(DashboardItem item) {
    dashboardItemStore.update(item);
  }

  @Override
  @Transactional(readOnly = true)
  public DashboardItem getDashboardItem(String uid) {
    return dashboardItemStore.getByUid(uid);
  }

  @Override
  @Transactional(readOnly = true)
  public Dashboard getDashboardFromDashboardItem(DashboardItem dashboardItem) {
    return dashboardItemStore.getDashboardFromDashboardItem(dashboardItem);
  }

  @Override
  public void deleteDashboardItem(DashboardItem item) {
    dashboardItemStore.delete(item);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getVisualizationDashboardItems(Visualization visualization) {
    return dashboardItemStore.getVisualizationDashboardItems(visualization);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getEventVisualizationDashboardItems(
      EventVisualization eventVisualization) {
    return dashboardItemStore.getEventVisualizationDashboardItems(eventVisualization);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getEventChartDashboardItems(EventChart eventChart) {
    return dashboardItemStore.getEventChartDashboardItems(eventChart);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getMapDashboardItems(Map map) {
    return dashboardItemStore.getMapDashboardItems(map);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getEventReportDashboardItems(EventReport eventReport) {
    return dashboardItemStore.getEventReportDashboardItems(eventReport);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getUserDashboardItems(User user) {
    return dashboardItemStore.getUserDashboardItems(user);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getReportDashboardItems(Report report) {
    return dashboardItemStore.getReportDashboardItems(report);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DashboardItem> getDocumentDashboardItems(Document document) {
    return dashboardItemStore.getDocumentDashboardItems(document);
  }

  // -------------------------------------------------------------------------
  // Supportive methods
  // -------------------------------------------------------------------------

  private int getMax(
      DashboardItemType type, Set<DashboardItemType> maxTypes, Integer count, Integer maxCount) {
    int dashboardsMax = ObjectUtils.firstNonNull(maxCount, MAX_HITS_PER_OBJECT);
    int dashboardsCount = ObjectUtils.firstNonNull(count, HITS_PER_OBJECT);

    return maxTypes != null && maxTypes.contains(type) ? dashboardsMax : dashboardsCount;
  }

  private List<SimpleVisualizationView> convertFromVisualization(
      List<Visualization> visualizations) {
    List<SimpleVisualizationView> views = new ArrayList<>();

    if (isNotEmpty(visualizations)) {
      for (Visualization visualization : visualizations) {
        views.add(convertFrom(visualization));
      }
    }

    return views;
  }

  private SimpleVisualizationView convertFrom(Visualization visualization) {
    SimpleVisualizationView view = new SimpleVisualizationView();
    BeanUtils.copyProperties(visualization, view);

    return view;
  }

  private List<SimpleEventVisualizationView> convertFromEventVisualization(
      List<EventVisualization> eventVisualizations) {
    List<SimpleEventVisualizationView> views = new ArrayList<>();

    if (isNotEmpty(eventVisualizations)) {
      for (EventVisualization eventVisualization : eventVisualizations) {
        views.add(convertFrom(eventVisualization));
      }
    }

    return views;
  }

  private SimpleEventVisualizationView convertFrom(EventVisualization visualization) {
    SimpleEventVisualizationView view = new SimpleEventVisualizationView();
    BeanUtils.copyProperties(visualization, view);

    return view;
  }
}

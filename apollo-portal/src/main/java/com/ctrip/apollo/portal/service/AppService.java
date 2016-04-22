package com.ctrip.apollo.portal.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import com.ctrip.apollo.common.utils.ExceptionUtils;
import com.ctrip.apollo.core.dto.AppDTO;
import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.portal.PortalSettings;
import com.ctrip.apollo.portal.api.AdminServiceAPI;
import com.ctrip.apollo.portal.entity.ClusterNavTree;

@Service
public class AppService {

  private Logger logger = LoggerFactory.getLogger(AppService.class);

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private PortalSettings portalSettings;

  @Autowired
  private AdminServiceAPI.AppAPI appAPI;

  public List<AppDTO> findAll() {
    // TODO: 16/4/21 先从 portalSettings第一个环境去apps,后续可以优化
    Env env = portalSettings.getEnvs().get(0);
    return appAPI.getApps(env);
  }

  public ClusterNavTree buildClusterNavTree(String appId) {
    ClusterNavTree tree = new ClusterNavTree();

    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      ClusterNavTree.Node clusterNode = new ClusterNavTree.Node(env);
      clusterNode.setClusters(clusterService.findClusters(env, appId));
      tree.addNode(clusterNode);
    }
    return tree;
  }

  public void save(AppDTO app) {
    List<Env> envs = portalSettings.getEnvs();
    for (Env env : envs) {
      try {
        appAPI.save(env, app);
      } catch (HttpStatusCodeException e) {
        logger.error(ExceptionUtils.toString(e));
        throw e;
      }
    }
  }

}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cmdbuild.bim;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.toOptional;
import jakarta.activation.DataHandler;
import jakarta.annotation.Nullable;
import static java.lang.String.format;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.cmdbuild.common.beans.CardIdAndClassName;
import org.cmdbuild.config.BimConfiguration;
import org.cmdbuild.dao.beans.CMRelation;
import static org.cmdbuild.dao.constants.SystemAttributes.ATTR_IDOBJ1;
import static org.cmdbuild.dao.constants.SystemAttributes.ATTR_IDOBJ2;
import org.cmdbuild.dao.core.q3.DaoService;
import static org.cmdbuild.dao.core.q3.QueryBuilder.EQ;
import org.cmdbuild.dao.entrytype.Classe;
import org.cmdbuild.etl.EtlException;
import org.cmdbuild.minions.MinionComponent;
import org.cmdbuild.minions.MinionHandler;
import org.cmdbuild.minions.MinionHandlerImpl;
import static org.cmdbuild.minions.MinionRuntimeStatus.MRS_NOTRUNNING;
import static org.cmdbuild.minions.MinionRuntimeStatus.MRS_READY;
import org.cmdbuild.navtree.NavTree;
import org.cmdbuild.navtree.NavTreeNode;
import org.cmdbuild.navtree.NavTreeService;
import static org.cmdbuild.utils.date.CmDateUtils.now;
import org.cmdbuild.utils.ifc.utils.XktUtils;
import static org.cmdbuild.utils.io.CmIoUtils.newDataHandler;
import static org.cmdbuild.utils.io.CmIoUtils.toByteArray;
import static org.cmdbuild.utils.lang.CmExceptionUtils.runtime;
import static org.cmdbuild.utils.lang.CmPreconditions.checkNotBlank;
import static org.cmdbuild.utils.lang.CmStringUtils.toStringOrNull;
import static org.cmdbuild.utils.random.CmRandomUtils.randomId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BimServiceImpl implements BimService, MinionComponent {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DaoService dao;
    private final BimConfiguration configuration;
    private final NavTreeService navTreeService;
    private final BimObjectRepository objectRepository;
    private final BimProjectRepository projectRepository;
    private final MinionHandler minionHandler;

    public BimServiceImpl(DaoService dao, BimConfiguration configuration, NavTreeService navTreeService, BimObjectRepository objectRepository, BimProjectRepository projectRepository) {
        this.configuration = checkNotNull(configuration);
        this.navTreeService = checkNotNull(navTreeService);
        this.objectRepository = checkNotNull(objectRepository);
        this.projectRepository = checkNotNull(projectRepository);
        this.dao = checkNotNull(dao);
        this.minionHandler = MinionHandlerImpl.builder()
                .withName("BIM Service")
                .withConfigEnabler("org.cmdbuild.bim.enabled")
                .withEnabledChecker(configuration::isEnabled)
                .withStatusChecker(() -> configuration.isEnabled() ? MRS_READY : MRS_NOTRUNNING)
                .reloadOnConfigs(BimConfiguration.class)
                .build();
    }

    @Override
    public MinionHandler getMinionHandler() {
        return minionHandler;
    }

    @Override
    public boolean isEnabled() {
        return MinionComponent.super.isEnabled();
    }

    @Override
    @Nullable
    public BimObject setOwnerForProject(BimProjectExt data, @Nullable CardIdAndClassName owner) {
        return objectRepository.setOwnerForProject(data, owner);
    }

    @Override
    @Nullable
    public BimObject getBimObjectForProjectGlobalIdOrNull(BimProject bimProject, String globalId) {
        return objectRepository.getBimObjectForProjectGlobalIdOrNull(bimProject, globalId);
    }

    @Override
    @Nullable
    @Deprecated //global id may be duplicate, use projectid+globalid
    public BimObject getBimObjectForGlobalIdOrNull(String globalId) {
        return objectRepository.getBimObjectForGlobalIdOrNull(globalId);
    }

    @Override
    public Collection<BimProject> getAllProjects() {
        return projectRepository.getAllProjects();
    }

    @Override
    public BimProject getProjectById(long id) {
        return projectRepository.getProjectById(id);
    }

    @Override
    @Nullable
    public BimProjectIfc getProjectIfcByProjectId(String projectId) {
        return projectRepository.getProjectIfcByProjectId(projectId);
    }

    @Override
    public void deleteProjectIfcByProjectId(String projectId) {
        projectRepository.deleteProjectIfcByProjectId(projectId);
    }

    @Override
    public void deleteProjectById(long id) {
        projectRepository.deleteProjectById(id);
    }

    @Override
    public void delete(BimObject bimObject) {
        objectRepository.delete(bimObject);
    }

    @Override
    public BimObject create(BimObject bimObject) {
        return objectRepository.create(bimObject);
    }

    @Override
    public BimProject createProject(BimProject bimProject) {
        String projId = randomId();
        bimProject = BimProjectImpl.copyOf(bimProject).withProjectId(projId).build();
        return projectRepository.createProject(bimProject);
    }

    @Override
    public BimProject updateProject(BimProject bimProject) {
        BimProject current = getProjectById(bimProject.getId());
        bimProject = BimProjectImpl.copyOf(current)
                .withActive(bimProject.isActive())
                .withDescription(bimProject.getDescription())
                .withImportMapping(bimProject.getImportMapping())
                .build();
        //TODO handle active/inactive on bim server
        return projectRepository.updateProject(bimProject);
    }

    @Override
    @Nullable
    public BimProjectExt getProjectByCodeOrNull(String projectCode) {
        return projectRepository.getAllProjects().stream().filter(p -> equal(p.getName(), projectCode)).collect(toOptional()).map(p -> getProjectExt(p.getId())).orElse(null);
    }

    @Override
    public BimProjectExt createProjectExt(BimProjectExt data) {
        BimProject created = createProject(data);
        if (data.hasOwner()) {
            objectRepository.createBimObjectForProject(created, data.getOwner());
        }
        return getProjectExt(created.getId());
    }

    @Override
    public BimProjectExt updateProjectExt(BimProjectExt data) {
        updateProject(data);
        objectRepository.setOwnerForProject(data, data.getOwnerOrNull());
        return getProjectExt(data.getId());
    }

    @Override
    public boolean hasBim(Classe classe) {
        NavTree navTree = getNavTreeOrNull();
        return navTree != null && navTree.getData().getThisNodeAndAllDescendants().stream().anyMatch(n -> equal(n.getTargetClassName(), classe.getName()));
    }

    @Override
    @Nullable
    public BimObject getBimObjectForCardOrNull(CardIdAndClassName card) {
        return objectRepository.getBimObjectForCardOrNull(card);
    }

    @Override
    @Nullable
    public BimObject getBimObjectForCardOrViaNavTreeOrNull(CardIdAndClassName card) {
        BimObject obj = getBimObjectForCardOrNull(card);
        if (obj != null) {
            return obj;
        } else {
            NavTree navTree = getNavTreeOrNull();
            if (navTree != null) {
                NavTreeNode node = navTree.getData().getThisNodeAndAllDescendants().stream().filter(n -> dao.getType(card).equalToOrDescendantOf(n.getTargetClassName())).collect(toOptional()).orElse(null);
                if (node != null && node.hasParent()) {
                    CMRelation relation = dao.selectAll().from(dao.getDomain(node.getDomainName())).where(node.getDirect() ? ATTR_IDOBJ2 : ATTR_IDOBJ1, EQ, card.getId()).getRelationOrNull();
                    if (relation != null) {
                        return getBimObjectForCardOrViaNavTreeOrNull(relation.getRelationWithSource(card.getId()).getTargetCard());
                    }
                }
            }
            return null;
        }
    }

    @Override
    @Nullable
    public BimObject getBimObjectForProjectOrNull(BimProject bimProject) {
        return objectRepository.getBimObjectForProjectOrNull(bimProject);
    }

    @Override
    public DataHandler downloadIfcFile(long projectId, @Nullable String ifcFormat) {
        BimProject project = projectRepository.getProjectById(projectId);
        BimProjectIfc projectIfcByProjectId = projectRepository.getProjectIfcByProjectId(project.getProjectId());
        return newDataHandler(projectIfcByProjectId.getIfcDecompressedFile(), "text/plain", format("%s.ifc", project.getName()));
    }

    @Override
    public DataHandler downloadXktFile(long projectId) {
        BimProject project = projectRepository.getProjectById(projectId);
        return newDataHandler(project.getXktFile(), "application/octet-stream", format("%s.xkt", project.getName()));
    }

    @Override
    public BimProject convertIfcProjectToXkt(long projectId) {
        BimProject project = projectRepository.getProjectById(projectId);
        BimProjectIfc projectIfcByProjectId = projectRepository.getProjectIfcByProjectId(project.getProjectId());
        byte[] downloadedIfc = projectIfcByProjectId.getIfcDecompressedFile();
        if (downloadedIfc == null) {
            throw runtime("Unable to convert project with id %s ifc not found", projectId);
        } else {
            byte[] xktFile = XktUtils.ifcToXkt(downloadedIfc, configuration.getConversionTimeout());
            return dao.update(BimProjectImpl.copyOf(project).withXktFile(xktFile).withLastCheckin(now()).build());
        }
    }

    @Override
    public BimProject uploadXktFile(long projectId, DataHandler dataHandler, boolean isNewProject) {
        BimProject project = projectRepository.getProjectById(projectId);
        BimProjectIfc projectIfcByProjectId = projectRepository.getProjectIfcByProjectId(project.getProjectId());
        uploadIfcToDatabase(projectIfcByProjectId, dataHandler, project.getProjectId());
        try {
            return convertIfcProjectToXkt(projectId);
        } catch (Exception ex) {
            if (isNewProject) {
                objectRepository.delete(objectRepository.getBimObjectForProjectOrNull(project));
                projectRepository.deleteProjectById(projectId);
                projectRepository.deleteProjectIfcByProjectId(project.getProjectId());
                logger.error("Unable to convert project with id {}, removed bim project", projectId);
            } else {
                logger.error("There was a problem with the project {} conversion", projectId);
            }
            throw new EtlException(ex, "error converting ifc project to xkt");
        }
    }

    @Override
    public BimProjectExt getProjectExt(long projectId) {
        BimProject project = getProjectById(projectId);
        BimObject bimObject = getBimObjectForProjectOrNull(project);
        return new BimProjectExtImpl(project, bimObject);
    }

    @Override
    public List<BimProjectExt> getAllProjectsAndObjects() {
        return getAllProjects().stream().map((p) -> {
            BimObject bimObject = getBimObjectForProjectOrNull(p);//TODO this is inefficent, just a quick fix; change bim service to run a join query or something else
            return new BimProjectExtImpl(p, bimObject);
        }).collect(toImmutableList());
    }

    @Override
    public void deleteProject(long id) {
        String globalProjectId = projectRepository.getProjectById(id).getProjectId();
        dao.selectAll().from(BimObject.class).where("ProjectId", EQ, checkNotBlank(toStringOrNull(globalProjectId))).getCards().forEach(e -> {
            dao.delete(e);
        });
        projectRepository.deleteProjectById(id);
        projectRepository.deleteProjectIfcByProjectId(globalProjectId);
    }

    @Override
    public BimObject createBimObjectForProject(BimProject bimProject, CardIdAndClassName card) {
        return objectRepository.createBimObjectForProject(bimProject, card);
    }

    @Override
    public BimObject updateBimObject(BimObject bimObject) {
        BimObject current = objectRepository.getBimObjectForCardOrNull(bimObject.getOwnerCard());
        if (current != null && equal(current.getProjectId(), bimObject.getProjectId()) && equal(current.getGlobalId(), bimObject.getGlobalId())) {
            //nothing to do
            return current;
        } else {
            Optional.ofNullable(current).ifPresent(objectRepository::delete);
            Optional.ofNullable(objectRepository.getBimObjectForGlobalIdOrNull(bimObject.getGlobalId())).ifPresent(objectRepository::delete);
            return objectRepository.create(bimObject);
        }
    }

    @Nullable
    private NavTree getNavTreeOrNull() {
        return navTreeService.getTreeOrNull(BIM_NAV_TREE);
    }

    private void uploadIfcToDatabase(BimProjectIfc bimProjectIfc, DataHandler ifcFile, String projectId) {
        if (bimProjectIfc == null) {
            logger.info(format("Ifc for project id %s not existing, creating", projectId));
            dao.create(BimProjectIfcImpl.builder().withProjectId(projectId).withIfcFile(toByteArray(ifcFile)).build());
        } else {
            logger.info(format("Ifc for project id %s already existing, updating", projectId));
            dao.update(BimProjectIfcImpl.copyOf(bimProjectIfc).withIfcFile(toByteArray(ifcFile)).build());
        }
    }

}

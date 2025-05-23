/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cmdbuild.service.rest.v3.endpoint;

import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.collect.Ordering;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import java.util.List;
import static java.util.stream.Collectors.toList;
import org.cmdbuild.classe.access.UserClassService;
import org.cmdbuild.classe.access.UserDomainService;
import static org.cmdbuild.config.api.ConfigValue.FALSE;
import org.cmdbuild.dao.entrytype.Domain;
import static org.cmdbuild.dao.utils.DomainUtils.getClassDomainsIndexes;
import static org.cmdbuild.service.rest.common.utils.WsRequestUtils.isAdminViewMode;
import static org.cmdbuild.service.rest.common.utils.WsResponseUtils.response;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.DETAILED;
import static org.cmdbuild.service.rest.common.utils.WsSerializationAttrs.VIEW_MODE_HEADER_PARAM;
import org.cmdbuild.service.rest.common.serializationhelpers.DomainSerializationHelper;
import org.cmdbuild.utils.lang.CmMapUtils;

@Path("{a:classes|processes}/{classId}/domains")
@Produces(APPLICATION_JSON)
public class ClassOrProcessDomainsWs {

    private final UserClassService classService;
    private final UserDomainService domainService;
    private final DomainSerializationHelper domainSerializationHelper;

    public ClassOrProcessDomainsWs(UserClassService classService, UserDomainService domainService, DomainSerializationHelper domainSerializationHelper) {
        this.classService = checkNotNull(classService);
        this.domainService = checkNotNull(domainService);
        this.domainSerializationHelper = checkNotNull(domainSerializationHelper);
    }

    @GET
    @Path("")
    public Object getDomains(@HeaderParam(VIEW_MODE_HEADER_PARAM) String viewMode, @PathParam("classId") String classId, @QueryParam(DETAILED) @DefaultValue(FALSE) Boolean includeFullDetails) {
        List<Domain> domains = isAdminViewMode(viewMode) ? domainService.getUserDomainsForClasse(classId) : domainService.getActiveUserDomainsForClasse(classId);
        CmMapUtils.FluentMap<String, Integer> domainIndex = getClassDomainsIndexes(domains, classService.getUserClass(classId));
        return response(domains.stream().sorted(Ordering.natural().onResultOf(d -> domainIndex.get(d.getName()))).map(d -> {
            return (includeFullDetails ? domainSerializationHelper.serializeDetailedDomain(d) : domainSerializationHelper.serializeBasicDomain(d)).with("_index", domainIndex.get(d.getName()));
        }).collect(toList()));
    }

}

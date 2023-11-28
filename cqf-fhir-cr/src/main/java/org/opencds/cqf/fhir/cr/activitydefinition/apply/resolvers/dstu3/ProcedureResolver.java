package org.opencds.cqf.fhir.cr.activitydefinition.apply.resolvers.dstu3;

import java.util.Collections;
import org.hl7.fhir.dstu3.model.ActivityDefinition;
import org.hl7.fhir.dstu3.model.Procedure;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.fhir.cr.activitydefinition.apply.BaseRequestResourceResolver;

public class ProcedureResolver extends BaseRequestResourceResolver {
    private final ActivityDefinition activityDefinition;

    public ProcedureResolver(ActivityDefinition activityDefinition) {
        this.activityDefinition = activityDefinition;
    }

    @Override
    public Procedure resolve(IIdType subjectId, IIdType encounterId, IIdType practitionerId, IIdType organizationId) {
        var procedure = new Procedure();

        procedure.setStatus(Procedure.ProcedureStatus.UNKNOWN);
        procedure.setSubject(new Reference(subjectId));

        if (activityDefinition.hasUrl()) {
            procedure.setDefinition(Collections.singletonList(new Reference(activityDefinition.getUrl())));
        }

        if (activityDefinition.hasCode()) {
            procedure.setCode(activityDefinition.getCode());
        }

        if (activityDefinition.hasBodySite()) {
            procedure.setBodySite(activityDefinition.getBodySite());
        }

        return procedure;
    }
}

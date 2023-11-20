package org.opencds.cqf.fhir.cr.activitydefinition.apply.resolvers.r4;

import org.hl7.fhir.r4.model.ActivityDefinition;
import org.hl7.fhir.r4.model.Claim;
import org.opencds.cqf.fhir.cr.activitydefinition.apply.BaseRequestResourceResolver;

public class ClaimResolver extends BaseRequestResourceResolver {
    private final ActivityDefinition activityDefinition;

    public ClaimResolver(ActivityDefinition activityDefinition) {
        this.activityDefinition = activityDefinition;
    }

    @Override
    public Claim resolve(String subjectId, String encounterId, String practitionerId, String organizationId) {
        if (activityDefinition == null) {
            return null;
        }

        return null;
    }
}

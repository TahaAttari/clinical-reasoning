package org.opencds.cqf.fhir.cr.activitydefinition.apply.resolvers.r5;

import java.util.Collections;

import org.hl7.fhir.r5.model.ActivityDefinition;
import org.hl7.fhir.r5.model.Attachment;
import org.hl7.fhir.r5.model.CodeableReference;
import org.hl7.fhir.r5.model.Communication;
import org.hl7.fhir.r5.model.Enumerations.EventStatus;
import org.hl7.fhir.r5.model.Reference;
import org.opencds.cqf.fhir.cr.activitydefinition.apply.BaseRequestResourceResolver;

public class CommunicationResolver extends BaseRequestResourceResolver {
    private final ActivityDefinition activityDefinition;

    public CommunicationResolver(ActivityDefinition activityDefinition) {
        this.activityDefinition = activityDefinition;
    }

    @Override
    public Communication resolve(String subjectId, String encounterId, String practitionerId, String organizationId) {
        var communication = new Communication();

        communication.setStatus(EventStatus.UNKNOWN);
        communication.setSubject(new Reference(subjectId));

        if (activityDefinition.hasCode()) {
            communication.setReason(Collections.singletonList(new CodeableReference(activityDefinition.getCode())));
        }

        if (activityDefinition.hasRelatedArtifact()) {
            for (var artifact : activityDefinition.getRelatedArtifact()) {
                if (artifact.hasResourceElement()) {
                    var attachment = new Attachment()
                            .setUrl(artifact.getResourceElement().getValue());
                    if (artifact.hasDisplay()) {
                        attachment.setTitle(artifact.getDisplay());
                    }

                    var payload = new Communication.CommunicationPayloadComponent();
                    payload.setContent(artifact.hasDisplay() ? attachment.setTitle(artifact.getDisplay()) : attachment);
                    communication.setPayload(Collections.singletonList(payload));
                }
            }
        }

        return communication;
    }
}
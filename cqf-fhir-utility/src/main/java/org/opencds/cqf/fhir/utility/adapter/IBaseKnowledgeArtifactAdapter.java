package org.opencds.cqf.fhir.utility.adapter;

import java.util.Date;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IIdType;

public interface IBaseKnowledgeArtifactAdapter extends ResourceAdapter {

    IBaseResource get();

    default IIdType getId() {
        return this.get().getIdElement();
    }

    default void setId(IIdType id) {
        this.get().setId(id);
    }

    String getName();

    void setName(String name);

    String getUrl();

    void setUrl(String url);

    String getVersion();

    void setVersion(String version);

    List<IDependencyInfo> getDependencies();

    Date getApprovalDate();

    void setApprovalDate(Date approvalDate);

    ICompositeType getEffectivePeriod();

    List<? extends ICompositeType> getRelatedArtifact();

    List<? extends ICompositeType> getComponents();

    String releaseLabelUrl = "http://hl7.org/fhir/StructureDefinition/artifact-releaseLabel";
    String releaseDescriptionUrl = "http://hl7.org/fhir/StructureDefinition/artifact-releaseDescription";
    String usPhContextTypeUrl = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context-type";
    String contextTypeUrl = "http://terminology.hl7.org/CodeSystem/usage-context-type";
    String contextUrl = "http://hl7.org/fhir/us/ecr/CodeSystem/us-ph-usage-context";
    String isOwnedUrl = "http://hl7.org/fhir/StructureDefinition/crmi-isOwned";
}

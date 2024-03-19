package org.opencds.cqf.fhir.utility.adapter.r5;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseHasExtensions;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IDomainResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.MetadataResource;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.RelatedArtifact;
import org.hl7.fhir.r5.model.RelatedArtifact.RelatedArtifactType;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.utility.adapter.IDependencyInfo;
import org.opencds.cqf.fhir.utility.visitor.KnowledgeArtifactVisitor;

public class KnowledgeArtifactAdapter extends ResourceAdapter
        implements org.opencds.cqf.fhir.utility.adapter.KnowledgeArtifactAdapter {
    MetadataResource adaptedResource;

    public KnowledgeArtifactAdapter(IDomainResource resource) {
        super(resource);
        if (!(resource instanceof MetadataResource)) {
            throw new IllegalArgumentException("resource argument is not a MetadataResource resource");
        }
        this.adaptedResource = (MetadataResource) resource;
    }

    public KnowledgeArtifactAdapter(MetadataResource resource) {
        super(resource);
        this.adaptedResource = resource;
    }

    @Override
    public MetadataResource get() {
        return this.adaptedResource;
    }

    @Override
    public MetadataResource copy() {
        return this.get().copy();
    }

    @Override
    public String getUrl() {
        return this.get().getUrl();
    }

    @Override
    public boolean hasUrl() {
        return this.get().hasUrl();
    }

    @Override
    public void setUrl(String url) {
        this.get().setUrl(url);
    }

    @Override
    public void setVersion(String version) {
        this.get().setVersion(version);
    }

    @Override
    public String getVersion() {
        return this.get().getVersion();
    }

    @Override
    public boolean hasVersion() {
        return this.get().hasVersion();
    }

    @Override
    public String getName() {
        return this.get().getName();
    }

    @Override
    public void setName(String name) {
        this.get().setName(name);
    }

    @Override
    public Date getApprovalDate() {
        return this.get().getApprovalDate();
    }

    @Override
    public Date getDate() {
        return this.get().getDate();
    }

    @Override
    public void setDate(Date approvalDate) {
        this.get().setDate(approvalDate);
    }

    @Override
    public void setDateElement(IPrimitiveType<Date> date) {
        if (date != null && !(date instanceof DateTimeType)) {
            throw new UnprocessableEntityException("Date must be " + DateTimeType.class.getName());
        }
        this.get().setDateElement((DateTimeType) date);
    }

    @Override
    public void setApprovalDate(Date approvalDate) {
        this.get().setApprovalDate(approvalDate);
    }

    @Override
    public Period getEffectivePeriod() {
        return this.get().getEffectivePeriod();
    }

    @Override
    public List<IDependencyInfo> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public IBase accept(KnowledgeArtifactVisitor visitor, Repository repository, IBaseParameters operationParameters) {
        return visitor.visit(this, repository, operationParameters);
    }

    @Override
    public void setEffectivePeriod(ICompositeType effectivePeriod) {
        if (effectivePeriod != null && !(effectivePeriod instanceof Period)) {
            throw new UnprocessableEntityException("EffectivePeriod must be a valid " + Period.class.getName());
        }
        this.get().setEffectivePeriod((Period) effectivePeriod);
    }

    @Override
    public boolean hasRelatedArtifact() {
        return this.get().hasRelatedArtifact();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RelatedArtifact> getRelatedArtifact() {
        return this.get().getRelatedArtifact();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RelatedArtifact> getRelatedArtifactsOfType(String codeString) {
        RelatedArtifactType type;
        try {
            type = RelatedArtifactType.fromCode(codeString);
        } catch (FHIRException e) {
            throw new UnprocessableEntityException("Invalid related artifact code");
        }
        return this.getRelatedArtifact().stream()
                .filter(ra -> ra.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends ICompositeType & IBaseHasExtensions> void setRelatedArtifact(List<T> relatedArtifacts)
            throws UnprocessableEntityException {
        this.get()
                .setRelatedArtifact(relatedArtifacts.stream()
                        .map(ra -> {
                            try {
                                return (RelatedArtifact) ra;
                            } catch (ClassCastException e) {
                                throw new UnprocessableEntityException(
                                        "All related artifacts must be of type " + RelatedArtifact.class.getName());
                            }
                        })
                        .collect(Collectors.toList()));
    }

    @Override
    public void setStatus(String statusCodeString) {
        PublicationStatus status;
        try {
            status = PublicationStatus.fromCode(statusCodeString);
        } catch (FHIRException e) {
            throw new UnprocessableEntityException("Invalid status code");
        }
        this.get().setStatus(status);
    }

    @Override
    public String getStatus() {
        return this.get().getStatus() == null ? null : this.get().getStatus().toCode();
    }

    @Override
    public boolean getExperimental() {
        return this.get().getExperimental();
    }

    @Override
    public void setExtension(List<IBaseExtension<?, ?>> extensions) {
        this.get().setExtension(extensions.stream().map(e -> (Extension) e).collect(Collectors.toList()));
    }
}

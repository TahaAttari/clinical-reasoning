package org.opencds.cqf.fhir.utility.adapter.r4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.opencds.cqf.cql.evaluator.fhir.util.DependencyInfo;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.utility.visitor.KnowledgeArtifactVisitor;
import org.opencds.cqf.fhir.utility.visitor.r4.r4KnowledgeArtifactVisitor;

public class PlanDefinitionAdapter extends KnowledgeArtifactAdapter implements r4PlanDefinitionAdapter {

  private PlanDefinition planDefinition;

  public PlanDefinitionAdapter(PlanDefinition planDefinition) {
    super(planDefinition);

    if (!planDefinition.fhirType().equals("PlanDefinition")) {
      throw new IllegalArgumentException(
          "resource passed as planDefinition argument is not a PlanDefinition resource");
    }

    this.planDefinition = planDefinition;
  }

  public IBase accept(r4KnowledgeArtifactVisitor visitor, Repository repository, Parameters operationParameters) {
    return visitor.visit(this, repository, operationParameters);
  }

  protected PlanDefinition getPlanDefinition() {
    return this.planDefinition;
  }

  @Override
  public PlanDefinition get() {
    return this.planDefinition;
  }

  @Override
  public IIdType getId() {
    return this.getPlanDefinition().getIdElement();
  }

  @Override
  public void setId(IIdType id) {
    this.getPlanDefinition().setId(id);
  }

  @Override
  public String getName() {
    return this.getPlanDefinition().getName();
  }

  @Override
  public void setName(String name) {
    this.getPlanDefinition().setName(name);
  }

  @Override
  public String getUrl() {
    return this.getPlanDefinition().getUrl();
  }

  @Override
  public void setUrl(String url) {
    this.getPlanDefinition().setUrl(url);
  }

  @Override
  public String getVersion() {
    return this.getPlanDefinition().getVersion();
  }

  @Override
  public void setVersion(String version) {
    this.getPlanDefinition().setVersion(version);
  }
  
  @Override
  public List<DependencyInfo> getDependencies() {
    List<DependencyInfo> references = new ArrayList<>();

    /*
      relatedArtifact[].resource
      library[]
      action[]..trigger[].dataRequirement[].profile[]
      action[]..trigger[].dataRequirement[].codeFilter[].valueSet
      action[]..condition[].expression.reference
      action[]..input[].profile[]
      action[]..input[].codeFilter[].valueSet
      action[]..output[].profile[]
      action[]..output[].codeFilter[].valueSet
      action[]..definitionCanonical
      action[]..dynamicValue[].expression.reference
      extension[cpg-partOf]
     */

    // relatedArtifact[].resource
    references.addAll(getRelatedArtifactReferences(this.getPlanDefinition(), this.getPlanDefinition().getRelatedArtifact()));

    // library[]
    List<CanonicalType> libraries = this.getPlanDefinition().getLibrary();
    for (CanonicalType ct : libraries) {
      DependencyInfo dependency = new DependencyInfo(this.getPlanDefinition().getUrl(), ct.getValue(), ct.getExtension());
      references.add(dependency);
    }

    // TODO: Complete retrieval from other elements (action etc.). Ideally use $data-requirements code

    return references;
  }
  public Date getApprovalDate() {
    return this.getPlanDefinition().getApprovalDate();
  }
  public Period getEffectivePeriod(){
    return this.getPlanDefinition().getEffectivePeriod();
  }
}
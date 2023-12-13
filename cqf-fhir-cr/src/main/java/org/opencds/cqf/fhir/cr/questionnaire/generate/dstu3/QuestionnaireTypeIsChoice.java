package org.opencds.cqf.fhir.cr.questionnaire.generate.dstu3;

import java.util.List;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ElementDefinition;
import org.hl7.fhir.dstu3.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.ValueSet;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.dstu3.model.ValueSet.ValueSetExpansionContainsComponent;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.utility.dstu3.SearchHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuestionnaireTypeIsChoice {
    protected static final Logger logger = LoggerFactory.getLogger(QuestionnaireTypeIsChoice.class);
    protected Repository repository;

    public QuestionnaireTypeIsChoice(Repository repository) {
        this.repository = repository;
    }

    public QuestionnaireItemComponent addProperties(ElementDefinition element, QuestionnaireItemComponent item) {
        final ValueSet valueSet = getValueSet(element);
        if (valueSet.hasExpansion()) {
            addAnswerOptionsForValueSetWithExpansionComponent(valueSet, item);
        } else {
            addAnswerOptionsForValueSetWithComposeComponent(valueSet, item);
        }
        return item;
    }

    protected void addAnswerOptionsForValueSetWithExpansionComponent(
            ValueSet valueSet, QuestionnaireItemComponent item) {
        final List<ValueSetExpansionContainsComponent> expansionList =
                valueSet.getExpansion().getContains();
        expansionList.forEach(expansion -> {
            final Coding coding = getCoding(expansion);
            item.addOption().setValue(coding);
        });
    }

    protected void addAnswerOptionsForValueSetWithComposeComponent(ValueSet valueSet, QuestionnaireItemComponent item) {
        final List<ConceptSetComponent> systems = valueSet.getCompose().getInclude();
        systems.forEach(system -> system.getConcept().forEach(concept -> {
            final Coding coding = getCoding(concept, system.getSystem());
            item.addOption().setValue(coding);
        }));
    }

    protected Coding getCoding(ConceptReferenceComponent code, String systemUri) {
        return new Coding().setCode(code.getCode()).setSystem(systemUri).setDisplay(code.getDisplay());
    }

    protected Coding getCoding(ValueSetExpansionContainsComponent code) {
        return new Coding().setCode(code.getCode()).setSystem(code.getSystem()).setDisplay(code.getDisplay());
    }

    protected ValueSet getValueSet(ElementDefinition element) {
        final String valueSetUrl = element.getBinding().getValueSet().primitiveValue();
        return (ValueSet)
                SearchHelper.searchRepositoryByCanonical(repository, new StringType().setValue(valueSetUrl));
    }
}

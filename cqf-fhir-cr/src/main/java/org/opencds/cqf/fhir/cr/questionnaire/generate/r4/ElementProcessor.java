package org.opencds.cqf.fhir.cr.questionnaire.generate.r4;

import static org.opencds.cqf.fhir.cr.questionnaire.common.ItemValueTransformer.transformValue;

import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.ICompositeType;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemType;
import org.hl7.fhir.r4.model.Type;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.cr.common.IOperationRequest;
import org.opencds.cqf.fhir.cr.questionnaire.generate.ElementHasCqfExpression;
import org.opencds.cqf.fhir.cr.questionnaire.generate.ElementHasDefaultValue;
import org.opencds.cqf.fhir.cr.questionnaire.generate.IElementProcessor;
import org.opencds.cqf.fhir.utility.Constants;

public class ElementProcessor implements IElementProcessor {
    protected static final String ITEM_TYPE_ERROR = "Unable to determine type for element: %s";
    protected final QuestionnaireTypeIsChoice questionnaireTypeIsChoice;
    protected final ElementHasDefaultValue elementHasDefaultValue;
    protected final ElementHasCqfExpression elementHasCqfExpression;

    public ElementProcessor(Repository repository) {
        questionnaireTypeIsChoice = new QuestionnaireTypeIsChoice(repository);
        elementHasDefaultValue = new ElementHasDefaultValue();
        elementHasCqfExpression = new ElementHasCqfExpression();
    }

    @Override
    public IBaseBackboneElement processElement(
            IOperationRequest request,
            ICompositeType baseElement,
            String profileUrl,
            String childLinkId,
            IBaseResource caseFeature) {
        var element = (ElementDefinition) baseElement;
        final QuestionnaireItemType itemType = getItemType(element);
        final QuestionnaireItemComponent item = initializeQuestionnaireItem(itemType, profileUrl, element, childLinkId);
        if (itemType == QuestionnaireItemType.CHOICE) {
            questionnaireTypeIsChoice.addProperties(element, item);
        }
        if (element.hasFixedOrPattern()) {
            elementHasDefaultValue.addProperties(request, element.getFixedOrPattern(), item);
        } else if (element.hasDefaultValue()) {
            elementHasDefaultValue.addProperties(request, element.getDefaultValue(), item);
        } else if (element.hasExtension(Constants.CQF_EXPRESSION)) {
            elementHasCqfExpression.addProperties(request, element, item);
        } else if (caseFeature != null) {
            var path = element.getPath().split("\\.")[1].replace("[x]", "");
            var pathValue = request.resolvePath(caseFeature, path);
            if (pathValue instanceof Type) {
                item.addInitial().setValue(transformValue((Type) pathValue));
            }
        }
        item.setRequired(element.hasMin() && element.getMin() == 1);
        // set repeat based on? if expression result type is a list?
        // set enableWhen based on? use
        // http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-enableWhenExpression
        return item;
    }

    protected QuestionnaireItemComponent initializeQuestionnaireItem(
            QuestionnaireItemType itemType, String profileUrl, ElementDefinition element, String childLinkId) {
        final String definition = profileUrl + "#" + element.getPath();
        return new QuestionnaireItemComponent()
                .setType(itemType)
                .setDefinition(definition)
                .setLinkId(childLinkId)
                .setText(getElementText(element));
    }

    public QuestionnaireItemType getItemType(ElementDefinition element) {
        final String elementType = element.getType().get(0).getCode();
        final QuestionnaireItemType itemType = parseItemType(elementType, element.hasBinding());
        if (itemType == null) {
            final String message = String.format(ITEM_TYPE_ERROR, element.getId());
            throw new IllegalArgumentException(message);
        }
        return itemType;
    }

    public QuestionnaireItemType parseItemType(String elementType, Boolean hasBinding) {
        if (Boolean.TRUE.equals(hasBinding)) {
            return QuestionnaireItemType.CHOICE;
        }
        switch (elementType) {
            case "CodeableConcept":
                return QuestionnaireItemType.CHOICE;
            case "uri":
                return QuestionnaireItemType.URL;
            case "BackboneElement":
                return QuestionnaireItemType.GROUP;
            case "Quantity":
                return QuestionnaireItemType.QUANTITY;
            case "Reference":
                return QuestionnaireItemType.REFERENCE;
            case "code":
                return QuestionnaireItemType.STRING;
            default:
                return QuestionnaireItemType.fromCode(elementType);
        }
    }

    public String getElementText(ElementDefinition element) {
        return element.hasLabel() ? element.getLabel() : getElementDescription(element);
    }

    protected String getElementDescription(ElementDefinition element) {
        return element.hasShort() ? element.getShort() : element.getPath();
    }
}

package org.opencds.cqf.fhir.cr.questionnaire.generate.r4;

import static org.opencds.cqf.fhir.cr.questionnaire.common.ItemValueTransformer.transformValue;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Questionnaire.QuestionnaireItemComponent;
import org.hl7.fhir.r4.model.Type;
import org.opencds.cqf.fhir.utility.Constants;

public class ElementHasDefaultValue {
    public QuestionnaireItemComponent addProperties(Type value, QuestionnaireItemComponent questionnaireItem) {
        questionnaireItem.addInitial().setValue(transformValue(value));
        questionnaireItem.addExtension(Constants.SDC_QUESTIONNAIRE_HIDDEN, new BooleanType(true));
        questionnaireItem.setReadOnly(true);
        return questionnaireItem;
    }
}

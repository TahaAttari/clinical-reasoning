package org.opencds.cqf.fhir.cr.questionnaireresponse.r4.processparameters;

import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseCoding;
import org.opencds.cqf.fhir.cr.questionnaireresponse.common.ModelResolverService;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class QuestionnaireCodeMapHelper {
    ModelResolverService modelResolverService;

    public static class QuestionnaireCodeMap {
        private final String linkId;
        private final IBaseCoding coding;

        public QuestionnaireCodeMap(String linkId, IBaseCoding coding) {
            this.linkId = linkId;
            this.coding = coding;
        }
        public String getLinkId() {
            return linkId;
        }
        public IBaseCoding getCoding() {
            return coding;
        }
    }
    @Nonnull
    Map<String, List<IBaseCoding>> createCodeMap(@Nonnull IBaseBackboneElement questionnaire) {
        final List<IBaseBackboneElement> questionnaireItems = modelResolverService.getDynamicValues(questionnaire, "entry");
        final List<QuestionnaireCodeMap> questionnaireCodeMap = questionnaireItems.stream()
            .map(this::buildQuestionnaireCodeMap)
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return questionnaireCodeMap.stream()
            .collect(Collectors.groupingBy(
                QuestionnaireCodeMap::getLinkId,
                Collectors.mapping(QuestionnaireCodeMap::getCoding, Collectors.toList())
            )
        );
    }


    private List<QuestionnaireCodeMap> buildQuestionnaireCodeMap(IBaseBackboneElement item) {
        final List<QuestionnaireCodeMap> questionnaireCodeMap = new ArrayList<>();
        final List<IBaseBackboneElement> subItems = modelResolverService.getDynamicValues(item, "item");
        if (!subItems.isEmpty()) {
            subItems.forEach(subItem -> {
                final List<QuestionnaireCodeMap> codeMap = buildQuestionnaireCodeMap(subItem);
                questionnaireCodeMap.addAll(codeMap);
            });
        } else {
            final String linkId = modelResolverService.getDynamicStringValue(item, "linkId");
            final IBaseBackboneElement code = modelResolverService.getDynamicValue(item, "code");
            questionnaireCodeMap.add(new QuestionnaireCodeMap(linkId, (IBaseCoding) code));

        }
        return questionnaireCodeMap;
    }
}

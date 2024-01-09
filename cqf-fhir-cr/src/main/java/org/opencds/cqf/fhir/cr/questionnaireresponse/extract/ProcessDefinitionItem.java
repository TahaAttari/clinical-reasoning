package org.opencds.cqf.fhir.cr.questionnaireresponse.extract;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseReference;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.opencds.cqf.fhir.cr.common.ExpressionProcessor;
import org.opencds.cqf.fhir.cr.questionnaire.common.ResolveExpressionException;
import org.opencds.cqf.fhir.utility.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessDefinitionItem {
    protected static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionItem.class);
    final ExpressionProcessor expressionProcessor;

    public ProcessDefinitionItem() {
        this(new ExpressionProcessor());
    }

    private ProcessDefinitionItem(ExpressionProcessor expressionProcessor) {
        this.expressionProcessor = expressionProcessor;
    }

    public void processDefinitionItem(
            ExtractRequest request, IBaseBackboneElement item, List<IBaseResource> resources, IBaseReference subject)
            throws ResolveExpressionException {
        processDefinitionItem(request, item, resources, subject, null);
    }

    public void processDefinitionItem(
            ExtractRequest request,
            IBaseBackboneElement item,
            List<IBaseResource> resources,
            IBaseReference subject,
            IBaseExtension<?, ?> contextExtension)
            throws ResolveExpressionException {
        // Definition-based extraction -
        // http://build.fhir.org/ig/HL7/sdc/extraction.html#definition-based-extraction

        var contextExtensionUrl = Constants.SDC_QUESTIONNAIRE_ITEM_EXTRACTION_CONTEXT;
        var itemExtractionContext = request.hasExtension(item, contextExtensionUrl)
                ? expressionProcessor.getCqfExpression(request, request.getExtensions(item), contextExtensionUrl)
                : expressionProcessor.getCqfExpression(
                        request, request.getExtensions(request.getQuestionnaireResponse()), contextExtensionUrl);
        if (itemExtractionContext != null) {
            var context = expressionProcessor.getExpressionResultForItem(
                    request, itemExtractionContext, request.getItemLinkId(item));
            if (context != null && !context.isEmpty()) {
                // TODO: edit context instead of creating new resources
            }
        }

        var definition = request.resolvePathString(item, "definition");
        var resourceType = getDefinitionType(definition);
        var resource = (IBaseResource) newValue(request, resourceType);
        var resourceDefinition = request.getFhirContext().getElementDefinition(resource.getClass());
        resource.setId(new IdType(resourceType, request.getExtractId() + "-" + request.getItemLinkId(item)));
        resolveMeta(resource, definition);
        var subjectPath = getSubjectPath(resourceDefinition);
        if (subjectPath != null) {
            request.getModelResolver().setValue(resource, subjectPath, subject);
        }
        var authorPath = getAuthorPath(resourceDefinition);
        if (authorPath != null) {
            var authorValue = request.resolvePath(request.getQuestionnaireResponse(), "author");
            if (authorValue != null) {
                request.getModelResolver().setValue(resource, authorPath, authorValue);
            }
        }
        var dateAuthored = request.resolvePath(request.getQuestionnaireResponse(), "authored");
        if (dateAuthored != null) {
            var dateDefs = getDateDefs(resourceDefinition);
            if (dateDefs != null && !dateDefs.isEmpty()) {
                dateDefs.forEach(dateDef -> {
                    try {
                        // dateDef.
                        // request.getModelResolver().setValue(resource, dateDef.getElementName(), )
                        // var propertyDef = request.getFhirContext()
                        //         .getElementDefinition(
                        //                 p.getTypeCode().contains("|")
                        //                         ? p.getTypeCode().split("\\|")[0]
                        //                         : p.getTypeCode());
                        // if (propertyDef != null) {
                        //     modelResolver.setValue(
                        //             resource,
                        //             datePath,
                        //             propertyDef
                        //                     .getImplementingClass()
                        //                     .getConstructor(Date.class)
                        //                     .newInstance(questionnaireResponse.getAuthored()));
                        // }
                    } catch (Exception ex) {
                        var message = String.format(
                                "Error encountered attempting to set property (%s) on resource type (%s): %s",
                                dateDef.getElementName(), resource.fhirType(), ex.getMessage());
                        logger.error(message);
                        request.logException(message);
                    }
                });
            }
        }
        request.getItems(item).forEach(childItem -> {
            var childDefinition = request.resolvePathString(childItem, "definition");
            if (childDefinition != null) {
                var path = childDefinition.split("#")[1];
                // First element is always the resource type, so it can be ignored
                path = path.replace(resourceType + ".", "");
                var answers = request.resolvePathList(childItem, "answer", IBaseBackboneElement.class);
                var answerValue = answers.isEmpty() ? null : request.resolvePath(answers.get(0), "value");
                if (answerValue != null) {
                    request.getModelResolver().setValue(resource, path, answerValue);
                }
            }
        });

        resources.add(resource);
    }

    private String getDefinitionType(String definition) {
        if (!definition.contains("#")) {
            throw new IllegalArgumentException(
                    String.format("Unable to determine resource type from item definition: %s", definition));
        }
        return definition.split("#")[1];
    }

    private String getSubjectPath(BaseRuntimeElementDefinition<?> definition) {
        return definition.getChildByName("subject") != null
                ? "subject"
                : definition.getChildByName("patient") != null ? "patient" : null;
    }

    private String getAuthorPath(BaseRuntimeElementDefinition<?> definition) {
        return definition.getChildByName("recorder") != null
                ? "recorder"
                : definition.getName().equals("Observation") ? "performer" : null;
    }

    private List<BaseRuntimeChildDefinition> getDateDefs(BaseRuntimeElementDefinition<?> definition) {
        List<BaseRuntimeChildDefinition> results = new ArrayList<>();
        results.add(definition.getChildByName("onset"));
        results.add(definition.getChildByName("issued"));
        results.add(definition.getChildByName("effective"));
        results.add(definition.getChildByName("recordDate"));

        return results.stream().filter(p -> p != null).collect(Collectors.toList());
    }

    private void resolveMeta(IBaseResource resource, String definition) {
        var meta = resource.getMeta();
        // Consider setting source and lastUpdated here?
        if (definition != null && !definition.isEmpty()) {
            meta.addProfile(definition.split("#")[0]);
        }
    }

    private IBase newValue(ExtractRequest request, String type) {
        try {
            return (IBase) Class.forName(String.format(
                            "org.hl7.fhir.%s.model.%s",
                            request.getFhirVersion().toString().toLowerCase(), type))
                    .getConstructor()
                    .newInstance();
        } catch (ClassNotFoundException
                | IllegalAccessException
                | IllegalArgumentException
                | InstantiationException
                | NoSuchMethodException
                | SecurityException
                | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}

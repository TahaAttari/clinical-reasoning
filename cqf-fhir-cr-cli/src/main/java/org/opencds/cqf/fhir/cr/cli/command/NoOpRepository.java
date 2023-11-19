package org.opencds.cqf.fhir.cr.cli.command;

import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.opencds.cqf.fhir.api.Repository;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.api.MethodOutcome;

public class NoOpRepository implements Repository {

    private final FhirContext fhirContext;

    public NoOpRepository(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    @Override
    public <T extends IBaseResource, I extends IIdType> T read(Class<T> resourceType, I id,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <T extends IBaseResource> MethodOutcome create(T resource, Map<String, String> headers) {
        return null;
    }

    @Override
    public <I extends IIdType, P extends IBaseParameters> MethodOutcome patch(I id, P patchParameters,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <T extends IBaseResource> MethodOutcome update(T resource, Map<String, String> headers) {
        return null;
    }

    @Override
    public <T extends IBaseResource, I extends IIdType> MethodOutcome delete(Class<T> resourceType, I id,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle, T extends IBaseResource> B search(Class<B> bundleType, Class<T> resourceType,
            Map<String, List<IQueryParameterType>> searchParameters, Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle> B link(Class<B> bundleType, String url, Map<String, String> headers) {
        return null;
    }

    @Override
    public <C extends IBaseConformance> C capabilities(Class<C> resourceType, Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle> B transaction(B transaction, Map<String, String> headers) {
        return null;
    }

    @Override
    public <R extends IBaseResource, P extends IBaseParameters> R invoke(String name, P parameters, Class<R> returnType,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <P extends IBaseParameters> MethodOutcome invoke(String name, P parameters, Map<String, String> headers) {
        return null;
    }

    @Override
    public <R extends IBaseResource, P extends IBaseParameters, T extends IBaseResource> R invoke(Class<T> resourceType,
            String name, P parameters, Class<R> returnType, Map<String, String> headers) {
        return null;
    }

    @Override
    public <P extends IBaseParameters, T extends IBaseResource> MethodOutcome invoke(Class<T> resourceType, String name,
            P parameters, Map<String, String> headers) {
        return null;
    }

    @Override
    public <R extends IBaseResource, P extends IBaseParameters, I extends IIdType> R invoke(I id, String name,
            P parameters, Class<R> returnType, Map<String, String> headers) {
        return null;
    }

    @Override
    public <P extends IBaseParameters, I extends IIdType> MethodOutcome invoke(I id, String name, P parameters,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle, P extends IBaseParameters> B history(P parameters, Class<B> returnType,
            Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle, P extends IBaseParameters, T extends IBaseResource> B history(Class<T> resourceType,
            P parameters, Class<B> returnType, Map<String, String> headers) {
        return null;
    }

    @Override
    public <B extends IBaseBundle, P extends IBaseParameters, I extends IIdType> B history(I id, P parameters,
            Class<B> returnType, Map<String, String> headers) {
        return null;
    }

    @Override
    public FhirContext fhirContext() {
        return this.fhirContext;
    }
}

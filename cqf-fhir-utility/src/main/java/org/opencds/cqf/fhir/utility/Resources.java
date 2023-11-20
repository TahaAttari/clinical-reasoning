package org.opencds.cqf.fhir.utility;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseBackboneElement;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Resources {

    private Resources() {}

    public static <T extends IBase> Optional<T> castOrThrow(IBase obj, Class<T> type, String errorMessage) {
        if (obj == null) return Optional.empty();
        if (type.isInstance(obj)) {
            return Optional.of(type.cast(obj));
        }
        throw new IllegalArgumentException(errorMessage);
    }

    public static <T extends IBaseResource, I extends IIdType> T newResource(
            Class<T> theResourceClass, String theIdPart) {
        checkNotNull(theResourceClass);
        checkNotNull(theIdPart);
        checkArgument(
                !theIdPart.contains("/"), "theIdPart must be a simple id. Do not include resourceType or history");
        T resource = newResource(theResourceClass);

        @SuppressWarnings("unchecked")
        I id = (I) Ids.newId(theResourceClass, theIdPart);
        resource.setId(id);
        return resource;
    }

    public static <T extends IBaseResource> T newResource(Class<T> theResourceClass) {
        checkNotNull(theResourceClass);
        T resource = null;
        try {
            resource = theResourceClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "theResourceClass must be a type with an empty default constructor to use this function");
        }

        return resource;
    }

    public static <T extends IBaseBackboneElement> T newBackboneElement(Class<T> theBackboneElementClass) {
        checkNotNull(theBackboneElementClass);
        T backboneElement = null;
        try {
            backboneElement = theBackboneElementClass.getConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "theBackboneElementClass must be a type with an empty default constructor to use this function");
        }

        return backboneElement;
    }
}

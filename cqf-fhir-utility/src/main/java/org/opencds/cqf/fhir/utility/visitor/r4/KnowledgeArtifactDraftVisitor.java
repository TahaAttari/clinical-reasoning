package org.opencds.cqf.fhir.utility.visitor.r4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryRequestComponent;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Library;
import org.hl7.fhir.r4.model.MetadataResource;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.PlanDefinition;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.RelatedArtifact;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.UsageContext;
import org.hl7.fhir.r4.model.ValueSet;
import org.opencds.cqf.cql.evaluator.fhir.util.DependencyInfo;
import org.opencds.cqf.fhir.api.Repository;
import org.opencds.cqf.fhir.utility.Canonicals;
import org.opencds.cqf.fhir.utility.adapter.IBaseKnowledgeArtifactAdapter;
import org.opencds.cqf.fhir.utility.adapter.IBaseLibraryAdapter;
import org.opencds.cqf.fhir.utility.adapter.IBasePlanDefinitionAdapter;
import org.opencds.cqf.fhir.utility.adapter.ValueSetAdapter;
import org.opencds.cqf.fhir.utility.adapter.r4.AdapterFactory;
import org.opencds.cqf.fhir.utility.adapter.r4.KnowledgeArtifactAdapter;
import org.opencds.cqf.fhir.utility.adapter.r4.LibraryAdapter;
import org.opencds.cqf.fhir.utility.adapter.r4.r4KnowledgeArtifactAdapter;
import org.opencds.cqf.fhir.utility.adapter.r4.r4LibraryAdapter;
import org.opencds.cqf.fhir.utility.r4.MetadataResourceHelper;
import org.opencds.cqf.fhir.utility.r4.ResourceClassMapHelper;
import org.opencds.cqf.fhir.utility.repository.InMemoryFhirRepository;
import org.opencds.cqf.fhir.utility.visitor.KnowledgeArtifactVisitor;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;


public class KnowledgeArtifactDraftVisitor implements r4KnowledgeArtifactVisitor  {
  public Bundle visit(r4LibraryAdapter library, Repository repository, Parameters draftParameters) {
    // String version = ((StringType)(((Parameters)draftParameters).getParameter("version").getValue())).getValue();
    String version = MetadataResourceHelper.getParameter("version", draftParameters, StringType.class).map(r -> r.getValue()).orElseThrow(() -> new UnprocessableEntityException("The version argument is required"));
    Library libRes = (Library)library.get();
    // check valid semverversion
    checkVersionValidSemver(version);

    // remove release label and extension
    List<Extension> removeReleaseLabelAndDescription = libRes.getExtension()
        .stream()
        .filter(ext -> !ext.getUrl().equals(IBaseKnowledgeArtifactAdapter.releaseDescriptionUrl) && !ext.getUrl().equals(IBaseKnowledgeArtifactAdapter.releaseLabelUrl))
        .collect(Collectors.toList());
    libRes.setExtension(removeReleaseLabelAndDescription);
    // remove approval date
    libRes.setApprovalDate(null);
    // new draft version
    String draftVersion = version + "-draft";
    String draftVersionUrl = Canonicals.getUrl(library.getUrl()) + "|" + draftVersion;

    // Root artifact must NOT have status of 'Active'. Existing drafts of
    // reference artifacts with the right verison number will be adopted.
    // This check is performed here to facilitate that different treatment
    // for the root artifact and those referenced by it.
    if (libRes.getStatus() != Enumerations.PublicationStatus.ACTIVE) {
        throw new PreconditionFailedException(
            String.format("Drafts can only be created from artifacts with status of 'active'. Resource '%s' has a status of: %s", library.getUrl(), String.valueOf(libRes.getStatus())));
    }
    // Ensure only one resource exists with this URL
		Bundle existingArtifactsForUrl = searchResourceByUrl(draftVersionUrl, repository);
		if(existingArtifactsForUrl.getEntry().size() != 0){
			throw new PreconditionFailedException(
				String.format("A draft of Program '%s' already exists with version: '%s'. Only one draft of a program version can exist at a time.", library.getUrl(), draftVersionUrl));
		}
    // create draft resources
    List<MetadataResource> resourcesToCreate = createDraftsOfArtifactAndRelated(libRes, repository, version, new ArrayList<MetadataResource>());
    Bundle transactionBundle = new Bundle()
        .setType(Bundle.BundleType.TRANSACTION);
    List<IdType> urnList = resourcesToCreate.stream().map(res -> new IdType("urn:uuid:" + UUID.randomUUID().toString())).collect(Collectors.toList());
    TreeSet<String> ownedResourceUrls = createOwnedResourceUrlCache(resourcesToCreate);
    for (int i = 0; i < resourcesToCreate.size(); i++) {
        KnowledgeArtifactAdapter newResourceAdapter = new KnowledgeArtifactAdapter(resourcesToCreate.get(i));
        updateUsageContextReferencesWithUrns(resourcesToCreate.get(i), resourcesToCreate, urnList);
        updateRelatedArtifactUrlsWithNewVersions(combineComponentsAndDependencies(library), draftVersion, ownedResourceUrls);
        MetadataResource updateIdForBundle = (MetadataResource) newResourceAdapter.copy();
        updateIdForBundle.setId(urnList.get(i));
        transactionBundle.addEntry(createEntry(updateIdForBundle));
    }
    // return InMemoryFhirRepository.transactionStub(transactionBundle, repository);
    return repository.transaction(transactionBundle);

    // DependencyInfo --document here that there is a need for figuring out how to determine which package the dependency is in.
    // what is dependency, where did it originate? potentially the package?
  }
  public IBase visit(IBaseLibraryAdapter library, Repository repository, IBaseParameters draftParameters){
    return new OperationOutcome();
  }
  public IBase visit(IBaseKnowledgeArtifactAdapter library, Repository repository, IBaseParameters draftParameters){
    return new OperationOutcome();
  }
//   public IBase visit(r4KnowledgeArtifactAdapter library, Repository repository, IBaseParameters draftParameters){
//     return new OperationOutcome();
//   }
  public IBase visit(IBasePlanDefinitionAdapter planDefinition, Repository repository, IBaseParameters operationParameters) {
    List<DependencyInfo> dependencies = planDefinition.getDependencies();
    for (DependencyInfo dependency : dependencies) {
      System.out.println(String.format("'%s' references '%s'", dependency.getReferenceSource(), dependency.getReference()));
    }
    return new OperationOutcome();
  }

  public IBase visit(ValueSetAdapter valueSet, Repository repository, IBaseParameters operationParameters) {
    List<DependencyInfo> dependencies = valueSet.getDependencies();
    for (DependencyInfo dependency : dependencies) {
      System.out.println(String.format("'%s' references '%s'", dependency.getReferenceSource(), dependency.getReference()));
    }
    return new OperationOutcome();
  }
    private List<MetadataResource> createDraftsOfArtifactAndRelated(MetadataResource resource, Repository repository, String version, List<MetadataResource> resourcesToCreate) {
        String draftVersion = version + "-draft";
        String draftVersionUrl = Canonicals.getUrl(resource.getUrl()) + "|" + draftVersion;

        // TODO: Decide if we need both of these checks
        Optional<MetadataResource> existingArtifactsWithMatchingUrl = KnowledgeArtifactAdapter.findLatestVersion(searchResourceByUrl(draftVersionUrl, repository));
        Optional<MetadataResource> draftVersionAlreadyInBundle = resourcesToCreate.stream().filter(res -> res.getUrl().equals(Canonicals.getUrl(draftVersionUrl)) && res.getVersion().equals(draftVersion)).findAny();
        MetadataResource newResource = null;
        if (existingArtifactsWithMatchingUrl.isPresent()) {
            newResource = existingArtifactsWithMatchingUrl.get();
        } else if(draftVersionAlreadyInBundle.isPresent()) {
            newResource = draftVersionAlreadyInBundle.get();
        }

        if (newResource == null) {
            r4KnowledgeArtifactAdapter sourceResourceAdapter = new AdapterFactory().createKnowledgeArtifactAdapter(resource);
            sourceResourceAdapter.setEffectivePeriod(null);
            newResource = resource.copy();
            newResource.setStatus(Enumerations.PublicationStatus.DRAFT);
            newResource.setVersion(draftVersion);
            resourcesToCreate.add(newResource);
            for (RelatedArtifact ra : sourceResourceAdapter.getOwnedRelatedArtifacts()) {
            // If it’s an owned RelatedArtifact composed-of then we want to copy it
            // (references are updated in createDraftBundle before adding to the bundle
            // hence they are ignored here)
            // e.g. 
            // relatedArtifact: [
            //  { 
            //    resource: www.test.com/Library/123|0.0.1 <- try to update this reference to the latest version in createDraftBundle
            //   },
            //  { 
            //    extension: [{url: .../isOwned, valueBoolean: true}],
            //    resource: www.test.com/Library/190|1.2.3 <- resolve this resource, create a draft of it and recursively check descendants
            //  }
            // ]
                if (ra.hasUrl()) {
                    Bundle referencedResourceBundle = searchResourceByUrl(ra.getUrl(), repository);
                    processReferencedResourceForDraft(repository, referencedResourceBundle, ra, version, resourcesToCreate);
                } else if (ra.hasResource()) {
                    Bundle referencedResourceBundle = searchResourceByUrl(ra.getResourceElement().getValueAsString(), repository);
                    processReferencedResourceForDraft(repository, referencedResourceBundle, ra, version, resourcesToCreate);
                }
            }
        }
        return resourcesToCreate;
    }
	
	private void processReferencedResourceForDraft(Repository repository, Bundle referencedResourceBundle, RelatedArtifact ra, String version, List<MetadataResource> transactionBundle) {
		if (!referencedResourceBundle.getEntryFirstRep().isEmpty()) {
			Bundle.BundleEntryComponent referencedResourceEntry = referencedResourceBundle.getEntry().get(0);
			if (referencedResourceEntry.hasResource() && referencedResourceEntry.getResource() instanceof MetadataResource) {
				MetadataResource referencedResource = (MetadataResource) referencedResourceEntry.getResource();

				createDraftsOfArtifactAndRelated(referencedResource, repository, version, transactionBundle);
			}
		}
	}
    private void checkVersionValidSemver(String version) throws UnprocessableEntityException {
		if (version == null || version.isEmpty()) {
			throw new UnprocessableEntityException("The version argument is required");
		}
		if (version.contains("draft")) {
			throw new UnprocessableEntityException("The version cannot contain 'draft'");
		}
		if (version.contains("/") || version.contains("\\") || version.contains("|")) {
			throw new UnprocessableEntityException("The version contains illegal characters");
		}
		Pattern pattern = Pattern.compile("^(\\d+\\.)(\\d+\\.)(\\d+\\.)?(\\*|\\d+)$", Pattern.CASE_INSENSITIVE);
    	Matcher matcher = pattern.matcher(version);
    	boolean matchFound = matcher.find();
		if (!matchFound) {
			throw new UnprocessableEntityException("The version must be in the format MAJOR.MINOR.PATCH or MAJOR.MINOR.PATCH.REVISION");
		}
	}
    private TreeSet<String> createOwnedResourceUrlCache(List<MetadataResource> resources) {
		TreeSet<String> retval = new TreeSet<String>();
		resources.stream()
			.map(KnowledgeArtifactAdapter::new)
            .map(KnowledgeArtifactAdapter::getOwnedRelatedArtifacts).flatMap(List::stream)
            .filter(RelatedArtifact::hasResource).map(RelatedArtifact::getResource)
			.map(Canonicals::getUrl)
			.forEach(retval::add);
		return retval;
	}
	private void updateUsageContextReferencesWithUrns(MetadataResource newResource, List<MetadataResource> resourceListWithOriginalIds, List<IdType> idListForTransactionBundle) {
		List<UsageContext> useContexts = newResource.getUseContext();
		for (UsageContext useContext : useContexts) {
			// TODO: will we ever need to resolve these references?
			if (useContext.hasValueReference()) {
				Reference useContextRef = useContext.getValueReference();
				if (useContextRef != null) {
					resourceListWithOriginalIds.stream()
						.filter(resource -> (resource.getClass().getSimpleName() + "/" + resource.getIdElement().getIdPart()).equals(useContextRef.getReference()))
						.findAny()
						.ifPresent(resource -> {
							int indexOfDraftInIdList = resourceListWithOriginalIds.indexOf(resource);
							useContext.setValue(new Reference(idListForTransactionBundle.get(indexOfDraftInIdList)));
						});
				}
			}
		}
	}

	private void updateRelatedArtifactUrlsWithNewVersions(List<DependencyInfo> referenceList, String updatedVersion, TreeSet<String> ownedUrlCache){
		// For each  relatedArtifact, update the version of the reference.
		referenceList.stream()
			// only update the references to owned resources (including dependencies)
			.filter(ra -> ownedUrlCache.contains(Canonicals.getUrl(ra.getReference())))
			.collect(Collectors.toList())
			.replaceAll(ra -> {
        ra.setReference(Canonicals.getUrl(ra.getReference()) + "|" + updatedVersion);
        return ra;
      });
	}
  private List<DependencyInfo> combineComponentsAndDependencies(r4LibraryAdapter adapter) {
		return Stream.concat(adapter.getComponents().stream().map(ra -> convertRelatedArtifact(ra, adapter.getUrl() + "|" + adapter.getVersion())), adapter.getDependencies().stream()).collect(Collectors.toList());
	}
    private DependencyInfo convertRelatedArtifact(RelatedArtifact ra, String source) {
        return new DependencyInfo(source, ra.getResource(), ra.getExtension());
    }
  private BundleEntryComponent createEntry(IBaseResource resource) {
		BundleEntryComponent entry = new Bundle.BundleEntryComponent()
				.setResource((Resource) resource)
				.setRequest(createRequest(resource));
		String fullUrl = entry.getRequest().getUrl();
		if (resource instanceof MetadataResource) {
			MetadataResource metadataResource = (MetadataResource) resource;
			if (metadataResource.hasUrl()) {
				fullUrl = metadataResource.getUrl();
				if (metadataResource.hasVersion()) {
					fullUrl += "|" + metadataResource.getVersion();
				}
			}
		}
		entry.setFullUrl(fullUrl);
		return entry;
	}

	private BundleEntryRequestComponent createRequest(IBaseResource resource) {
		Bundle.BundleEntryRequestComponent request = new Bundle.BundleEntryRequestComponent();
		if (resource.getIdElement().hasValue() && !resource.getIdElement().getValue().contains("urn:uuid")) {
			request
				.setMethod(Bundle.HTTPVerb.PUT)
				.setUrl(resource.getIdElement().getValue());
		} else {
			request
				.setMethod(Bundle.HTTPVerb.POST)
				.setUrl(resource.fhirType());
		}
		return request;
	}
  /**
 * search by versioned Canonical URL
 * @param url canonical URL of the form www.example.com/Patient/123|0.1
 * @param repository to do the searching
 * @return a bundle of results
 */
	private Bundle searchResourceByUrl(String url, Repository repository) {
		Map<String, List<IQueryParameterType>> searchParams = new HashMap<>();

		List<IQueryParameterType> urlList = new ArrayList<>();
		urlList.add(new UriParam(Canonicals.getUrl(url)));
		searchParams.put("url", urlList);

		List<IQueryParameterType> versionList = new ArrayList<>();
		String version = Canonicals.getVersion(url);
		if (version != null && !version.isEmpty()) {
			versionList.add(new TokenParam(Canonicals.getVersion((url))));
			searchParams.put("version", versionList);
		}

		Bundle searchResultsBundle = repository.search(Bundle.class,ResourceClassMapHelper.getClass(Canonicals.getResourceType(url)), searchParams);
		return searchResultsBundle;
	}
    
}
/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.governance.api.generic;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.governance.api.common.GovernanceArtifactManager;
import org.wso2.carbon.governance.api.common.dataobjects.GovernanceArtifact;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for a generic governance artifact.
 */
@SuppressWarnings("unused")
public class GenericArtifactManager {

    private String artifactNameAttribute;
    private String artifactNamespaceAttribute;
    private String artifactElementNamespace;
    private GovernanceArtifactManager manager;

    /**
     * Constructor accepting an instance of the registry, and also details on the type of manager.
     *
     * @param registry                   the instance of the registry.
     * @param mediaType                  the media type of resources being saved or fetched.
     * @param artifactNameAttribute      the attribute that specifies the name of the artifact.
     * @param artifactNamespaceAttribute the attribute that specifies the namespace of the artifact.
     * @param artifactElementRoot        the attribute that specifies the root artifact element.
     * @param artifactElementNamespace   the attribute that specifies the artifact element's
     *                                   namespace.
     * @param pathExpression             the expression that can be used to compute where to store
     *                                   the artifact.
     * @param relationshipDefinitions    the relationship definitions for the types of associations
     *                                   that will be created when the artifact gets updated.
     */
    public GenericArtifactManager(Registry registry, String mediaType,
                                     String artifactNameAttribute,
                                     String artifactNamespaceAttribute, String artifactElementRoot,
                                     String artifactElementNamespace, String pathExpression,
                                     Association[] relationshipDefinitions) {
        manager = new GovernanceArtifactManager(registry, mediaType, artifactNameAttribute,
                artifactNamespaceAttribute, artifactElementRoot, artifactElementNamespace,
                pathExpression, relationshipDefinitions);
        this.artifactNameAttribute = artifactNameAttribute;
        this.artifactNamespaceAttribute = artifactNamespaceAttribute;
        this.artifactElementNamespace = artifactElementNamespace;
    }

    /**
     * Constructor accepting an instance of the registry, and key identifying the type of manager.
     * 
     * @param registry the instance of the registry.
     * @param key      the key short name of the artifact type.
     */
    public GenericArtifactManager(Registry registry, String key) throws RegistryException {
        try {
            GovernanceArtifactConfiguration configuration =
                    GovernanceUtils.findGovernanceArtifactConfiguration(key, registry);
            this.artifactNameAttribute = configuration.getArtifactNameAttribute();
            this.artifactNamespaceAttribute = configuration.getArtifactNamespaceAttribute();
            this.artifactElementNamespace = configuration.getArtifactElementNamespace();
            manager = new GovernanceArtifactManager(registry, configuration.getMediaType(),
                    this.artifactNameAttribute, this.artifactNamespaceAttribute,
                    configuration.getArtifactElementRoot(), this.artifactElementNamespace,
                    configuration.getPathExpression(), configuration.getRelationshipDefinitions());
        } catch (RegistryException e) {
            throw new GovernanceException("Unable to obtain governance artifact configuration", e);
        }
    }

    /**
     * Creates a new artifact from the given qualified name.
     *
     * @param qName the qualified name of this artifact.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(QName qName) throws GovernanceException {
        GenericArtifact genericArtifact = new GenericArtifact(manager.newGovernanceArtifact()) {};
        genericArtifact.setQName(qName);
        return genericArtifact;
    }

    /**
     * Creates a new artifact from the given content.
     *
     * @param content the artifact content.
     *
     * @return the artifact added.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact newGovernanceArtifact(OMElement content)
            throws GovernanceException {
        GenericArtifact genericArtifact =
                new GenericArtifact(manager.newGovernanceArtifact(content)) {};
        String name = GovernanceUtils.getAttributeValue(content,
                artifactNameAttribute, artifactElementNamespace);
        String namespace = (artifactNamespaceAttribute != null) ?
                GovernanceUtils.getAttributeValue(content,
                        artifactNamespaceAttribute, artifactElementNamespace) : null;
        if (name != null && !name.equals("")) {
            genericArtifact.setQName(new QName(namespace, name));
        } else {
            throw new GovernanceException("Unable to compute QName from given XML payload, " +
                    "please ensure that the content passed in matches the configuration.");
        }
        return genericArtifact;
    }

    /**
     * Adds the given artifact to the registry.
     *
     *
     * @param artifact the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void addGenericArtifact(GenericArtifact artifact) throws GovernanceException {
         manager.addGovernanceArtifact(artifact);
    }

    /**
     * Updates the given artifact on the registry.
     *
     * @param artifact the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void updateGenericArtifact(GenericArtifact artifact) throws GovernanceException {
        manager.updateGovernanceArtifact(artifact);
    }

    /**
     * Fetches the given artifact on the registry.
     *
     * @param artifactId the identifier of the artifact.
     *
     * @return the artifact.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact getGenericArtifact(String artifactId) throws GovernanceException {
        GovernanceArtifact governanceArtifact = manager.getGovernanceArtifact(artifactId);
        if (governanceArtifact == null) {
            return null;
        }
        return new GenericArtifact(governanceArtifact) {};
    }

    /**
     * Removes the given artifact from the registry.
     *
     * @param artifactId the identifier of the artifact.
     *
     * @throws GovernanceException if the operation failed.
     */
    public void removeGenericArtifact(String artifactId) throws GovernanceException {
        manager.removeGovernanceArtifact(artifactId);
    }

    /**
     * Finds all artifacts matching the given filter criteria.
     *
     * @param criteria the filter criteria to be matched.
     *
     * @return the artifacts that match.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact[] findGenericArtifacts(GenericArtifactFilter criteria)
            throws GovernanceException {
        List<GenericArtifact> artifacts = new ArrayList<GenericArtifact>();
        for (GenericArtifact artifact : getAllGenericArtifacts()) {
            if (artifact != null) {
                if (criteria.matches(artifact)) {
                    artifacts.add(artifact);
                }
            }
        }
        return artifacts.toArray(new GenericArtifact[artifacts.size()]);
    }

    /**
     * Finds all artifacts on the registry.
     *
     * @return all artifacts on the registry.
     * @throws GovernanceException if the operation failed.
     */
    public GenericArtifact[] getAllGenericArtifacts() throws GovernanceException {
        return getGenericArtifacts(manager.getAllGovernanceArtifacts());
    }

    // Method to obtain artifacts from governance artifacts.
    private GenericArtifact[] getGenericArtifacts(GovernanceArtifact[] governanceArtifacts) {
        List<GenericArtifact> artifacts =
                new ArrayList<GenericArtifact>(governanceArtifacts.length);
        for (GovernanceArtifact governanceArtifact : governanceArtifacts) {
            artifacts.add(new GenericArtifact(governanceArtifact) {});
        }
        return artifacts.toArray(new GenericArtifact[artifacts.size()]);
    }

    /**
     * Finds all identifiers of the artifacts on the registry.
     *
     * @return an array of identifiers of the artifacts.
     * @throws GovernanceException if the operation failed.
     */
    public String[] getAllGenericArtifactIds() throws GovernanceException {
        return manager.getAllGovernanceArtifactIds();
    }

}

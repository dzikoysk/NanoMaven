/*
 * Copyright (c) 2020 Dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.panda_lang.nanomaven;

import java.util.List;

public final class NanoConfiguration {

    private int port;
    private String hostname;

    private List<String> repositories;
    private boolean repositoryPathEnabled;
    private boolean indexingEnabled;
    private boolean deployEnabled;

    private boolean nestedMaven;
    private String externalMaven;

    public boolean isDeployEnabled() {
        return deployEnabled;
    }

    public String getExternalMaven() {
        return externalMaven;
    }

    public boolean isNestedMaven() {
        return nestedMaven;
    }

    public boolean isIndexingEnabled() {
        return indexingEnabled;
    }

    public boolean isRepositoryPathEnabled() {
        return repositoryPathEnabled;
    }

    public List<? extends String> getRepositories() {
        return repositories;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setRepositories(List<String> repositories) {
        this.repositories = repositories;
    }

    public void setRepositoryPathEnabled(boolean repositoryPathEnabled) {
        this.repositoryPathEnabled = repositoryPathEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled) {
        this.indexingEnabled = indexingEnabled;
    }

    public void setNestedMaven(boolean nestedMaven) {
        this.nestedMaven = nestedMaven;
    }

    public void setExternalMaven(String externalMaven) {
        this.externalMaven = externalMaven;
    }

    public void setDeployEnabled(boolean deployEnabled) {
        this.deployEnabled = deployEnabled;
    }

}

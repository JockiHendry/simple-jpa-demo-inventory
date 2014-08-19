/*
 * Copyright 2014 Jocki Hendry.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package project

import simplejpa.artifact.repository.RepositoryManager

import java.util.concurrent.ConcurrentHashMap

class StubRepositoryManager implements RepositoryManager {

    public final Map<String, Object> instances = new ConcurrentHashMap<>()

    public StubRepositoryManager() {

    }

    public Object findRepository(String name) {
        if (!name.endsWith('Repository')) name += 'Repository'
        instances[name]
    }

    @Override
    Object doInstantiate(String name, boolean triggerEvent) {
        return instances.get(name)
    }

    @Override
    Map<String, Object> getRepositories() {
        instances
    }

}

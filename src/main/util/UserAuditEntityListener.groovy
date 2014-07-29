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
package util

import domain.Container
import javax.persistence.PrePersist
import javax.persistence.PreUpdate

class UserAuditEntityListener {

    @PrePersist
    void create(Object target) {
        target.createdDate = Calendar.instance.time
        if (target.hasProperty('createdBy')) {
            target.createdBy = Container.app.currentUser?.nama
        }
    }

    @PreUpdate
    void update(Object target) {
        target.modifiedDate = Calendar.instance.time
        if (target.hasProperty('modifiedBy')) {
            target.modifiedBy = Container.app.currentUser?.nama
        }
    }

}

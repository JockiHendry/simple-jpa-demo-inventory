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

package project.main

import org.jdesktop.swingx.plaf.BusyLabelUI
import util.BusyLayerUI

import javax.swing.*
import java.awt.*
import java.awt.event.*
import griffon.util.GriffonNameUtils

class MainGroupController {

    def model
    def view
    def groupId

    def switchPage = { ActionEvent event, Map arguments = [:] ->

        execInsideUISync {
            BusyLayerUI.instance.show()

            // destroying previous MVCGroup before switching to a new one
            if (groupId) {
                app.mvcGroupManager.destroyMVCGroup(groupId)
            }

            groupId = event.actionCommand

            // destroying current MVCGroup if it was not destroyed properly before
            if (app.mvcGroupManager.findConfiguration(groupId)) {
                app.mvcGroupManager.destroyMVCGroup(groupId)
            }

            def (m, v, c) = app.mvcGroupManager.createMVCGroup(groupId, groupId, arguments)

            view.mainPanel.removeAll()
            view.mainPanel.add(v.mainPanel, BorderLayout.CENTER)
            view.mainPanel.revalidate()
            view.mainPanel.repaint()
            BusyLayerUI.instance.hide()                        
            view.mainFrame.title = "${app.config.application.title} ${app.metadata.getApplicationVersion()}: ${GriffonNameUtils.getNaturalName(groupId)}"
        }
    }

}
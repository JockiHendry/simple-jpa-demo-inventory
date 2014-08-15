package ${g.targetPackageName}

import javax.swing.*
import java.awt.*
import java.awt.event.*
import griffon.util.GriffonNameUtils

class ${g.scaffolding.startupGroupName}Controller {

	def model
	def view
	def groupId

	def switchPage = { ActionEvent event ->
		// destroying previous MVCGroup before switching to a new one
		if (groupId) {
			app.mvcGroupManager.destroyMVCGroup(groupId)
		}

		execInsideUISync { view.busyLabel.visible = true }

		groupId = event.actionCommand

		// destroying current MVCGroup if it was not destroyed properly before
		if (app.mvcGroupManager.findConfiguration(groupId)) {
			app.mvcGroupManager.destroyMVCGroup(groupId)
		}

		def (m,v,c) = app.mvcGroupManager.createMVCGroup(groupId, groupId)

		execInsideUIAsync {
			view.mainPanel.removeAll()
			view.mainPanel.add(v.mainPanel, BorderLayout.CENTER)
			view.mainPanel.revalidate()
			view.mainPanel.repaint()
			view.mainFrame.title = "\${app.config.application.title} \${app.metadata.getApplicationVersion()}: \${GriffonNameUtils.getNaturalName(groupId)}"
			view.busyLabel.visible = false
		}
	}

}
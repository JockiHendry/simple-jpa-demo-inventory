package ${g.targetPackageName}

import static ca.odell.glazedlists.gui.AbstractTableComparatorChooser.*
import static javax.swing.SwingConstants.*
import net.miginfocom.swing.MigLayout
import org.joda.time.*
import java.awt.*

actions {
    action(id: 'save', name: app.getMessage('simplejpa.dialog.update.button'), closure: controller.save)
    action(id: 'cancel', name: app.getMessage("simplejpa.dialog.cancel.button"), closure: controller.clear)
    action(id: 'delete', name: app.getMessage("simplejpa.dialog.delete.button"), closure: controller.delete)
    action(id: 'close', name: app.getMessage("simplejpa.dialog.close.button"), closure: controller.close)
${g.actions(1)}}

application(title: '${g.domainClassNameAsNatural}',
		preferredSize: [520, 340],
		pack: true,
		locationByPlatform: true,
		iconImage: imageIcon('/griffon-icon-48x48.png').image,
		iconImages: [imageIcon('/griffon-icon-48x48.png').image,
				imageIcon('/griffon-icon-32x32.png').image,
				imageIcon('/griffon-icon-16x16.png').image]) {

	panel(id: 'mainPanel') {
		borderLayout()

		panel(constraints: CENTER) {
			borderLayout()
			panel(constraints: PAGE_START, layout: new FlowLayout(FlowLayout.LEADING)) {
				label(text: bind('searchMessage', source: model))
			}
			scrollPane(constraints: CENTER) {
				glazedTable(id: 'table', list: model.${g.domainClassGlazedListVariable}, sortingStrategy: SINGLE_COLUMN, onValueChanged: controller.tableSelectionChanged${g.tableActions()}) {
${g.table(5)}
				}
			}
		}

		panel(id: "form", layout: new MigLayout('', '[right][left][left,grow]',''), constraints: PAGE_END, focusCycleRoot: true) {
${g.dataEntry(3)}

			panel(constraints: 'span, growx, wrap') {
				flowLayout(alignment: FlowLayout.LEADING)
				button(action: save)
				button(visible: bind{table.isRowSelected}, action: cancel)
				button(visible: bind{table.isRowSelected}, action: delete)
				button(action: close)
			}
		}
	}
}

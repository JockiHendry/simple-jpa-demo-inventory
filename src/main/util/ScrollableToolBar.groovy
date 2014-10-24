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

import javax.swing.*
import javax.swing.event.*
import javax.swing.plaf.basic.BasicArrowButton
import java.awt.*
import java.awt.event.*

public class ScrollableToolBar extends JPanel {

    JToolBar toolBar
    JScrollPane scrollPane
    JButton btnLeft
    JButton btnRight
    ActionLeft actionLeft = new ActionLeft()
    ActionRight actionRight = new ActionRight()

    public ScrollableToolBar() {
        toolBar = new JToolBar()
        scrollPane = new JScrollPane(toolBar, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.getViewport().addChangeListener(new DisplayButtonChangeListener())
        btnLeft = new BasicArrowButton(SwingConstants.WEST)
        btnLeft.setAction(actionLeft)
        btnLeft.addMouseListener(new PressButtonMouseAdapter(actionLeft))
        btnRight = new BasicArrowButton(SwingConstants.EAST)
        btnRight.addMouseListener(new PressButtonMouseAdapter(actionRight))

        setLayout(new BorderLayout())
        add(scrollPane, BorderLayout.CENTER)
        add(btnLeft, BorderLayout.LINE_START)
        add(btnRight, BorderLayout.LINE_END)
    }

    @Override
    public Component add(Component comp) {
        toolBar.add(comp)
    }

    public class ActionLeft extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            JScrollBar scrollBar = scrollPane.getHorizontalScrollBar()
            scrollBar.setValue(scrollBar.getValue() - scrollBar.getBlockIncrement())
        }

    }

    public class ActionRight extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            JScrollBar scrollBar = scrollPane.getHorizontalScrollBar()
            scrollBar.setValue(scrollBar.getValue() + scrollBar.getBlockIncrement())
        }

    }

    public class PressButtonMouseAdapter extends MouseAdapter implements ActionListener {

        private Action action
        private Timer timer

        public PressButtonMouseAdapter(final Action action) {
            this.action = action
            timer = new Timer(20, this)
        }

        @Override
        public void mousePressed(MouseEvent e) {
            timer.start()
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            timer.stop();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            action.actionPerformed(null);
        }
    }

    private class DisplayButtonChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            JViewport viewport = scrollPane.getViewport();
            boolean buttonVisible = (toolBar.getWidth() > viewport.getWidth());
            btnLeft.setVisible(buttonVisible);
            btnRight.setVisible(buttonVisible);
        }

    }

}

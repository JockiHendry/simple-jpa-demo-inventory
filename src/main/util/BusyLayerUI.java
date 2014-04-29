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

package util;

import javax.swing.*;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;

class BusyLayerUI extends LayerUI<JPanel> {

    public static final BusyLayerUI instance = new BusyLayerUI();
    public static BusyLayerUI getInstance() {
        return instance;
    }

    private boolean visible = false;

    private BusyLayerUI() {}

    public void show() {
        visible = true;
        firePropertyChange("visible", false, true);
    }

    public void hide() {
        visible = false;
        firePropertyChange("visible", true, false);
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        super.paint(g, c);
        if (!visible) return;

        int w = c.getWidth();
        int h = c.getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Composite currentComposite = g2.getComposite();

        // Buat layar terlihat seperti tidak aktif (lebih gelap dan kabur)
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g2.setPaint(new GradientPaint(0, 0, Color.BLACK, 0, h, Color.GRAY));
        g2.fillRect(0, 0, w, h);

        // Selesai
        g2.setComposite(currentComposite);
        g2.dispose();
    }

    @Override
    public void applyPropertyChange(PropertyChangeEvent evt, JLayer<? extends JPanel> l) {
        if ("visible".equals(evt.getPropertyName())) {
            l.repaint();
        }
    }

    @Override
    public void installUI(JComponent c) {
        super.installUI(c);
        ((JLayer)c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
    }

    @Override
    public void uninstallUI(JComponent c) {
        ((JLayer)c).setLayerEventMask(0);
        super.uninstallUI(c);
    }

    @Override
    public void eventDispatched(AWTEvent e, JLayer<? extends JPanel> l) {
        if (visible && e instanceof InputEvent) {
            ((InputEvent)e).consume();
        }
    }

}

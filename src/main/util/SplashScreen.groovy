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

import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JWindow
import java.awt.Color
import java.awt.Graphics
import java.awt.Image
import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder

class SplashScreen {

    private static SplashScreen instance
    public static SplashScreen getInstance() {
        if (instance==null) {
            instance = new SplashScreen()
        }
        instance
    }

    Image image
    JWindow window
    GriffonApplication app

    private SplashScreen() {
        app = ApplicationHolder.application
        image = ImageIO.read(app.getResourceAsURL('splashscreen.png'))
        window = new JWindow()
        window.setBackground(new Color(0,0,0,0))
        window.setSize(image.getWidth(null), image.getHeight(null))
        JComponent glassPane = new JPanel() {

            @Override
            protected void paintComponent(Graphics g) {
                g.drawImage(image, 0, 0, null)
                g.drawString("Versi ${app.metadata.getApplicationVersion()}", 180, 193)
            }

        }
        window.setGlassPane(glassPane)
        glassPane.size = window.size
        glassPane.setLayout(null)
        JLabel progress = new JLabel(new ImageIcon(app.getResourceAsURL('loading.gif')))
        progress.setBounds(110, 70, 180, 40)
        glassPane.add(progress)
        glassPane.visible = true
        //window.getContentPane().add(new JLabel(new ImageIcon(image)))

        window.setLocationRelativeTo(null)

    }

    public void show() {
        window.setVisible(true)
        window.toFront()
    }

    public void dispose() {
        window.setVisible(false)
        window.dispose()
    }

}

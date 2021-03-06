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

import domain.general.PesanGiroJatuhTempo
import domain.general.PesanLevelMinimum
import domain.general.PesanPiutangJatuhTempo
import groovy.xml.MarkupBuilder
import project.penjualan.FakturJualRepository
import project.user.PesanRepository
import simplejpa.SimpleJpaUtil
import simplejpa.swing.DialogUtils
import util.HttpUtil
import javax.swing.JEditorPane
import javax.swing.JOptionPane
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.FormSubmitEvent

@SuppressWarnings("GroovyUnusedDeclaration")
class PesanController {

    PesanModel model
    def view
    PesanRepository pesanRepository

    void mvcGroupInit(Map args) {
        refresh()
    }

    def refresh = {
        model.pesanList = pesanRepository.findAllPesan()
        app.event(PesanRepository.EVENT_UPDATE_PESAN, [!model.pesanList.empty])
        JEditorPane dashboard = view.dashboard

        def htmlOutput = new StringWriter()
        def builder = new MarkupBuilder(htmlOutput)
        builder.html {
            head {
                style(type: 'text/css') {
                    mkp.yieldUnescaped "h1{ margin-left: 10px; padding-top: 10px; color: #666666; font-size: 20px; font-family: Georgia, serif; border-top: 1px solid #888888;}\n" +
                        "body { font-size: 14px; }\n" +
                        "div { margin-left: 10px; }\n" +
                        ".info { color: #CC0000; }\n"
                }
            }
            body {
                img(src: "${app.getResourceAsURL('beranda.png')}")

                // Informasi untuk piutang yang akan jatuh tempo
                List piutangJatuhTempo = model.pesanList.findAll { it instanceof PesanPiutangJatuhTempo }
                h1 "Piutang Yang Akan Jatuh Tempo"
                div {
                    if (piutangJatuhTempo.empty) {
                        mkp.yield "Tidak ada piutang yang akan jatuh tempo."
                    } else {
                        mkp.yieldUnescaped "Terdapat <strong>${piutangJatuhTempo.size()}</strong> piutang yang akan segera jatuh tempo.  Berikut adalah daftar piutang tersebut:"
                        ul {
                            piutangJatuhTempo.each { PesanPiutangJatuhTempo p -> li {mkp.yieldUnescaped(p.pesan)} }
                        }
                    }
                }
                if (!piutangJatuhTempo.empty) {
                    div {
                        mkp.yieldUnescaped "Anda juga dapat melihat informasi ini dalam bentuk tabel di <a href='#piutang'>Screen Piutang</a>."
                    }
                }

                // Informasi untuk bilyet giro yang jatuh tempo
                List giroJatuhTempo = model.pesanList.findAll { it instanceof PesanGiroJatuhTempo }
                h1 "Bilyet Giro"
                div {
                    if (giroJatuhTempo.empty) {
                        mkp.yield "Tidak ada bilyet giro yang sudah jatuh tempo."
                    } else {
                        mkp.yieldUnescaped "Terdapat <strong>${giroJatuhTempo.size()}</strong> bilyet giro yang pembayarannya telah jatuh tempo.  Berikut adalah daftar bilyet giro tersebut:"
                        ul {
                            giroJatuhTempo.each { PesanGiroJatuhTempo p -> li {mkp.yieldUnescaped(p.pesan)}}
                        }
                    }
                }

                // Informasi untuk stock level
                List stockLevelMinimum = model.pesanList.findAll { it instanceof PesanLevelMinimum }
                h1 "Stock Level"
                div {
                    if (stockLevelMinimum.empty) {
                        mkp.yield 'Tidak ada produk yang jumlahnya di bawah level minimum.'
                    } else {
                        mkp.yieldUnescaped "Terdapat <strong>${stockLevelMinimum.size()}</strong> produk yang jumlahnya dibawah batas stock level.  Berikut adalah daftar produk tersebut:"
                        ul {
                            stockLevelMinimum.each { PesanLevelMinimum p -> li {mkp.yieldUnescaped(p.pesan)}}
                        }
                    }
                }

                // Saran
                h1 "Pesan"
                div {
                    mkp.yield "Anda ingin memberikan pesan, saran dan keluhan untuk developer?  Kami memiliki server yang online 24-jam untuk menampung masukan dari Anda."
                    div {
                        form(action: '#') {
                            p { textArea(rows: 5, cols: 50, name: 'saran') }
                            p { input(type: 'submit', value: 'Kirim Sekarang') }
                        }
                    }
                }
            }
        }

        dashboard.setContentType('text/html')
        dashboard.text = htmlOutput.toString()
        dashboard.editorKit.autoFormSubmission = false
    }

    def aksi = { HyperlinkEvent e ->
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (e instanceof FormSubmitEvent) {
                String userName = SimpleJpaUtil.instance.user?.userName
                if (HttpUtil.instance.sendNotification(userName, e.data)) {
                    DialogUtils.message(view.mainPanel, "Terima kasih, $userName. Server kami telah menerima masukan dari Anda.", 'Pesan Diterima', JOptionPane.INFORMATION_MESSAGE)
                } else {
                    DialogUtils.message(view.mainPanel, "Maaf, $userName.  Program sedang tidak dapat menghubungi server kami.", 'Pesan Gagal Dikirim', JOptionPane.ERROR_MESSAGE)
                }
            } else {
                if (e.description == '#piutang') {
                    app.mvcGroupManager.findGroup('mainGroup').view.mainTab.addMVCTab('piutang', [statusPiutangSearch: FakturJualRepository.StatusPiutangSearch.AKAN_JATUH_TEMPO])
                }
            }
        }
    }
}

package com.jamenoy.connect

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var port: UsbSerialPort? = null
    private var io: SerialInputOutputManager? = null
    private var bt: BluetoothSocket? = null

    private fun hex(b: ByteArray) = b.joinToString("") { "%02x".format(it) }
    private fun js(code: String) { web.post { web.evaluateJavascript(code, null) } }
    private fun log(s: String) { js("lg('" + s.replace("'", "\\'") + "')") }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= 31) perms += Manifest.permission.BLUETOOTH_CONNECT
        requestPermissions(perms.toTypedArray(), 1)
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(o: String, cb: GeolocationPermissions.Callback) = cb.invoke(o, true, false)
        }
        web.addJavascriptInterface(Bridge(), "Native")
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")
    }

    inner class Bridge {
        @JavascriptInterface fun usbOpen(baud: Int) {
            try {
                val m = getSystemService(Context.USB_SERVICE) as UsbManager
                val d = UsbSerialProber.getDefaultProber().findAllDrivers(m).firstOrNull()
                    ?: return log("ไม่พบสาย USB (ตรวจ OTG และสาย FTDI)")
                if (!m.hasPermission(d.device)) {
                    val pi = PendingIntent.getBroadcast(this@MainActivity, 0,
                        Intent("com.jamenoy.USB").setPackage(packageName), PendingIntent.FLAG_MUTABLE)
                    m.requestPermission(d.device, pi)
                    return log("กดอนุญาต USB แล้วกดเปิดพอร์ตอีกครั้ง")
                }
                val c = m.openDevice(d.device) ?: return log("เปิดอุปกรณ์ไม่ได้")
                port = d.ports[0].also {
                    it.open(c)
                    it.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
                }
                io = SerialInputOutputManager(port, object : SerialInputOutputManager.Listener {
                    override fun onNewData(data: ByteArray) = js("onRx('" + hex(data) + "')")
                    override fun onRunError(e: Exception) = log("USB error: " + e.message)
                }).also { it.start() }
                log("เปิดพอร์ต FTDI $baud 8N1 แล้ว")
            } catch (e: Exception) { log("ผิดพลาด: " + e.message) }
        }

        @JavascriptInterface fun brk(ms: Int) {
            try { port?.setBreak(true); Thread.sleep(ms.toLong()); port?.setBreak(false) }
            catch (e: Exception) { log("break: " + e.message) }
        }

        @JavascriptInterface fun write(h: String) {
            try {
                val b = ByteArray(h.length / 2) { h.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
                if (bt != null) bt!!.outputStream.write(b) else port?.write(b, 1000) ?: log("ยังไม่ได้เปิดพอร์ต")
            } catch (e: Exception) { log("write: " + e.message) }
        }

        @JavascriptInterface fun btList(): String =
            BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.joinToString("|") { it.name + "," + it.address } ?: ""

        @JavascriptInterface fun btOpen(addr: String) {
            Thread {
                try {
                    val dev = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(addr)
                    val sock = dev.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
                    sock.connect(); bt = sock
                    log("เชื่อมต่อบลูทูธ " + dev.name + " แล้ว")
                    val buf = ByteArray(256)
                    while (true) { val n = sock.inputStream.read(buf); if (n < 0) break; js("onRx('" + hex(buf.copyOf(n)) + "')") }
                } catch (e: Exception) { log("บลูทูธ: " + e.message); bt = null }
            }.start()
        }
    }
}

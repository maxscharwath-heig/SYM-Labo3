package ch.heigvd.sym.sym_labo3

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.IntentFilter
import android.util.Log
import android.widget.Button
import java.time.Duration
import java.time.LocalDateTime


class NFCActivity : AppCompatActivity() {

    companion object {
        private enum class SecurityLevel(val duration: Int) {
            HIGH(10),
            MEDIUM(30),
            LOW(60);
        }
    }

    private lateinit var max_button : Button
    private lateinit var medium_button : Button
    private lateinit var low_button : Button
    private var nfcAdapter: NfcAdapter? = null
    private var lastNFCTime : LocalDateTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        max_button = findViewById(R.id.btn_max_secu)
        medium_button = findViewById(R.id.btn_med_secu)
        low_button = findViewById(R.id.btn_low_secu)

        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show()
            finish()
            return

        }
        if (!nfcAdapter?.isEnabled!!) {
            Toast.makeText(
                this,
                "NFC disabled on this device. Turn on to proceed",
                Toast.LENGTH_SHORT
            ).show()
        }

        max_button.setOnClickListener {
            if (Duration.between(lastNFCTime, LocalDateTime.now()).seconds <= SecurityLevel.HIGH.duration) {
                Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this, "Too late !", Toast.LENGTH_SHORT).show()
            }
        }

        lastNFCTime = LocalDateTime.now()
    }

    private fun setupForegroundDispatch() {
        if (nfcAdapter == null) return
        val intent = Intent(this.applicationContext, this.javaClass)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this.applicationContext, 0, intent, 0)
        val filters = arrayOfNulls<IntentFilter>(1)
        val techList = arrayOf<Array<String>>()
        // On souhaite être notifié uniquement pour les TAG au format NDEF
        filters[0] = IntentFilter()
        filters[0]!!.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
        filters[0]!!.addCategory(Intent.CATEGORY_DEFAULT)
        try {
            filters[0]!!.addDataType("text/plain")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            Log.e(TAG, "MalformedMimeTypeException", e)
        }
        nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, filters, techList)
    }

    // called in onPause()
    private fun stopForegroundDispatch() {
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResume() {
        super.onResume()
        setupForegroundDispatch()
    }

    override fun onPause() {
        super.onPause()
        stopForegroundDispatch();
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        var action = intent?.action
        if(action == null) action = "nothing"
        Toast.makeText(this, action, Toast.LENGTH_SHORT).show()
    }
}
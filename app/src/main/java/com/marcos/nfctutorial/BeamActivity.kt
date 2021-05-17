package com.marcos.nfctutorial

import android.content.Context
import android.media.RingtoneManager
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException


class BeamActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private lateinit var mNfcAdapter: NfcAdapter
    private lateinit var mUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beam)
        val mEtWeb = findViewById<EditText>(R.id.etAmount)
        val mBtnSend = findViewById<Button>(R.id.btnSend)

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        //Chequeamos que el dispositivo cuente con tecnología NFC
        if (mNfcAdapter == null) {
            Toast.makeText(this, "Este dispositivo no cuenta con tecnología NFC.", Toast.LENGTH_LONG).show()
        } else {
            //Chequeamos que la antena NFC este encendida
            if (!mNfcAdapter.isEnabled) {
                Toast.makeText(this, "Antena Nfc desactivada.", Toast.LENGTH_LONG).show()
            }
        }

        mBtnSend.setOnClickListener { v ->
            mUrl = "${"https://www."}${mEtWeb.text}${".com"}"
            Toast.makeText(this, "Url creada: $mUrl " + ". Acercá el dispositivo a la etiqueta NFC"  , Toast.LENGTH_LONG).show()
            v.hideKeyboard()
        }

    }

    override fun onTagDiscovered(tag: Tag) {
        // Los Tags NFC pueden soportar diferentes tecnologías. En este caso utilizaremos
        // Tecnología Ndef
        var mNdef = Ndef.get(tag);

        if (mNdef != null) {

            // Creamos un Ndef Record a partir de una Uri con nuestra Url
            var mRecord = NdefRecord.createUri(mUrl);

            // Agregamos el NdefRecord a nuestro NdefMessage
            var mNdefMsg = NdefMessage(mRecord)

            //Intentamos abrir una conexión y escribir el Tag con nuestro NdefMessage
            try {
                mNdef.connect();
                mNdef.writeNdefMessage(mNdefMsg);

                // Si el Tag fue escrito con éxito mostramos el Toast
                runOnUiThread {
                    Toast.makeText(this, "Tag escrito con éxito", Toast.LENGTH_SHORT).show()
                }

                // Make a Sound
                try {
                    var notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    var ringtone = RingtoneManager.getRingtone(
                        applicationContext,
                        notification
                    );
                    ringtone.play();
                } catch (e: Exception) {
                    // Some error playing sound
                    runOnUiThread {
                        Toast.makeText(this, "Error al intentar escribir", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            } catch (e: Exception) {
                // Acá entramos si el Tag es inválido
            } finally {
                // Cerramos la conexión con el Tag (Evitemos errores y mal uso de recursos)
                try {
                    mNdef.close();
                } catch (e: IOException) {
                    // Mostramos mensaje en caso de que la operación se haya interrumpido
                    Toast.makeText(this, "Error al intentar escribir", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Tag Invalido", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter != null) {
            val options = Bundle()
            // Agregamos unos milisegundos adicionales para que el Tag se lea correctamente
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            // Habilitamos el modo de lectura para que detecte el Tag
            mNfcAdapter.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        // Deshabilitamos el modo de lectura para que solamente detectemos Tags con la App en 1er plano
        mNfcAdapter.disableReaderMode(this);
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

}
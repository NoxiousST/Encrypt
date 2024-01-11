package net.stiller.encrypt.ui.main.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import net.stiller.encrypt.R
import net.stiller.encrypt.databinding.FragmentHomeBinding
import net.stiller.encrypt.utils.Crypt
import org.apache.commons.lang3.StringUtils

class HomeFragment : Fragment(), View.OnClickListener {

    private var key: String? = null
    private val crypt = Crypt()
    private lateinit var b: FragmentHomeBinding

    var type = MutableLiveData<Int>()
    var tEnc = MutableLiveData<String>()
    var tDec = MutableLiveData<String>()
    var tKey = MutableLiveData<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type.value = Int.MIN_VALUE
        tEnc.value = StringUtils.EMPTY
        tDec.value = StringUtils.EMPTY
        tKey.value = StringUtils.EMPTY
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        b = FragmentHomeBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.frag = this
        b.lifecycleOwner = this

        observeTextChanges(b.editTextKey) { tKey.value = it }
        observeTextChanges(b.editTextEncrypt) { tEnc.value = it }
        observeTextChanges(b.editTextDecrypt) { tDec.value = it }

        val types = listOf("DES-ECB-ZeroBytePadding", "AES-ECB-PKCS5Padding")
        val adapterType = ArrayAdapter(requireContext(), R.layout.list_type, types)
        b.actvType.setAdapter(adapterType)

        b.actvType.setOnItemClickListener { _, _, position, _ ->
            type.value = position
            if (tKey.value!!.length > 8 && type.value == 0)
                b.editTextKey.setText(key!!.substring(0, 8))
        }

        b.ptCopy.setOnClickListener(this)
        b.ctCopy.setOnClickListener(this)

        b.ptPaste.setOnClickListener(this)
        b.ctPaste.setOnClickListener(this)

        b.ptClear.setOnClickListener(this)
        b.ctClear.setOnClickListener(this)

        b.keyShuffle.setOnClickListener(this)
        b.btnEncrypt.setOnClickListener(this)
        b.btnDecrypt.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val clipboardManager =
            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        when (v) {
            b.btnEncrypt -> {
                val encryption =
                    if (type.value == 0) crypt.encryptDES(tEnc.value!!, tKey.value!!)
                    else crypt.encryptAES(tEnc.value!!, tKey.value!!)
                b.editTextDecrypt.setText(encryption)
            }

            b.btnDecrypt -> {
                val decryption =
                    if (type.value == 0) crypt.decryptDES(tDec.value!!, tKey.value!!)
                    else crypt.decryptAES(tDec.value!!, tKey.value!!)
                b.editTextEncrypt.setText(decryption)
            }

            b.keyShuffle -> {
                key = crypt.generateKey()
                if (type.value == 0) key = key!!.substring(0, 8)
                b.editTextKey.setText(key)
            }

            b.ptCopy -> {
                if (b.editTextEncrypt.text!!.isNotEmpty()) {
                    val clipData =
                        ClipData.newPlainText("text", tEnc.value!!)
                    clipboardManager.setPrimaryClip(clipData)
                }
            }

            b.ctCopy -> {
                if (b.editTextEncrypt.text.toString().isNotEmpty()) {
                    val clipData =
                        ClipData.newPlainText("text", tDec.value!!)
                    clipboardManager.setPrimaryClip(clipData)
                }
            }

            b.ptPaste -> {
                if (clipboardManager.hasPrimaryClip()) {
                    val item = clipboardManager.primaryClip!!.getItemAt(0)
                    val textFromClipboard = item.text.toString()
                    b.editTextEncrypt.append(textFromClipboard)
                }
            }

            b.ctPaste -> {
                if (clipboardManager.hasPrimaryClip()) {
                    val item = clipboardManager.primaryClip!!.getItemAt(0)
                    val textFromClipboard = item.text.toString()
                    b.editTextDecrypt.append(textFromClipboard)
                }
            }

            b.ctClear -> b.editTextDecrypt.setText("")
            b.ptClear -> b.editTextEncrypt.setText("")
        }
    }


    private fun observeTextChanges(editText: EditText, onNextAction: (String) -> Unit) {
        val observable: Observable<CharSequence> = editText.textChanges().skipInitialValue()
        observable.subscribe(object : DisposableObserver<CharSequence>() {
            override fun onNext(t: CharSequence) {
                onNextAction(t.toString())
            }

            override fun onError(e: Throwable) {
                // Handle error if needed
            }

            override fun onComplete() {
                // Handle completion if needed
            }
        })
    }

    companion object {
        private fun newInstance() = HomeFragment()
        private const val TAG = "LoginFragment"
    }

}



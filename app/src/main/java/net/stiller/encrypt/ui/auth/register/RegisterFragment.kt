package net.stiller.encrypt.ui.auth.register

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.stiller.encrypt.R
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.databinding.FragmentRegisterBinding
import net.stiller.encrypt.ui.auth.AuthActivity
import net.stiller.encrypt.ui.auth.AuthViewModel
import net.stiller.encrypt.ui.auth.login.LoginFragment
import net.stiller.encrypt.ui.main.MainActivity
import net.stiller.encrypt.utils.AppUtils
import net.stiller.encrypt.utils.AuthStatus

class RegisterFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: AuthViewModel
    private lateinit var b: FragmentRegisterBinding

    private var isDisplayName = false
    private var isEmail = false
    private var isPassword = false
    private var isCPassword = false
    private var isPhone = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        b = FragmentRegisterBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intView()
        subscribeObservers()
        formValidation()
    }

    private fun intView() {
        b.btnRegister.setOnClickListener(this)
        b.toLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            b.btnRegister -> performRegister()
            b.toLogin -> (activity as AuthActivity?)!!
                .replaceFragment(LoginFragment.newInstance())
        }
    }

    private fun performRegister() {
        b.btnRegister.startAnimation()
        val user = User()
        user.username = b.etUsername.text.toString()
        user.email = b.etEmail.text.toString()
        user.password = b.etPassword.text.toString()
        viewModel.register(user)
    }

    private fun subscribeObservers() {
        viewModel.onRegister.observe(viewLifecycleOwner) { stateResource ->
            stateResource?.let {
                when (stateResource.status) {
                    AuthStatus.SUCCESS -> {
                        b.btnRegister.doneLoadingAnimation(R.color.green,
                            AppUtils.drawableToBitmap(requireContext(), R.drawable.ic_check)!!
                        )
                        lifecycleScope.launch {
                            delay(600)
                            toMainActivity()
                        }
                    }
                    AuthStatus.ERROR -> {
                        b.btnRegister.revertAnimation()
                        b.btnRegister.recoverInitialState()
                        showSnackBar(stateResource.message)
                    }
                    AuthStatus.LOADING -> {}
                }
            }
        }
    }

    private fun formValidation() {
        val usernameObservable: Observable<Boolean> = b.etUsername.textChanges().skipInitialValue()
            .map { isValidForm(it.toString(), 0) }
        val emailObservable: Observable<Boolean> = b.etEmail.textChanges().skipInitialValue()
            .map { isValidForm(it.toString(), 1) }
        val passwordObservable: Observable<Boolean> = b.etPassword.textChanges().skipInitialValue()
            .map { isValidForm(it.toString(), 2) }
        val cPasswordObservable: Observable<Boolean> = b.etCPassword.textChanges().skipInitialValue()
            .map { isValidForm(it.toString(), 3) }
        val phoneTextObservable: Observable<CharSequence> = b.etPhone.textChanges().skipInitialValue()

        phoneTextObservable.subscribe(object : DisposableObserver<CharSequence>() {
            override fun onNext(t: CharSequence) {
                val s = b.etPhone.editableText
                val prefixLength = 4

                // Disable delete prefix
                if (t.length <= prefixLength - 1) {
                    s.insert(prefixLength - 1, " ")
                }

                // Replace starting 0
                if (t.length == prefixLength + 2 && t[prefixLength] == '0') {
                    s.delete(prefixLength, prefixLength + 1)
                }

                // Insert char where needed.
                if (t.length == 4 + prefixLength || t.length == 9 + prefixLength) {
                    val c = t[t.length - 1]
                    // Only if its a digit where there should be a space we insert a space
                    if (Character.isDigit(c) && TextUtils.split(t.toString(), " ").size <= 3) {
                        s.insert(
                            t.length - 1, "-"
                        )
                    }
                }

                // Remove spacing char
                if (t.length == 4 + prefixLength || t.length == 9 + prefixLength) {
                    val c = t[t.length - 1]
                    if (c == '-') {
                        s.delete(t.length - 1, t.length)
                    }
                }

            }

            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
        val phoneObservable = phoneTextObservable.map { isValidForm(it.toString(), 4) }

        val combined1Observable = Observable.merge(usernameObservable, emailObservable, phoneObservable)
        val combined2Observable = Observable.merge(passwordObservable, cPasswordObservable)
        val combinedObservable = Observable.merge(combined1Observable, combined2Observable)

        combinedObservable.subscribe(object : DisposableObserver<Boolean>() {
            override fun onNext(t: Boolean) {
                b.btnRegister.isEnabled = t
            }
            override fun onError(e: Throwable) {}
            override fun onComplete() {}
        })
    }

    private fun isValidForm(text: String, index: Int): Boolean {
        with(b) {
            when (index) {
                0 -> {
                    tlUsername.isErrorEnabled = text.isEmpty()
                    tlUsername.error = if (!tlUsername.isErrorEnabled) null else "Username should not be empty"
                    isDisplayName = tlUsername.error == null
                }
                1 -> {
                    tlEmail.isErrorEnabled = !(Patterns.EMAIL_ADDRESS.matcher(text).matches() || text.isEmpty())
                    tlEmail.error = if (!tlEmail.isErrorEnabled) null else "Please enter a valid email address"
                    isEmail = tlEmail.error == null
                }
                2 -> {
                    tlPassword.isErrorEnabled = !(text.length > 7 || text.isEmpty())
                    tlPassword.error = if (!tlPassword.isErrorEnabled) null else "Password must contain 8 or more digits"

                    tlCPassword.isErrorEnabled = !(text == etCPassword.text.toString() || etCPassword.text.toString().isEmpty())
                    tlCPassword.error = if (!tlCPassword.isErrorEnabled) null else "Password confirmation does not match"
                    isPassword = tlPassword.error == null
                    isCPassword = tlCPassword.error == null
                }
                3 -> {
                    tlCPassword.isErrorEnabled = !(text == etPassword.text.toString() || text.isEmpty())
                    tlCPassword.error = if (!tlCPassword.isErrorEnabled) null else "Password confirmation does not match"
                    isCPassword = tlCPassword.error == null
                }
                4 -> {
                    tlPhone.isErrorEnabled = !(text.length == 17 || text.isEmpty())
                    tlPhone.error = if (!tlPhone.isErrorEnabled) null else "Incorrect phone number"
                    isPhone = tlPhone.error == null
                }
            }

            return isDisplayName && isEmail && isPassword && isCPassword && isPhone
        }
    }

    private fun toMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun showSnackBar(msg: String?) {
        Snackbar.make(b.root, msg!!, Snackbar.LENGTH_LONG)
            .setBackgroundTint(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.md_theme_dark_surface
                )
            )
            .show()
    }


    companion object {
        private const val TAG = "RegisterFragment"
        fun newInstance() = RegisterFragment()
    }


}
package net.stiller.encrypt.ui.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.stiller.encrypt.R
import net.stiller.encrypt.databinding.FragmentLoginBinding
import net.stiller.encrypt.ui.auth.AuthActivity
import net.stiller.encrypt.ui.auth.AuthViewModel
import net.stiller.encrypt.ui.auth.register.RegisterFragment
import net.stiller.encrypt.ui.main.MainActivity
import net.stiller.encrypt.utils.AppUtils.Companion.drawableToBitmap
import net.stiller.encrypt.utils.AuthStatus

class LoginFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: AuthViewModel
    private lateinit var b: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        b = FragmentLoginBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intView()
        subscribeObservers()
    }

    private fun intView() {
        b.btnLogin.setOnClickListener(this)
        b.toRegister.setOnClickListener(this)
        b.btnForget.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v) {
            b.btnLogin -> performLogin()
            b.toRegister -> (activity as AuthActivity?)!!
                .replaceFragment(RegisterFragment.newInstance())
            b.btnForget -> {}
        }
    }

    private fun performLogin() {
        b.btnLogin.startAnimation()
        val email = b.etUserEmail.text.toString().trim { it <= ' ' }
        val password = b.etPassword.text.toString().trim { it <= ' ' }
        viewModel.login(email, password)
    }

    private fun subscribeObservers() {
        viewModel.onLogin.observe(viewLifecycleOwner) {
            when (it.status) {
                AuthStatus.SUCCESS -> {
                    b.btnLogin.doneLoadingAnimation(R.color.green,
                        drawableToBitmap(requireContext(), R.drawable.ic_check)!!)
                    lifecycleScope.launch {
                        delay(600)
                        toMainActivity()
                    }
                }

                AuthStatus.ERROR -> {
                    b.btnLogin.revertAnimation()
                    b.btnLogin.recoverInitialState()
                    showSnackBar(it.message)
                }
                AuthStatus.LOADING -> {}
            }
        }
    }

    private fun toMainActivity() {
        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }


    private fun showSnackBar(msg: String?) {
        Snackbar.make(b.root, msg!!, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(requireContext(), R.color.md_theme_dark_surface))
            .setTextColor(getColor(requireContext(), R.color.md_theme_dark_onSurface))
            .show()
    }

    companion object {
        private const val TAG = "LoginFragment"
        fun newInstance() = LoginFragment()
    }
}
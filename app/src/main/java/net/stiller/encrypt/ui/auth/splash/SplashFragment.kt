package net.stiller.encrypt.ui.auth.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.stiller.encrypt.databinding.FragmentSplashBinding
import net.stiller.encrypt.ui.auth.AuthActivity
import net.stiller.encrypt.ui.auth.AuthViewModel
import net.stiller.encrypt.ui.auth.login.LoginFragment
import net.stiller.encrypt.ui.main.MainActivity

class SplashFragment : Fragment() {

    private lateinit var viewModel: AuthViewModel
    private lateinit var b: FragmentSplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AuthViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        b = FragmentSplashBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        runBlocking {
            changeView()
        }
    }

    private suspend fun changeView() {
        delay(1000).apply {
            viewModel.session().observe(viewLifecycleOwner) { aBoolean: Boolean ->
                if (aBoolean) {
                    val act = (activity as AuthActivity)
                    act.startActivity(Intent(act, MainActivity::class.java))
                    act.finish()
                } else {
                    (activity as AuthActivity?)!!
                        .replaceFragment(LoginFragment.newInstance())
                }
            }
        }
    }

    companion object {
        private const val TAG = "SplashFragment"
        fun newInstance() = SplashFragment()
    }
}
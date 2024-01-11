package net.stiller.encrypt.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.CompletableObserver
import io.reactivex.rxjava3.core.FlowableSubscriber
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import net.stiller.encrypt.data.models.User
import net.stiller.encrypt.data.repository.FirestoreRepository
import net.stiller.encrypt.utils.StateAuth
import net.stiller.encrypt.utils.StateAuth.Companion.error
import net.stiller.encrypt.utils.StateAuth.Companion.loading
import net.stiller.encrypt.utils.StateAuth.Companion.success
import org.reactivestreams.Subscription


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val firestoreRepository: FirestoreRepository
    val onLogin: MediatorLiveData<StateAuth<*>>
    val onRegister: MediatorLiveData<StateAuth<*>>
    private val disposable: CompositeDisposable

    init {
        firestoreRepository = FirestoreRepository(application)
        onLogin = MediatorLiveData()
        onRegister = MediatorLiveData()
        disposable = CompositeDisposable()
    }

    fun login(email: String?, password: String?) {
        firestoreRepository.login(email!!, password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : FlowableSubscriber<User> {
                override fun onSubscribe(s: Subscription) {
                    s.request(Long.MAX_VALUE)
                }

                override fun onNext(user: User) {
                    runBlocking {
                        firestoreRepository.writeUserStore(user)
                    }
                    onLogin.value = loading<Any>()
                }

                override fun onError(e: Throwable) {
                    onLogin.value = error<Any>(e.message!!)
                }

                override fun onComplete() {
                    onLogin.value = success<Any>()
                }
            })
    }

    fun register(user: User) {
        firestoreRepository.register(user)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    onRegister.value = loading<Any>()
                }

                override fun onComplete() {
                    runBlocking {
                        firestoreRepository.writeUserStore(user)
                    }
                    onRegister.value = success<Any>()
                }

                override fun onError(e: Throwable) {
                    onRegister.value = error<Any>(e.message!!)
                }
            })
    }

    fun session(): MutableLiveData<Boolean> {
        val isLogin = MutableLiveData<Boolean>()
        val d = firestoreRepository.isSession()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ value: Boolean -> isLogin.postValue(value) })
            { throwable: Throwable? ->
                Log.e(TAG, "Error reading user store", throwable)
            }
        disposable.add(d)
        return isLogin
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    companion object {
        private const val TAG = "LoginViewModel"
    }
}

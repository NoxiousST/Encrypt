package net.stiller.encrypt.ui.main.profile

import android.app.Application
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
import net.stiller.encrypt.utils.Crypt
import net.stiller.encrypt.utils.StateAuth
import net.stiller.encrypt.utils.StateAuth.Companion.error
import net.stiller.encrypt.utils.StateAuth.Companion.loading
import net.stiller.encrypt.utils.StateAuth.Companion.success
import org.reactivestreams.Subscription


class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    val userLiveData: MutableLiveData<User>
    private val onUpdate: MediatorLiveData<StateAuth<*>>
    private val firestoreRepository: FirestoreRepository
    private val disposable: CompositeDisposable
    private val crypt: Crypt

    init {
        firestoreRepository = FirestoreRepository(application)
        userLiveData = MutableLiveData()
        disposable = CompositeDisposable()
        onUpdate = MediatorLiveData()
        crypt = Crypt()
        fetchUserDataStore()
    }

    private fun fetchUserDataStore() {
        runBlocking {
            firestoreRepository.readUserStore()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : FlowableSubscriber<User> {
                    override fun onSubscribe(s: Subscription) {
                        s.request(Long.MAX_VALUE)
                    }

                    override fun onError(t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onComplete() {

                    }

                    override fun onNext(t: User) {
                        userLiveData.postValue(t)
                    }
                })
        }
    }

    fun update(id: String, field: String, value: String) {
        firestoreRepository.update(id, field, value)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : CompletableObserver {
                override fun onSubscribe(d: Disposable) {
                    disposable.add(d)
                    onUpdate.value = loading<Any>()
                }

                override fun onComplete() {
                    onUpdate.value = success<Any>()
                    read(id)
                }

                override fun onError(e: Throwable) {
                    onUpdate.value = error<Any>(e.message!!)
                }
            })
    }

    fun read(id: String) {
        firestoreRepository.readFirestore(id)
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
                    userLiveData.postValue(user)
                    onUpdate.value = loading<Any>()
                }

                override fun onError(e: Throwable) {
                    onUpdate.value = error<Any>(e.message!!)
                }

                override fun onComplete() {
                    onUpdate.value = success<Any>()
                }
            })
    }


    fun logout() {
        runBlocking {
            firestoreRepository.logout()
        }
    }

    private fun getDESKey(id: String): String {
        return id.substring(0, 8)
    }

    private fun getAESKey(id: String): String {
        return id.substring(0, 16)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    companion object {
        private const val TAG = "ProfileViewModel"
    }
}

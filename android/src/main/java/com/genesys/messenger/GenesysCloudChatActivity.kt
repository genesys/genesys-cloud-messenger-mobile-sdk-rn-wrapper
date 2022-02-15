package com.genesys.messenger

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.genesys.cloud.core.utils.NRError
import com.genesys.cloud.core.utils.toast
import com.genesys.cloud.integration.core.AccountInfo
import com.genesys.cloud.integration.core.StateEvent
import com.genesys.cloud.integration.messenger.MessengerAccount
import com.genesys.cloud.ui.structure.controller.ChatController
import com.genesys.cloud.ui.structure.controller.ChatEventListener
import com.genesys.cloud.ui.structure.controller.ChatLoadResponse
import com.genesys.cloud.ui.structure.controller.ChatLoadedListener

class GenesysCloudChatActivity : AppCompatActivity(), ChatEventListener {

    private var chatController: ChatController? = null

    private var endMenu: MenuItem? = null

    lateinit var account: AccountInfo

    private var onError: ((NRError) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(GenTag, "onCreate:")

        setContentView(R.layout.fragment_layout)

        setSupportActionBar(findViewById(R.id.chat_toolbar))

        requestedOrientation = intent.getIntExtra(ScreenOrientation, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)

        //        onError = (intent.getParcelableExtra<ChatListener>(ErrorListener) as ChatListener)::onError
        onError = eventListener.onError

        initAccount()
        createChat()
    }

    override fun onStart() {
        super.onStart()
        Log.i(GenTag, "onCreate: ${chatController?.isActive}")
    }

    private fun initAccount() {
        account = MessengerAccount(intent.getStringExtra(DeploymentId), intent.getStringExtra(Domain)).apply {
            tokenStoreKey = intent.getStringExtra(TokenStoreKey)
            logging = intent.getBooleanExtra(Logging, false)
        }
    }

    private fun createChat() {

        chatController = ChatController.Builder(this).apply {
            chatEventListener(this@GenesysCloudChatActivity)

        }.build(account, object : ChatLoadedListener {

            override fun onComplete(result: ChatLoadResponse) {

                val error = result.error ?: takeIf { result.fragment == null }?.let {
                    NRError(NRError.EmptyError, "Chat UI failed to init")
                }

                error?.let {
                    Log.e(GenTag, "!!! Messenger chat : $it")
                    finish()
                    onError(it)

                } ?: openConversationFragment((result.fragment!!))

            }
        })
    }

    private fun openConversationFragment(fragment: Fragment) {
        supportFragmentManager.takeUnless {
            it.isStateSaved || this.isFinishing
        }?.let {

            val chatFrag = it.findFragmentByTag(CONVERSATION_FRAGMENT_TAG)
            if (chatFrag?.equals(fragment) == true) return@let

            chatFrag?.let { chatController?.restoreChat(chatFrag) } ?: it.beginTransaction()
                .replace(R.id.chat_container, fragment, CONVERSATION_FRAGMENT_TAG).apply {
                    addToBackStack(CONVERSATION_FRAGMENT_TAG)
                }.commit()
        }
    }

    override fun onError(error: NRError) {
        super.onError(error)
        Log.e(GenTag, error.description ?: error.reason ?: error.errorCode)

        onError?.invoke(error)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            chatController?.endChat(true)
        }
        super.onBackPressed()
    }

    override fun onChatStateChanged(stateEvent: StateEvent) {
        Log.i(GenTag, "Got Chat state: ${stateEvent.state}")
        when (stateEvent.state) {
            StateEvent.Ended -> {
                finish()
            }

            StateEvent.Started -> {
                findViewById<ProgressBar>(R.id.waiting)?.visibility = View.GONE
                enableMenu(endMenu, true)
            }

        }
        super.onChatStateChanged(stateEvent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        this.endMenu = menu?.findItem(R.id.end_current_chat)

        return true
    }

    private fun destructChat() {
        chatController?.takeUnless { it.wasDestructed }?.run {
            terminateChat()
            destruct()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.end_current_chat -> {
                chatController?.endChat(false)
                item.isEnabled = false
                return true
            }

            R.id.destruct_chat -> {
                finish() //destructChat()
                enableMenu(endMenu, false)
                item.isEnabled = false
                return true
            }

            else -> {
            }
        }
        return false
    }

    private fun enableMenu(menuItem: MenuItem?, enable: Boolean) {
        if (menuItem != null) {
            menuItem.isEnabled = enable
            if (enable && !menuItem.isVisible) menuItem.isVisible = true
        }
    }

    override fun onStop() {
        super.onStop()

        if (isFinishing) {
            destructChat()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(GenTag, "onConfigurationChanged:")
    }

    companion object {
        const val CONVERSATION_FRAGMENT_TAG = "conversation_fragment"
        const val GenTag = "GenesysChatActivity"

        const val DeploymentId = "deploymentId"
        const val Domain = "domain"
        const val TokenStoreKey = "tokenStoreKey"
        const val Logging = "logging"
        const val ScreenOrientation = "screenOrientation"
        const val ErrorListener = "errorListener"

        var eventListener: OnEventListener = OnEventListener { }

        fun intentFactory(deploymentId: String, domain: String, tokenStoreKey: String, logging: Boolean,
            screenOrientation: Int, errorListener: OnEventListener): Intent {

            eventListener = errorListener

            return Intent("com.intent.action.Messenger_CHAT").apply {
                putExtra(DeploymentId, deploymentId)
                putExtra(Domain, domain)
                putExtra(TokenStoreKey, tokenStoreKey)
                putExtra(Logging, logging)
                putExtra(ScreenOrientation, screenOrientation)
            //                putExtra(ErrorListener, errorListener as Parcelable)
            }
        }
    }

}
package com.genesys.messenger

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.integration.core.StateEvent
import com.nanorep.convesationui.messenger.MessengerAccount
import com.nanorep.convesationui.structure.controller.ChatController
import com.nanorep.convesationui.structure.controller.ChatEventListener
import com.nanorep.convesationui.structure.controller.ChatLoadResponse
import com.nanorep.convesationui.structure.controller.ChatLoadedListener
import com.nanorep.nanoengine.AccountInfo
import com.nanorep.sdkcore.utils.NRError

class GenesysCloudChatActivity : AppCompatActivity(), ChatEventListener {

    private var boldController: ChatController? = null

    private var endMenu: MenuItem? = null

    lateinit var account: AccountInfo


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(GenTag,"onCreate:")

        setContentView(R.layout.fragment_layout)

        setSupportActionBar(findViewById(R.id.chat_toolbar))

        initAccount()
        createChat()
    }

    override fun onStart() {
        super.onStart()

        Log.i(GenTag,"onCreate: ${boldController?.isActive}")

    }

    private fun initAccount() {
        account = MessengerAccount().apply {
            deploymentId = intent.getStringExtra(DeploymentId)
            domain = intent.getStringExtra(Domain)
            tokenStoreKey = intent.getStringExtra(TokenStoreKey) 
            logging = intent.getBooleanExtra(Logging, false)
        }
    }

    private fun createChat() {

        boldController = ChatController.Builder(this).apply {
            chatEventListener(this@GenesysCloudChatActivity)

        }.build(account, object : ChatLoadedListener {

            override fun onComplete(result: ChatLoadResponse) {

                val error = result.error ?: takeIf { result.fragment == null }?.let {
                    NRError(NRError.EmptyError, "Chat UI failed to init")
                }

                error?.let {
                    Log.e(GenTag, "!!! Failed to load chat: $it")

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

            chatFrag?.let { boldController?.restoreChat(chatFrag) } ?: it.beginTransaction()
                .replace(R.id.chat_container, fragment, CONVERSATION_FRAGMENT_TAG).apply {
                    addToBackStack(CONVERSATION_FRAGMENT_TAG)
                }.commit()
        }
    }

    override fun onError(error: NRError) {
        super.onError(error)
        Log.e(GenTag, error.description ?: error.reason ?: error.errorCode)
    }


    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 1) {
            boldController?.endChat(true)
        }
        super.onBackPressed()
    }

    override fun onChatStateChanged(stateEvent: StateEvent) {
        Log.i(GenTag, "Got Chat state: ${stateEvent.state}")
        when (stateEvent.state) {
            StateEvent.Ended -> {
                finish()
            }

            StateEvent.Started ->         {
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
        boldController?.takeUnless { it.wasDestructed }?.run {
            terminateChat()
            destruct()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.end_current_chat -> {
                boldController?.endChat(false)
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
        const val GenTag = "GenesysCloudChatActivity"

        const val DeploymentId = "deploymentId"
        const val Domain = "domain"
        const val TokenStoreKey = "tokenStoreKey"
        const val Logging = "logging"
       
        fun intentFactory(
            deploymentId: String,
            domain: String,
            tokenStoreKey: String,
            logging: Boolean
        ): Intent {

            return Intent("com.intent.action.Messenger_CHAT").apply {
                putExtra(DeploymentId, deploymentId)
                putExtra(Domain, domain)
                putExtra(TokenStoreKey, tokenStoreKey)
                putExtra(Logging, logging)
            }
        }
    }

}
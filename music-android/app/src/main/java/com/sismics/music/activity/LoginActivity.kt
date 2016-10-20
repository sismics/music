package com.sismics.music.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import com.androidquery.AQuery
import com.loopj.android.http.JsonHttpResponseHandler
import com.sismics.music.R
import com.sismics.music.model.ApplicationContext
import com.sismics.music.resource.UserResource
import com.sismics.music.util.DialogUtil
import com.sismics.music.util.PreferenceUtil
import com.sismics.music.util.form.Validator
import com.sismics.music.util.form.validator.Required
import org.apache.http.Header
import org.json.JSONObject

/**
 * Login activity.

 * @author bgamard
 */
class LoginActivity : FragmentActivity() {

    /**
     * User interface.
     */
    private var loginForm: View? = null
    private var progressBar: View? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val aq = AQuery(this)
        aq.id(R.id.loginExplain).text(Html.fromHtml(getString(R.string.login_explain))).textView.movementMethod = LinkMovementMethod.getInstance()

        val txtServer = aq.id(R.id.txtServer).editText
        val txtUsername = aq.id(R.id.txtUsername).editText
        val txtPassword = aq.id(R.id.txtPassword).editText
        val btnConnect = aq.id(R.id.btnConnect).button
        loginForm = aq.id(R.id.loginForm).view
        progressBar = aq.id(R.id.progressBar).view

        // PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        loginForm!!.visibility = View.GONE
        progressBar!!.visibility = View.VISIBLE

        // Form validation
        val validator = Validator(false)
        validator.addValidable(this, txtServer, Required())
        validator.addValidable(this, txtUsername, Required())
        validator.addValidable(this, txtPassword, Required())
        validator.setOnValidationChanged { btnConnect.isEnabled = validator.isValidated }

        // Preset saved server URL
        val serverUrl = PreferenceUtil.getStringPreference(this, PreferenceUtil.Pref.SERVER_URL)
        if (serverUrl != null) {
            txtServer.setText(serverUrl)
        }

        tryConnect()

        // Login button
        btnConnect.setOnClickListener {
            loginForm!!.visibility = View.GONE
            progressBar!!.visibility = View.VISIBLE

            PreferenceUtil.setServerUrl(this@LoginActivity, txtServer.text.toString())

            try {
                UserResource.login(applicationContext, txtUsername.text.toString(), txtPassword.text.toString(), object : JsonHttpResponseHandler() {
                    override fun onSuccess(json: JSONObject?) {
                        // Empty previous user caches
                        PreferenceUtil.resetUserCache(applicationContext)

                        // Getting user info and redirecting to main activity
                        ApplicationContext.fetchUserInfo(this@LoginActivity) {
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onFailure(statusCode: Int, headers: Array<Header>, responseBytes: ByteArray?, throwable: Throwable) {
                        loginForm!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE

                        if (responseBytes != null && String(responseBytes).contains("\"ForbiddenError\"")) {
                            DialogUtil.showOkDialog(this@LoginActivity, R.string.login_fail_title, R.string.login_fail)
                        } else {
                            DialogUtil.showOkDialog(this@LoginActivity, R.string.network_error_title, R.string.network_error)
                        }
                    }
                })
            } catch (e: IllegalArgumentException) {
                // Given URL is not valid
                loginForm!!.visibility = View.VISIBLE
                progressBar!!.visibility = View.GONE
                PreferenceUtil.setServerUrl(this@LoginActivity, null)
                DialogUtil.showOkDialog(this@LoginActivity, R.string.invalid_url_title, R.string.invalid_url)
            }
        }
    }

    /**
     * Try to get a "session".
     */
    private fun tryConnect() {
        val serverUrl = PreferenceUtil.getStringPreference(this, PreferenceUtil.Pref.SERVER_URL)
        if (serverUrl == null) {
            // Server URL is empty
            loginForm!!.visibility = View.VISIBLE
            progressBar!!.visibility = View.GONE
            return
        }

        if (ApplicationContext.isLoggedIn) {
            // If we are already connected (from cache data)
            // redirecting to main activity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Trying to get user data
            UserResource.info(applicationContext, object : JsonHttpResponseHandler() {
                override fun onSuccess(json: JSONObject?) {
                    if (json!!.optBoolean("anonymous", true)) {
                        loginForm!!.visibility = View.VISIBLE
                        return
                    }

                    // Save user data in application context
                    ApplicationContext.setUserInfo(applicationContext, json)

                    // Redirecting to main activity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }

                override fun onFailure(statusCode: Int, headers: Array<Header>, responseBytes: ByteArray?, throwable: Throwable) {
                    DialogUtil.showOkDialog(this@LoginActivity, R.string.network_error_title, R.string.network_error)
                    loginForm!!.visibility = View.VISIBLE
                }

                override fun onFinish() {
                    progressBar!!.visibility = View.GONE
                }
            })
        }
    }
}
package eu.kanade.tachiyomi.ui.more

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.preference.PreferenceScreen
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.aboutlibraries.LibsBuilder
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.updater.UpdateResult
import eu.kanade.tachiyomi.data.updater.UpdaterService
import eu.kanade.tachiyomi.data.updater.github.GithubUpdateChecker
import eu.kanade.tachiyomi.ui.base.controller.DialogController
import eu.kanade.tachiyomi.ui.base.controller.NoToolbarElevationController
import eu.kanade.tachiyomi.ui.base.controller.openInBrowser
import eu.kanade.tachiyomi.ui.setting.SettingsController
import eu.kanade.tachiyomi.util.CrashLogUtil
import eu.kanade.tachiyomi.util.lang.launchNow
import eu.kanade.tachiyomi.util.preference.add
import eu.kanade.tachiyomi.util.preference.onClick
import eu.kanade.tachiyomi.util.preference.preference
import eu.kanade.tachiyomi.util.preference.titleRes
import eu.kanade.tachiyomi.util.system.copyToClipboard
import eu.kanade.tachiyomi.util.system.toast
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

class AboutController : SettingsController(), NoToolbarElevationController {

    private val updateChecker by lazy { GithubUpdateChecker() }

    private val isUpdaterEnabled = BuildConfig.INCLUDE_UPDATER

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        add(AboutHeaderPreference(context))

        if (isUpdaterEnabled) {
            preference {
                key = "pref_about_check_for_updates"
                titleRes = R.string.check_for_updates

                onClick { checkVersion() }
            }
        }
        preference {
            key = "pref_about_whats_new"
            titleRes = R.string.whats_new

            onClick {
                val url = if (BuildConfig.DEBUG) {
                    "https://github.com/tachiyomiorg/tachiyomi-preview/releases/tag/r${BuildConfig.COMMIT_COUNT}"
                } else {
                    "https://github.com/tachiyomiorg/tachiyomi/releases/tag/v${BuildConfig.VERSION_NAME}"
                }
                openInBrowser(url)
            }
        }
        preference {
            key = "pref_about_build_info"
            titleRes = R.string.build_info
            summary = "#${BuildConfig.COMMIT_COUNT}-${BuildConfig.BUILD_TYPE}-${BuildConfig.COMMIT_SHA}" +
                "\n${BuildConfig.BUILD_TIME}"
        }
        preference {
            key = "pref_about_copy_debug_info"
            titleRes = R.string.copy_debug_info
            onClick {
                activity?.let {
                    val deviceInfo = CrashLogUtil(it).getDebugInfo()
                    it.copyToClipboard("Debug information", deviceInfo)
                }
            }
        }
        preference {
            key = "pref_about_licenses"
            titleRes = R.string.licenses
            onClick {
                LibsBuilder()
                    .withActivityTitle(activity!!.getString(R.string.licenses))
                    .withAboutIconShown(false)
                    .withAboutVersionShown(false)
                    .withLicenseShown(true)
                    .withEdgeToEdge(true)
                    .start(activity!!)
            }
        }

        add(AboutLinksPreference(context))
    }

    /**
     * Checks version and shows a user prompt if an update is available.
     */
    private fun checkVersion() {
        if (activity == null) return

        activity?.toast(R.string.update_check_look_for_updates)

        launchNow {
            try {
                when (val result = updateChecker.checkForUpdate()) {
                    is UpdateResult.NewUpdate<*> -> {
                        val body = result.release.info
                        val url = result.release.downloadLink

                        // Create confirmation window
                        NewUpdateDialogController(body, url).showDialog(router)
                    }
                    is UpdateResult.NoNewUpdate -> {
                        activity?.toast(R.string.update_check_no_new_updates)
                    }
                }
            } catch (error: Exception) {
                activity?.toast(error.message)
                Timber.e(error)
            }
        }
    }

    class NewUpdateDialogController(bundle: Bundle? = null) : DialogController(bundle) {

        constructor(body: String, url: String) : this(
            bundleOf(BODY_KEY to body, URL_KEY to url)
        )

        override fun onCreateDialog(savedViewState: Bundle?): Dialog {
            return MaterialDialog(activity!!)
                .title(res = R.string.update_check_notification_update_available)
                .message(text = args.getString(BODY_KEY) ?: "")
                .positiveButton(R.string.update_check_confirm) {
                    val appContext = applicationContext
                    if (appContext != null) {
                        // Start download
                        val url = args.getString(URL_KEY) ?: ""
                        UpdaterService.start(appContext, url)
                    }
                }
                .negativeButton(R.string.update_check_ignore)
        }

        private companion object {
            const val BODY_KEY = "NewUpdateDialogController.body"
            const val URL_KEY = "NewUpdateDialogController.key"
        }
    }

    private fun getFormattedBuildTime(): String? {
        return try {
            val inputDf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputDf.timeZone = TimeZone.getTimeZone("UTC")
            inputDf.parse(BuildConfig.BUILD_TIME)?.run {
                val formatter = DateTimeFormatter.ofPattern("E, MMM dd yyyy HH:mm:ss a z").withLocale(Locale.US)
                toInstant().atZone(ZoneId.of("UTC")).format(formatter)
//                DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.LONG, Locale.US).format(this)
            }
        } catch (e: ParseException) {
            BuildConfig.BUILD_TIME
        }
    }
}

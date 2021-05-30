package eu.kanade.tachiyomi.ui.more

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.util.CrashLogUtil
import eu.kanade.tachiyomi.util.system.copyToClipboard

class AboutHeaderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val version = holder.findViewById(R.id.version) as TextView
        version.text = if (BuildConfig.DEBUG) {
            "Preview r${BuildConfig.COMMIT_COUNT}"
        } else {
            "Stable v${BuildConfig.VERSION_NAME}"
        }
        version.setOnClickListener {
            val deviceInfo = CrashLogUtil(context).getDebugInfo()
            context.copyToClipboard("Debug information", deviceInfo)
        }
    }

    init {
        layoutResource = R.layout.pref_about_header
        isSelectable = false
    }
}

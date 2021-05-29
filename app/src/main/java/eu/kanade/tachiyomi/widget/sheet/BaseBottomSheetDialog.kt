package eu.kanade.tachiyomi.widget.sheet

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.google.android.material.bottomsheet.BottomSheetDialog
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.preference.PreferenceValues
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.util.system.displayCompat
import eu.kanade.tachiyomi.util.view.setNavigationBarTransparentCompat
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

abstract class BaseBottomSheetDialog(context: Context) : BottomSheetDialog(context) {

    abstract fun createView(inflater: LayoutInflater): View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootView = createView(layoutInflater)
        setContentView(rootView)

        // Enforce max width for tablets
        val width = context.resources.getDimensionPixelSize(R.dimen.bottom_sheet_width)
        if (width > 0) {
            behavior.maxWidth = width
        }

        // Set peek height to 50% display height
        context.displayCompat?.let {
            val metrics = DisplayMetrics()
            it.getRealMetrics(metrics)
            behavior.peekHeight = metrics.heightPixels / 2
        }

        // Set navbar color to transparent for edge-to-edge bottom sheet if we can use light navigation bar
        // TODO Replace deprecated systemUiVisibility when material-components uses new API to modify status bar icons
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bottomSheet = rootView.parent as ViewGroup
            val isLandscape = context.resources.configuration.orientation and Configuration.ORIENTATION_LANDSCAPE != 0
            if (isLandscape) {
                window?.navigationBarColor = Color.TRANSPARENT

                // Workaround for padding issue on landscape
                // https://github.com/material-components/material-components-android/issues/2221
                ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
                    (bottomSheet.parent as ViewGroup).updatePadding(
                        left = bottomSheet.paddingLeft,
                        right = bottomSheet.paddingRight
                    )
                    bottomSheet.updatePadding(left = 0, right = 0)
                    insets
                }
            } else {
                window?.setNavigationBarTransparentCompat(context)
            }

            val isDarkMode = when (Injekt.get<PreferencesHelper>().themeMode().get()) {
                PreferenceValues.ThemeMode.light -> false
                PreferenceValues.ThemeMode.dark -> true
                PreferenceValues.ThemeMode.system ->
                    context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            }
            var flags = bottomSheet.systemUiVisibility
            flags = if (isDarkMode || isLandscape) {
                flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
            } else {
                flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            bottomSheet.systemUiVisibility = flags
        }
    }
}

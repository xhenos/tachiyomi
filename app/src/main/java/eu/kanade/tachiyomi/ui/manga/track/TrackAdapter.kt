package eu.kanade.tachiyomi.ui.manga.track

import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.elevation.ElevationOverlayProvider
import eu.kanade.tachiyomi.data.preference.PreferenceValues
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.databinding.TrackItemBinding
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get

class TrackAdapter(listener: OnClickListener) : RecyclerView.Adapter<TrackHolder>() {

    private lateinit var binding: TrackItemBinding

    var items = emptyList<TrackItem>()
        set(value) {
            if (field !== value) {
                field = value
                notifyDataSetChanged()
            }
        }

    val rowClickListener: OnClickListener = listener

    fun getItem(index: Int): TrackItem? {
        return items.getOrNull(index)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackHolder {
        binding = TrackItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val ctx = binding.card.context
        val isDarkMode = when (Injekt.get<PreferencesHelper>().themeMode().get()) {
            PreferenceValues.ThemeMode.light -> false
            PreferenceValues.ThemeMode.dark -> true
            PreferenceValues.ThemeMode.system -> ctx.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        }
        // TODO: Don't.
        if (isDarkMode) {
            binding.card.setCardBackgroundColor(
                ElevationOverlayProvider(ctx).compositeOverlay(
                    binding.card.cardBackgroundColor.defaultColor,
                    binding.card.cardElevation
                )
            )
        }
        return TrackHolder(binding, this)
    }

    override fun onBindViewHolder(holder: TrackHolder, position: Int) {
        holder.bind(items[position])
    }

    interface OnClickListener {
        fun onLogoClick(position: Int)
        fun onSetClick(position: Int)
        fun onTitleLongClick(position: Int)
        fun onStatusClick(position: Int)
        fun onChaptersClick(position: Int)
        fun onScoreClick(position: Int)
        fun onStartDateClick(position: Int)
        fun onFinishDateClick(position: Int)
    }
}

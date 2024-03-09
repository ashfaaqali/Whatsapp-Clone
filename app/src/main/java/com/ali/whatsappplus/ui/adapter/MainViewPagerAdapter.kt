import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ali.whatsappplus.ui.fragment.CallsFragment
import com.ali.whatsappplus.ui.fragment.CommunitiesFragment
import com.ali.whatsappplus.ui.fragment.OngoingChatsFragment
import com.ali.whatsappplus.ui.fragment.UpdatesFragment

class MainViewPagerAdapter(fm: FragmentManager, private val tabCount: Int) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> CommunitiesFragment()
            1 -> OngoingChatsFragment()
            2 -> UpdatesFragment()
            3 -> CallsFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}

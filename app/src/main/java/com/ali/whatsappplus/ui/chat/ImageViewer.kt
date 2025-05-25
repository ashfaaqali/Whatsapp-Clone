package com.ali.whatsappplus.ui.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.PagerAdapter
import com.ali.whatsappplus.databinding.ActivityImageViewerBinding
import com.bumptech.glide.Glide
import com.igreenwood.loupe.Loupe
import com.igreenwood.loupe.extensions.setOnViewTranslateListener

class ImageViewer : AppCompatActivity() {

    companion object {
        private const val IMAGE_LIST = "images"
        private const val START_POSITION = "start_position"

        fun createIntent(context: Context, urls: ArrayList<String>, position: Int): Intent {
            return Intent(context, ImageViewer::class.java).apply {
                putExtra(IMAGE_LIST, urls)
                putExtra(START_POSITION, position)
            }
        }
    }

    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var adapter: ImagePagerAdapter
    private var imageList = listOf<String>()
    private var startPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageList = intent.getStringArrayListExtra(IMAGE_LIST) ?: emptyList()
        startPosition = intent.getIntExtra(START_POSITION, 0)

        adapter = ImagePagerAdapter(this, imageList)
        binding.viewPager.adapter = adapter
        binding.viewPager.currentItem = startPosition
    }

    class ImagePagerAdapter(val context: Context, private val urls: List<String>) : PagerAdapter() {
        override fun getCount() = urls.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            Glide.with(context).load(urls[position]).into(imageView)

            Loupe.create(imageView, container) {
                dismissAnimationDuration = Loupe.DEFAULT_ANIM_DURATION
                setOnViewTranslateListener(
                    onDismiss = {
                        (context as? Activity)?.finishAfterTransition()
                    }
                )
            }

            container.addView(imageView)
            return imageView
        }

        override fun isViewFromObject(view: View, obj: Any) = view == obj

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }
    }
}
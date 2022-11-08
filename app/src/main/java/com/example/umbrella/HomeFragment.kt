package com.example.umbrella

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.umbrella.core.Inspection
import kotlinx.android.synthetic.main.fragment_home.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.sample_image)
        inspection.image = bitmap

        button_live.setOnClickListener {
            replaceFragment(LiveFragment())
        }

        button_alignment.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.setupBackButton(true)

            toAlignmentType()
        }

        button_blob.setOnClickListener {
            val activity = activity as MainActivity?
            activity?.setupBackButton(true)

            toBlob()
        }
    }

    private fun setupActionBar() {
        val activity = activity as MainActivity?
        activity?.title = "Home"
        activity?.setupBackButton(false)
    }

    private fun replaceFragment(fragment: Fragment) {
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(com.example.umbrella.R.id.container, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.supportFragmentManager?.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val inspection = Inspection

    private fun toAlignmentType() {
        val fragment = AlignmentTypeFragment()
        fragment.onNextClicked = {
            toAlignmentAlgorithm()
        }

        replaceFragment(fragment)
    }

    private fun toAlignmentAlgorithm() {
        val fragment = AlignmentAlgorithmFragment()
        fragment.onNextClicked = {
            toAlignment()
        }

        replaceFragment(fragment)
    }

    private fun toAlignment() {
        val fragment = AlignmentFragment()
        replaceFragment(fragment)
    }

    private fun toBlob() {
        val fragment = BlobFragment()
        replaceFragment(fragment)
    }
}
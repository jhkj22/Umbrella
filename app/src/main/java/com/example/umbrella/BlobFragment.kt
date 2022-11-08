package com.example.umbrella

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alignment.*
import kotlinx.android.synthetic.main.fragment_alignment.button_next
import kotlinx.android.synthetic.main.fragment_blob.*

class BlobFragment : Fragment() {

    var onNextClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_blob, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = canvas_blob.getTitle()
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            toPrev()
        }

        button_next.setOnClickListener {
            toNext()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                toPrev()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toNext() {
        if (!canvas_blob.toNext()) {
            onNextClicked?.invoke()
        } else {
            activity?.title = canvas_blob.getTitle()
        }
    }

    private fun toPrev() {
        if (!canvas_blob.toPrev()) {
            activity?.supportFragmentManager?.popBackStack()
        } else {
            activity?.title = canvas_blob.getTitle()
        }
    }
}
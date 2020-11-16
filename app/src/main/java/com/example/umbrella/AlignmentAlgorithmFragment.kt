package com.example.umbrella

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_alignment_algorithm.*
import kotlinx.android.synthetic.main.fragment_alignment_type.radioGroup

class AlignmentAlgorithmFragment : Fragment() {

    var onNextClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_alignment_algorithm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        radioGroup.check(radioNc.id)

        button5.setOnClickListener {
            onNextClicked?.invoke()
        }
    }
}
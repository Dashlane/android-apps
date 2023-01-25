package com.dashlane.ui.screens.settings.list.general.labs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dashlane.R
import com.dashlane.databinding.FragmentDashlaneLabsBinding
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.launchUrl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashlaneLabsFragment : Fragment(R.layout.fragment_dashlane_labs) {

    private val viewModel by viewModels<DashlaneLabsViewModels>()

    private val featureFlipAdapter = DashlaneRecyclerAdapter<DashlaneLabsItem>()
    private lateinit var binding: FragmentDashlaneLabsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDashlaneLabsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        featureFlipAdapter.populateItems(viewModel.getLabsFeatureFlip())
        binding.featureFlipList.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = featureFlipAdapter
        }
        binding.feedbackButton.onClick = {
            
            val feedbackForm = "https://forms.gle/SpSK8aNZQaBmMx3D6".toUri()
            requireContext().launchUrl(feedbackForm)
        }
    }
}
package com.example.mineprompt.ui.likes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mineprompt.databinding.FragmentLikesBinding

class LikesFragment : Fragment() {

    private var _binding: FragmentLikesBinding? = null
    
    // This property is only valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val likesViewModel =
            ViewModelProvider(this).get(LikesViewModel::class.java)

        _binding = FragmentLikesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textLikes
        likesViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

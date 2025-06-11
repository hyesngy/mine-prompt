package com.example.mineprompt.ui.prompt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mineprompt.databinding.FragmentPromptBottomSheetBinding
import com.example.mineprompt.utils.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PromptBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPromptBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var promptTitle: String = ""
    private var promptContent: String = ""

    companion object {
        private const val ARG_TITLE = "prompt_title"
        private const val ARG_CONTENT = "prompt_content"

        fun newInstance(title: String, content: String): PromptBottomSheetFragment {
            return PromptBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_CONTENT, content)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            promptTitle = it.getString(ARG_TITLE, "")
            promptContent = it.getString(ARG_CONTENT, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPromptBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        binding.apply {
            tvPromptTitle.text = promptTitle
            tvPromptContent.text = promptContent
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnClose.setOnClickListener {
                dismiss()
            }

            btnCopyPrompt.setOnClickListener {
                copyPromptToClipboard()
            }

            tvPromptContent.setTextIsSelectable(true)
        }
    }

    private fun copyPromptToClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("프롬프트", promptContent)
        clipboard.setPrimaryClip(clip)

        ToastUtils.showPromptCopied(requireContext())
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
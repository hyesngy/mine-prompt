package com.example.mineprompt.ui.create

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mineprompt.R
import com.example.mineprompt.databinding.FragmentCreateBinding
import com.example.mineprompt.utils.ToastUtils
import com.google.android.material.button.MaterialButton

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private lateinit var createViewModel: CreateViewModel

    private val languageOptions = arrayOf(
        "한국어", "영어", "일본어", "중국어",
        "스페인어", "프랑스어", "독일어", "러시아어"
    )

    private var selectedLanguage = "한국어"
    private var selectedLength = "MEDIUM" // SHORT, MEDIUM, LONG
    private var selectedStyle = "일반적" // 일반적, 창의적, 공식적, 논리적, 감성적, 전문적

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        createViewModel = ViewModelProvider(this)[CreateViewModel::class.java]

        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupDefaultValues()
        setupClickListeners()
        observeViewModel()

        return root
    }

    private fun setupDefaultValues() {
        updateLengthButtons(selectedLength)
        updateStyleButtons(selectedStyle)
        binding.btnLanguageSelector.text = selectedLanguage
    }

    private fun setupClickListeners() {
        // 언어 선택 다이얼로그
        binding.btnLanguageSelector.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // 길이 선택 버튼
        binding.btnLengthShort.setOnClickListener {
            selectedLength = "SHORT"
            updateLengthButtons(selectedLength)
        }

        binding.btnLengthMedium.setOnClickListener {
            selectedLength = "MEDIUM"
            updateLengthButtons(selectedLength)
        }

        binding.btnLengthLong.setOnClickListener {
            selectedLength = "LONG"
            updateLengthButtons(selectedLength)
        }

        // 스타일 선택 버튼
        binding.btnStyleGeneral.setOnClickListener {
            selectedStyle = "일반적"
            updateStyleButtons(selectedStyle)
        }

        binding.btnStyleCreative.setOnClickListener {
            selectedStyle = "창의적"
            updateStyleButtons(selectedStyle)
        }

        binding.btnStyleFormal.setOnClickListener {
            selectedStyle = "공식적"
            updateStyleButtons(selectedStyle)
        }

        binding.btnStyleLogical.setOnClickListener {
            selectedStyle = "논리적"
            updateStyleButtons(selectedStyle)
        }

        binding.btnStyleEmotional.setOnClickListener {
            selectedStyle = "감성적"
            updateStyleButtons(selectedStyle)
        }

        binding.btnStyleProfessional.setOnClickListener {
            selectedStyle = "전문적"
            updateStyleButtons(selectedStyle)
        }

        // 초기화 버튼
        binding.btnReset.setOnClickListener {
            resetForm()
        }

        // 생성 버튼
        binding.btnGenerate.setOnClickListener {
            generatePrompt()
        }
    }

    private fun observeViewModel() {
        createViewModel.isGenerating.observe(viewLifecycleOwner) { isGenerating ->
            // 로딩 상태에 따른 UI 변경
            binding.btnGenerate.isEnabled = !isGenerating
            if (isGenerating) {
                binding.btnGenerate.text = "생성 중..."
            } else {
                binding.btnGenerate.text = "프롬프트 생성"
            }
        }

        createViewModel.generationResult.observe(viewLifecycleOwner) { result ->
            if (result.isSuccess) {
                ToastUtils.showPromptCreated(requireContext())
                // TODO: 생성된 프롬프트 상세 화면으로 이동
                // val promptId = result.getOrNull()
                // navigateToPromptDetail(promptId)
            } else {
                ToastUtils.showGeneralError(requireContext())
            }
        }
    }

    private fun showLanguageSelectionDialog() {
        val currentIndex = languageOptions.indexOf(selectedLanguage)

        AlertDialog.Builder(requireContext())
            .setTitle("언어 선택")
            .setSingleChoiceItems(languageOptions, currentIndex) { dialog, which ->
                selectedLanguage = languageOptions[which]
                binding.btnLanguageSelector.text = selectedLanguage
                dialog.dismiss()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateLengthButtons(selected: String) {
        resetLengthButtonStyles()
        when (selected) {
            "SHORT" -> setButtonSelected(binding.btnLengthShort)
            "MEDIUM" -> setButtonSelected(binding.btnLengthMedium)
            "LONG" -> setButtonSelected(binding.btnLengthLong)
        }
    }

    private fun updateStyleButtons(selected: String) {
        resetStyleButtonStyles()
        when (selected) {
            "일반적" -> setButtonSelected(binding.btnStyleGeneral)
            "창의적" -> setButtonSelected(binding.btnStyleCreative)
            "공식적" -> setButtonSelected(binding.btnStyleFormal)
            "논리적" -> setButtonSelected(binding.btnStyleLogical)
            "감성적" -> setButtonSelected(binding.btnStyleEmotional)
            "전문적" -> setButtonSelected(binding.btnStyleProfessional)
        }
    }

    private fun resetLengthButtonStyles() {
        setButtonUnselected(binding.btnLengthShort)
        setButtonUnselected(binding.btnLengthMedium)
        setButtonUnselected(binding.btnLengthLong)
    }

    private fun resetStyleButtonStyles() {
        setButtonUnselected(binding.btnStyleGeneral)
        setButtonUnselected(binding.btnStyleCreative)
        setButtonUnselected(binding.btnStyleFormal)
        setButtonUnselected(binding.btnStyleLogical)
        setButtonUnselected(binding.btnStyleEmotional)
        setButtonUnselected(binding.btnStyleProfessional)
    }

    private fun setButtonSelected(button: MaterialButton) {
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_500)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.primary_500)
    }

    private fun setButtonUnselected(button: MaterialButton) {
        button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.transparent)
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_300))
        button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.gray_400)
    }

    private fun resetForm() {
        // 입력 필드 초기화
        binding.etPurpose.setText("")
        binding.etKeywords.setText("")

        // 옵션들 기본값으로 리셋
        selectedLength = "MEDIUM"
        selectedStyle = "일반적"
        selectedLanguage = "한국어"

        setupDefaultValues()
        ToastUtils.showShort(requireContext(), "입력 폼이 초기화되었습니다")
    }

    private fun generatePrompt() {
        val purpose = binding.etPurpose.text.toString().trim()
        val keywords = binding.etKeywords.text.toString().trim()

        // 유효성 검사
        if (purpose.isEmpty()) {
            binding.etPurpose.error = "목적/의도를 입력해주세요"
            binding.etPurpose.requestFocus()
            ToastUtils.showShort(requireContext(), "목적/의도를 입력해주세요")
            return
        }

        if (keywords.isEmpty()) {
            binding.etKeywords.error = "포함할 키워드를 입력해주세요"
            binding.etKeywords.requestFocus()
            ToastUtils.showShort(requireContext(), "포함할 키워드를 입력해주세요")
            return
        }

        // 에러 상태 클리어
        binding.etPurpose.error = null
        binding.etKeywords.error = null

        // 생성 요청 데이터 준비
        val promptRequest = PromptGenerationRequest(
            purpose = purpose,
            keywords = keywords,
            length = selectedLength,
            style = selectedStyle,
            language = selectedLanguage
        )

        // 프롬프트 생성 요청
        createViewModel.generatePrompt(promptRequest)

        ToastUtils.showShort(requireContext(), "AI가 프롬프트를 생성 중입니다...")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class PromptGenerationRequest(
    val purpose: String,
    val keywords: String,
    val length: String,
    val style: String,
    val language: String
)
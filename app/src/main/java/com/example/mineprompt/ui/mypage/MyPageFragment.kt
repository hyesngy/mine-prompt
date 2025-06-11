package com.example.mineprompt.ui.mypage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mineprompt.utils.ToastUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mineprompt.databinding.FragmentMypageBinding
import com.example.mineprompt.ui.auth.LoginActivity
import com.example.mineprompt.utils.ViewModelFactory

class MyPageFragment : Fragment() {

    private var _binding: FragmentMypageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MyPageViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // 사용자 정보 로드
        viewModel.loadUserInfo()
    }

    private fun setupClickListeners() {
        // 회원정보 수정
        binding.layoutEditProfile.setOnClickListener {
            showComingSoonToast()
        }

        // 문의하기
        binding.layoutInquiry.setOnClickListener {
            showComingSoonToast()
        }

        // 공지사항
        binding.layoutNotice.setOnClickListener {
            showComingSoonToast()
        }

        // 서비스 이용약관
        binding.layoutTerms.setOnClickListener {
            showComingSoonToast()
        }

        // 개인정보처리방침
        binding.layoutPrivacy.setOnClickListener {
            showComingSoonToast()
        }

        // 회원탈퇴
        binding.layoutDeleteAccount.setOnClickListener {
            showComingSoonToast()
        }

        // 로그아웃 (실제 기능)
        binding.layoutLogout.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeViewModel() {
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            binding.tvUserNickname.text = user.nickname
            binding.tvUserEmail.text = user.email
        }

        viewModel.logoutSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToLogin()
            }
        }
    }

    private fun showComingSoonToast() {
        ToastUtils.showComingSoon(requireContext())
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
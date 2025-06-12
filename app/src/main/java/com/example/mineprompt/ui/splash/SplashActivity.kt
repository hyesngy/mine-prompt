package com.example.mineprompt.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.mineprompt.MainActivity
import com.example.mineprompt.data.DataInitializer
import com.example.mineprompt.data.UserPreferences
import com.example.mineprompt.databinding.ActivitySplashBinding
import com.example.mineprompt.ui.auth.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var dataInitializer: DataInitializer
    private var isReady = false

    companion object {
        private const val SPLASH_DELAY = 2000L
        private const val ANIMATION_DURATION = 1500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !isReady }
        setupFullScreen()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        dataInitializer = DataInitializer(this)

        setupInitialState()
        startSplashAnimation()
        initializeAppData()
    }

    private fun setupFullScreen() {
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = false

        supportActionBar?.hide()
    }

    private fun setupInitialState() {
        binding.ivAppLogo.alpha = 0f
        binding.ivAppLogo.scaleX = 0.3f
        binding.ivAppLogo.scaleY = 0.3f

        binding.tvAppTitle.alpha = 0f
        binding.tvAppTitle.translationY = 50f

        binding.tvSubtitle.alpha = 0f
        binding.tvSubtitle.translationY = 30f

        binding.progressBar.alpha = 0f

        binding.tvLoadingText.alpha = 0f
        binding.tvLoadingText.translationY = 20f

        binding.ivBgDecoration1.alpha = 0f
        binding.ivBgDecoration1.rotation = -45f
        binding.ivBgDecoration2.alpha = 0f
        binding.ivBgDecoration2.rotation = 45f
    }

    private fun startSplashAnimation() {
        val handler = Handler(Looper.getMainLooper())

        handler.post {
            animateBackgroundElements()
        }

        handler.postDelayed({
            animateLogo()
        }, 300)

        handler.postDelayed({
            animateTitle()
        }, 800)

        handler.postDelayed({
            animateSubtitle()
        }, 1200)

        handler.postDelayed({
            animateLoadingElements()
        }, 1800)
    }

    private fun animateBackgroundElements() {
        ObjectAnimator.ofFloat(binding.ivBgDecoration1, "alpha", 0f, 0.1f).apply {
            duration = 1500
            start()
        }
        ObjectAnimator.ofFloat(binding.ivBgDecoration1, "rotation", -45f, 0f).apply {
            duration = 2000
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ObjectAnimator.ofFloat(binding.ivBgDecoration2, "alpha", 0f, 0.08f).apply {
            duration = 1800
            start()
        }
        ObjectAnimator.ofFloat(binding.ivBgDecoration2, "rotation", 45f, 0f).apply {
            duration = 2200
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun animateLogo() {
        val scaleXAnimator = ObjectAnimator.ofFloat(binding.ivAppLogo, "scaleX", 0.3f, 1.2f, 1.0f)
        val scaleYAnimator = ObjectAnimator.ofFloat(binding.ivAppLogo, "scaleY", 0.3f, 1.2f, 1.0f)
        val alphaAnimator = ObjectAnimator.ofFloat(binding.ivAppLogo, "alpha", 0f, 1f)

        val logoAnimatorSet = AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator, alphaAnimator)
            duration = 800
            interpolator = OvershootInterpolator(1.2f)
        }
        logoAnimatorSet.start()
    }

    private fun animateTitle() {
        val translateAnimator = ObjectAnimator.ofFloat(binding.tvAppTitle, "translationY", 50f, 0f)
        val alphaAnimator = ObjectAnimator.ofFloat(binding.tvAppTitle, "alpha", 0f, 1f)

        val titleAnimatorSet = AnimatorSet().apply {
            playTogether(translateAnimator, alphaAnimator)
            duration = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        titleAnimatorSet.start()
    }

    private fun animateSubtitle() {
        val translateAnimator = ObjectAnimator.ofFloat(binding.tvSubtitle, "translationY", 30f, 0f)
        val alphaAnimator = ObjectAnimator.ofFloat(binding.tvSubtitle, "alpha", 0f, 1f)

        val subtitleAnimatorSet = AnimatorSet().apply {
            playTogether(translateAnimator, alphaAnimator)
            duration = 500
            interpolator = AccelerateDecelerateInterpolator()
        }
        subtitleAnimatorSet.start()
    }

    private fun animateLoadingElements() {
        ObjectAnimator.ofFloat(binding.progressBar, "alpha", 0f, 1f).apply {
            duration = 400
            start()
        }
        val translateAnimator = ObjectAnimator.ofFloat(binding.tvLoadingText, "translationY", 20f, 0f)
        val alphaAnimator = ObjectAnimator.ofFloat(binding.tvLoadingText, "alpha", 0f, 1f)

        val loadingAnimatorSet = AnimatorSet().apply {
            playTogether(translateAnimator, alphaAnimator)
            duration = 400
            interpolator = AccelerateDecelerateInterpolator()
        }
        loadingAnimatorSet.start()
    }

    private fun initializeAppData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 데이터베이스 초기화
                dataInitializer.initializeAppData()

                withContext(Dispatchers.Main) {
                    isReady = true

                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToNextScreen()
                    }, SPLASH_DELAY)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isReady = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToNextScreen()
                    }, SPLASH_DELAY)
                }
            }
        }
    }

    private fun navigateToNextScreen() {
        val intent = if (userPreferences.isLoggedIn()) {
            Intent(this, MainActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        startActivity(intent)
        finish()

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
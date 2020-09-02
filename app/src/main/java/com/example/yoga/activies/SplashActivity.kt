package com.example.yoga.activies

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.loadAnimation
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.yoga.R
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.item_view.*
import java.lang.Exception

class SplashActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var bottonanimation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAGS_CHANGED, WindowManager.LayoutParams.FLAGS_CHANGED)
        setContentView(R.layout.activity_splash)

        startThread()
    }

    private fun startThread() {
        Thread {
            try {
                startAnim()
                Thread.sleep(3000)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
            }
        }.start()
    }

    private fun startAnim() {
        val image = loadAnimation(this, R.anim.topanimation)
        image.startAnimation(image)

        val bottonanimation = loadAnimation(this, R.anim.topanimation)
        bottonanimation.startAnimation(bottonanimation)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}
private fun Animation.startAnimation(image: Animation?) {
}


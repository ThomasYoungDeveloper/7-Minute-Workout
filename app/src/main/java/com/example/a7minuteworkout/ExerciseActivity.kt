package com.example.a7minuteworkout

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minuteworkout.databinding.ActivityExcerciseBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    // general binding syntax
    private var restTimer: CountDownTimer? = null
    private var restProgress = 0
    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var tts: TextToSpeech? = null

    //media player variable
    private var player: MediaPlayer? = null

    private var exerciseAdapter : ExerciseStatusAdapter? = null


    // create a binding variable
    private var binding: ActivityExcerciseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExcerciseBinding.inflate(layoutInflater)

        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        exerciseList = Constants.defaultExerciseList()

        tts = TextToSpeech(this, this)
        binding?.toolbarExercise?.setNavigationOnClickListener {
            onBackPressed()
        }


        setupRestView()
        setUpExerciseStatusRecyclerView()

    }

    private fun setUpExerciseStatusRecyclerView(){
        binding?.rvExerciseStatus?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.rvExerciseStatus?.adapter = exerciseAdapter
    }


    private fun setupRestView() {

        // set up media player
        try {
            val soundURI = Uri.parse("android.resource://com.example.a7minuteworkout/" + R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()
        } catch (e: Exception){
            e.printStackTrace()
        }

        binding?.flRestView?.visibility = View.VISIBLE
        binding?.tvTitle?.visibility = View.VISIBLE
        binding?.tvExerciseName?.visibility = View.INVISIBLE
        binding?.flExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.VISIBLE
        binding?.tvUpcomingLabel?.visibility = View.VISIBLE

        if (restTimer != null) {
            restTimer!!.cancel()
            restProgress = 0
        }

        binding?.tvUpcomingExerciseName?.text = exerciseList!![currentExercisePosition + 1].getName()

        setRestProgressBar()
        speakOut("Nice Job! You can relax")

    }

    private fun setRestProgressBar() {

        binding?.progressBar?.progress = restProgress

        restTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 10 - restProgress
                binding?.tvTimer?.text =
                    (10 - restProgress).toString()
            }

            override fun onFinish() {
                currentExercisePosition++
                setupExerciseView()
            }
        }.start()
    }


    private fun setupExerciseView() {
        binding?.flRestView?.visibility = View.INVISIBLE
        binding?.tvTitle?.visibility = View.INVISIBLE
        binding?.tvExerciseName?.visibility = View.VISIBLE
        binding?.flExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.tvUpcomingExerciseName?.visibility = View.INVISIBLE
        binding?.tvUpcomingLabel?.visibility = View.INVISIBLE

        if (exerciseTimer != null) {
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.tvExerciseName?.text = exerciseList!![currentExercisePosition].getName()
        setExerciseProgressBar()
    }

    private fun setExerciseProgressBar() {

        binding?.progressBarExercise?.progress = exerciseProgress

        exerciseTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress =  - exerciseProgress
                binding?.tvTimerExercise?.text = (30 - exerciseProgress).toString()

            }

            override fun onFinish() {

                if(currentExercisePosition < exerciseList?.size!! - 1) {
                    setupRestView()
                } else {
                    Toast.makeText(this@ExerciseActivity, "Workout finish", Toast.LENGTH_SHORT).show()
                }
                setupExerciseView()
            }
        }.start()
    }


    public override fun onDestroy() {
        if (restTimer != null) {
            restTimer?.cancel()
            restProgress = 0
        }
        super.onDestroy()

        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        if(player != null){
            player!!.stop()
        }

        binding = null
    }



    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage((Locale.US))
        } else {
            Log.e("TTS", "Initialization Failed")
        }

    }

    private fun speakOut(text: String){
        tts!!.speak(text,TextToSpeech.QUEUE_FLUSH, null, "")
    }
}


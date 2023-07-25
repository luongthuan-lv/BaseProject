package com.bigsoft.clap.myphone.viewmodels

import com.bigsoft.clap.myphone.R
import com.bigsoft.clap.myphone.base.BaseViewModel
import com.bigsoft.clap.myphone.models.IntroTutorial

class TutorialViewModel : BaseViewModel() {
    var listIntro = listOf(
        IntroTutorial(
            R.drawable.intro_first,
            R.string.title_fist,
            R.string.content_fist
        ),
        IntroTutorial(
            R.drawable.intro_second,
            R.string.title_second,
            R.string.content_second
        ),
        IntroTutorial(
            R.drawable.intro_third,
            R.string.title_third,
            R.string.content_third
        ),
        IntroTutorial(
            R.drawable.intro_four,
            R.string.title_final,
            R.string.content_final
        )
    )

}
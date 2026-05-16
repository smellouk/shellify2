package io.shellify.app.screenshot

import com.github.takahirom.roborazzi.RoborazziOptions

internal val screenshotOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.005f)
)

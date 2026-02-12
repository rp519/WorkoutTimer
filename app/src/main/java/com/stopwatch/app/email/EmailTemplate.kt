package com.stopwatch.app.email

object EmailTemplate {

    fun generateWorkoutSummaryEmail(
        userName: String?,
        currentMonth: String,
        monthlyWorkouts: Int,
        monthlyRounds: Int,
        monthlyDuration: String,
        monthlyStreak: Int,
        ytdWorkouts: Int,
        ytdRounds: Int,
        ytdDuration: String,
        ytdActiveDays: Int,
        mostUsedWorkout: String?,
        workoutBreakdown: List<com.stopwatch.app.data.model.WorkoutBreakdown> = emptyList()
    ): String {
        val displayName = userName ?: "Champion"

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Your Workout Summary</title>
</head>
<body style="margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5;">
    <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
        <tr>
            <td align="center" style="padding: 40px 20px;">
                <table role="presentation" width="600" cellspacing="0" cellpadding="0" border="0" style="max-width: 600px; background-color: #ffffff; border-radius: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">

                    <!-- Header -->
                    <tr>
                        <td style="background: linear-gradient(135deg, #00897B 0%, #4DB6AC 100%); padding: 40px 30px; border-radius: 16px 16px 0 0; text-align: center;">
                            <h1 style="margin: 0; color: #ffffff; font-size: 32px; font-weight: 700;">🏆 Amazing Progress!</h1>
                            <p style="margin: 10px 0 0 0; color: #ffffff; font-size: 18px; opacity: 0.95;">Your Workout Journey Continues</p>
                        </td>
                    </tr>

                    <!-- Greeting -->
                    <tr>
                        <td style="padding: 30px 30px 20px 30px;">
                            <p style="margin: 0; font-size: 18px; color: #333333; line-height: 1.6;">
                                Hi <strong>$displayName</strong>,<br><br>
                                You're crushing it! 💪 Here's a snapshot of your incredible fitness journey.
                            </p>
                        </td>
                    </tr>

                    <!-- Current Month Section -->
                    <tr>
                        <td style="padding: 20px 30px;">
                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color: #FFF9E6; border-radius: 12px; padding: 20px;">
                                <tr>
                                    <td>
                                        <h2 style="margin: 0 0 15px 0; color: #FF6F00; font-size: 20px; font-weight: 600;">📊 $currentMonth Highlights</h2>
                                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td width="50%" style="padding: 10px 10px 10px 0;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 32px; font-weight: 700; color: #00897B;">$monthlyWorkouts</div>
                                                        <div style="font-size: 14px; color: #666666; margin-top: 5px;">Workouts</div>
                                                    </div>
                                                </td>
                                                <td width="50%" style="padding: 10px 0 10px 10px;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 32px; font-weight: 700; color: #00897B;">$monthlyRounds</div>
                                                        <div style="font-size: 14px; color: #666666; margin-top: 5px;">Rounds</div>
                                                    </div>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td width="50%" style="padding: 10px 10px 0 0;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 32px; font-weight: 700; color: #FF6F00;">$monthlyDuration</div>
                                                        <div style="font-size: 14px; color: #666666; margin-top: 5px;">Total Time</div>
                                                    </div>
                                                </td>
                                                <td width="50%" style="padding: 10px 0 0 10px;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 32px; font-weight: 700; color: #FF6F00;">$monthlyStreak</div>
                                                        <div style="font-size: 14px; color: #666666; margin-top: 5px;">Day Streak 🔥</div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- Workout Breakdown Section -->
                    ${if (workoutBreakdown.isNotEmpty()) """
                    <tr>
                        <td style="padding: 20px 30px;">
                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color: #F3E5F5; border-radius: 12px; padding: 20px;">
                                <tr>
                                    <td>
                                        <h2 style="margin: 0 0 15px 0; color: #6A1B9A; font-size: 20px; font-weight: 600;">📊 Workout Breakdown</h2>
                                        ${workoutBreakdown.mapIndexed { index, workout ->
                                            val duration = formatDuration(workout.totalSeconds)
                                            val badge = when {
                                                workout.count > 10 -> "🔥"
                                                workout.count > 5 -> "⭐"
                                                else -> "✓"
                                            }
                                            """
                                            <div style="background-color: #ffffff; border-radius: 8px; padding: 15px; margin-bottom: 10px; border-left: 4px solid #6A1B9A;">
                                                <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                                                    <tr>
                                                        <td width="40" style="vertical-align: middle;">
                                                            <div style="font-size: 20px; font-weight: 700; color: #6A1B9A; text-align: center;">#${index + 1}</div>
                                                        </td>
                                                        <td style="vertical-align: middle; padding-left: 10px;">
                                                            <div style="font-size: 16px; font-weight: 600; color: #333333;">${workout.planName}</div>
                                                            <div style="font-size: 13px; color: #666666; margin-top: 2px;">${workout.count} session${if (workout.count != 1) "s" else ""} • $duration</div>
                                                        </td>
                                                        <td width="40" style="vertical-align: middle; text-align: right;">
                                                            <div style="font-size: 24px;">$badge</div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                            """
                                        }.joinToString("")}
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    """ else ""}

                    <!-- Year-to-Date Section -->
                    <tr>
                        <td style="padding: 20px 30px;">
                            <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color: #E8F5F3; border-radius: 12px; padding: 20px;">
                                <tr>
                                    <td>
                                        <h2 style="margin: 0 0 15px 0; color: #00695C; font-size: 20px; font-weight: 600;">🎯 Year-to-Date Progress</h2>
                                        <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td width="50%" style="padding: 10px 10px 10px 0;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 28px; font-weight: 700; color: #00897B;">$ytdWorkouts</div>
                                                        <div style="font-size: 13px; color: #666666; margin-top: 5px;">Total Workouts</div>
                                                    </div>
                                                </td>
                                                <td width="50%" style="padding: 10px 0 10px 10px;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 28px; font-weight: 700; color: #00897B;">$ytdRounds</div>
                                                        <div style="font-size: 13px; color: #666666; margin-top: 5px;">Total Rounds</div>
                                                    </div>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td width="50%" style="padding: 10px 10px 0 0;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 28px; font-weight: 700; color: #00695C;">$ytdDuration</div>
                                                        <div style="font-size: 13px; color: #666666; margin-top: 5px;">Total Time</div>
                                                    </div>
                                                </td>
                                                <td width="50%" style="padding: 10px 0 0 10px;">
                                                    <div style="text-align: center; background-color: #ffffff; border-radius: 8px; padding: 15px;">
                                                        <div style="font-size: 28px; font-weight: 700; color: #00695C;">$ytdActiveDays</div>
                                                        <div style="font-size: 13px; color: #666666; margin-top: 5px;">Active Days</div>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                        ${if (mostUsedWorkout != null) """
                                        <div style="margin-top: 15px; padding: 12px; background-color: #ffffff; border-radius: 8px; text-align: center;">
                                            <div style="font-size: 13px; color: #666666;">⭐ Favorite Workout</div>
                                            <div style="font-size: 16px; font-weight: 600; color: #00695C; margin-top: 5px;">$mostUsedWorkout</div>
                                        </div>
                                        """ else ""}
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>

                    <!-- Motivational Message -->
                    <tr>
                        <td style="padding: 20px 30px;">
                            <div style="background: linear-gradient(135deg, #FFE082 0%, #FFD54F 100%); border-radius: 12px; padding: 20px; text-align: center;">
                                <p style="margin: 0; font-size: 16px; color: #5D4037; line-height: 1.6; font-weight: 500;">
                                    ${getMotivationalMessage(monthlyWorkouts, monthlyStreak)}
                                </p>
                            </div>
                        </td>
                    </tr>

                    <!-- Footer -->
                    <tr>
                        <td style="padding: 30px; text-align: center; border-top: 1px solid #E0E0E0;">
                            <p style="margin: 0 0 10px 0; font-size: 14px; color: #999999;">
                                Keep up the amazing work! 💪
                            </p>
                            <p style="margin: 0; font-size: 12px; color: #BDBDBD;">
                                Sent with ❤️ from Workout Timer
                            </p>
                        </td>
                    </tr>

                </table>
            </td>
        </tr>
    </table>
</body>
</html>
        """.trimIndent()
    }

    fun generateSubjectLine(monthlyWorkouts: Int, monthlyStreak: Int): String {
        return when {
            monthlyStreak >= 30 -> "🔥 Unstoppable! Your 30-Day Streak Continues!"
            monthlyStreak >= 14 -> "💪 2 Weeks Strong! Check Your Amazing Progress"
            monthlyStreak >= 7 -> "⚡ Week Streak Unlocked! Your Fitness Journey Shines"
            monthlyWorkouts >= 20 -> "🌟 20+ Workouts This Month! You're On Fire!"
            monthlyWorkouts >= 10 -> "🎯 Double Digits! Your Consistency Pays Off"
            monthlyWorkouts >= 5 -> "💚 Great Progress! Your Monthly Workout Summary"
            else -> "🏆 Your Fitness Journey Summary – Keep Going!"
        }
    }

    private fun getMotivationalMessage(workouts: Int, streak: Int): String {
        return when {
            streak >= 30 -> "A full month of dedication! You're building habits that last a lifetime. This is what champions are made of! 🌟"
            streak >= 14 -> "Two weeks of consistency! You're proving to yourself what's possible. Keep this momentum going! ⚡"
            streak >= 7 -> "7 days strong! You've built the foundation. Each workout is making you stronger! 💪"
            workouts >= 15 -> "Over $workouts workouts! Your commitment is inspiring. The best version of you is emerging! ✨"
            workouts >= 8 -> "Consistency is key, and you've got it! Each workout brings you closer to your goals! 🎯"
            workouts > 0 -> "Every workout counts, and you're making them count! Keep showing up for yourself! 💚"
            else -> "Your next workout is your next victory. Let's make it happen! 🚀"
        }
    }

    private fun formatDuration(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "${totalSeconds}s"
        }
    }
}

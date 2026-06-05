package com.gymcats.data.debug

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.AccountDao
import com.gymcats.data.local.dao.CycleLogDao
import com.gymcats.data.local.dao.ExerciseLogDao
import com.gymcats.data.local.dao.ProgressPhotoDao
import com.gymcats.data.local.dao.UserProfileDao
import com.gymcats.data.local.dao.WorkoutDao
import com.gymcats.data.local.entities.CycleLog
import com.gymcats.data.local.entities.ExerciseLog
import com.gymcats.data.local.entities.ProgressPhoto
import com.gymcats.data.local.entities.UserProfile
import com.gymcats.data.local.entities.Workout
import com.gymcats.domain.model.CyclePhase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebugDataSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionManager: SessionManager,
    private val accountDao: AccountDao,
    private val userProfileDao: UserProfileDao,
    private val workoutDao: WorkoutDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val cycleLogDao: CycleLogDao,
    private val progressPhotoDao: ProgressPhotoDao
) {
    suspend fun seedCurrentAccount(): Result<String> = runCatching {
        val accountId = sessionManager.getAuthenticatedAccountId()
            ?: throw IllegalStateException("Nenhuma conta autenticada.")

        val existingWorkouts = workoutDao.getWorkoutsFromDate(accountId, "1900-01-01")
        val existingCycleLogs = cycleLogDao.getLogsFromDate(accountId, "1900-01-01")
        val existingPhotos = progressPhotoDao.countPhotos(accountId)
        if (existingWorkouts.isNotEmpty() || existingCycleLogs.isNotEmpty() || existingPhotos > 0) {
            throw IllegalStateException("Essa conta ja possui dados. O seed de debug so roda em contas vazias.")
        }

        val account = accountDao.getAccountNow(accountId)
        val existingProfile = userProfileDao.getProfileNow(accountId)
        val displayName = existingProfile?.name?.takeIf { it.isNotBlank() }
            ?: inferNameFromAccount(account?.email)

        val profile = UserProfile(
            accountId = accountId,
            name = displayName,
            goal = existingProfile?.goal?.ifBlank { "Hipertrofia" } ?: "Hipertrofia",
            lastPeriodDate = LocalDate.now().minusDays(11).toString(),
            cycleLength = existingProfile?.cycleLength ?: 28,
            periodLength = existingProfile?.periodLength ?: 5,
            preferredTrainingDays = setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY
            ).joinToString(",") { it.name },
            preferredTrainingHour = existingProfile?.preferredTrainingHour ?: 19,
            notificationsEnabled = existingProfile?.notificationsEnabled ?: true
        )
        userProfileDao.saveProfile(profile)

        val workouts = listOf(
            workoutTemplate(accountId, "Inferiores A", 172, 54, CyclePhase.FOLICULAR.name),
            workoutTemplate(accountId, "Upper body", 156, 46, CyclePhase.OVULATORIA.name),
            workoutTemplate(accountId, "Gluteo focus", 141, 57, CyclePhase.LUTEA.name),
            workoutTemplate(accountId, "Full body", 126, 43, CyclePhase.MENSTRUAL.name),
            workoutTemplate(accountId, "Quadriceps", 110, 59, CyclePhase.FOLICULAR.name),
            workoutTemplate(accountId, "Costas e biceps", 96, 48, CyclePhase.OVULATORIA.name),
            workoutTemplate(accountId, "Posteriores", 82, 55, CyclePhase.LUTEA.name),
            workoutTemplate(accountId, "Full body express", 69, 39, CyclePhase.MENSTRUAL.name),
            workoutTemplate(accountId, "Gluteo e posterior", 54, 52, CyclePhase.FOLICULAR.name),
            workoutTemplate(accountId, "Upper body", 41, 47, CyclePhase.OVULATORIA.name),
            workoutTemplate(accountId, "Quadriceps", 29, 58, CyclePhase.LUTEA.name),
            workoutTemplate(accountId, "Full body", 18, 44, CyclePhase.MENSTRUAL.name),
            workoutTemplate(accountId, "Gluteo focus", 9, 61, CyclePhase.FOLICULAR.name),
            workoutTemplate(accountId, "Inferiores", 3, 56, CyclePhase.OVULATORIA.name),
            workoutTemplate(accountId, "Treino de hoje", 0, 50, CyclePhase.LUTEA.name)
        ).map { template ->
            val workoutId = workoutDao.insertWorkout(template)
            template.copy(id = workoutId)
        }

        val exercisePlans = listOf(
            workouts[0].id to listOf(
                exercise("Stiff", "back-legs", 4, 10, 40f),
                exercise("Hip thrust", "waist", 4, 12, 70f),
                exercise("Mesa flexora", "upper-legs", 3, 12, 25f)
            ),
            workouts[1].id to listOf(
                exercise("Supino maquina", "chest", 4, 10, 22f),
                exercise("Remada baixa", "back", 4, 12, 28f),
                exercise("Desenvolvimento", "shoulders", 3, 10, 14f)
            ),
            workouts[2].id to listOf(
                exercise("Agachamento guiado", "upper-legs", 4, 10, 42f),
                exercise("Leg press", "upper-legs", 4, 12, 120f),
                exercise("Cadeira extensora", "upper-legs", 3, 15, 30f)
            ),
            workouts[3].id to listOf(
                exercise("Levantamento terra", "back", 3, 8, 50f),
                exercise("Supino halter", "chest", 3, 10, 16f),
                exercise("Afundo", "upper-legs", 3, 12, 18f)
            ),
            workouts[4].id to listOf(
                exercise("Hip thrust", "waist", 4, 10, 82f),
                exercise("Abducao maquina", "upper-legs", 3, 15, 35f),
                exercise("Stiff", "back-legs", 3, 10, 44f)
            ),
            workouts[5].id to listOf(
                exercise("Puxada frente", "back", 4, 10, 30f),
                exercise("Remada unilateral", "back", 3, 12, 18f),
                exercise("Rosca martelo", "lower-arms", 3, 12, 10f)
            ),
            workouts[6].id to listOf(
                exercise("Agachamento guiado", "upper-legs", 4, 8, 48f),
                exercise("Leg press", "upper-legs", 4, 10, 140f),
                exercise("Hip thrust", "waist", 4, 10, 88f)
            ),
            workouts[13].id to listOf(
                exercise("Stiff", "back-legs", 4, 10, 46f),
                exercise("Hip thrust", "waist", 4, 10, 92f),
                exercise("Abducao maquina", "upper-legs", 3, 15, 38f)
            )
        )

        exercisePlans.forEach { (workoutId, logs) ->
            logs.forEach { log ->
                exerciseLogDao.insertLog(log.copy(workoutId = workoutId))
            }
        }

        val moodTimeline = listOf(
            cycleLogTemplate(accountId, 168, 3, 3, "Neutra", false, 3, CyclePhase.FOLICULAR.name),
            cycleLogTemplate(accountId, 154, 4, 4, "Animada", false, 4, CyclePhase.OVULATORIA.name),
            cycleLogTemplate(accountId, 143, 2, 2, "Cansada", true, 2, CyclePhase.LUTEA.name),
            cycleLogTemplate(accountId, 129, 2, 2, "Exausta", true, 2, CyclePhase.MENSTRUAL.name),
            cycleLogTemplate(accountId, 114, 4, 4, "Animada", false, 4, CyclePhase.FOLICULAR.name),
            cycleLogTemplate(accountId, 101, 5, 5, "Euforica", false, 4, CyclePhase.OVULATORIA.name),
            cycleLogTemplate(accountId, 88, 3, 3, "Neutra", true, 3, CyclePhase.LUTEA.name),
            cycleLogTemplate(accountId, 73, 2, 2, "Cansada", true, 2, CyclePhase.MENSTRUAL.name),
            cycleLogTemplate(accountId, 60, 4, 4, "Animada", false, 4, CyclePhase.FOLICULAR.name),
            cycleLogTemplate(accountId, 47, 5, 5, "Euforica", false, 5, CyclePhase.OVULATORIA.name),
            cycleLogTemplate(accountId, 35, 3, 3, "Neutra", true, 3, CyclePhase.LUTEA.name),
            cycleLogTemplate(accountId, 22, 2, 2, "Exausta", true, 2, CyclePhase.MENSTRUAL.name),
            cycleLogTemplate(accountId, 14, 4, 4, "Animada", false, 4, CyclePhase.FOLICULAR.name),
            cycleLogTemplate(accountId, 7, 5, 5, "Euforica", false, 4, CyclePhase.OVULATORIA.name),
            cycleLogTemplate(accountId, 2, 3, 4, "Neutra", false, 4, CyclePhase.LUTEA.name),
            cycleLogTemplate(accountId, 0, 4, 4, "Animada", false, 4, CyclePhase.LUTEA.name)
        )

        moodTimeline.forEachIndexed { index, log ->
            cycleLogDao.insertLog(log.copy(notes = "Registro de teste ${index + 1}"))
        }

        val photoSources = listOf(
            Triple(workouts[1], "Primeiro mês", Color.parseColor("#F5B7B1")),
            Triple(workouts[5], "Consistência em 3 meses", Color.parseColor("#F9E79F")),
            Triple(workouts[9], "Forca em alta", Color.parseColor("#AED6F1")),
            Triple(workouts[13], "Evolução em 6 meses", Color.parseColor("#A9DFBF"))
        )

        photoSources.forEachIndexed { index, (workout, caption, color) ->
            val path = createPlaceholderPhoto(accountId, displayName, caption, color, index)
            progressPhotoDao.insertPhoto(
                ProgressPhoto(
                    accountId = accountId,
                    imagePath = path,
                    date = workout.date,
                    workoutId = workout.id
                )
            )
        }

        "Dados de teste inseridos para $displayName."
    }

    private fun workoutTemplate(
        accountId: Long,
        name: String,
        daysAgo: Long,
        durationMinutes: Int,
        phase: String
    ) = Workout(
        accountId = accountId,
        name = name,
        date = LocalDate.now().minusDays(daysAgo).toString(),
        durationMinutes = durationMinutes,
        cyclePhase = phase,
        isOpen = false
    )

    private fun cycleLogTemplate(
        accountId: Long,
        daysAgo: Long,
        energyLevel: Int,
        disposition: Int,
        mood: String,
        cramps: Boolean,
        sleepQuality: Int,
        cyclePhase: String
    ) = CycleLog(
        accountId = accountId,
        date = LocalDate.now().minusDays(daysAgo).toString(),
        energyLevel = energyLevel,
        disposition = disposition,
        mood = mood,
        cramps = cramps,
        sleepQuality = sleepQuality,
        cyclePhase = cyclePhase
    )

    private fun exercise(
        name: String,
        apiId: String,
        sets: Int,
        reps: Int,
        weight: Float
    ) = ExerciseLog(
        workoutId = 0,
        exerciseName = name,
        exerciseApiId = apiId,
        muscleGroup = name,
        sets = sets,
        reps = reps,
        weight = weight
    )

    private fun createPlaceholderPhoto(
        accountId: Long,
        displayName: String,
        caption: String,
        backgroundColor: Int,
        index: Int
    ): String {
        val outputDir = File(context.filesDir, "debug_progress")
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, "seed_${accountId}_$index.png")

        val bitmap = Bitmap.createBitmap(720, 960, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1F2933")
            textSize = 54f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#34495E")
            textSize = 34f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText(displayName, 360f, 320f, titlePaint)
        canvas.drawText("Foto de progresso mock", 360f, 420f, bodyPaint)
        canvas.drawText(caption, 360f, 490f, bodyPaint)
        canvas.drawText("GymCats Debug", 360f, 760f, bodyPaint)

        FileOutputStream(outputFile).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        bitmap.recycle()

        return outputFile.absolutePath
    }

    private fun inferNameFromAccount(email: String?): String {
        val raw = email?.substringBefore("@")?.trim().orEmpty()
        if (raw.isBlank()) return "Usuaria teste"
        return raw
            .replace('.', ' ')
            .replace('_', ' ')
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { token ->
                token.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }
}


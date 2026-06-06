package com.gymcats.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `accounts` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `email` TEXT NOT NULL,
                `phone` TEXT NOT NULL,
                `password` TEXT NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_email` ON `accounts` (`email`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_phone` ON `accounts` (`phone`)")

        addColumnIfMissing(db, "workouts", "accountId", "INTEGER NOT NULL DEFAULT 0")
        addColumnIfMissing(db, "cycle_logs", "accountId", "INTEGER NOT NULL DEFAULT 0")
        addColumnIfMissing(db, "progress_photos", "accountId", "INTEGER NOT NULL DEFAULT 0")

        recreateUserProfileTable(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE `progress_photos` SET `notes` = ''")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `cycle_logs` ADD COLUMN `workoutId` INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE `workouts_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `accountId` INTEGER NOT NULL DEFAULT 0,
                `name` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `durationMinutes` INTEGER NOT NULL,
                `cyclePhase` TEXT NOT NULL,
                `isOpen` INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO `workouts_new` (`id`, `accountId`, `name`, `date`, `durationMinutes`, `cyclePhase`, `isOpen`)
            SELECT `id`, `accountId`, `name`, `date`, `durationMinutes`, `cyclePhase`, `isOpen` FROM `workouts`
        """.trimIndent())
        db.execSQL("DROP TABLE `workouts`")
        db.execSQL("ALTER TABLE `workouts_new` RENAME TO `workouts`")

        db.execSQL("""
            CREATE TABLE `progress_photos_new` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `accountId` INTEGER NOT NULL DEFAULT 0,
                `imagePath` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `workoutId` INTEGER
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO `progress_photos_new` (`id`, `accountId`, `imagePath`, `date`, `workoutId`)
            SELECT `id`, `accountId`, `imagePath`, `date`, `workoutId` FROM `progress_photos`
        """.trimIndent())
        db.execSQL("DROP TABLE `progress_photos`")
        db.execSQL("ALTER TABLE `progress_photos_new` RENAME TO `progress_photos`")
    }
}

private fun addColumnIfMissing(
    db: SupportSQLiteDatabase,
    tableName: String,
    columnName: String,
    columnDefinition: String
) {
    if (!hasColumn(db, tableName, columnName)) {
        db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
    }
}

private fun recreateUserProfileTable(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `user_profile_new` (
            `accountId` INTEGER NOT NULL,
            `name` TEXT NOT NULL,
            `goal` TEXT NOT NULL,
            `lastPeriodDate` TEXT NOT NULL,
            `cycleLength` INTEGER NOT NULL,
            `periodLength` INTEGER NOT NULL,
            `preferredTrainingDays` TEXT NOT NULL,
            `preferredTrainingHour` INTEGER NOT NULL,
            `notificationsEnabled` INTEGER NOT NULL,
            PRIMARY KEY(`accountId`)
        )
        """.trimIndent()
    )

    if (tableExists(db, "user_profile")) {
        val notificationsExpression = if (hasColumn(db, "user_profile", "notificationsEnabled")) {
            "notificationsEnabled"
        } else {
            "1"
        }

        db.execSQL(
            """
            INSERT INTO `user_profile_new` (
                `accountId`,
                `name`,
                `goal`,
                `lastPeriodDate`,
                `cycleLength`,
                `periodLength`,
                `preferredTrainingDays`,
                `preferredTrainingHour`,
                `notificationsEnabled`
            )
            SELECT
                0,
                `name`,
                `goal`,
                `lastPeriodDate`,
                `cycleLength`,
                `periodLength`,
                `preferredTrainingDays`,
                `preferredTrainingHour`,
                $notificationsExpression
            FROM `user_profile`
            LIMIT 1
            """.trimIndent()
        )

        db.execSQL("DROP TABLE `user_profile`")
    }

    db.execSQL("ALTER TABLE `user_profile_new` RENAME TO `user_profile`")
}

private fun hasColumn(
    db: SupportSQLiteDatabase,
    tableName: String,
    columnName: String
): Boolean {
    db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
        val nameIndex = cursor.getColumnIndex("name")
        while (cursor.moveToNext()) {
            if (cursor.getString(nameIndex) == columnName) return true
        }
    }
    return false
}

private fun tableExists(db: SupportSQLiteDatabase, tableName: String): Boolean {
    db.query(
        "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        arrayOf(tableName)
    ).use { cursor ->
        return cursor.moveToFirst()
    }
}

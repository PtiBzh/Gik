package fr.ydel.gilets

import android.app.Application
import androidx.room.Room
import fr.ydel.gilets.data.AppDatabase
import fr.ydel.gilets.data.CampaignRepository
import fr.ydel.gilets.data.TestRepository

class TestGiletsApp : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "gilets-database"
        ).fallbackToDestructiveMigration().build()
    }

    val testRepository: TestRepository by lazy {
        TestRepository(database.testResultDao())
    }

    val campaignRepository: CampaignRepository by lazy {
        CampaignRepository(applicationContext, database.giletDao())
    }
}

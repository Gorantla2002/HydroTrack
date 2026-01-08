package uk.ac.tees.mad.s3548263

import android.app.Application
import com.google.firebase.FirebaseApp

class HydroTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

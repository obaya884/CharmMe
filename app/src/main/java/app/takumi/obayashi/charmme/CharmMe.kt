package app.takumi.obayashi.charmme

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class CharmMe : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .build()
//        Realm.deleteRealm(realmConfig)
        Realm.setDefaultConfiguration(realmConfig)
    }
}
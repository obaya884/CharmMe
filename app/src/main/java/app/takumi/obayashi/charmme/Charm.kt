package app.takumi.obayashi.charmme

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Charm(
    @PrimaryKey
    open var id: String = UUID.randomUUID().toString(),
    open var name: String = "",
    open var duration: Int = 0,
    open var createdAt: Date = Date(System.currentTimeMillis())
) : RealmObject()
package lv.rtu.dip701.kinguchat


class User{
    var email: String? = null
    var role: String? = null
    var uid: String? = null

    constructor()

    constructor(email: String?, role: String?, uid: String?){
        this.email = email
        this.role = role
        this.uid = uid
    }
}

package lv.rtu.dip701.kinguchat

data class Order(
    var orderId: String = "",
    var dishName: String = "",
    var quantity: String = "",
    var userId: String = "",
    var ready: Boolean = false
) {
    constructor() : this("", "", "", "", false)
}

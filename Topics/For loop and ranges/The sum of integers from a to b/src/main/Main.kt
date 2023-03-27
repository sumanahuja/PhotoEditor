fun main() {
    val a = readln().toInt()
    val b = readln().toInt()
    var sum = 0
    if(a < b) {
        for (i in a..b) {
            sum += i
        }
    }
    println(sum)

}
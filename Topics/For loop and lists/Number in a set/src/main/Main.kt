fun main() {
    val n = readln().toInt()
    val listN: MutableList<Int> = mutableListOf()
    for (i in 0 until n) {
        listN.add(readln().toInt())
    }
    val findV = readln().toInt()
    if (findV in listN) {
        println("YES")
    } else {
        println("NO")  
    }
}

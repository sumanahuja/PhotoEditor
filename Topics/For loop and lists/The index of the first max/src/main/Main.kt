fun main() {
    // write your code here
    val index = readln().toInt()
    val list = mutableListOf<Int>()
    for (i in 0 until index)
        list.add(i, readln().toInt())
    val max = 0
    println(list.indexOf(list.maxOrNull()))
    
}

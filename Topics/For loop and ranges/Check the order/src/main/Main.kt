import java.util.Scanner

fun main() {
    val N = readln().toInt()
    val list = mutableListOf<Int>()
    var result = 0
//
    val scanner = Scanner(System.`in`)
    {
        list.add( result++,scanner.nextInt())
    }
    result = 0
    for (i in 0 until N - 1) {
        if (list[i] < list[i + 1])
            result = i + 1
    }
    println(if(result == N && N>1) "YES" else "NO")
}

fun main() {
    val h1 = readln().toInt()
    val h2 = readln().toInt()
    val h3 = readln().toInt()
    if (h1 >= h2 && h2 >= h3 || h1 <= h2 && h2 <= h3 ) {
    	println(true)
	} else {
        println(false)
    }
      
}

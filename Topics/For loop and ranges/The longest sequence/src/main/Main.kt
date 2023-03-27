fun main() {
    val n = readln().toInt()
    var maxLength = 1;
    var maxLength2 = 1;
    val arr = IntArray(n)
    val arr2 = IntArray(n)

    for (i in 0 until n) {
        arr[i]=readln().toInt()
    }

    if(n == 0)
        maxLength2 = 0
    else if(n == 1)
        maxLength2 = 1
    else if (n == 2 && (arr[0] < arr[1]))
        maxLength2 = 2
    else if (n == 2 && (arr[0] > arr[1]))
        maxLength2 = 1
    else{

        for (i in 0 until n) {

                if (i != n-1 && arr[i] <= arr[i + 1]) {
                    maxLength++
                    //print("maxLength=")
                    //println(maxLength)
                } else {

                    if (maxLength2 < maxLength) {
                        maxLength2 = maxLength

                    }
                    maxLength = 1

                }
                }

        }
            println(maxLength2)




}

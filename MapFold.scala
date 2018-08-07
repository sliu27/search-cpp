
//package cats
import cats._
//.Monoid
// Start by wri ng out the signature of foldMap. It should accept the following parameters:
// • asequenceoftypeVector[A];
// • afunc onoftypeA=>B,wherethereisaMonoidforB;
// You will have to add implicit parameters or context bounds to complete the type signature.

// trait JsonWriter[A] {
//   def write(value: A): Json
// }

object HelloWorld {
    def foldMap[A, B: Monoid](values: Vector[A])(func: A => B): B = 
        B

    def main(args: Array[String]): Unit = {
        foldMap(Vector(1, 2, 3))(identity) == 6
        // res2: Int = 6
        import cats.instances.string._ // for Monoid
        // Mapping to a String uses the concatenation monoid:
        foldMap(Vector(1, 2, 3))(_.toString + "! ") == "1! 2! 3! "
        // res4: String = "1! 2! 3! "
        // Mapping over a String to produce a String:
        foldMap("Hello world!".toVector)(_.toString.toUpperCase) == "HELLO WORLD!"
    }

}
// res6: String = HELLO WORLD!
// B has a monoid

// trait foldMap[A] {
//     def apply(Vector[A]): Vector[A] {
//     }

//     def fold(Vector[A]): A {
//     }
// }
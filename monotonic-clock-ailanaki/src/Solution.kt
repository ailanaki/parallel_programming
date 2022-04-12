/**
 * В теле класса решения разрешено использовать только финальные переменные типа RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :Yakupova Aisha
 */
class Solution : MonotonicClock {
    private var l1 by RegularInt(0)
    private var l2 by RegularInt(0)
    private var m1 by RegularInt(0)
    private var m2 by RegularInt(0)
    private var r by RegularInt(0)

    override fun write(time: Time) {
        // write right-to-left
        l2 = time.d1;
        m2 = time.d2;

        r = time.d3;

        m1 = time.d2;
        l1 = time.d1;


    }

    override fun read(): Time {
        // read left-to-right
        val v1 = l1;
        val q1 = m1;
        val w = r;

        val q2 = m2;
        val v2 = l2;

        return if (v1 == v2) {
            if (q1 == q2) {
                Time(v2, q2, w)
            } else {
                Time(v2, q2, 0)
            }
        } else {
            Time(v2, 0, 0)
        }
    }
}

package kr.gyeongmunbaek.image;

public class Pair<E> {
    public E first;
    public E second;

    Pair(E first, E second) {
        this.first = first;
        this.second = second;
    }

    public String toString() {
        return "Pair[" + this.first + ", " + this.second + "]";
    }
}

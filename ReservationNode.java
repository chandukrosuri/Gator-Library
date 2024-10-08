import java.util.Arrays;

class Reservation {
    /** Defines the ID of a patron*/
    private int patronID;

    /** Defines the order of precedence in-case of a wait list*/
    private int priorityNumber;

    /** Defines the timestamp of the reservation*/
    private long timeOfReservation;

    public Reservation(int patronID, int priorityNumber, long timeOfReservation) {
        this.patronID = patronID;
        this.priorityNumber = priorityNumber;
        this.timeOfReservation = timeOfReservation;
    }

    public int getPatronId(){
        return patronID;
    }

    public int getPriorityNumber() {
        return priorityNumber;
    }

    public long getTimeOfReservation() {
        return timeOfReservation;
    }
}

public class ReservationNode {
    /** Defines the max length of the reservations wait list*/
    private int capacity = 20;

    /** Indicates the size of the heap*/
    private int size = 0;

    /** Defines the array of reservations as nodes of the heap*/
    private Reservation[] reservations = new Reservation[capacity];

    public int getSize() {
        return size;
    }

    public Reservation[] getReservations() {
        return reservations;
    }
    // Helper methods for getting indices of parent, left child, and right child
    private int getParentIndex(int childIndex) {
        return (childIndex - 1) / 2;
    }

    private int getLeftChildIndex(int parentIndex) {
        return 2 * parentIndex + 1;
    }

    private int getRightChildIndex(int parentIndex) {
        return 2 * parentIndex + 2;
    }

    private boolean hasParent(int index) {
        return getParentIndex(index) >= 0;
    }

    private boolean hasLeftChild(int index) {
        return getLeftChildIndex(index) < size;
    }

    private boolean hasRightChild(int index) {
        return getRightChildIndex(index) < size;
    }

    private Reservation parent(int index) {
        return reservations[getParentIndex(index)];
    }

    private Reservation leftChild(int index) {
        return reservations[getLeftChildIndex(index)];
    }

    private Reservation rightChild(int index) {
        return reservations[getRightChildIndex(index)];
    }

    private void swap(int index1, int index2) {
        Reservation temp = reservations[index1];
        reservations[index1] = reservations[index2];
        reservations[index2] = temp;
    }

    private void ensureCapacity() {
        if (size == capacity) {
            reservations = Arrays.copyOf(reservations, capacity * 2);
            capacity *= 2;
        }
    }

    public Reservation peek() {
        if (size == 0) throw new IllegalStateException();
        return reservations[0];
    }

    public Reservation poll() {
        if (size == 0) throw new IllegalStateException();
        Reservation reservation = reservations[0];
        reservations[0] = reservations[size - 1];
        size--;
        downHeapify();
        return reservation;
    }

    public void add(Reservation reservation) {
        ensureCapacity();
        reservations[size] = reservation;
        size++;
        upHeapify();
    }

    private void upHeapify() {
        int index = size - 1;
        while (hasParent(index) && isHigherPriority(reservations[index], parent(index))) {
            swap(getParentIndex(index), index);
            index = getParentIndex(index);
        }
    }

    private void downHeapify() {
        int index = 0;
        while (hasLeftChild(index)) {
            int smallerChildIndex = getLeftChildIndex(index);
            if (hasRightChild(index) && isHigherPriority(rightChild(index), leftChild(index))) {
                smallerChildIndex = getRightChildIndex(index);
            }

            if (isHigherPriority(reservations[index], reservations[smallerChildIndex])) {
                break;
            } else {
                swap(index, smallerChildIndex);
            }

            index = smallerChildIndex;
        }
    }

    private boolean isHigherPriority(Reservation a, Reservation b) {
        if (a.getPriorityNumber() < b.getPriorityNumber()) {
            return true;
        } else if (a.getPriorityNumber() == b.getPriorityNumber()) {
            return a.getTimeOfReservation() < b.getTimeOfReservation();
        } else {
            return false;
        }
    }
}

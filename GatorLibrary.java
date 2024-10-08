import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class GatorLibrary {
    private static Map<Integer, BookNode> bookMap = new HashMap<>();
    private static RbTree tree = new RbTree();

    public static void main(String[] args) {

        String inputFileName = args[0];
        String outputFileName = inputFileName.replaceFirst("[.][^.]+$", "") + "_output_file.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName));

             PrintWriter pw = new PrintWriter(new FileWriter(outputFileName))) {

            String line;
            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split("\\(");
                String[] tokens = lineSplit[1].split(",");
                for (int i=0; i< tokens.length; i++) {
                    String trimmedPart = tokens[i].trim().replaceAll("\\)+[^)]*$", "").replaceAll("^\"|\"$", "");
                    tokens[i] = trimmedPart;
                }

                switch (lineSplit[0]) {
                    case "PrintBook":
                        printBook(Integer.parseInt(tokens[0]), pw);
                        break;
                    case "PrintBooks":
                        printBooks(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), pw);
                        break;
                    case "InsertBook":
                        insertBook(Integer.parseInt(tokens[0]), tokens[1], tokens[2], parseStatus(tokens[3]));
                        break;
                    case "BorrowBook":
                        borrowBook(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]),
                                Integer.parseInt(tokens[2]), pw);
                        break;
                    case "ReturnBook":
                         returnBook(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]), pw);
                        break;
                    case "DeleteBook":
                        deleteBook(Integer.parseInt(tokens[0]), pw);
                        break;
                    case "FindClosestBook":
                        findClosestBook(Integer.parseInt(tokens[0]), pw);
                        break;
                    case "ColorFlipCount":
                        int colorFlipCount = colorFlipCount();
                        pw.println("Color Flip Count: " + colorFlipCount + "\n");
                        break;
                    case "Quit":
                        pw.print("Program Terminated!!");
                        return;
                    default:
                        System.out.println("Invalid operation: " + tokens[0]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printBook(int bookId, PrintWriter pw) {
        System.out.println("print book start: "+ bookId);
        if (bookMap.containsKey(bookId)) {
            BookNode book = bookMap.get(bookId);
            pw.println("BookID: " + book.getBookId());
            pw.println("BookName: " + book.getBookName());
            pw.println("AuthorName: " + book.getAuthorName());
            pw.println("AvailabilityStatus: " + (book.getAvailabilityStatus() ? "Yes" : "No"));
            pw.println("BorrowedBy: " + (book.getBorrowedBy() > 0? book.getBorrowedBy(): "None"));
            pw.println("Reservations: " + getReservationHeapAsString(book) + "\n");
        }
        System.out.println("print book end");
    }

    private static void printBooks(int bookId1, int bookId2, PrintWriter pw) {
        for (int i = bookId1; i <= bookId2; i++) {
            printBook(i, pw);
        }
    }

    private static void insertBook(int bookId, String bookName, String authorName, boolean availabilityStatus) {
        System.out.println("insert book start: " + bookId);
        BookNode newBook = new BookNode(bookId, bookName, authorName, availabilityStatus, new ReservationNode());
        bookMap.put(bookId, newBook);
        tree.insertKey(bookId);
        System.out.println("insert book end");
    }

    private static void borrowBook(int patronId, int bookId, int patronPriority, PrintWriter pw) {
        System.out.println("borrow book start");
        if (bookMap.containsKey(bookId)) {
            BookNode book = bookMap.get(bookId);

            if (book.getAvailabilityStatus()) {
                // Book is available, update book status and borrower
                book.setAvailabilityStatus(false);
                book.setBorrowedBy(patronId);
                pw.println("Book " + bookId + " Borrowed by Patron " + patronId + "\n");
            } else if (Arrays.stream(book.getReservationNode().getReservations()).anyMatch(Objects::isNull)){
                // Book is not available, create a reservation node in the heap
                book.getReservationNode().add(new Reservation(patronId, patronPriority, System.currentTimeMillis()));
                pw.println("Book " + bookId + " Reserved by Patron " + patronId + "\n");
            }
        }
        System.out.println("borrow book end");
    }

    private static void returnBook(int patronId, int bookId, PrintWriter pw) {
        System.out.println("return book start");
        if (bookMap.containsKey(bookId)) {
            BookNode book = bookMap.get(bookId);

            // Update book status and borrower
            book.setAvailabilityStatus(true);
            book.setBorrowedBy(-1); // Assuming -1 represents no borrower
            pw.println("Book " + bookId + " Returned by Patron " + patronId + "\n");

            // If there are reservations, assign the book to the patron with the highest priority
            if (book.getReservationNode().getSize() > 0) {
                Reservation highestPriorityReservation = book.getReservationNode().poll();
                book.setAvailabilityStatus(false);
                book.setBorrowedBy(highestPriorityReservation.getPatronId());
                pw.println("Book " + bookId + " Allotted to Patron " + highestPriorityReservation.getPatronId() + "\n");
            }
        }
        System.out.println("return book end");
    }

    private static void deleteBook(int bookId, PrintWriter pw) {
        System.out.println("delete book start: " + bookId);
        if (bookMap.containsKey(bookId)) {
            BookNode deletedBook = bookMap.remove(bookId);
            // Notify patrons in the reservation list that the book is no longer available to borrow
            ReservationNode reservationHeap = deletedBook.getReservationNode();
            int nonNullValues = 0;
            for(Reservation reservation: reservationHeap.getReservations()){
                if (reservation != null)
                    nonNullValues++;
            }
            int[] patronIds = new int[nonNullValues];
            nonNullValues = 0;
            while (reservationHeap.getSize() > 0) {
                var patronId = reservationHeap.poll().getPatronId();
                if (patronId != deletedBook.getBorrowedBy()){
                    patronIds[nonNullValues++] = patronId;
                }
            }

            if (patronIds.length > 0){
                StringBuilder sb = new StringBuilder();
                for (int patronId : patronIds) {
                    sb.append(patronId).append(",");
                }
                var patronIdsString = sb.substring(0, sb.length() - 1);

                pw.println("Book " + bookId + " is no longer available. Reservations made by Patrons " + patronIdsString + " have been cancelled." + "\n");
            } else {
                pw.println("Book " + bookId + " is no longer available.\n");
            }
        } else {
            pw.println("Book " + bookId + " not found in the Library" + "\n");
            }
        tree.deleteNode(bookId);
        System.out.println("delete book end");
    }

    private static void findClosestBook(int targetId, PrintWriter pw) {
        System.out.println("find closest book start");
        if (bookMap.isEmpty()) {
            pw.println("Library is empty\n");
            return;
        }

        int minDistance = Integer.MAX_VALUE;
        Map<Integer, Integer> distanceMap = new HashMap<>();

        for (BookNode book : bookMap.values()) {
            if (book.getBookId() != targetId){
                int distance = Math.abs(book.getBookId() - targetId);
                distanceMap.put(book.getBookId(), distance);
                if (distance < minDistance) {
                    minDistance = distance;
//                    closestBook = book;
                }
            }
        }

        for (int bookId: distanceMap.keySet().stream().sorted().toList()){
            if (distanceMap.get(bookId) == minDistance){
                var book = bookMap.get(bookId);
                if (book != null) {
                    pw.println("BookID: " + book.getBookId());
                    pw.println("BookName: " + book.getBookName());
                    pw.println("AuthorName: " + book.getAuthorName());
                    pw.println("AvailabilityStatus: " + book.getAvailabilityStatus());
                    pw.println("BorrowedBy: " + book.getBorrowedBy());
                    pw.println("ReservationHeap: " + getReservationHeapAsString(book) + "\n");
                }
            }
        }

        System.out.println("find closest book end");
    }

    private static int colorFlipCount() {
        return tree.getColorFlips();
    }

    private static String getReservationHeapAsString(BookNode bookNode) {
        var reservationNode = bookNode.getReservationNode();
        if (reservationNode.getSize() < 1) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        var reservations = Arrays.stream(reservationNode.getReservations())
                .filter(reservation -> reservation!=null && reservation.getPatronId()!=bookNode.getBorrowedBy())
                .sorted(Comparator.comparing(Reservation::getPatronId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Reservation reservation: reservations) {
            sb.append(reservation.getPatronId()).append(",");
        }

        return "["+sb.substring(0, sb.length() - 1)+"]"; // Remove the trailing comma
    }

    public static boolean parseStatus(String status){
        return status.equalsIgnoreCase("yes") || status.equalsIgnoreCase("true");
    }
}

class BookNode {
    private int bookId;
    private String bookName;
    private String authorName;
    private boolean availabilityStatus;
    private int borrowedBy;
    private ReservationNode reservationNode;

    public BookNode(int bookId, String bookName, String authorName, boolean availabilityStatus, ReservationNode reservationNode) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.authorName = authorName;
        this.availabilityStatus = availabilityStatus;
        this.reservationNode = reservationNode;
    }

    public int getBookId() {
        return bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public boolean getAvailabilityStatus() {
        return availabilityStatus;
    }

    public int getBorrowedBy() {
        return borrowedBy;
    }

    public ReservationNode getReservationNode() {
        return reservationNode;
    }

    public void setAvailabilityStatus(boolean availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public void setBorrowedBy(int borrowedBy) {
        this.borrowedBy = borrowedBy;
    }

}
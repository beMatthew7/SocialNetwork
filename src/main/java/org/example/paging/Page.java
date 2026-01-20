package org.example.paging;

public class Page<E> {

    private Iterable<E> elementsOnPage;
    private int totalNumberOfElements;

    public Page(Iterable<E> elementsOnPage, int totalNumberOfElements) {
        this.elementsOnPage = elementsOnPage;
        this.totalNumberOfElements = totalNumberOfElements;
    }

    public Iterable<E> getElementsOnPage() {
        return elementsOnPage;
    }

    public int getTotalNumberOfElements() {
        return totalNumberOfElements;
    }

    // Metoda ajutatoare pentru a calcula numarul total de pagini
    public int getTotalNumberOfPages(int pageSize) {
        if (totalNumberOfElements == 0) return 0;
        return (int) Math.ceil((double) totalNumberOfElements / pageSize);
    }
}


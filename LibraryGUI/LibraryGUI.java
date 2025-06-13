import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;

 class LibraryGUI extends Application {

    private List<Book> books = new ArrayList<>();
    private int idCounter = 1000;

    private VBox bookListView;
    private TextField titleField;
    private TextField authorField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("📚 Library Management System");

        // Input form
        titleField = new TextField();
        titleField.setPromptText("Book Title");

        authorField = new TextField();
        authorField.setPromptText("Author Name");

        Button addBtn = new Button("Add Book");
        addBtn.setOnAction(e -> addBook());

        HBox inputForm = new HBox(10, titleField, authorField, addBtn);
        inputForm.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Book list area
        bookListView = new VBox(10);
        bookListView.setStyle("-fx-padding: 10;");

        ScrollPane scrollPane = new ScrollPane(bookListView);
        scrollPane.setFitToWidth(true);

        VBox root = new VBox(10, inputForm, scrollPane);
        root.setStyle("-fx-padding: 20;");

        renderBooks();

        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }

    private void addBook() {
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();

        if (title.isEmpty() || author.isEmpty()) {
            showAlert("Please fill in all fields.");
            return;
        }

        Book book = new Book(idCounter++, title, author, true);
        books.add(book);
        renderBooks();

        titleField.clear();
        authorField.clear();
    }

    private void renderBooks() {
        bookListView.getChildren().clear();

        if (books.isEmpty()) {
            bookListView.getChildren().add(new Label("No books found."));
            return;
        }

        for (Book book : books) {
            VBox card = new VBox(5);
            card.setStyle("-fx-border-color: gray; -fx-border-radius: 5; -fx-padding: 10; -fx-background-color: #f9f9f9;");

            Label id = new Label("ID: " + book.getId());
            Label title = new Label("Title: " + book.getTitle());
            Label author = new Label("Author: " + book.getAuthor());
            Label status = new Label("Status: " + (book.isAvailable() ? "Available" : "Borrowed"));

            Button actionBtn = new Button(book.isAvailable() ? "Borrow" : "Return");
            actionBtn.setOnAction(e -> {
                if (book.isAvailable()) {
                    book.setAvailable(false);
                } else {
                    book.setAvailable(true);
                }
                renderBooks();
            });

            card.getChildren().addAll(id, title, author, status, actionBtn);
            bookListView.getChildren().add(card);
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Book class
    public static class Book {
        private int id;
        private String title;
        private String author;
        private boolean available;

        public Book(int id, String title, String author, boolean available) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.available = available;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getAuthor() { return author; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
    }
}

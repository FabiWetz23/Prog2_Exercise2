package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.api.MovieAPI;
import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import at.ac.fhcampuswien.fhmdb.ui.MovieCell;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;


import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static at.ac.fhcampuswien.fhmdb.api.MovieAPI.getAllMovies;

public class HomeController implements Initializable {
    @FXML
    public JFXButton searchBtn;

    @FXML
    public JFXButton resetBtn;

    @FXML
    public TextField searchField;

    @FXML
    public JFXListView movieListView;

    @FXML
    public JFXComboBox genreComboBox;

    @FXML
    public JFXComboBox releaseYearComboBox;

    @FXML JFXComboBox ratingComboBox;


    @FXML
    public JFXButton sortBtn;

    public List<Movie> allMovies;

    protected ObservableList<Movie> observableMovies = FXCollections.observableArrayList();

    protected SortedState sortedState;

    public static ArrayList<Label> titlesList = new ArrayList<Label>();
    public static ArrayList<Label> descriptionsList = new ArrayList<Label>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeState();
        initializeLayout();
    }

    public void initializeState() {
        //allMovies = Movie.initializeMovies();

        allMovies = getAllMovies();
        observableMovies.clear();
        observableMovies.addAll(allMovies); // add all movies to the observable list
        sortedState = SortedState.ASCENDING;
    }

    public void initializeLayout() {
        movieListView.setItems(observableMovies);   // set the items of the listview to the observable list
        movieListView.setCellFactory(movieListView -> new MovieCell()); // apply custom cells to the listview

        Object[] genres = Genre.values();   // get all genres
        genreComboBox.getItems().add("No filter");  // add "no filter" to the combobox
        genreComboBox.getItems().addAll(genres);    // add all genres to the combobox
        genreComboBox.setPromptText("Filter by Genre");

        releaseYearComboBox.setPromptText("Filter by Release Year");
        Integer[] releaseYears = new Integer[78];
        for (int i = 0; i < 78; i++) {
            releaseYears[i] = 2023 - i;
        }
        //releaseYearComboBox.getItems().add("No filter");
        releaseYearComboBox.getItems().addAll(releaseYears);

        ratingComboBox.setPromptText("Filter by rating");
        Double[] rating = new Double[]{1.00, 2.00, 3.00, 4.00, 5.00, 6.00, 7.00, 8.00, 9.00, 10.00};
        ratingComboBox.getItems().addAll(rating);
    }

    // sort movies based on sortedState
    // by default sorted state is NONE
    // afterwards it switches between ascending and descending
    public void sortMovies() {
        if (sortedState == SortedState.NONE || sortedState == SortedState.DESCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle));
            sortedState = SortedState.ASCENDING;
        } else if (sortedState == SortedState.ASCENDING) {
            observableMovies.sort(Comparator.comparing(Movie::getTitle).reversed());
            sortedState = SortedState.DESCENDING;
        }
    }

    public List<Movie> filterByQuery(List<Movie> movies, String query) {
        // Check if the query is null or empty
        if (query == null || query.isEmpty()) return movies;

        // Check if the list of movies is null
        if (movies == null) {
            throw new IllegalArgumentException("Movies list must not be null");
        }

        // Filter movies based on the search query (case-insensitive)
        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie ->
                        movie.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                movie.getDescription().toLowerCase().contains(query.toLowerCase())
                )
                .collect(Collectors.toList());
    }


    public List<Movie> filterByGenre(List<Movie> movies, Genre genre){
        if(genre == null) return movies;

        if(movies == null) {
            throw new IllegalArgumentException("movies must not be null");
        }

        return movies.stream()
                .filter(Objects::nonNull)
                .filter(movie -> movie.getGenres().contains(genre))
                .toList();
    }

    public void applyAllFilters(String searchQuery, Object genre, String releaseYear, String rating) {
        // Prüfen, ob der Suchbegriff leer ist und ihn in Kleinbuchstaben konvertieren
        searchQuery = (searchQuery == null || searchQuery.isEmpty()) ? null : searchQuery.toLowerCase();

        // Prüfen, ob das Genre "No filter" ist oder null ist
        Genre selectedGenre = null;
        if (genre != null && !genre.toString().equals("No filter")) {
            if (genre instanceof Genre) {
                selectedGenre = (Genre) genre;
            } else {
                throw new IllegalArgumentException("Invalid genre parameter");
            }
        }

        List<Movie> filteredMovies = getAllMovies(
                searchQuery,
                selectedGenre,
                (releaseYear != null && !releaseYear.isEmpty()) ? releaseYear : null,
                (rating != null && !rating.isEmpty()) ? rating : null
        );

        observableMovies.clear();
        observableMovies.addAll(filteredMovies);
    }



    public void searchBtnClicked(ActionEvent actionEvent) {
        String searchQuery = searchField.getText().trim().toLowerCase();
        Object genre = genreComboBox.getSelectionModel().getSelectedItem();
        String releaseYear = "";
        String rating = "";
        if (releaseYearComboBox.getSelectionModel().getSelectedItem() != null)
            releaseYear = releaseYearComboBox.getSelectionModel().getSelectedItem().toString();
        if (ratingComboBox.getSelectionModel().getSelectedItem() != null)
            rating = ratingComboBox.getSelectionModel().getSelectedItem().toString();

        applyAllFilters(searchQuery, genre, releaseYear, rating);


    }

    public void sortBtnClicked(ActionEvent actionEvent) {
        sortMovies();
    }

    public void resetBtnClicked(ActionEvent actionEvent) {
        releaseYearComboBox.getSelectionModel().clearSelection();
        searchField.clear();
        ratingComboBox.getSelectionModel().clearSelection();


    }

    public long countMoviesFrom(List<Movie> movies, String director) {
        return movies.stream()
                .filter(movie -> movie.getDirectors().contains(director))
                .count();
    }

    public int getLongestMovieTitle(List<Movie> movies) {
        return movies.stream()
                .map(Movie::getTitle)
                .mapToInt(String::length)
                .max()
                .orElse(0);
    }

    public List<Movie> getMoviesBetweenYears(List<Movie> movies, int startYear, int endyear) {
        return movies.stream()
                .filter(movie -> movie.getReleaseYear() >= startYear && movie.getReleaseYear() <= endyear)
                .collect(Collectors.toList());
    }

    public String getMostPopularActor(List<Movie> movies) {
        return movies.stream()
                .flatMap(movie -> movie.getMainCast().stream())
                .collect(Collectors.groupingBy(String::toLowerCase, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");
    }

}
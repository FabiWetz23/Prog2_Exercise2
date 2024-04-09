package at.ac.fhcampuswien.fhmdb;

import at.ac.fhcampuswien.fhmdb.models.Genre;
import at.ac.fhcampuswien.fhmdb.models.Movie;
import at.ac.fhcampuswien.fhmdb.models.SortedState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import at.ac.fhcampuswien.fhmdb.api.MovieAPI;

import java.util.Arrays;
import java.util.List;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class HomeControllerTest {
    private static HomeController homeController;

    @BeforeAll
    static void init() {
        homeController = new HomeController();
    }

    @Test
    void at_initialization_allMovies_and_observableMovies_should_be_filled_and_equal() {
        homeController.initializeState();
        assertEquals(homeController.allMovies, homeController.observableMovies);
    }

    @Test
    void if_not_yet_sorted_sort_is_applied_in_ascending_order() {
        // given
        HomeController homeController = new HomeController();

        // when
        homeController.initializeState();
        homeController.sortedState = SortedState.NONE;
        homeController.sortMovies();

        // then
        List<Movie> actualMovies = homeController.observableMovies;
        List<Movie> expectedMovies = MovieAPI.getAllMovies();
        expectedMovies.sort(Comparator.comparing(Movie::getTitle)); // Sortieren der erwarteten Filme nach Titel
        assertEquals(expectedMovies, actualMovies);
    }


    @Test
    void if_last_sort_ascending_next_sort_should_be_descending() {

        // given
        homeController.initializeState();
        homeController.sortMovies();
        SortedState initialState = homeController.sortedState;

        // when
        homeController.sortMovies(); // Sort again
        SortedState nextState = homeController.sortedState;

        // then
        assertNotEquals(initialState, nextState);
        assertEquals(SortedState.ASCENDING, initialState);
        assertEquals(SortedState.DESCENDING, nextState);
    }

    @Test
    void if_last_sort_descending_next_sort_should_be_ascending() {

        // given
        homeController.initializeState();
        SortedState initialState = homeController.sortedState;

        // when
        homeController.sortMovies();
        SortedState nextState = homeController.sortedState;

        // then
        assertNotEquals(initialState, nextState); // Assert that the state has changed
        assertEquals(SortedState.NONE, initialState); // Initial state should be NONE
        assertEquals(SortedState.ASCENDING, nextState); // Next state should be ASCENDING

    }

    @Test
    void query_filter_matches_with_lower_and_uppercase_letters(){
        // given
        homeController.initializeState();

        // when
        List<Movie> actualMovies = MovieAPI.getAllMovies("iFe",null,"","");

        // then

        List<Movie> expectedMovies = MovieAPI.getAllMovies("ife",null,"","");
        assertEquals(expectedMovies, actualMovies);
        }


    @Test
    void query_filter_with_null_movie_list_throws_exception(){
        // given
        homeController.initializeState();
        String query = "IfE";

        // when and then
        assertThrows(IllegalArgumentException.class, () -> homeController.filterByQuery(null, query));
    }

    @Test
    void query_filter_with_null_value_returns_unfiltered_list() {
        // given
        homeController.initializeState();
        String query = null;

        // when
        List<Movie> actual = homeController.filterByQuery(homeController.observableMovies, query);

        // then
        assertEquals(homeController.observableMovies, actual);
    }

    @Test
    void genre_filter_with_null_value_returns_unfiltered_list() {
        // given
        homeController.initializeState();
        Genre genre = null;

        // when
        List<Movie> actual = homeController.filterByGenre(homeController.observableMovies, genre);

        // then
        assertEquals(homeController.observableMovies, actual);
    }

    @Test
    void genre_filter_returns_all_movies_containing_given_genre() {
        // given
        homeController.initializeState();
        Genre genre = Genre.DRAMA;

        // when
        List<Movie> actualMovies = MovieAPI.getAllMovies("",Genre.DRAMA,"","");
        // then
        assertEquals(22, actualMovies.size());
    }

    @Test
    void no_filtering_ui_if_empty_query_or_no_genre_is_set() {
        // given
        homeController.initializeState();

        // when
        homeController.applyAllFilters("", null, "", "");

        // then
        assertEquals(homeController.allMovies, homeController.observableMovies);
    }

}


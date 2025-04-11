package Assign32starter;

/**
 * Represents a movie with a name and its corresponding correct answer.
 * The movie name is expected to correlate with associated image file names,
 * excluding the image number and file extension.
 */
public class Movie {
    /**
     * Represents the name of the movie, which should match the image file names
     * corresponding to the movie (excluding the image number and file extension).
     * This variable is immutable once initialized.
     */
    private final String movieName;    // Should match the image file names (without the image number and extension)
    /**
     * Stores the correct answer for a given movie.
     * This value represents the expected response associated with the movie instance.
     */
    private final String correctAnswer;

    /**
     * Constructs a new Movie object with the specified movie name and correct answer.
     *
     * @param movieName     the name of the movie. Should match the image file names (excluding number and extension).
     * @param correctAnswer the correct answer associated with the movie.
     */
    public Movie(String movieName, String correctAnswer) {
        this.movieName = movieName;
        this.correctAnswer = correctAnswer;
    }

    /**
     * Retrieves the name of the movie.
     *
     * @return the name of the movie as a String
     */
    public String getMovieName() {
        return movieName;
    }

    /**
     * Retrieves the correct answer associated with the movie.
     *
     * @return the correct answer as a String
     */
    public String getCorrectAnswer() {
        return correctAnswer;
    }
}

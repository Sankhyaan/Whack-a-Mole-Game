package whackamole;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates persistence of high scores using Java serialization.
 */
public class HighScoreManager {

    private static final String FILE_NAME = "scores.dat";

    public void saveScores(List<PlayerScore> scores) throws HighScoreException {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(new ArrayList<>(scores));
        } catch (IOException e) {
            throw new HighScoreException("Failed to save scores", e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<PlayerScore> loadScores() throws HighScoreException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object obj = in.readObject();
            if (obj instanceof List) {
                List<PlayerScore> scores = (List<PlayerScore>) obj;
                Collections.sort(scores);
                return scores;
            } else {
                return new ArrayList<>();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new HighScoreException("Failed to load scores", e);
        }
    }
}
